package edu.tamu.ieclab.service;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.tamu.ieclab.entity.SAI;
import edu.tamu.ieclab.logic.problemSelectionHeuristics.*;

@Path("")
public class ProblemSelectorService {
	
    @Context ServletContext context;

	@GET
	@Path("/getProblem")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProblem(@Context UriInfo info){
		
		String parentFolder = this.context.getInitParameter("folder");
		String criteria = info.getQueryParameters().getFirst("criteria");
		String studentID = info.getQueryParameters().getFirst("studentID");
		String tutorname = info.getQueryParameters().getFirst("folder").split(",")[1];
		String folder = parentFolder+"/"+info.getQueryParameters().getFirst("folder").replace(",", "/");
		System.out.println(" Criteria : "+criteria+" Student ID : "+studentID+" Folder : "+folder);
		ProblemSelector ps = new ProblemSelector();
		List<SAI> problemSAI = ps.getProblem(criteria,studentID,folder,tutorname);
		return Response.status(200).entity(problemSAI).build();
	}
}
