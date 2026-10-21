import React, { useState } from 'react'
import Header from '../../Menubar/Header/Header'
import ExploreMenu from '../../Menubar/ExploreMenu/ExploreMenu'
import FoodDisplay from '../../FoodDisplay/FoodDisplay'

const Homepage = () => {
  const[category,setCategory]=useState('All');
  return (
    <div>
      <Header/>
      <ExploreMenu category={category} setCategory={setCategory}/>
      <FoodDisplay category={category} searchText={""}/>
    </div>
  )
}

export default Homepage
