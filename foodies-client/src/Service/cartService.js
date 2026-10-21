import axios from "axios";
import {toast} from 'react-toastify';
const API_URL = `${import.meta.env.VITE_API_URL}/api/cart`;



export const addToCart=async(foodId,token)=>{
    try {
        await axios.post(API_URL,{foodId},{headers:{"Authorization":`Bearer ${token}`}});
    } catch (error) {
        toast.error("Error while adding Item , Login first");
        console.log(error);
        throw error;
    }
}

export const decreaseFromCart=async(foodId,token)=>{
    try {
        await axios.post(API_URL+'/remove',{foodId},{headers:{"Authorization":`Bearer ${token}`}})
    } catch (error) {
        toast.error("Error while removing Item");
        console.log(error);
        throw error;
    }
}

export const getCartItem=async(token)=>{
    try {
        const response= await axios.get(API_URL,{headers:{"Authorization":`Bearer ${token}`}});
        return response.data.items;
    } catch (error) {
        toast.error("Error while fetching details");
        console.log(error);
        throw error;
    }
}

export default{addToCart,decreaseFromCart,getCartItem};