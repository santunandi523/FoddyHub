import { useState } from 'react'
import './App.css'
import Menubar from './components/Menubar/Menubar'
import Contact from './components/pages/contactus/Contact';
import Explore from './components/pages/explore/Explore';
import Homepage from './components/pages/homepage/Homepage';
import { Route, Routes } from 'react-router-dom';
import FoodDetails from './components/pages/FoodDetails/FoodDetails';
import Cart from './components/pages/Cart/Cart';
import PlaceOrder from './components/pages/PlaceOrder/PlaceOrder';
import Login from './components/Login/Login';
import Register from './components/Register/Register';
import { ToastContainer } from 'react-toastify';
import MyOrders from './components/pages/MyOrders/MyOrders';
import AiChatWidget from './components/AiAssistant/AiChatWidget';

function App() {

  return (
    
   <div>
    <Menubar/>
    
    <ToastContainer/>
    
    <Routes>
      <Route path='/' element={ <Homepage/>}/>
      <Route path='contact' element={<Contact/>}/>
      <Route path='/explore' element={<Explore/>} />
      <Route path='/food/:id' element={<FoodDetails/>} />
      <Route path='/cart' element={<Cart/>}/>
      <Route path='/orders' element={<PlaceOrder/>}/>
      <Route path='/myorders' element={<MyOrders/>}/>
      <Route path='/login' element={<Login/>}/>
      <Route path='/register' element={<Register/>}/>
      <Route path='/assistant' element={<AiChatWidget isFullPage={true}/>}/>
    </Routes>

    {/* Floating AI Assistant available on all pages */}
    <AiChatWidget />

   </div>
  )
}

export default App;
