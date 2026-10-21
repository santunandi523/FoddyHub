import React, { useContext, useState } from 'react';
import { StoreContext } from '../context/StoreContext';
import { modifyProposedOrder, transferProposedOrderToCart, clearProposedOrder } from '../../Service/aiService';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';

const ProposedOrderCard = ({ proposedOrder, onOrderUpdated }) => {
  const { token, loadCartData } = useContext(StoreContext);
  const navigate = useNavigate();
  const [isTransferring, setIsTransferring] = useState(false);

  if (!proposedOrder || !proposedOrder.items || proposedOrder.items.length === 0) {
    return null;
  }

  const handleUpdateQty = async (foodId, currentQty, delta) => {
    const newQty = currentQty + delta;
    try {
      const updated = await modifyProposedOrder({
        action: newQty <= 0 ? 'REMOVE' : 'UPDATE_QTY',
        foodId,
        quantity: newQty
      });
      if (onOrderUpdated) onOrderUpdated(updated);
    } catch (err) {
      toast.error("Failed to update item quantity");
    }
  };

  const handleRemoveItem = async (foodId) => {
    try {
      const updated = await modifyProposedOrder({
        action: 'REMOVE',
        foodId
      });
      if (onOrderUpdated) onOrderUpdated(updated);
      toast.info("Item removed from proposed meal");
    } catch (err) {
      toast.error("Failed to remove item");
    }
  };

  const handleClear = async () => {
    try {
      await clearProposedOrder();
      if (onOrderUpdated) {
        onOrderUpdated({ ...proposedOrder, items: [], subtotal: 0, estimatedTotal: 0 });
      }
      toast.info("Proposed meal cleared");
    } catch (err) {
      toast.error("Failed to clear proposed meal");
    }
  };

  const handleTransferToCart = async () => {
    if (!token) {
      toast.warning("Please log in to transfer your proposed meal to your cart!");
      navigate('/login');
      return;
    }

    try {
      setIsTransferring(true);
      const res = await transferProposedOrderToCart();
      if (res.success) {
        toast.success(res.message || "Meal transferred to cart!");
        // Refresh local cart store
        await loadCartData(token);
      }
    } catch (err) {
      toast.error("Failed to transfer meal to cart. Please check login.");
    } finally {
      setIsTransferring(false);
    }
  };

  const nutrition = proposedOrder.nutritionSummary;

  return (
    <div className="proposed-order-card">
      <div className="proposed-order-header d-flex justify-content-between align-items-center">
        <div>
          <span className="badge bg-warning text-dark me-2">Proposed Meal</span>
          <strong>Server-Verified Plan</strong>
        </div>
        <button className="btn btn-link btn-sm text-danger text-decoration-none p-0" onClick={handleClear} title="Clear Proposed Order">
          <i className="bi bi-trash3 me-1"></i>Clear
        </button>
      </div>

      {/* Items list */}
      <div className="proposed-items-list my-2">
        {proposedOrder.items.map((item) => (
          <div key={item.foodId} className="proposed-item-row d-flex justify-content-between align-items-center py-2 border-bottom">
            <div className="d-flex align-items-center gap-2">
              <img
                src={item.imageUrl || "https://images.unsplash.com/photo-1546069901-ba9599a7e63c?w=100"}
                alt={item.name}
                className="proposed-item-thumb"
              />
              <div>
                <div className="proposed-item-name">{item.name}</div>
                <div className="text-muted small">
                  &#8377;{item.unitPrice} each
                  {item.nutrition?.calories && (
                    <span className="ms-1">({Math.round(item.nutrition.calories * item.quantity)} kcal)</span>
                  )}
                </div>
              </div>
            </div>

            <div className="d-flex align-items-center gap-2">
              <div className="proposed-qty-control btn-group btn-group-sm">
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  onClick={() => handleUpdateQty(item.foodId, item.quantity, -1)}
                >
                  -
                </button>
                <span className="btn btn-light disabled px-2 text-dark font-monospace">
                  {item.quantity}
                </span>
                <button
                  type="button"
                  className="btn btn-outline-secondary"
                  onClick={() => handleUpdateQty(item.foodId, item.quantity, 1)}
                >
                  +
                </button>
              </div>

              <div className="proposed-item-subtotal text-end" style={{ minWidth: "60px" }}>
                <strong>&#8377;{item.itemSubtotal?.toFixed(2)}</strong>
              </div>

              <button
                type="button"
                className="btn btn-sm text-muted p-0 ms-1"
                onClick={() => handleRemoveItem(item.foodId)}
                title="Remove"
              >
                <i className="bi bi-x-circle"></i>
              </button>
            </div>
          </div>
        ))}
      </div>

      {/* Nutrition Summary */}
      {nutrition && (
        <div className="proposed-nutrition-box p-2 mb-2 bg-light rounded small border">
          <div className="fw-semibold text-secondary mb-1">
            <i className="bi bi-heart-pulse-fill text-danger me-1"></i>
            Meal Nutrition Breakdown:
          </div>
          <div className="d-flex flex-wrap gap-3 text-dark">
            <div><strong>{Math.round(nutrition.totalCalories || 0)}</strong> kcal</div>
            <div><strong>{nutrition.totalProtein || 0}g</strong> protein</div>
            <div><strong>{nutrition.totalCarbohydrates || 0}g</strong> carbs</div>
            <div><strong>{nutrition.totalFat || 0}g</strong> fat</div>
            <div><strong>{nutrition.totalFiber || 0}g</strong> fiber</div>
          </div>
          {nutrition.note && (
            <div className="text-muted mt-1 fst-italic" style={{ fontSize: "0.75rem" }}>
              * {nutrition.note}
            </div>
          )}
        </div>
      )}

      {/* Pricing Breakdown */}
      <div className="proposed-totals-box bg-light p-2 rounded small mb-2 border">
        <div className="d-flex justify-content-between mb-1">
          <span className="text-muted">Subtotal (Verified)</span>
          <span>&#8377;{proposedOrder.subtotal?.toFixed(2)}</span>
        </div>
        <div className="d-flex justify-content-between mb-1">
          <span className="text-muted">Est. Delivery Fee</span>
          <span>&#8377;{proposedOrder.estimatedShipping?.toFixed(2)}</span>
        </div>
        <div className="d-flex justify-content-between mb-1">
          <span className="text-muted">Est. GST / Tax (10%)</span>
          <span>&#8377;{proposedOrder.estimatedTax?.toFixed(2)}</span>
        </div>
        <div className="d-flex justify-content-between pt-1 border-top fw-bold text-dark fs-6">
          <span>Est. Total</span>
          <span className="text-primary">&#8377;{proposedOrder.estimatedTotal?.toFixed(2)}</span>
        </div>
        <div className="text-muted text-center mt-1" style={{ fontSize: "0.72rem" }}>
          * Final taxes and shipping are verified at checkout.
        </div>
      </div>

      {/* Safe Transfer to Cart Button */}
      <div className="d-grid gap-2">
        <button
          className="btn btn-success fw-bold ai-btn-transfer"
          onClick={handleTransferToCart}
          disabled={isTransferring}
        >
          {isTransferring ? (
            <span>
              <span className="spinner-border spinner-border-sm me-2"></span>
              Transferring...
            </span>
          ) : (
            <span>
              <i className="bi bi-cart-check-fill me-2"></i>
              Add Proposed Meal to Cart
            </span>
          )}
        </button>
      </div>
    </div>
  );
};

export default ProposedOrderCard;
