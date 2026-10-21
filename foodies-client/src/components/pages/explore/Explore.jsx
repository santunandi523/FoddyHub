import React, { useState } from 'react'
import FoodDisplay from '../../FoodDisplay/FoodDisplay'

const Explore = () => {
  const[category,setCategory]=useState('All');
  const [searchText,setSearchText]=useState("");
  return (
    <div className='container'>
      <div className="row justify-content-center">
        <div className="col-md-6">
          <form onSubmit={(e)=>e.preventDefault()} className="bg-white p-2 rounded-pill shadow-sm border mt-4 mb-5">
            <div className="input-group align-items-center">
              <select className="form-select border-0 bg-transparent fw-bold text-brand shadow-none" style={{"maxWidth":"160px"}} onChange={(e)=>setCategory(e.target.value)}>
                <option value="All">All</option>
                <option value="Biriyani">Biriyani</option>
                <option value="Burger">Burger</option>
                <option value="Cake">Cake</option>
                <option value="Icecreame">Icecreame</option>
                <option value="Pizza">Pizza</option>
                <option value="Rolls">Rolls</option>
                <option value="Salads">Salads</option>
              </select>
              <input type="text" className="form-control border-0 shadow-none px-3" placeholder="Search your favourite dish..." onChange={
                (e)=>setSearchText(e.target.value)} value={searchText}/>
              <button className="btn bg-brand text-white rounded-circle ms-2" type="submit" style={{width: "46px", height: "46px"}}>
                <i className='bi bi-search'></i>
              </button>
            </div>
          </form>
        </div>
      </div>
      <FoodDisplay category={category} searchText={searchText}/>
    </div>
  )
}

export default Explore

