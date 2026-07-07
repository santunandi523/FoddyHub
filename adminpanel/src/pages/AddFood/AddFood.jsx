import { useState,useEffect } from "react";
import { assets } from "../../assets/assets";
import { addFood } from "../../FoodService/foodServices";
import { toast } from "react-toastify";


const AddFood = () => {
  const[image,setImage]=useState(false);

  const [data,setData]=useState({
    name:'',
    description:'',
    price:'',
    category:''
  });

  const onChangeHandler=(e)=>{
    const name=e.target.name;
    const value=e.target.value;
    setData(data=>({...data, [name]:value}));
  }

  useEffect(() => {
   console.log(data);
  },[data])
  
  const onSubmitHandler=async (e)=>{
    e.preventDefault();
    if(!image){
      toast.error("Please select image.");
      return;
    }
    try{
      
      await addFood(data,image);
        toast.success('Food added successfully');
        setData({name:'',
                description:'',
                price:'',
                category:''
              })
              
        setImage(false);                
    
    }
    catch(error){
      toast.error('Error at adding food');
    }
  }
  return (
    <div>
      <div className="m-1">
        <div className="row">
          <div className="card col-md-4">
            <div className="card-body">
              <h2 className="mb-4">Add Food</h2>
              <form onSubmit={onSubmitHandler}>
                <div className="mb-2">
                  <label htmlFor="image" className="form-label">
                    <img src={image?URL.createObjectURL(image): assets.upload} alt="" width={90} height={90}/>
                    
                  </label>
                  <input type="file" className="form-control" id="image" hidden onChange={(e)=>setImage(e.target.files[0])}/>
                </div>

                <div className="mb-2">
                  <label htmlFor="name" className="form-label">Enter Food Name</label>
                  <input type="text" className="form-control" id="name" placeholder="ex: Chicken Biriyani" required name="name" onChange={onChangeHandler} value={data.name}/>
                </div>

                <div className="mb-2">
                  <label htmlFor="category" className="form-label">Category</label>
                  <select name="category" id="category" className="form-control" onChange={onChangeHandler} value={data.category}>
                    <option value="select" hidden >--select--</option>
                    <option value="Biriyani">Biriyani</option>
                    <option value="Cake">Cake</option>
                    <option value="Burger">Burger</option>
                    <option value="Pizza">Pizza</option>
                    <option value="Rolls">Rolls</option>
                    <option value="Salads">Salads</option>
                    <option value="Icecreame">Icecreame</option>
                  </select>
                </div>

                <div className="mb-2">
                  <label htmlFor="price" className="form-label">Price</label>
                  <input type="number" className="form-control" id="price" placeholder="&#8377;200" required name="price" onChange={onChangeHandler} value={data.price}/>
                </div>
                
                

                <div className="mb-2">
                  <label htmlFor="description" className="form-label">Description</label>
                  <textarea className="form-control" id="description" rows="5" placeholder="Write content here..." required name="description" onChange={onChangeHandler} value={data.description}></textarea>
                </div>
                <button type="submit" className="btn btn-primary">Save</button>
              </form>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default AddFood;
