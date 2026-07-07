export const calculateCartTotals=(cartItem,quantities)=>{
    //calculation
    const subTotal=cartItem.reduce((acc,food)=>acc+food.price*quantities[food.id],0);
    const shipping=subTotal===0?0.0:10;
    const tax=subTotal*0.1;
    const total=subTotal+shipping+tax;
    return {subTotal,total,tax,shipping};
}