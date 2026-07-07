import React from 'react'
import { Link } from 'react-router-dom'

const Header = () => {
  return (
   <main className='container'>
     <div className='p-5 mb-4 bg-light rounded-3 mt-1'>
        <div className="container-fluid py-5">
            <h1 className='display-5 fw-bold'>Order your favourite food here</h1>
            <p className='col-md-8 fs-4'>Discover the best foods and drinks in West Bengal</p>
            <Link to="/explore" className='btn btn-primary btn-log'>Explore</Link>
        </div>
      
    </div>

   </main>
  )
}

export default Header
