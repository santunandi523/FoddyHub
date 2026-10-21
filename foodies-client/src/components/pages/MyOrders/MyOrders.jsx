import React, { useContext, useEffect, useState } from 'react'
import { StoreContext } from '../../context/StoreContext'
import axios from 'axios';
import { assets } from '../../../assets/assets';
import './MyOrders.css';

const MyOrders = () => {
    const { token } = useContext(StoreContext);
    const [data, setData] = useState([]);
    const [loading, setLoading] = useState(false);

    const fetchOrders = async () => {
       
        setLoading(true);
        try {
            const response = await axios.get(
                `${import.meta.env.VITE_API_URL}/api/orders`,
                {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                }
            );

            setData(response.data);

        } catch (error) {
            console.log("REQUEST FAILED");
            console.log("ERROR:", error.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (token) {
            fetchOrders();
        }
    }, [token]);


  return (
    <div className='container myorders-section'>
        <div className="py-5 row justify-content-center">
            <div className="col-11 card p-3">
                <h2 className="myorders-title mb-4">My Orders</h2>

                {!token ? (
                    <div className="text-center py-5 text-muted">
                        <i className="bi bi-lock fs-1"></i>
                        <p className="mt-2">Please log in to view your orders.</p>
                    </div>
                ) : loading ? (
                    <div className="text-center py-5">
                        <div className="spinner-border text-warning" role="status">
                            <span className="visually-hidden">Loading...</span>
                        </div>
                        <p className="mt-2 text-muted">Fetching your orders...</p>
                    </div>
                ) : data.length === 0 ? (
                    <div className="text-center py-5 text-muted">
                        <i className="bi bi-bag-x fs-1"></i>
                        <p className="mt-2">No orders found.</p>
                    </div>
                ) : (
                <div className="orders-list">
                        {
                            data.map((order,index)=>{
                                return(
                                    <div key={index} className="order-card">
                                        <div className="order-card-icon">
                                            📦
                                        </div>
                                        <div className="order-card-body">
                                            <div className="order-card-items">
                                                {
                                                    (order.orderedItems || []).map((item,i)=>{
                                                        const items = order.orderedItems || [];
                                                        return item.name + " x"+item.quantity + (i===items.length-1 ? "" : ", ");
                                                    })
                                                }
                                                {(!order.orderedItems || order.orderedItems.length === 0) && (
                                                    <span className="text-muted fst-italic">No item details</span>
                                                )}
                                            </div>
                                            <div className="order-card-meta">
                                                Items: {order.orderedItems ? order.orderedItems.length : '—'}
                                            </div>
                                        </div>
                                        <div className="order-card-amount">
                                            &#x20B9;{order.amount.toFixed(2)}
                                        </div>
                                        <div>
                                            <span className={`order-status-badge ${order.orderStatus === 'Food Preparing' ? 'status-preparing' : order.orderStatus === 'Out for delivery' ? 'status-delivery' : order.orderStatus === 'Delivered' ? 'status-delivered' : 'status-default'}`}>
                                                &#x25cf; {order.orderStatus}
                                            </span>
                                        </div>
                                        <button className='btn-refresh ms-3' onClick={fetchOrders} title="Refresh Status">
                                            <i className='bi bi-arrow-clockwise'></i>
                                        </button>
                                    </div>
                                )
                            })
                        }
                    </div>
                )}
            </div>
        </div>
      
    </div>
  )
}

export default MyOrders
