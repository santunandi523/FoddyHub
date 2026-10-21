import axios from "axios";

const API_URL = `${import.meta.env.VITE_API_URL}/api`;


const authHeaders = {
    headers: { "Content-Type": "application/json" },
};

export const registerUser = async (data) => {
    try {
        const payload = { ...data, email: data.email.trim().toLowerCase() };
        const response = await axios.post(API_URL + "/register", payload, authHeaders);
        return response;
    } catch (error) {
        throw error;
    }
};

export const loginUser = async (data) => {
    try {
        const payload = { ...data, email: data.email.trim().toLowerCase() };
        const response = await axios.post(API_URL + "/login", payload, authHeaders);
        return response;
    } catch (error) {
        throw error;
    }
};