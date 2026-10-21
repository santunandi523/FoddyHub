import React, { useState } from 'react'
import './Register.css';
import { Link, useNavigate } from 'react-router-dom';
import { registerUser } from '../../Service/authService';
import axios from 'axios';
import {toast} from 'react-toastify';
const Register = () => {
  const [confirmPassword,setConfirmPassword]=useState("");
  const navigate=useNavigate();

  const[data,setData]=useState({
    name:'',
    email:'',
    password:''
  });  

  const onChangeHandler=(event)=>{
    const name=event.target.name;
    const value=event.target.value;
    setData(data=>({...data, [name]:value}));
  }

  const onSubmitHandler=async(event)=>{
    event.preventDefault();

    if(data.password!==confirmPassword){
        toast.error("Password doesn't match ,try again");
        setData({
        name:'',
        email:'',
        password:''
      });

      setConfirmPassword("");
      return;
    }

    try{
      const response=await registerUser(data);
      if(response.status===201){
        toast.success("Registered Successfully , Please Login");
        navigate("/login");
      }else{
        toast.error('Unable to register. Please try again ');
      }
    }catch(error){
      console.error("Register error:", error);
      const message = error?.response?.data?.message
          || error?.response?.data
          || error.message
          || "Unable to register. Please try again ";
      toast.error(typeof message === 'string' ? message : "Unable to register. Please try again.");
    }
    
    setData({
      name:'',
      email:'',
      password:''
    });

    setConfirmPassword("");
  }

  
  return (
    <div>
        <div className="register-container">
          <div className="container">
            <div className="col-lg-10 col-xl-9 mx-auto">
              <div className="card flex-row my-5 border-0 shadow rounded-3 overflow-hidden">
                <div className="card-img-left d-none d-md-flex">
                </div>
                <div className="card-body p-4 p-sm-5">
                  <h5 className="card-title text-center text-primary mb-5 fw-bold fs-3">Register</h5>
                  <form onSubmit={onSubmitHandler}>

                    <div className="form-floating mb-3">
                      <input type="text" className="form-control" id="floatingInputUsername" placeholder="myusername" required autoFocus name='name' onChange={onChangeHandler} value={data.name}/>
                      <label htmlFor="floatingInputUsername">Username</label>
                    </div>

                    <div className="form-floating mb-3">
                      <input type="email" className="form-control" id="floatingInputEmail" placeholder="name@example.com" required name='email' onChange={onChangeHandler} value={data.email} />
                      <label htmlFor="floatingInputEmail">Email address</label>
                    </div>

                    <div className="form-floating mb-3">
                      <input type="password" className="form-control" id="floatingPassword" placeholder="Password" required name='password' onChange={onChangeHandler} value={data.password}/>
                      <label htmlFor="floatingPassword">Password</label>
                    </div>

                    <div className="form-floating mb-3">
                      <input type="password" className="form-control" id="floatingPasswordConfirm" required placeholder="Confirm Password" value={confirmPassword} onChange={(e)=>setConfirmPassword(e.target.value)}/>
                      <label htmlFor="floatingPasswordConfirm">Confirm Password</label>
                    </div>

                    <div className="d-grid mb-2">
                      <button className="btn btn-lg btn-outline-primary btn-login fw-bold text-uppercase" type="submit" >Register</button>
                    </div>

                    <div className="mt-4 d-flex justify-content-center">
                      <span>Have an account ? </span>
                      <Link to="/login">
                        Sign In
                      </Link>
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

export default Register
