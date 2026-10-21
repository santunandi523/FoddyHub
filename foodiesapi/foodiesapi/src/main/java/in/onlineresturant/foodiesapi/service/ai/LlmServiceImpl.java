package in.onlineresturant.foodiesapi.service.ai;

import in.onlineresturant.foodiesapi.entity.AiConversationEntity;
import in.onlineresturant.foodiesapi.entity.NutritionInfo;
import in.onlineresturant.foodiesapi.io.FoodResponse;
import in.onlineresturant.foodiesapi.io.ai.NutritionSummaryResponse;
import in.onlineresturant.foodiesapi.io.ai.ProposedOrderResponse;
import in.onlineresturant.foodiesapi.service.NutritionService;
import in.onlineresturant.foodiesapi.service.ProposedOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmServiceImpl implements LlmService {

    private final AiToolRegistry toolRegistry;
    private final ProposedOrderService proposedOrderService;
    private final NutritionService nutritionService;

    @Value("${ai.provider:gemini}")
    private String aiProvider;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    @Value("${ai.gemini.model:gemini-1.5-flash}")
    private String geminiModel;

    @Value("${ai.openai.api-key:}")
    private String openAiApiKey;

    @Value("${ai.openai.base-url:https://api.openai.com/v1}")
    private String openAiBaseUrl;

    @Value("${ai.openai.model:gpt-4o-mini}")
    private String openAiModel;

    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public LlmExecutionResult processUserMessage(String userMessage,
                                                 List<AiConversationEntity.ChatMessage> history,
                                                 String userId,
                                                 String sessionId) {
        if (!StringUtils.hasText(userMessage)) {
            return LlmExecutionResult.builder()
                    .responseText("Hello! I'm Foodie AI. How can I assist you with your meal today?")
                    .suggestedActions(List.of("Spicy meal under ₹500", "Vegetarian under ₹200", "High-protein meal", "Show proposed order"))
                    .build();
        }

        // Try Live LLM Provider first if configured
        if ("gemini".equalsIgnoreCase(aiProvider) && StringUtils.hasText(geminiApiKey) && !geminiApiKey.contains("dummy")) {
            try {
                LlmExecutionResult res = callGeminiLlm(userMessage, history, userId, sessionId);
                if (res != null) return res;
            } catch (Exception e) {
                log.warn("Gemini LLM call failed or timed out: {}. Using deterministic NLP fallback.", e.getMessage());
            }
        } else if ("openai".equalsIgnoreCase(aiProvider) && StringUtils.hasText(openAiApiKey) && !openAiApiKey.contains("dummy")) {
            try {
                LlmExecutionResult res = callOpenAiLlm(userMessage, history, userId, sessionId);
                if (res != null) return res;
            } catch (Exception e) {
                log.warn("OpenAI LLM call failed or timed out: {}. Using deterministic NLP fallback.", e.getMessage());
            }
        }

        // Fallback deterministic NLP engine (always available, executes exact same tools against MongoDB)
        return executeDeterministicNlp(userMessage, history, userId, sessionId);
    }

    /**
     * Fallback NLP processor that understands natural language queries, extracts constraints,
     * and invokes the exact same server tools against MongoDB.
     */
    private LlmExecutionResult executeDeterministicNlp(String message,
                                                       List<AiConversationEntity.ChatMessage> history,
                                                       String userId,
                                                       String sessionId) {
        String msgLower = message.toLowerCase().trim();

        // 1. Proposed Order Queries: Clear
        if (msgLower.contains("clear") && (msgLower.contains("order") || msgLower.contains("meal") || msgLower.contains("cart"))) {
            Map<String, Object> args = Map.of("action", "CLEAR");
            ProposedOrderResponse order = (ProposedOrderResponse) toolRegistry.executeTool(
                    AiToolRegistry.TOOL_MODIFY_PROPOSED_ORDER, args, userId, sessionId);
            return LlmExecutionResult.builder()
                    .responseText("I've cleared your proposed order. Let me know what you'd like to eat!")
                    .proposedOrder(order)
                    .suggestedActions(List.of("Show vegetarian options", "Spicy dinner for 2", "Meals under ₹300"))
                    .build();
        }

        // 2. Proposed Order Queries: Show / View
        if ((msgLower.contains("show") || msgLower.contains("view") || msgLower.contains("what is"))
                && (msgLower.contains("proposed order") || msgLower.contains("proposed meal") || msgLower.contains("my meal"))) {
            ProposedOrderResponse order = (ProposedOrderResponse) toolRegistry.executeTool(
                    AiToolRegistry.TOOL_GET_PROPOSED_ORDER_SUMMARY, Collections.emptyMap(), userId, sessionId);
            if (order.getItems().isEmpty()) {
                return LlmExecutionResult.builder()
                        .responseText("You don't have any items in your proposed order yet. What would you like to have today?")
                        .proposedOrder(order)
                        .suggestedActions(List.of("Spicy meal for two under ₹500", "High protein lunch", "Vegetarian dishes"))
                        .build();
            }
            return LlmExecutionResult.builder()
                    .responseText(String.format("Here is your current proposed meal. Subtotal is ₹%.2f (Estimated total with tax & delivery is ₹%.2f).",
                            order.getSubtotal(), order.getEstimatedTotal()))
                    .proposedOrder(order)
                    .nutritionSummary(order.getNutritionSummary())
                    .suggestedActions(List.of("Add Proposed Meal to Cart", "Clear proposed order", "Replace with paneer"))
                    .build();
        }

        // 3. Replace item in proposed order
        if (msgLower.contains("replace")) {
            String replaceWith = extractReplaceTarget(msgLower);
            Map<String, Object> args = new HashMap<>();
            args.put("action", "REPLACE");
            if (msgLower.contains("second")) {
                args.put("foodName", "second");
            }
            args.put("replaceWithFoodName", replaceWith);

            ProposedOrderResponse order = (ProposedOrderResponse) toolRegistry.executeTool(
                    AiToolRegistry.TOOL_MODIFY_PROPOSED_ORDER, args, userId, sessionId);
            return LlmExecutionResult.builder()
                    .responseText("Sure! I've updated your proposed meal with an available " + replaceWith + " dish and recalculated the totals.")
                    .proposedOrder(order)
                    .nutritionSummary(order.getNutritionSummary())
                    .suggestedActions(List.of("Add Proposed Meal to Cart", "Show proposed order", "Clear order"))
                    .build();
        }

        // 4. Remove item from proposed order
        if (msgLower.contains("remove") || msgLower.contains("delete")) {
            String removeTarget = extractRemoveTarget(msgLower);
            Map<String, Object> args = new HashMap<>();
            args.put("action", "REMOVE");
            args.put("foodName", removeTarget);

            ProposedOrderResponse order = (ProposedOrderResponse) toolRegistry.executeTool(
                    AiToolRegistry.TOOL_MODIFY_PROPOSED_ORDER, args, userId, sessionId);
            return LlmExecutionResult.builder()
                    .responseText("I've removed " + removeTarget + " from your proposed meal and updated the subtotal.")
                    .proposedOrder(order)
                    .nutritionSummary(order.getNutritionSummary())
                    .suggestedActions(List.of("Add Proposed Meal to Cart", "Show proposed order"))
                    .build();
        }

        // 5. Quantity update: "Increase quantity of ... to 2"
        if (msgLower.contains("quantity") || msgLower.contains("increase") || msgLower.contains("decrease") || msgLower.contains("add another")) {
            Integer qty = extractQuantity(msgLower);
            String itemTarget = extractFoodKeyword(msgLower);
            Map<String, Object> args = new HashMap<>();
            args.put("action", "UPDATE_QTY");
            args.put("foodName", itemTarget);
            args.put("quantity", qty != null ? qty : 2);

            ProposedOrderResponse order = (ProposedOrderResponse) toolRegistry.executeTool(
                    AiToolRegistry.TOOL_MODIFY_PROPOSED_ORDER, args, userId, sessionId);
            return LlmExecutionResult.builder()
                    .responseText("Updated the quantity of " + itemTarget + " to " + (qty != null ? qty : 2) + ". Your subtotal has been recalculated.")
                    .proposedOrder(order)
                    .nutritionSummary(order.getNutritionSummary())
                    .suggestedActions(List.of("Add Proposed Meal to Cart", "Show proposed order"))
                    .build();
        }

        // 6. Meal Planning: Budget + People / Spicy / Meal build request
        // e.g. "I want something spicy for two people under ₹500" or "cheapest combination for three people"
        if (msgLower.contains("for two") || msgLower.contains("for 2") || msgLower.contains("for three") || msgLower.contains("for 3")
                || msgLower.contains("combination") || msgLower.contains("build a meal") || (msgLower.contains("under") && msgLower.contains("spicy"))) {
            Double budget = extractBudget(msgLower);
            Integer people = extractPeopleCount(msgLower);
            Boolean isVeg = extractDietaryPreference(msgLower);
            String spice = extractSpicePreference(msgLower);

            Map<String, Object> args = new HashMap<>();
            args.put("budget", budget != null ? budget : 500.0);
            args.put("numberOfPeople", people != null ? people : 2);
            args.put("isVegetarian", isVeg);
            args.put("spicePreference", spice);

            ProposedOrderResponse proposedOrder = (ProposedOrderResponse) toolRegistry.executeTool(
                    AiToolRegistry.TOOL_BUILD_MEAL_PLAN, args, userId, sessionId);

            String reply;
            if (proposedOrder.getItems().isEmpty()) {
                reply = "I couldn't find available dishes fitting strictly within that budget and criteria. Here are some options you might like:";
            } else {
                reply = String.format("I found dishes that fit your preferences for %d %s under ₹%.0f! Here is a suggested proposed meal. Subtotal is ₹%.2f (estimated total with ₹10 delivery & 10%% tax is ₹%.2f).",
                        people != null ? people : 2,
                        (people != null && people == 1) ? "person" : "people",
                        budget != null ? budget : 500.0,
                        proposedOrder.getSubtotal(),
                        proposedOrder.getEstimatedTotal());
            }

            return LlmExecutionResult.builder()
                    .responseText(reply)
                    .proposedOrder(proposedOrder)
                    .nutritionSummary(proposedOrder.getNutritionSummary())
                    .suggestedActions(List.of("Add Proposed Meal to Cart", "Replace second dish with paneer", "Increase quantity to 2"))
                    .build();
        }

        // 7. Nutrition / Calorie queries
        // e.g. "Build me a meal under 600 calories with at least 30 grams of protein"
        if (msgLower.contains("calorie") || msgLower.contains("protein") || msgLower.contains("nutrition")) {
            Double maxCal = extractCalories(msgLower);
            Double minProt = extractProtein(msgLower);

            if (msgLower.contains("nutrition for") || msgLower.contains("nutrition of")) {
                String foodName = msgLower.replace("nutrition for", "").replace("nutrition of", "").replace("show", "").trim();
                Map<String, Object> details = (Map<String, Object>) toolRegistry.executeTool(
                        AiToolRegistry.TOOL_GET_FOOD_NUTRITION, Map.of("foodName", foodName), userId, sessionId);
                NutritionInfo info = (NutritionInfo) details.get("nutrition");
                String resp;
                if (info == null || info.getCalories() == null) {
                    resp = "Nutrition information unavailable for " + details.get("foodName") + ".";
                } else {
                    resp = String.format("Nutritional facts for %s: Calories: %.0f kcal, Protein: %.1fg, Carbs: %.1fg, Fat: %.1fg, Fiber: %.1fg (%s).",
                            details.get("foodName"), info.getCalories(), info.getProtein(), info.getCarbohydrates(),
                            info.getFat(), info.getFiber(), Boolean.TRUE.equals(info.getIsVerified()) ? "Verified" : "Estimated");
                }
                return LlmExecutionResult.builder()
                        .responseText(resp)
                        .suggestedActions(List.of("Suggest high-protein dish", "Find meals under 600 calories", "Show proposed order"))
                        .build();
            }

            Map<String, Object> searchArgs = new HashMap<>();
            searchArgs.put("maxCalories", maxCal);
            searchArgs.put("minProtein", minProt);
            searchArgs.put("isVegetarian", extractDietaryPreference(msgLower));

            Object searchObj = toolRegistry.executeTool(AiToolRegistry.TOOL_SEARCH_FOODS, searchArgs, userId, sessionId);
            List<FoodResponse> foods = extractFoodList(searchObj);

            String reply = String.format("Here are verified menu items matching your nutrition goals (%s%s):",
                    maxCal != null ? "under " + maxCal.intValue() + " calories" : "",
                    minProt != null ? " with at least " + minProt.intValue() + "g protein" : "");

            return LlmExecutionResult.builder()
                    .responseText(reply)
                    .foodRecommendations(foods)
                    .suggestedActions(List.of("Order these for dinner", "Show proposed order", "Under ₹300"))
                    .build();
        }

        // 8. General search: "Find vegetarian food under ₹200", "Cheapest pizza", "Spicy food"
        Double budget = extractBudget(msgLower);
        Boolean isVeg = extractDietaryPreference(msgLower);
        String spice = extractSpicePreference(msgLower);
        String category = extractCategory(msgLower);
        String keyword = extractFoodKeyword(msgLower);

        Map<String, Object> searchArgs = new HashMap<>();
        searchArgs.put("query", keyword);
        searchArgs.put("category", category);
        searchArgs.put("maxPrice", budget);
        searchArgs.put("isVegetarian", isVeg);
        searchArgs.put("spicePreference", spice);

        Object searchObj = toolRegistry.executeTool(AiToolRegistry.TOOL_SEARCH_FOODS, searchArgs, userId, sessionId);
        List<FoodResponse> foods = extractFoodList(searchObj);

        if (foods.isEmpty()) {
            return LlmExecutionResult.builder()
                    .responseText("I couldn't find any live dishes matching those criteria. Would you like me to show our top recommended dishes instead?")
                    .suggestedActions(List.of("Show all Biriyani", "Vegetarian dishes", "Pizzas under ₹300"))
                    .build();
        }

        String replyText = "Here are live dishes from our menu that fit what you're looking for:";
        if (msgLower.contains("cheapest")) {
            replyText = "Here is the best value option matching your request:";
        }

        return LlmExecutionResult.builder()
                .responseText(replyText)
                .foodRecommendations(foods)
                .suggestedActions(List.of("Add to Proposed Meal", "Show proposed order", "Spicy meal for two under ₹500"))
                .build();
    }

    private LlmExecutionResult callGeminiLlm(String message,
                                             List<AiConversationEntity.ChatMessage> history,
                                             String userId,
                                             String sessionId) {
        String url = String.format("https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                geminiModel, geminiApiKey);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        // System prompt and context
        Map<String, Object> systemInstruction = Map.of(
                "parts", List.of(Map.of("text",
                        "You are Foodie AI, an intelligent food ordering and nutrition assistant for Foodies restaurant.\n" +
                        "CRITICAL RULES:\n" +
                        "1. Use tools to search real menu items from MongoDB. NEVER hallucinate food items, prices, or nutrition.\n" +
                        "2. Delivery charge is ₹10 and tax is 10%. Clearly label subtotals as estimates until full checkout.\n" +
                        "3. Do NOT automatically place orders or execute payments. The user must review and click Add to Cart.\n" +
                        "4. Always invoke backend tools when user asks for search, meal plan, order modification, or nutrition."
                ))
        );

        List<Map<String, Object>> contents = new ArrayList<>();
        if (history != null) {
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                AiConversationEntity.ChatMessage m = history.get(i);
                String role = "user".equalsIgnoreCase(m.getRole()) ? "user" : "model";
                contents.add(Map.of("role", role, "parts", List.of(Map.of("text", m.getContent()))));
            }
        }
        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", message))));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("systemInstruction", systemInstruction);
        requestBody.put("contents", contents);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            List candidates = (List) body.get("candidates");
            if (candidates != null && !candidates.isEmpty()) {
                Map first = (Map) candidates.get(0);
                Map content = (Map) first.get("content");
                if (content != null) {
                    List parts = (List) content.get("parts");
                    if (parts != null && !parts.isEmpty()) {
                        Map firstPart = (Map) parts.get(0);
                        String text = (String) firstPart.get("text");
                        if (StringUtils.hasText(text)) {
                            // If text contains meal recommendations, augment with real DB search results
                            return augmentLlmResponseWithTools(text, message, userId, sessionId);
                        }
                    }
                }
            }
        }
        return null;
    }

    private LlmExecutionResult callOpenAiLlm(String message,
                                             List<AiConversationEntity.ChatMessage> history,
                                             String userId,
                                             String sessionId) {
        String url = openAiBaseUrl + "/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(openAiApiKey);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content",
                "You are Foodie AI, an intelligent food ordering and nutrition assistant for Foodies restaurant.\n" +
                "Always recommend dishes from live menu. Never invent prices. Deliver charge is ₹10 and tax 10%.\n" +
                "Never place orders or bypass checkout."
        ));

        if (history != null) {
            int start = Math.max(0, history.size() - 6);
            for (int i = start; i < history.size(); i++) {
                AiConversationEntity.ChatMessage m = history.get(i);
                messages.add(Map.of("role", m.getRole(), "content", m.getContent()));
            }
        }
        messages.add(Map.of("role", "user", "content", message));

        Map<String, Object> requestBody = Map.of(
                "model", openAiModel,
                "messages", messages,
                "temperature", 0.7
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            Map body = response.getBody();
            List choices = (List) body.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map firstChoice = (Map) choices.get(0);
                Map msg = (Map) firstChoice.get("message");
                if (msg != null) {
                    String content = (String) msg.get("content");
                    if (StringUtils.hasText(content)) {
                        return augmentLlmResponseWithTools(content, message, userId, sessionId);
                    }
                }
            }
        }
        return null;
    }

    private LlmExecutionResult augmentLlmResponseWithTools(String llmText, String userMessage, String userId, String sessionId) {
        // Run deterministic NLP in parallel to attach verified structured cards (ProposedOrder or FoodRecommendations)
        LlmExecutionResult structuredResult = executeDeterministicNlp(userMessage, null, userId, sessionId);
        return LlmExecutionResult.builder()
                .responseText(llmText)
                .foodRecommendations(structuredResult.getFoodRecommendations())
                .proposedOrder(structuredResult.getProposedOrder())
                .nutritionSummary(structuredResult.getNutritionSummary())
                .suggestedActions(structuredResult.getSuggestedActions())
                .build();
    }

    // Helper text extractors
    private Double extractBudget(String text) {
        Pattern p = Pattern.compile("(?:under|below|budget of|within|less than)\\s*(?:₹|rs\\.?|inr)?\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return Double.parseDouble(m.group(1));
        }
        return null;
    }

    private Integer extractPeopleCount(String text) {
        if (text.contains("for two") || text.contains("for 2") || text.contains("two people") || text.contains("2 people")) return 2;
        if (text.contains("for three") || text.contains("for 3") || text.contains("three people") || text.contains("3 people")) return 3;
        if (text.contains("for four") || text.contains("for 4") || text.contains("four people") || text.contains("4 people")) return 4;
        if (text.contains("for one") || text.contains("for 1") || text.contains("single person")) return 1;
        Pattern p = Pattern.compile("(\\d+)\\s*(?:people|persons)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1));
        return 1;
    }

    private Boolean extractDietaryPreference(String text) {
        if (text.contains("non-veg") || text.contains("non veg") || text.contains("chicken") || text.contains("mutton") || text.contains("fish")) return false;
        if (text.contains("vegetarian") || text.contains("pure veg") || text.contains("veg ") || text.contains("paneer")) return true;
        return null;
    }

    private String extractSpicePreference(String text) {
        if (text.contains("medium spice") || text.contains("medium spicy")) return "medium";
        if (text.contains("very spicy") || text.contains("extra spicy") || text.contains("spicy")) return "spicy";
        if (text.contains("mild") || text.contains("less spicy") || text.contains("not spicy")) return "mild";
        return null;
    }

    private Double extractCalories(String text) {
        Pattern p = Pattern.compile("(?:under|below|less than)?\\s*(\\d+)\\s*(?:calories|kcal)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return Double.parseDouble(m.group(1));
        return null;
    }

    private Double extractProtein(String text) {
        Pattern p = Pattern.compile("(?:at least|minimum|min)?\\s*(\\d+)\\s*(?:g|grams)?\\s*(?:of\\s*)?protein", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return Double.parseDouble(m.group(1));
        return null;
    }

    private String extractCategory(String text) {
        String[] cats = {"biriyani", "pizza", "burger", "rolls", "salads", "salad", "cake", "icecreame", "ice cream"};
        for (String c : cats) {
            if (text.contains(c)) return c;
        }
        return null;
    }

    private String extractFoodKeyword(String text) {
        if (text.contains("biryani") || text.contains("biriyani")) return "biriyani";
        if (text.contains("paneer")) return "paneer";
        if (text.contains("pizza")) return "pizza";
        if (text.contains("burger")) return "burger";
        if (text.contains("salad")) return "salad";
        if (text.contains("cake")) return "cake";
        if (text.contains("rolls") || text.contains("roll")) return "rolls";
        return null;
    }

    private String extractReplaceTarget(String text) {
        Pattern p = Pattern.compile("replace.*?with\\s+([a-zA-Z0-9\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) {
            return m.group(1).trim();
        }
        if (text.contains("paneer")) return "paneer";
        return "paneer";
    }

    private String extractRemoveTarget(String text) {
        Pattern p = Pattern.compile("(?:remove|delete)\\s+(?:the\\s+)?([a-zA-Z0-9\\s]+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return m.group(1).trim();
        return "drink";
    }

    private Integer extractQuantity(String text) {
        if (text.contains(" to two") || text.contains(" to 2")) return 2;
        if (text.contains(" to three") || text.contains(" to 3")) return 3;
        if (text.contains(" to four") || text.contains(" to 4")) return 4;
        if (text.contains(" to one") || text.contains(" to 1")) return 1;
        Pattern p = Pattern.compile("(?:to|quantity of|qty)\\s+(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text);
        if (m.find()) return Integer.parseInt(m.group(1));
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<FoodResponse> extractFoodList(Object obj) {
        if (obj instanceof List) {
            return (List<FoodResponse>) obj;
        }
        if (obj instanceof Map) {
            Map m = (Map) obj;
            if (m.get("alternatives") instanceof List) {
                return (List<FoodResponse>) m.get("alternatives");
            }
        }
        return Collections.emptyList();
    }
}
