import React, { useEffect, useState } from 'react'

const Contact = () => {
  const[firstName,setFirstName]=useState("");
  const [lastName,setLastName]=useState("");
  const[email,setEmail]=useState("");
  const[message,setMessage]=useState("");
  const [contactData,setContactData]=useState({});
  useEffect(() => {
   console.log(contactData);
  }, [contactData])
  
  return (
    <div>
      <section className="py-5">
        <div className="container">
            <div className="row justify-content-center">
                <div className="col-lg-8">
                    <div className="contact-form p-5 shadow-sm bg-white">
                        <h2 className="text-center mb-4">Contact Us</h2>
                        <form onSubmit={(e)=>e.preventDefault()}>
                            <div className="row g-3">
                                <div className="col-md-6">
                                    <input type="text" className="form-control custom-input" placeholder="First Name" onChange={(e)=>setFirstName(e.target.value)}/>
                                </div>
                                <div className="col-md-6">
                                    <input type="text" className="form-control custom-input" placeholder="Last Name" onChange={(e)=>setLastName(e.target.value)}/>
                                </div>
                                <div className="col-12">
                                    <input type="email" className="form-control custom-input" placeholder="Email Address" onChange={(e)=>setEmail(e.target.value)}/>
                                </div>
                                <div className="col-12">
                                    <textarea className="form-control custom-input" rows="5" placeholder="Your Message" onChange={(e)=>setMessage(e.target.value)}></textarea>
                                </div>
                                <div className="col-12">
                                    <button className="btn btn-primary w-100 py-3" type="submit" onClick={()=>setContactData({firstName:firstName,lastName:lastName,email:email,message:message})}>Send Message</button>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </section>
    </div>
  )
}

export default Contact
