import { createContext, useEffect, useState } from "react";
import { fetchFoodList } from "../../Service/FoodService";

export const StoreContext= createContext(null);

export const StoreContextProvider=(props)=>{

    const [foodList,setFoodList]=useState([]);
    const[quantities,setQuantities]=useState({});
   
    const increaseQty=(foodId)=>{
        setQuantities((prev)=>({...prev,[foodId]:(prev[foodId] || 0)+1}));
    }

    const decreaseQty=(foodId)=>{
        setQuantities((prev)=>({...prev,[foodId]:prev[foodId]>0?prev[foodId]- 1:0}));
    }

    const removeFromCart=(foodId)=>setQuantities((prevQty)=>{
        const updatedQty={...prevQty};
        delete updatedQty[foodId];
        return updatedQty;
    })

    const contextValue={
        foodList,
        increaseQty,
        decreaseQty,
        quantities,
        removeFromCart,
       
    };

    useEffect(()=>{
        async function loadData(){
            const data=await fetchFoodList();
            setFoodList(data);
            console.log(data);
        }
        loadData();
    },[]);
    return(
        <StoreContext.Provider value={contextValue}>
            {props.children}
        </StoreContext.Provider>
    )
}