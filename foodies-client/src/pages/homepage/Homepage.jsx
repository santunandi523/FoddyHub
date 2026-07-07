import React, { useState } from 'react'
import Header from '../../components/Menubar/Header/Header'
import ExploreMenu from '../../components/Menubar/ExploreMenu/ExploreMenu'
import FoodDisplay from '../../components/FoodDisplay/FoodDisplay'

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
