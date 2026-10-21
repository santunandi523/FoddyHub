import React, { useContext, useState } from "react";
import "./PlaceOrder.css";
import { assets } from "../../../assets/assets";
import { StoreContext } from "../../context/StoreContext";
import { calculateCartTotals } from "../../../util/cartUtils";
import { useNavigate } from "react-router-dom";
import { toast } from "react-toastify";
import axios from "axios";
import { RAZORPAY_KEY } from "../../../util/constants";

const PlaceOrder = () => {
  const { foodList, quantities, setQuantities, token } = useContext(StoreContext);
  const navigate = useNavigate();

  const [formData, setFormData] = useState({
    firstName: "",
    lastName: "",
    email: "",
    phone: "",
    address: "",
    address2: "",
    country: "",
    state: "",
    zip: ""
  });

  const onChangeHandler = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const cartItem = foodList.filter(food => quantities[food.id] > 0);
  const { subTotal, total, tax, shipping } = calculateCartTotals(cartItem, quantities);

  const onSubmitHandler = async (e) => {
    e.preventDefault();
    if (!token) {
      toast.error("Please login to place an order");
      navigate("/login");
      return;
    }

    if (cartItem.length === 0) {
      toast.error("Your cart is empty");
      return;
    }

    const orderItems = cartItem.map(item => ({
      foodId: item.id,
      quantity: quantities[item.id],
      price: item.price,
      category: item.category,
      imageUrl: item.imageUrl,
      description: item.description,
      name: item.name
    }));

    const orderRequest = {
      orderItems,
      userAddress: `${formData.address}${formData.address2 ? ", " + formData.address2 : ""}, ${formData.state}, ${formData.country} - ${formData.zip}`,
      amount: parseFloat(total.toFixed(2)),
      email: formData.email,
      phoneNumber: formData.phone,
      orderStatus: "Food Preparing"
    };

    try {
      const response = await axios.post(`${import.meta.env.VITE_API_URL}/api/orders/create`, orderRequest, {
        headers: { Authorization: `Bearer ${token}` }
      });
      if ((response.status === 200 || response.status === 201) && response.data.razorpayOrderId) {
        initiateRazorpayPayment(response.data);
      } else {
        toast.error("Failed to place order");
      }
    } catch (error) {
      console.error(error);
      toast.error("Error placing order. Please try again.");
    }
  };

  const initiateRazorpayPayment=(order)=>{
    const options={
      key:RAZORPAY_KEY,
      amount:order.amount,
      currency:"INR",
      name : "Food Plaza",
      description : "Food order Payment",
      order_id:order.razorpayOrderId,
      handler: async function(razorpayResponse){
        await verifyPayment(razorpayResponse);
      },
      prefill:{
        name:`${formData.firstName} ${formData.lastName}`,
        email: formData.email,
        contact: formData.phone
      },
      theme:{color:"#3399cc"},
      modal: {
        ondismiss: async function (){
          toast.error("Payment cancelled. ");
          await deleteOrder(order.id);
        },
      },
    };
    const razorpay=new window.Razorpay(options);
    razorpay.open();
  };

  const verifyPayment=async(razorpayResponse)=>{
    const paymentData={
      razorpay_payment_id : razorpayResponse.razorpay_payment_id,
      razorpay_order_id : razorpayResponse.razorpay_order_id,
      razorpay_signature :razorpayResponse. razorpay_signature
    };
    try {
      const response=await axios.post(`${import.meta.env.VITE_API_URL}/api/orders/verify`,paymentData,{headers:{'Authorization':`Bearer ${token}`}});

    if(response.status===200){
      toast.success('Payment successful.');
      await clearCart();
      navigate("/myorders");
    }else{
      toast.error('Payment failed. Please try again.');
      navigate('/');
    }
    } catch (error) {
      toast.error('Payment failed. Please try again.');
    }
  };

  const deleteOrder=async(orderId)=>{
    try {
      await axios.delete(`${import.meta.env.VITE_API_URL}/api/orders/`+orderId,{headers:{'Authorization' :`Bearer ${token}`}}); 
    } catch (error) {
      toast.error('Something went wrong. Contact support.');
    }
  }

  const clearCart=async()=>{
    try {
      await axios.delete(`${import.meta.env.VITE_API_URL}/api/cart`,{headers :{'Authorization': `Bearer ${token}`}});
      setQuantities({});
    } catch (error) {
      toast.error("Error while clearing the cart.");
    }
  }

  return (
    <div>
      <div className="container mt-4">
        <div className="place-order-header">
          <img src={assets.foodies} alt="" height={98} width={250} className="mb-4"/>
        </div>

        <div className="row">
          <div className="col-md-4 order-md-2 mb-4">
            <div className="place-order-summary">
              <h4 className="summary-heading d-flex justify-content-between align-items-center">
                <span className="text-primary">Your cart</span>
                <span className="badge bg-primary badge-pill">{cartItem.length}</span>
              </h4>

              <div className="summary-item-list">
                {cartItem.map(item=>(
                  <div key={item.id} className="summary-item">
                  <div>
                    <h6 className="my-0">{item.name}</h6>
                    <span>X {quantities[item.id]}</span>
                  </div>
                  <span className="text-muted">&#8377;{item.price*quantities[item.id]}</span>
                </div>
                ))}

                <div className="summary-item">
                  <span>Shipping Charge</span>
                  <span>&#8377;{shipping.toFixed(2)}</span>               
                </div>

                <div className="summary-item">
                  <span>Tax (10%)</span>
                  <span>&#8377;{tax.toFixed(2)}</span>               
                </div>              

                <div className="summary-item">
                  <span>Total</span>
                  <strong>&#8377;{total.toFixed(2)}</strong>
                </div>
              </div>
            </div>
          </div>

          <div className="col-md-8 order-md-1 checkout-form">
            <form className="needs-validation" onSubmit={onSubmitHandler} noValidate>
              <div className="row">
                <div className="col-md-6 mb-3">
                  <label htmlFor="firstName">First name</label>
                  <input
                    type="text"
                    className="form-control"
                    id="firstName"
                    name="firstName"
                    required
                    value={formData.firstName}
                    onChange={onChangeHandler}
                  />
                  <div className="invalid-feedback">
                    Valid first name is required.
                  </div>
                </div>

                <div className="col-md-6 mb-3">
                  <label htmlFor="lastName">Last name</label>
                  <input
                    type="text"
                    className="form-control"
                    id="lastName"
                    name="lastName"
                    required
                    value={formData.lastName}
                    onChange={onChangeHandler}
                  />
                  <div className="invalid-feedback">
                    Valid last name is required.
                  </div>
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="email">
                  Email <span className="text-muted"></span>
                </label>

                <input
                  type="email"
                  className="form-control"
                  id="email"
                  name="email"
                  placeholder="abcd@example.com"
                  value={formData.email}
                  onChange={onChangeHandler}
                />
                <div className="invalid-feedback">
                  Please enter a valid email address for shipping updates.
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="phone">
                  Phone no. <span className="text-muted"></span>
                </label>

                <input
                  type="tel"
                  className="form-control"
                  id="phone"
                  placeholder="973285xxx"
                  value={formData.phone}
                  name="phone"
                  onChange={onChangeHandler}
                />
              </div>

              <div className="mb-3">
                <label htmlFor="address">Address</label>

                <input
                  type="text"
                  className="form-control"
                  id="address"
                  placeholder="1234 Main St"
                  required
                  value={formData.address}
                  name="address"
                  onChange={onChangeHandler}
                />
                <div className="invalid-feedback">
                  Please enter your shipping address.
                </div>
              </div>

              <div className="mb-3">
                <label htmlFor="address2">
                  Address 2 <span className="text-muted">(Optional)</span>
                </label>
                <input
                  type="text"
                  className="form-control"
                  id="address2"
                  name="address2"
                  placeholder="Apartment or suite"
                  value={formData.address2}
                  onChange={onChangeHandler}
                />
              </div>

              <div className="row">
                <div className="col-md-5 mb-3">
                  <label htmlFor="country">Country</label>
                  <select
                    className="custom-select d-block w-100"
                    id="country"
                    required
                    value={formData.country}
                    onChange={onChangeHandler}
                    name="country"
                  >
                    <option value="">Choose...</option>
                    <option value="India">India</option>
                  </select>
                  <div className="invalid-feedback">
                    Please select a valid country.
                  </div>
                </div>

                <div className="col-md-4 mb-3">
                  <label htmlFor="state">State</label>
                  <select
                    className="custom-select d-block w-100"
                    id="state"
                    required
                    value={formData.state}
                    name="state"
                    onChange={onChangeHandler}
                  >
                    <option value="">Choose...</option>
                    <option value="West Bengal">West Bengal</option>
                    <option value="Chandigarh">Chandigarh</option>
                  </select>
                  <div className="invalid-feedback">
                    Please provide a valid state.
                  </div>
                </div>

                <div className="col-md-3 mb-3">
                  <label htmlFor="zip">Zip</label>
                  <input
                    type="text"
                    className="form-control"
                    id="zip"
                    required
                    value={formData.zip}
                    onChange={onChangeHandler}
                    name="zip"
                  />
                  <div className="invalid-feedback">Zip code required.</div>
                </div>
              </div>          

              <hr className="mb-4" />

              <button className="btn-place-order" type="submit" disabled={cartItem.length===0}>
                Continue to checkout
              </button>
            </form>
          </div>
        </div>

        <footer className="my-5 pt-5 text-muted text-center text-small">
          <p className="mb-1">&copy; 2017-2018 Company Name</p>
          <ul className="list-inline">
            <li className="list-inline-item"><a href="#">Privacy</a></li>
            <li className="list-inline-item"><a href="#">Terms</a></li>
            <li className="list-inline-item"><a href="#">Support</a></li>
          </ul>
        </footer>
      </div>
    </div>
  );
};

export default PlaceOrder;
