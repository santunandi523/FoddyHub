import { useState } from 'react'
import './App.css'
import Menubar from './components/Menubar/Menubar'
import Contact from './pages/contactus/Contact';
import Explore from './pages/explore/Explore';
import Homepage from './pages/homepage/Homepage';
import { Route, Routes } from 'react-router-dom';
import FoodDetails from './pages/FoodDetails/FoodDetails';
import Cart from './pages/Cart/Cart';
import PlaceOrder from './pages/PlaceOrder/PlaceOrder';
import Login from './components/Login/Login';
import Register from './components/Register/Register';


function App() {

  return (
    
   <div>
    <Menubar/>
    
    
    <Routes>
      <Route path='/' element={ <Homepage/>}/>
      <Route path='contact' element={<Contact/>}/>
      <Route path='/explore' element={<Explore/>} />
      <Route path='/food/:id' element={<FoodDetails/>} />
      <Route path='/cart' element={<Cart/>}/>
      <Route path='/orders' element={<PlaceOrder/>}/>
      <Route path='/login' element={<Login/>}/>
      <Route path='/register' element={<Register/>}/>
    </Routes>

   </div>
  )
}

export default App;
