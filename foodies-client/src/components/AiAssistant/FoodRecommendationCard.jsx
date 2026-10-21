import React, { useContext } from 'react';
import { Link } from 'react-router-dom';
import { StoreContext } from '../context/StoreContext';
import { toast } from 'react-toastify';

const FoodRecommendationCard = ({ food, onAddToProposed }) => {
  const { increaseQty, token } = useContext(StoreContext);

  const handleAddToCart = async () => {
    if (!token) {
      toast.warning("Please log in to add items directly to your cart");
      return;
    }
    try {
      await increaseQty(food.id);
      toast.success(`${food.name} added to cart!`);
    } catch (err) {
      toast.error("Failed to add to cart");
    }
  };

  const calories = food.nutrition?.calories;
  const protein = food.nutrition?.protein;

  return (
    <div className="ai-food-card">
      <div className="ai-food-img-wrapper">
        <img
          src={food.imageUrl || "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=300"}
          alt={food.name}
          className="ai-food-img"
          loading="lazy"
        />
        {food.isVegetarian !== undefined && food.isVegetarian !== null && (
          <span className={`ai-diet-badge ${food.isVegetarian ? 'veg' : 'non-veg'}`}>
            <span className="ai-diet-dot"></span>
            {food.isVegetarian ? 'Veg' : 'Non-Veg'}
          </span>
        )}
      </div>

      <div className="ai-food-body">
        <div className="d-flex justify-content-between align-items-start gap-1 mb-1">
          <h6 className="ai-food-title mb-0">{food.name}</h6>
          <span className="ai-food-price">&#8377;{food.price}</span>
        </div>

        <div className="ai-food-meta mb-2">
          <span className="badge bg-light text-secondary border me-1">{food.category}</span>
          {food.spiceLevel && (
            <span className={`badge me-1 ${food.spiceLevel.toLowerCase() === 'spicy' ? 'text-bg-danger' : 'text-bg-warning'}`}>
              🌶️ {food.spiceLevel}
            </span>
          )}
        </div>

        {/* Nutritional Highlights */}
        <div className="ai-nutrition-pills mb-2">
          {calories !== undefined && calories !== null ? (
            <span className="ai-nutri-chip">
              <i className="bi bi-fire text-danger me-1"></i>
              {Math.round(calories)} kcal
            </span>
          ) : (
            <span className="ai-nutri-chip text-muted">
              <i className="bi bi-info-circle me-1"></i>Nutrition unavail.
            </span>
          )}
          {protein !== undefined && protein !== null && (
            <span className="ai-nutri-chip">
              <i className="bi bi-lightning-charge text-primary me-1"></i>
              {protein}g protein
            </span>
          )}
        </div>

        {food.description && (
          <p className="ai-food-desc text-muted mb-2">
            {food.description.length > 70 ? `${food.description.substring(0, 70)}...` : food.description}
          </p>
        )}

        {/* Actions */}
        <div className="ai-food-actions">
          <Link to={`/food/${food.id}`} className="btn btn-outline-secondary btn-sm ai-btn-view">
            Details
          </Link>
          <button
            type="button"
            className="btn btn-primary btn-sm ai-btn-add"
            onClick={handleAddToCart}
          >
            <i className="bi bi-cart-plus me-1"></i> Add to Cart
          </button>
        </div>
      </div>
    </div>
  );
};

export default FoodRecommendationCard;
