# Foodies Platform: System Architecture & Technical Specification 📐

This document provides a comprehensive technical breakdown of the architecture, subsystems, data flow pipelines, security guardrails, database models, and AI engine design powering the **Foodies (FoodyHub)** full-stack application.

---

## 1. Executive Architectural Overview

The Foodies application is engineered around a clean, layered micro-monolith backend paired with decoupled React customer and administrative frontends. The core innovation lies in the integration of an **AI Food Ordering Assistant** and an **AI Nutrition & Calorie Analyzer** that operate strictly as trusted services above live MongoDB data.

### Core Architectural Principles:
1. **Zero Hallucination Guarantee**: The AI engine never generates synthetic food items, imaginary discounts, or made-up prices. All recommendations are grounded in live database documents.
2. **Deterministic Server-Side Arithmetic**: Macro sums, item subtotals, tax (10%), and delivery charges (₹10) are strictly calculated by Java code in Spring Boot. The client and LLM outputs are treated as untrusted.
3. **Cart & Payment Safety Isolation**: The AI manages a detached `proposed_orders` document. The AI is structurally incapable of invoking payment APIs or finalizing orders. A user must deliberately review and transfer items to the real shopping cart (`carts`) to enter the standard Razorpay checkout flow.
4. **Dual AI Engine Resilience**: Supports external LLM providers (**Google Gemini 1.5 Flash** or **OpenAI**) with seamless automatic fallback to an internal deterministic NLP engine if credentials are not configured or rate limits are reached.

---

## 2. High-Level Component Architecture

```mermaid
graph TB
    subgraph ClientTier ["Client Presentation Layer"]
        UserApp["Customer Web Application\n(React 19 + Vite 8)\nPort 5173"]
        AdminApp["Restaurant Admin Portal\n(React 19 + Vite 8)\nPort 5174"]
    end

    subgraph ApiGatewayTier ["API & Security Gateway Layer"]
        CORS["CORS Filter\n(Allowed Origins Config)"]
        SecFilterChain["Spring Security 6 Filter Chain"]
        JWTFilter["JwtAuthenticateFilter\n(Stateless Bearer Token Extraction)"]
    end

    subgraph ControllerTier ["REST Controller Layer"]
        AuthCtrl["AuthController\n(/api/login, /api/register)"]
        FoodCtrl["FoodController\n(/api/foods/**)"]
        CartCtrl["CartController\n(/api/cart/**)"]
        OrderCtrl["OrderController\n(/api/orders/**)"]
        AiCtrl["AiAssistantController\n(/api/ai/**)"]
        NutriCtrl["NutritionController\n(/api/nutrition/**)"]
    end

    subgraph CoreServiceTier ["Core Business Services Layer"]
        UserSvc["UserService & AppUserDetailsService"]
        FoodSvc["FoodService (CRUD & S3 Upload)"]
        CartSvc["CartService (Active Cart Mutator)"]
        OrderSvc["OrderService (Razorpay Client & Verification)"]
    end

    subgraph AiServiceTier ["AI & Nutrition Engine Layer"]
        AiAssistantSvc["AiAssistantService (Session & Ownership)"]
        LlmSvc["LlmService (Gemini / OpenAI / Fallback NLP)"]
        ToolRegistry["AiToolRegistry (Validated Dispatcher)"]
        FoodSearchSvc["FoodSearchService (Query & Constraint Engine)"]
        MealPlanSvc["MealPlanningService (Multi-Person Combinations)"]
        ProposedOrderSvc["ProposedOrderService (Lifecycle & Price Audit)"]
        NutriSvc["NutritionService (Java Macro Math)"]
    end

    subgraph DataStorageTier ["Persistence & Cloud Infrastructure"]
        Mongo["MongoDB Atlas / Local (Port 27017)\n[foods, carts, orders, users, proposed_orders, ai_conversations]"]
        S3["Amazon AWS S3 Bucket\n(foodyhub-foods)"]
        RazorpayApi["Razorpay Payment Gateway API"]
    end

    UserApp -->|"HTTPS / JSON"| CORS
    AdminApp -->|"HTTPS / JSON"| CORS
    CORS --> SecFilterChain
    SecFilterChain --> JWTFilter
    JWTFilter --> AuthCtrl & FoodCtrl & CartCtrl & OrderCtrl & AiCtrl & NutriCtrl

    AuthCtrl --> UserSvc
    FoodCtrl --> FoodSvc
    CartCtrl --> CartSvc
    OrderCtrl --> OrderSvc
    AiCtrl --> AiAssistantSvc & ProposedOrderSvc
    NutriCtrl --> NutriSvc

    AiAssistantSvc --> LlmSvc
    LlmSvc --> ToolRegistry
    ToolRegistry --> FoodSearchSvc & MealPlanSvc & ProposedOrderSvc & NutriSvc

    UserSvc --> Mongo
    FoodSvc --> Mongo & S3
    CartSvc --> Mongo
    OrderSvc --> Mongo & RazorpayApi
    FoodSearchSvc --> Mongo
    ProposedOrderSvc --> Mongo
    NutriSvc --> Mongo
    ProposedOrderSvc -.->|"Transfer to Real Cart"| CartSvc
```

---

## 3. Subsystem Breakdown

### 3.1 AI Food Ordering Assistant Subsystem

The assistant subsystem orchestrates natural language processing, user intent extraction, tool execution, and session management.

```mermaid
sequenceDiagram
    autonumber
    actor User as Customer (React Client)
    participant Ctrl as AiAssistantController
    participant Svc as AiAssistantService
    participant LLM as LlmService (Gemini / Fallback NLP)
    participant Reg as AiToolRegistry
    participant Search as FoodSearchService
    participant Prop as ProposedOrderService
    participant DB as MongoDB (foods, proposed_orders)

    User->>Ctrl: POST /api/ai/chat {"message": "Spicy meal for two under ₹500"}
    Ctrl->>Svc: handleChat(request, authenticatedUserId)
    Svc->>DB: Load/Create AiConversationEntity
    Svc->>LLM: processUserMessage(message, history, userId, sessionId)
    
    note over LLM,Reg: Intent Recognition: Meal Planning for 2 within ₹500 budget
    LLM->>Reg: executeTool("buildMealPlan", {budget: 500, numberOfPeople: 2, spice: "spicy"})
    Reg->>Search: searchFoods(criteria: spice=spicy, budgetTarget=445)
    Search->>DB: Query foods collection
    DB-->>Search: Return matching FoodEntities
    Reg->>Prop: setProposedOrderItems(userId, sessionId, selectedItems)
    Prop->>Prop: Recalculate Subtotal, Shipping (₹10), Tax (10%), Macros
    Prop->>DB: Save ProposedOrderEntity
    Prop-->>Reg: Return ProposedOrderResponse
    Reg-->>LLM: Tool Result Payload
    
    LLM-->>Svc: LlmExecutionResult (Chat text + ProposedOrder + Nutrition)
    Svc->>DB: Append user & assistant messages to conversation
    Svc-->>Ctrl: ChatResponse DTO
    Ctrl-->>User: 200 OK (Render chat bubble + ProposedOrderCard + Food Cards)
```

#### The 6 AI Backend Tools:
The LLM communicates exclusively through structured function calling using narrow, typed backend tools implemented in `AiToolRegistry`:

| Tool Name | Parameters | Responsibility |
| :--- | :--- | :--- |
| `searchAvailableFoods` | `query`, `category`, `maxPrice`, `isVegetarian`, `spicePreference`, `minProtein`, `maxCalories`, `limit` | Live search filtered by dietary constraints and price. Returns available menu items or closest alternatives. |
| `getFoodDetails` | `foodId` or `foodName` | Fetches comprehensive details for a specific dish directly from database. |
| `getFoodNutrition` | `foodId` or `foodName` | Retrieves macronutrient breakdown and data source verification for a dish. |
| `buildMealPlan` | `budget`, `numberOfPeople`, `isVegetarian`, `spicePreference`, `maxCalories`, `minProtein` | Assembles a balanced multi-dish meal combination fitting within budget accounting for taxes and delivery. |
| `modifyProposedOrder` | `action` (ADD, REMOVE, REPLACE, UPDATE_QTY, CLEAR), `foodId`, `foodName`, `replaceWithFoodName`, `quantity` | Modifies the server-side proposed order and recalculates verified subtotals. |
| `getProposedOrderSummary`| *(none)* | Returns the user's current proposed order, live pricing, and aggregated macro metrics. |

---

### 3.2 AI Nutrition & Calorie Analyzer Subsystem

The nutrition subsystem provides verified macronutrient calculations, dietary classification, and macro-constrained meal generation.

```mermaid
flowchart LR
    Dish["Food Document\n(nutrition field)"] --> Check{"Nutrition Data\nPresent?"}
    
    Check -->|No / Calories null| Missing["Flag incompleteData = true\nAdd to missingNutritionDishes\nDisplay: 'Nutrition unavail.'"]
    Check -->|Yes| MacroMath["Java Arithmetic:\nCalories * Qty\nProtein * Qty\nCarbs * Qty\nFat * Qty\nFiber * Qty"]
    
    Missing --> Aggregator["NutritionSummary Response"]
    MacroMath --> Aggregator
    
    Aggregator --> AuditNote{"All Items\nVerified?"}
    AuditNote -->|Yes| VerifiedBadge["Badge: 'Verified Lab Data'"]
    AuditNote -->|No| EstimateBadge["Badge: 'Recipe Estimate'\nFootnote: Standards Guideline"]
```

#### Key Nutrition Capabilities:
- **Zero Hallucination Policy**: If a dish in the database has no nutritional record, the engine returns `isCompleteData = false` with a clear label (`"Nutrition information unavailable"`). It never invents numbers.
- **Serving Scale Multipliers**: Total calories, proteins, carbohydrates, and fats scale deterministically with the ordered quantity ($C_{total} = \sum c_i \times q_i$).
- **Goal-Oriented Filtering**: High-protein and low-calorie searches query MongoDB using numerical operators ($Calories \le Max$ and $Protein \ge Min$).

---

### 3.3 Cart & Financial Checkout Safety Flow

To prevent accidental charges or unauthorized order placement by an AI model, proposed orders are completely isolated from active shopping carts until explicit human confirmation:

```mermaid
sequenceDiagram
    autonumber
    actor Customer as Authenticated Customer
    participant UI as React UI (Foodie AI Widget)
    participant AiCtrl as AiAssistantController
    participant PropSvc as ProposedOrderService
    participant CartSvc as CartService
    participant OrderSvc as OrderService
    participant Razorpay as Razorpay Gateway
    participant DB as MongoDB

    Customer->>UI: Reviews Proposed Meal in Chat
    Customer->>UI: Clicks "Add Proposed Meal to Cart"
    UI->>AiCtrl: POST /api/ai/proposed-order/transfer-to-cart
    AiCtrl->>PropSvc: transferToCart(userId, sessionId)
    PropSvc->>DB: Fetch ProposedOrderEntity
    PropSvc->>DB: Upsert CartEntity with verified item IDs & quantities
    PropSvc-->>AiCtrl: Transferred item count
    AiCtrl-->>UI: 200 OK (Cart updated)
    
    note over UI: Customer navigates to standard Cart & Checkout page
    Customer->>UI: Clicks "Proceed to Checkout"
    Customer->>UI: Submits Delivery Address & Details
    UI->>OrderSvc: POST /api/orders/create (OrderRequest)
    OrderSvc->>DB: Verify prices from foods collection & create OrderEntity
    OrderSvc->>Razorpay: razorpayClient.orders.create({amount: total * 100, currency: "INR"})
    Razorpay-->>OrderSvc: Return razorpayOrderId
    OrderSvc->>DB: Save OrderEntity with paymentStatus="Pending"
    OrderSvc-->>UI: OrderResponse with razorpayOrderId
    
    Customer->>Razorpay: Completes payment in Razorpay modal
    Razorpay-->>UI: Returns razorpay_payment_id & signature
    UI->>OrderSvc: POST /api/orders/verify
    OrderSvc->>OrderSvc: Cryptographic HMAC-SHA256 signature verification
    OrderSvc->>DB: Update order status to "Food Preparing", clear user cart
    OrderSvc-->>UI: 200 OK (Payment Verified)
```

---

## 4. Database Schema & Data Models

All data is stored in MongoDB collections within the `foodsResturant` database:

```mermaid
erDiagram
    FoodEntity ||--o{ NutritionInfo : embeds
    FoodEntity ||--o{ OrderItem : references
    FoodEntity ||--o{ ProposedOrderItem : references
    UserEntity ||--o{ CartEntity : owns
    UserEntity ||--o{ OrderEntity : places
    UserEntity ||--o{ ProposedOrderEntity : owns
    UserEntity ||--o{ AiConversationEntity : owns
    ProposedOrderEntity ||--o{ ProposedOrderItem : contains
    ProposedOrderEntity ||--o{ NutritionSummary : embeds
    OrderEntity ||--o{ OrderItem : contains

    FoodEntity {
        string id PK
        string name
        string description
        double price
        string category
        string imageUrl
        boolean available
        boolean isVegetarian
        string spiceLevel
        NutritionInfo nutrition
    }

    NutritionInfo {
        Double calories
        Double protein
        Double carbohydrates
        Double fat
        Double fiber
        Double sugar
        Double servingSize
        string servingUnit
        Boolean isVegetarian
        list allergens
        string dataSource
        Boolean isVerified
    }

    ProposedOrderEntity {
        string id PK
        string userId FK
        string sessionId
        list items
        double subtotal
        double estimatedTax
        double estimatedShipping
        double estimatedTotal
        NutritionSummary nutritionSummary
        Instant updatedAt
    }

    AiConversationEntity {
        string id PK
        string userId FK
        string sessionId
        list messages
        Instant createdAt
        Instant updatedAt
    }

    CartEntity {
        string id PK
        string userId FK
        map items
    }

    OrderEntity {
        string id PK
        string userId FK
        string userAddress
        string phoneNumber
        string email
        list orderedItems
        double amount
        string paymentStatus
        string razorpayOrderId
        string razorpaySignature
        string razorpayPaymentId
        string orderStatus
    }

    UserEntity {
        string id PK
        string name
        string email
        string password
    }
```

---

## 5. Security Architecture & Threat Model

| Risk / Threat | Mitigation Mechanism | Implementation Location |
| :--- | :--- | :--- |
| **Price Tampering** | Client-supplied prices are strictly ignored. All item totals and subtotals are recalculated from database entities. | `ProposedOrderServiceImpl.java`<br>`OrderServiceImpl.java` |
| **Unauthorized Data Access** | Every conversation and proposed order lookup verifies that `userId` matches the authenticated JWT principal. | `AiAssistantServiceImpl.java`<br>`UserServiceImp.java` |
| **Hallucinated Orders** | The AI assistant has no access to the payment gateway or order creation services. Transfer to cart is an explicit user step. | `AiToolRegistry.java`<br>`ProposedOrderService.java` |
| **External AI Downtime** | If Gemini or OpenAI is unavailable or rate-limited, an internal deterministic NLP fallback executes the exact same tools. | `LlmServiceImpl.java` |
| **Payment Signature Forgery** | Razorpay payment callbacks verify the HMAC-SHA256 signature using the secret key before marking orders as paid. | `OrderServiceImpl.java` |
| **Sensitive Credential Exposure** | Database credentials, JWT secret, AWS keys, and Razorpay secrets are bound via environment variables. | `application.properties`<br>`.gitignore` |

---

## 6. REST API Endpoint Catalog

### Core Store & Menu Endpoints
- `GET /api/foods` — Retrieve list of all available food dishes.
- `GET /api/foods/{id}` — Retrieve detailed information for a single dish.
- `POST /api/foods` — (Admin) Add new food item with multipart image upload to AWS S3.
- `DELETE /api/foods/{id}` — (Admin) Delete food item and remove its image from S3.

### AI Assistant Endpoints
- `POST /api/ai/chat` — Process natural language queries, execute backend tools, and return chat response.
- `GET /api/ai/conversations/{conversationId}` — Retrieve conversation history for authenticated user or session.
- `GET /api/ai/proposed-order` — Retrieve current proposed meal plan and server-verified calculations.
- `PATCH /api/ai/proposed-order` — Mutate proposed order (add, remove, replace, update quantities).
- `DELETE /api/ai/proposed-order` — Clear current proposed order.
- `POST /api/ai/proposed-order/transfer-to-cart` — Safely move proposed items into the customer's active shopping cart.

### Nutrition Endpoints
- `GET /api/nutrition/food/{foodId}` — Retrieve verified nutritional data and data source for a dish.
- `POST /api/nutrition/calculate` — Compute aggregate macros across an arbitrary map of dish IDs and quantities.

### Cart & Checkout Endpoints
- `GET /api/cart` — Get items in active shopping cart for authenticated user.
- `POST /api/cart` — Add item to cart.
- `POST /api/cart/remove` — Decrease item quantity in cart.
- `DELETE /api/cart` — Clear all items in active cart.
- `POST /api/orders/create` — Create order entity and initiate Razorpay gateway order.
- `POST /api/orders/verify` — Verify Razorpay cryptographic payment signature.
- `GET /api/orders` — List user's historical orders.
- `GET /api/orders/all` — (Admin) List all customer orders.
- `PATCH /api/orders/status/{orderId}` — (Admin) Update preparation and delivery status.
