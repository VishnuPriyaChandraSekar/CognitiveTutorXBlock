package edu.tamu.ieclab.service;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import edu.tamu.ieclab.entity.User;

@Path("/user")
public class UserRest {
   User user = null;
   
   
   @GET
   @Path("/hello")
   @Produces(MediaType.APPLICATION_JSON)
   public Response hello(){ 
	   return Response.ok().build();
   }
   
	
   @GET
   @Path("/getuser")
   @Produces(MediaType.APPLICATION_JSON)
	public Response getUser(){
	   System.out.println(" Current user : "+user);
		if(user != null){
			String result = user.toString();
			return Response.status(201).entity(result).build();
		}
		
		return Response.status(404).entity("Not found").build();
	}
}
