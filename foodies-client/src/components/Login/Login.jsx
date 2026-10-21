import React, { useContext, useState } from 'react'
import "./Login.css";
import { Link, useNavigate } from 'react-router-dom';
import { loginUser } from '../../Service/authService';
import { toast } from 'react-toastify';
import { StoreContext } from '../context/StoreContext';

const Login = () => {
    const {setToken, loadCartData}=useContext(StoreContext);
    const navigate=useNavigate();
    const[data,setData]=useState({
        email:'',
        password:''
    });

    const onChangeHandler=(event)=>{
        const name=event.target.name;
        const value=event.target.value;
        setData(data=>({...data,[name]:value}))
    }

    const onSubmitHandler=async(e)=>{
        e.preventDefault();
        try{
            const response = await loginUser(data);
            if(response.status===200){
                const newToken = response.data.token;
                setToken(newToken);
                localStorage.setItem('token', newToken);
                await loadCartData(newToken);
                navigate('/');
                toast.success("Login successfully");
            }else{
                toast.error("Unable to login , please try again")
            }
        }catch(error){
            console.error("Login error:", error);
            const message = error?.response?.data?.message
                || error?.response?.data
                || "Unable to login. Please check your credentials and try again.";
            toast.error(typeof message === 'string' ? message : "Unable to login. Please try again.");
        }

    }

  return (
    <div >
        <div className="login-container">
            <div className="container">
            <div className="col-sm-9 col-md-7 col-lg-5 mx-auto">
                <div className="card border-0 shadow rounded-3 my-5">
                <div className="card-body p-4 p-sm-5">
                    <h5 className="card-title text-center text-primary mb-5 fw-bold fs-3">Sign In</h5>
                    <form onSubmit={onSubmitHandler}>
                    <div className="form-floating mb-3">
                        <input type="email" className="form-control" id="floatingInput" placeholder="name@example.com" name="email" value={data.email} onChange={onChangeHandler}/>
                        <label htmlFor="floatingInput">Email address</label>
                    </div>
                    <div className="form-floating mb-3">
                        <input type="password" className="form-control" id="floatingPassword" placeholder="Password" name="password" value={data.password} onChange={onChangeHandler}/>
                        <label htmlFor="floatingPassword">Password</label>
                    </div>

                    <div className="form-check mb-3">
                        <input className="form-check-input" type="checkbox" value="" id="rememberPasswordCheck"/>
                        <label className="form-check-label" htmlFor="rememberPasswordCheck">
                        Remember password
                        </label>
                    </div>
                    <div className="d-grid">
                        <button className="btn btn-outline-primary btn-lg btn-login text-uppercase fw-bold" type="submit">Sign
                        in</button>
                    </div>
                    <div className="mt-4">
                        Don't have an account ? <Link to="/register">Sign Up</Link>
                    </div>

                    </form>
                </div>
                </div>
            </div>
            </div>
        </div>
    </div>
  )
}

export default Login
