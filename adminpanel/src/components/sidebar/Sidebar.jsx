import { Link } from "react-router-dom";
import { assets } from "../../assets/assets";

const Sidebar = ({sidebarVisible}) => {
  return (
    <div>
      <div className={`border-end bg-white ${sidebarVisible?'':'d-none'} `} id="sidebar-wrapper" >
          <div className="sidebar-heading border-bottom bg-light"><img src={assets.foodies} alt="Foodies" height={40} className="p-10" /></div>
          <div className="list-group list-group-flush">
              <Link className="list-group-item list-group-item-action list-group-item-light p-3" to="/add"> <i className="bi bi-plus-circle m-1"></i> Add Food</Link>
              <Link className="list-group-item list-group-item-action list-group-item-light p-3" to="/list"><i className="bi bi-list-task m-1"></i> List Food</Link>
              <Link className="list-group-item list-group-item-action list-group-item-light p-3" to="/orders"> <i className="bi bi-cart m-1"></i>Orders</Link>
             
        </div>
    </div>
    </div>
  )
}

export default Sidebar;
