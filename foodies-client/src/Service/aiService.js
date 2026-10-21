import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_URL || "http://localhost:8080";
const AI_API = `${BASE_URL}/api/ai`;
const NUTRITION_API = `${BASE_URL}/api/nutrition`;

// Get or generate a persistent session ID for guest conversation continuity
export const getAiSessionId = () => {
    let sessionId = localStorage.getItem("ai_session_id");
    if (!sessionId) {
        sessionId = "sess_" + Math.random().toString(36).substring(2, 12) + "_" + Date.now();
        localStorage.setItem("ai_session_id", sessionId);
    }
    return sessionId;
};

const getHeaders = () => {
    const token = localStorage.getItem("token");
    const headers = { "Content-Type": "application/json" };
    if (token) {
        headers["Authorization"] = `Bearer ${token}`;
    }
    return headers;
};

export const sendChatMessage = async (message, conversationId = null) => {
    try {
        const sessionId = getAiSessionId();
        const payload = {
            message,
            conversationId,
            sessionId
        };
        const response = await axios.post(`${AI_API}/chat`, payload, {
            headers: getHeaders()
        });
        return response.data;
    } catch (error) {
        console.error("Error sending AI chat message:", error);
        throw error;
    }
};

export const getConversationHistory = async (conversationId) => {
    try {
        const sessionId = getAiSessionId();
        const response = await axios.get(`${AI_API}/conversations/${conversationId}?sessionId=${sessionId}`, {
            headers: getHeaders()
        });
        return response.data;
    } catch (error) {
        console.error("Error fetching conversation history:", error);
        throw error;
    }
};

export const getProposedOrder = async () => {
    try {
        const sessionId = getAiSessionId();
        const response = await axios.get(`${AI_API}/proposed-order?sessionId=${sessionId}`, {
            headers: getHeaders()
        });
        return response.data;
    } catch (error) {
        console.error("Error fetching proposed order:", error);
        throw error;
    }
};

export const modifyProposedOrder = async (actionData) => {
    try {
        const sessionId = getAiSessionId();
        const response = await axios.patch(`${AI_API}/proposed-order?sessionId=${sessionId}`, actionData, {
            headers: getHeaders()
        });
        return response.data;
    } catch (error) {
        console.error("Error modifying proposed order:", error);
        throw error;
    }
};

export const clearProposedOrder = async () => {
    try {
        const sessionId = getAiSessionId();
        const response = await axios.delete(`${AI_API}/proposed-order?sessionId=${sessionId}`, {
            headers: getHeaders()
        });
        return response.data;
    } catch (error) {
        console.error("Error clearing proposed order:", error);
        throw error;
    }
};

export const transferProposedOrderToCart = async () => {
    try {
        const sessionId = getAiSessionId();
        const response = await axios.post(`${AI_API}/proposed-order/transfer-to-cart?sessionId=${sessionId}`, {}, {
            headers: getHeaders()
        });
        return response.data;
    } catch (error) {
        console.error("Error transferring proposed order to cart:", error);
        throw error;
    }
};

export const fetchFoodNutrition = async (foodId) => {
    try {
        const response = await axios.get(`${NUTRITION_API}/food/${foodId}`);
        return response.data;
    } catch (error) {
        console.error("Error fetching food nutrition:", error);
        throw error;
    }
};

export const calculateCustomNutrition = async (foodIdQuantities) => {
    try {
        const response = await axios.post(`${NUTRITION_API}/calculate`, foodIdQuantities);
        return response.data;
    } catch (error) {
        console.error("Error calculating nutrition:", error);
        throw error;
    }
};

export default {
    sendChatMessage,
    getConversationHistory,
    getProposedOrder,
    modifyProposedOrder,
    clearProposedOrder,
    transferProposedOrderToCart,
    fetchFoodNutrition,
    calculateCustomNutrition,
    getAiSessionId
};
