import React, { useContext } from 'react'
import './Cart.css';
import { StoreContext } from '../../context/StoreContext';
import { Link, useNavigate } from 'react-router-dom';
import { calculateCartTotals } from '../../../util/cartUtils';
const Cart = () => {
    const navigate=useNavigate();
    const {removeFromCart,foodList,decreaseQty,increaseQty,quantities}=useContext(StoreContext);
    //cart items
    const cartItem=foodList.filter(food=>quantities[food.id]>0);

    //calculation
    const {subTotal,total,tax,shipping}=calculateCartTotals(cartItem,quantities);

  return (
    <div>
        <div className="container py-5">
        <h1 className="cart-page-title">Your <span>Shopping Cart</span></h1>
        <div className="row">
            <div className="col-lg-8">

            {
                cartItem.length===0?(
                    <div className="cart-empty"><i className="bi bi-cart-x"></i><p>Your cart is empty. Time to add some delicious food!</p></div>
                ):(
                    <div className="mb-4">
                            {cartItem.map((food)=>(
                                <div key={food.id} className="cart-item-card">
                                  <img src={food.imageUrl} alt={food.name} className="cart-item-img" />
                                  <div className="cart-item-info">
                                    <div className="cart-item-name">{food.name}</div>
                                    <div className="cart-item-category">{food.category}</div>
                                  </div>
                                  <div className="cart-qty-group">
                                    <button className="cart-qty-btn" type="button" onClick={()=>decreaseQty(food.id)}>-</button>
                                    <div className="cart-qty-val">{quantities[food.id]}</div>
                                    <button className="cart-qty-btn" type="button" onClick={()=>increaseQty(food.id)}>+</button>
                                  </div>
                                  <div className="cart-item-price ms-3 me-3">&#8377;{food.price * quantities[food.id]}</div>
                                  <button className="cart-remove-btn" onClick={()=>removeFromCart(food.id)}>
                                    <i className="bi bi-trash3-fill"></i>
                                  </button>
                                </div>
                            ))}                            
                    </div>
                )
            }
            
                <div className="text-start mb-4">
                    <Link to={'/'} href="#" className="btn btn-outline-primary">
                        <i className="bi bi-arrow-left me-2"></i>Continue Shopping
                    </Link>
                </div>
            </div>
            <div className="col-lg-4">

                <div className="order-summary-card">
                        <div className="order-summary-title">Order Summary</div>
                        <div className="order-summary-row"><span>Subtotal</span><span>&#8377;{subTotal.toFixed(2)}</span></div>
                        <div className="order-summary-row"><span>Shipping</span><span>&#8377;{shipping.toFixed(2)}</span></div>
                        <div className="order-summary-row"><span>Tax (10%)</span><span>&#8377;{tax.toFixed(2)}</span></div>
                        <div className="order-summary-row total"><span>Total</span><span>&#8377;{total.toFixed(2)}</span></div>
                        <button className="btn-checkout" 
                            disabled={cartItem.length===0} 
                            onClick={()=>navigate('/orders')}>
                            Proceed to Checkout
                        </button>
                </div>
            </div>
        </div>
    </div>
    </div>
  )
}

export default Cart
