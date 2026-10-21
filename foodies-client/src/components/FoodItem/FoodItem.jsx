import React, { useContext } from 'react'
import './FoodItem.css'
import { Link } from 'react-router-dom'
import { StoreContext } from '../context/StoreContext'

const FoodItem = ({name, description, id, imageUrl, price, isVegetarian, nutrition, spiceLevel}) => {
  const {decreaseQty, increaseQty, quantities} = useContext(StoreContext);

  return (
    <div className="col-12 col-sm-6 col-md-4 col-lg-3 mb-4 d-flex justify-content-center">
      <div className="food-card">
        {/* Image */}
        <Link to={`/food/${id}`} style={{textDecoration: "none"}}>
          <div className="food-card-img-wrapper position-relative">
            <img src={imageUrl} alt={name} />
            {isVegetarian !== undefined && isVegetarian !== null && (
              <span className={`badge position-absolute top-0 start-0 m-2 ${isVegetarian ? 'bg-success' : 'bg-danger'}`} style={{fontSize: "0.68rem"}}>
                {isVegetarian ? 'Veg' : 'Non-Veg'}
              </span>
            )}
          </div>
        </Link>

        {/* Body */}
        <div className="card-body px-3 pt-3 pb-2">
          <h5 className="card-title">{name}</h5>
          <p className="card-text">{description}</p>
          
          {nutrition?.calories && (
            <div className="d-flex gap-2 mb-2" style={{fontSize: "0.72rem"}}>
              <span className="badge bg-light text-dark border">
                <i className="bi bi-fire text-danger me-1"></i>{Math.round(nutrition.calories)} kcal
              </span>
              {nutrition.protein && (
                <span className="badge bg-light text-dark border">
                  <i className="bi bi-lightning-charge text-primary me-1"></i>{nutrition.protein}g P
                </span>
              )}
            </div>
          )}

          <div className="d-flex justify-content-between align-items-center mt-2">
            <span className="food-price">&#8377;{price}</span>
            <div>
              <i className="bi bi-star-fill text-warning" style={{fontSize:"0.75rem"}}></i>
              <i className="bi bi-star-fill text-warning" style={{fontSize:"0.75rem"}}></i>
              <i className="bi bi-star-fill text-warning" style={{fontSize:"0.75rem"}}></i>
              <i className="bi bi-star-fill text-warning" style={{fontSize:"0.75rem"}}></i>
              <i className="bi bi-star-half text-warning" style={{fontSize:"0.75rem"}}></i>
              <small className="text-muted ms-1" style={{fontSize:"0.7rem"}}>(4.5)</small>
            </div>
          </div>
        </div>

        {/* Footer */}
        <div className="food-card-footer">
          <Link className="btn btn-primary btn-sm btn-view-food" to={`/food/${id}`}>View</Link>

          {quantities[id] > 0 ? (
            <div className="qty-controls">
              <button className="btn btn-danger btn-sm btn-qty" onClick={() => decreaseQty(id)}>
                <i className="bi bi-dash"></i>
              </button>
              <span className="qty-badge">{quantities[id]}</span>
              <button className="btn btn-success btn-sm btn-qty" onClick={() => increaseQty(id)}>
                <i className="bi bi-plus"></i>
              </button>
            </div>
          ) : (
            <button className="btn-add-cart" onClick={() => increaseQty(id)}>
              <i className="bi bi-plus"></i>
            </button>
          )}
        </div>
      </div>
    </div>
  )
}

export default FoodItem
