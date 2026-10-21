package in.onlineresturant.foodiesapi.service;

import com.razorpay.RazorpayException;
import in.onlineresturant.foodiesapi.io.OrderRequest;
import in.onlineresturant.foodiesapi.io.OrderResponse;

import java.util.List;
import java.util.Map;

public interface OrderService {
OrderResponse createOrderWithPayment(OrderRequest request) throws RazorpayException;

void verifyPayment(Map<String,String> paymentData,String status);

//for loggedin users
List<OrderResponse> getuserOrders();

void removeOrder(String orderId);

//for all orders for admin
List<OrderResponse> getOrdersOfAllUsers();

void updateOrderStatus(String orderId,String status);


}
