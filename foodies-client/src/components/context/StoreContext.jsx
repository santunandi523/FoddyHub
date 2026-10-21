import { createContext, useEffect, useState } from "react";
import { fetchFoodList } from "../../Service/FoodService";
import axios from "axios";
import { addToCart, decreaseFromCart, getCartItem } from "../../Service/cartService";

export const StoreContext= createContext(null);

export const StoreContextProvider=(props)=>{

    const [foodList,setFoodList]=useState([]);
    const[quantities,setQuantities]=useState({});
    const[token,setToken]=useState(localStorage.getItem('token') || "");
   
    const handleAuthError = (error) => {
        if (error.response && (error.response.status === 403 || error.response.status === 401)) {
            localStorage.removeItem('token');
            setToken("");
            setQuantities({});
        }
    }

    const increaseQty=async(foodId)=>{
        setQuantities((prev)=>({...prev,[foodId]:(prev[foodId] || 0)+1}));
        try {
            await addToCart(foodId,token);
        } catch (error) {
            handleAuthError(error);
        }
    }

    const decreaseQty=async(foodId)=>{
        setQuantities((prev)=>({...prev,[foodId]:prev[foodId]>0?prev[foodId]- 1:0}));
        try {
            await decreaseFromCart(foodId,token);
        } catch (error) {
            handleAuthError(error);
        }
    }

    const removeFromCart=(foodId)=>setQuantities((prevQty)=>{
        const updatedQty={...prevQty};
        delete updatedQty[foodId];
        return updatedQty;
    });

    const loadCartData=async(token)=>{
        try {
            const items=await getCartItem(token);
            setQuantities(items || {});
        } catch (error) {
            setQuantities({});
            handleAuthError(error);
        }
    }

    const contextValue={
        foodList,
        increaseQty,
        decreaseQty,
        quantities,
        removeFromCart,
        token,
        setToken, 
        setQuantities,      
        loadCartData,
    };

    useEffect(()=>{
        async function loadData(){
            try {
                const data=await fetchFoodList();
                setFoodList(Array.isArray(data) ? data : []);
            } catch (error) {
                setFoodList([]);
            }
            if(localStorage.getItem("token")){
                setToken(localStorage.getItem("token"));
                await loadCartData(localStorage.getItem("token"));
            }
        }
        loadData();
    },[]);
    return(
        <StoreContext.Provider value={contextValue}>
            {props.children}
        </StoreContext.Provider>
    )
}