import axios from "axios";

const BASE_URL = import.meta.env.VITE_API_URL;
const API_URL = `${BASE_URL}/api/foods`;
const REGISTER_API = `${BASE_URL}/api/register`;


export const fetchFoodList=async()=>{
    try{
        const response=await axios.get(API_URL);
        return response.data;
    }catch(error){
        console.log('Error fetching the food list',error);
        throw error;
    }
    
}

export const fetchFoodDetails=async(id) =>{
    try{

        const response=await axios.get(API_URL+"/"+id) ;
        return response.data;

    }catch(error){
        console.log('Error while fetching food details',error);
        throw error;
    }
        
}

export const registerUser=async()=>{
    try{
        await axios.post(REGISTER_API);
        
    }catch(err){
        console.log('Error while registering');
        throw err;
    }
}