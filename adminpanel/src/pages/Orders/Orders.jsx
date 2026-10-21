import axios from 'axios';
import { assets } from '../../assets/assets';
import { useEffect, useState } from 'react';

const Orders = () => {
  const[data,setData]=useState([]);

  const fetchOrders=async()=>{
    const response= await axios.get(`${import.meta.env.VITE_API_URL}/api/orders/all`);
    setData(response.data);
  }

  const updateStatus=async(event,orderId)=>{
    const response =await axios.patch(`${import.meta.env.VITE_API_URL}/api/orders/status/${orderId}?status=${event.target.value }`);

    if(response.status===200){
      await fetchOrders();
    }

  }
  useEffect(()=>{
     fetchOrders();
  },[]);

  return (
    <div className='container'>
        <div className="py-5 row justify-content-center">
            <div className="col-12 admin-card">
                <h2 className="admin-card-title">Orders Management</h2>

                
                    {/* <div className="text-center py-5 text-muted">
                        <i className="bi bi-lock fs-1"></i>
                        <p className="mt-2">Please log in to view your orders.</p>
                    </div>
                 
                    <div className="text-center py-5">
                        <div className="spinner-border text-warning" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                        <p className="mt-2 text-muted">Fetching your orders...</p>
                    </div>
                 
                    <div className="text-center py-5 text-muted">
                        <i className="bi bi-bag-x fs-1"></i>
                        <p className="mt-2">No orders found.</p>
                    </div> */}
                
                <table className="table admin-table table-hover"><thead><tr><th>Order ID</th><th>Items</th><th>Amount</th><th>Status</th></tr></thead>
                    <tbody>
                        {
                            data.map((order,index)=>{
                                return(
                                    <tr key={index}>
                                        <td><span className="badge bg-secondary">#{order.id.substring(0,8)}</span></td>

                                        <td>
                                          <div className="fw-bold fs-5">
                                            {(order.orderedItems || []).map((item, index) => (
                                              <span key={index}>
                                                {item.name} x{item.quantity}
                                                {index !== order.orderedItems.length - 1 && ", "}
                                              </span>
                                            ))}
                                          </div>

                                          {(!order.orderedItems || order.orderedItems.length === 0) && (
                                            <span className="text-muted fst-italic">
                                              No item details
                                            </span>
                                          )}

                                          <div className='font-color-primary'>
                                            {order.userAddress}
                                          </div>

                                        </td>
                                        <td>
                                            &#x20B9;{order.amount.toFixed(2)}
                                        </td>
                                        <td>Items: {order.orderedItems ? order.orderedItems.length : 'â€”'}</td>
                                        
                                        <td>
                                            <select className={`status-select ${order.orderStatus === 'Food Preparing' ? 'preparing' : order.orderStatus === 'Out for delivery' ? 'delivery' : order.orderStatus === 'Delivered' ? 'delivered' : ''}`} onChange={(event)=>updateStatus(event,order.id)} value={order.orderStatus}>
                                              <option value="Food Preparing">Food Preparing</option>
                                              <option value="Out for delivery">Out for delivery</option>
                                              <option value="Delivered">Delevered</option>
                                            </select>
                                        </td>
                                    </tr>
                                )
                            })
                        }
                    </tbody>
                </table>
                
            </div>
        </div>
      
    </div>
  )
}

export default Orders;

