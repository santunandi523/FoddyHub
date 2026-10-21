import { useEffect, useState } from "react";
import{toast} from "react-toastify";
import axios from "axios"
import './ListFood.css';
import { deleteFood, getFoodList } from "../../FoodService/foodServices";
const ListFood = () => {
  const [list,setList]=useState([]);
    const fetchList=async()=>{
      try {
        const data =await getFoodList();
        setList(Array.isArray(data) ? data : []);
      } catch (error) {
        setList([]);
        toast.error('Error occured while fetching foods')
      }
    }

    const removeFood=async(foodId)=>{
      try {
        const value=await deleteFood(foodId);
        if(value){
          await fetchList();
          toast.success('Successfully deleted food');
        }else{
          toast.error('Error occurred while deleting food');
        }
      } catch (error) {
        toast.error('Error occurred while deleting food');
      }
    }
    useEffect(()=>{
      fetchList();
    },[]);
  return (
    
    <div className="py-5 row justify-content-center">
      <div className="col-11 card">
        <table className="table">
          <thead>
            <tr>
              <th>Image</th>
              <th>Name</th>
              <th>Category</th>
              <th>Price</th>
              <th>Action</th>
            </tr>
          </thead>
          <tbody>
            {
              list.map((item,index)=>{
                return(
                  <tr key={index}>
                    <td>
                      <img src={item.imageUrl} alt="" height={48} width={48}/>
                    </td>
                    <td>{item.name}</td>
                    <td>{item.category}</td>
                    <td>&#8377;{item.price}</td>
                    <td className="text-danger">
                      <i className="bi bi-x-circle-fill" onClick={()=>removeFood(item.id)}></i>
                    </td>

                  </tr>
                )
                 
              })
            }
          </tbody>
        </table>
      </div>
      
    </div>
  )
}

export default ListFood;
