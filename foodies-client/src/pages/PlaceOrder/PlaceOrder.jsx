import React, { useContext } from "react";
import "./PlaceOrder.css";
import { assets } from "../../assets/assets";
import { StoreContext } from "../../context/StoreContext";
import { calculateCartTotals } from "../../util/cartUtils";
const PlaceOrder = () => {
  const {foodList,quantities,setQuantities}=useContext(StoreContext);
  
   //cart items
    const cartItem=foodList.filter(food=>quantities[food.id]>0);
    
    //calculation
    const {subTotal,total,tax,shipping}=calculateCartTotals(cartItem,quantities);

  return (
    <div>
      <div className="container mt-4">
        <div className="d-flex flex-column align-items-center mt-2 mb-5">
          <img src={assets.foodies} alt="" height={98} width={250} className="mb-4"/>
          
        </div>

        <div className="row">
          {/* Cart Section */}
          <div className="col-md-4 order-md-2 mb-4">
            <h4 className="d-flex justify-content-between align-items-center mb-3">
              <span className="text-primary">Your cart</span>
              <span className="badge bg-primary badge-pill">{cartItem.length}</span>
            </h4>

            <ul className="list-group mb-3">
              {cartItem.map(item=>(
                <li className="list-group-item d-flex justify-content-between lh-condensed">
                <div>
                  <h6 className="my-0">{item.name}</h6>
                  <span>X {quantities[item.id]}</span>
                </div>
                <span className="text-muted">&#8377;{item.price*quantities[item.id]}</span>
              </li>
              ))}

              <li className="list-group-item d-flex justify-content-between">
                <span>Shipping Charge</span>
                <span>&#8377;{shipping.toFixed(2)}</span>               
              </li>

              <li className="list-group-item d-flex justify-content-between">
                <span>Tax (10%)</span>
                <span>&#8377;{tax.toFixed(2)}</span>               
              </li>              

              <li className="list-group-item d-flex justify-content-between">
                <span>Total</span>
                <strong>&#8377;{total.toFixed(2)}</strong>
              </li>

              
            </ul>

          </div>

          {/* Billing Address */}
          <div className="col-md-8 order-md-1">
            

            <form className="needs-validation" noValidate>
              <div className="row">
                <div className="col-md-6 mb-3">
                  <label htmlFor="firstName">First name</label>
                  <input
                    type="text"
                    className="form-control"
                    id="firstName"
                    required
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
                    required
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
                  placeholder="abcd@example.com"
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
                  type="number"
                  className="form-control"
                  id="phone"
                  placeholder="973285xxx"
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
                  placeholder="Apartment or suite"
                />
              </div>

              <div className="row">
                <div className="col-md-5 mb-3">
                  <label htmlFor="country">Country</label>
                  <select
                    className="custom-select d-block w-100"
                    id="country"
                    required
                  >
                    <option value="">Choose...</option>
                    <option>India</option>
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
                  >
                    <option value="">Choose...</option>
                    <option>West Bengal</option>
                    <option value="">Chandigarh</option>
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
                  />

                  <div className="invalid-feedback">Zip code required.</div>
                </div>
              </div>          

              <hr className="mb-4" />

              <button
                className="btn btn-primary btn-lg w-100
                 btn-block"
                type="submit"
                disabled={cartItem.length===0}
              >
                Continue to checkout
              </button>
            </form>
          </div>
        </div>

        <footer className="my-5 pt-5 text-muted text-center text-small">
          <p className="mb-1">&copy; 2017-2018 Company Name</p>

          <ul className="list-inline">
            <li className="list-inline-item">
              <a href="#">Privacy</a>
            </li>

            <li className="list-inline-item">
              <a href="#">Terms</a>
            </li>

            <li className="list-inline-item">
              <a href="#">Support</a>
            </li>
          </ul>
        </footer>
      </div>
    </div>
  );
};

export default PlaceOrder;
