import axios from 'axios';
import React, { useContext, useEffect, useState } from 'react';
import { Navigate, useNavigate, useParams } from 'react-router-dom';
import { fetchFoodDetails } from '../../../Service/FoodService';
import {toast} from 'react-toastify';
import { StoreContext } from '../../context/StoreContext';
const FoodDetails = () => {
    const {id}=useParams();
    const [data,setData]=useState({});
    const {increaseQty}=useContext(StoreContext);
    const navigate=useNavigate();
    useEffect(()=>{
        const loadFoodDetails=async()=>{
           try {
             const foodData= await fetchFoodDetails(id);
             setData(foodData);
           } catch (error) {
            toast.error('Error displaying the food details');
           }
        }
        loadFoodDetails();
    },[id]);


    const addToCart=()=>{
        increaseQty(data.id);
        navigate("/cart");
    }
  return (
    <div>
      <section className="py-5">
            <div className="container px-4 px-lg-5 my-5">
                <div className="row gx-4 gx-lg-5 align-items-center">
                    <div className="col-md-6"><img className="card-img-top mb-5 mb-md-0" src={data.imageUrl} alt="..." /></div>
                    <div className="col-md-6">
                        <div className="fs-5 mb-1 d-flex align-items-center gap-2">
                            <span>Category: <span className='badge text-bg-warning'>{data.category}</span></span>
                            {data.isVegetarian !== undefined && data.isVegetarian !== null && (
                                <span className={`badge ${data.isVegetarian ? 'bg-success' : 'bg-danger'}`}>
                                    {data.isVegetarian ? '🌱 Vegetarian' : '🍗 Non-Vegetarian'}
                                </span>
                            )}
                            {data.spiceLevel && (
                                <span className={`badge ${data.spiceLevel.toLowerCase() === 'spicy' ? 'bg-danger' : 'bg-warning text-dark'}`}>
                                    🌶️ {data.spiceLevel}
                                </span>
                            )}
                        </div>
                        <h1 className="display-5 fw-bolder">{data.name}</h1>
                        <div className="fs-5 mb-2">
                            <span>&#8377;{data.price}.00</span>
                        </div>
                        <p className="lead">{data.description}</p>

                        {/* Nutrition Analyzer Section */}
                        <div className="card mb-4 border-primary-subtle bg-light shadow-sm">
                            <div className="card-header bg-white py-2 d-flex justify-content-between align-items-center">
                                <span className="fw-bold text-dark">
                                    <i className="bi bi-heart-pulse-fill text-danger me-1"></i>
                                    Nutritional Information
                                </span>
                                {data.nutrition ? (
                                    <span className={`badge ${data.nutrition.isVerified ? 'bg-success' : 'bg-secondary'}`}>
                                        {data.nutrition.isVerified ? 'Verified Lab Data' : 'Recipe Estimate'}
                                    </span>
                                ) : (
                                    <span className="badge bg-secondary">Unavailable</span>
                                )}
                            </div>
                            <div className="card-body py-3">
                                {data.nutrition?.calories ? (
                                    <>
                                        <div className="row text-center g-2 mb-2">
                                            <div className="col-4 col-sm">
                                                <div className="p-2 bg-white rounded border">
                                                    <div className="text-muted small">Calories</div>
                                                    <div className="fs-6 fw-bold text-danger">{Math.round(data.nutrition.calories)} kcal</div>
                                                </div>
                                            </div>
                                            <div className="col-4 col-sm">
                                                <div className="p-2 bg-white rounded border">
                                                    <div className="text-muted small">Protein</div>
                                                    <div className="fs-6 fw-bold text-primary">{data.nutrition.protein || 0}g</div>
                                                </div>
                                            </div>
                                            <div className="col-4 col-sm">
                                                <div className="p-2 bg-white rounded border">
                                                    <div className="text-muted small">Carbs</div>
                                                    <div className="fs-6 fw-bold text-success">{data.nutrition.carbohydrates || 0}g</div>
                                                </div>
                                            </div>
                                            <div className="col-6 col-sm">
                                                <div className="p-2 bg-white rounded border">
                                                    <div className="text-muted small">Fat</div>
                                                    <div className="fs-6 fw-bold text-warning">{data.nutrition.fat || 0}g</div>
                                                </div>
                                            </div>
                                            <div className="col-6 col-sm">
                                                <div className="p-2 bg-white rounded border">
                                                    <div className="text-muted small">Fiber</div>
                                                    <div className="fs-6 fw-bold text-info">{data.nutrition.fiber || 0}g</div>
                                                </div>
                                            </div>
                                        </div>
                                        <div className="text-muted small">
                                            {data.nutrition.servingSize && (
                                                <span>Serving: {data.nutrition.servingSize} {data.nutrition.servingUnit || 'g'} • </span>
                                            )}
                                            <span>Source: {data.nutrition.dataSource || 'Standard Recipe Lab'}</span>
                                            {data.nutrition.allergens && data.nutrition.allergens.length > 0 && (
                                                <span className="ms-1 text-danger">• Allergens: {data.nutrition.allergens.join(', ')}</span>
                                            )}
                                        </div>
                                    </>
                                ) : (
                                    <div className="text-muted small fst-italic">
                                        Nutrition information unavailable for this dish.
                                    </div>
                                )}
                            </div>
                        </div>

                        <div className="d-flex">
                            <button className="btn btn-outline-dark flex-shrink-0" type="button" onClick={addToCart}>
                                <i className="bi-cart-fill me-1"></i>
                                Add to cart
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </section>

    </div>
  )
}

export default FoodDetails;
