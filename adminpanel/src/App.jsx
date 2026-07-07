import { Route, Routes } from "react-router-dom";
import AddFood from "./pages/AddFood/AddFood";
import ListFood from "./pages/ListFood/ListFood";
import Orders from "./pages/Orders/Orders";
import Sidebar from "./components/sidebar/Sidebar";
import Menubar from "./components/menubar/Menubar";
import { useState } from "react";
import { ToastContainer } from 'react-toastify';


const App = () => {
    const[sidebarVisible,setsidebarVisible]=useState(true);
    function toggleSidebar(){
        setsidebarVisible(!sidebarVisible);
    }
  return (
    <div>
      <div className="d-flex" id="wrapper">
        <Sidebar sidebarVisible={sidebarVisible}/>


        <div id="page-content-wrapper">
          <Menubar toggleSidebar={toggleSidebar}/>

          <ToastContainer/>

          <div className="container-fluid">
            <Routes>
              <Route path="/add" element={<AddFood />} />
              <Route path="/list" element={<ListFood />} />
              <Route path="/orders" element={<Orders />} />
              <Route path="/" element={<ListFood />} />
            </Routes>
          </div>
        </div>
      </div>
    </div>
  );
};

export default App;