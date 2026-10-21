package in.onlineresturant.foodiesapi.service;

import in.onlineresturant.foodiesapi.io.CartRequest;
import in.onlineresturant.foodiesapi.io.CartResponse;

public interface CartService {
    public CartResponse addToCart(CartRequest request);
    public CartResponse getCart();
    public void clearCart();
    public CartResponse removeFromCart(CartRequest request);
}
