package edu.tamu.ieclab.service;

import java.util.List;

import javax.servlet.ServletContext;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import edu.tamu.ieclab.entity.Problem;
import edu.tamu.ieclab.entity.SAI;
import edu.tamu.ieclab.logic.problem.ALPWrapper;
import edu.tamu.ieclab.logic.problem.ProblemBank;
import edu.tamu.ieclab.logic.problemSelectionHeuristics.*;

@Path("")
public class ProblemSelectorService {
	
    @Context ServletContext context;

	@GET
	@Path("/getProblem")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getProblem(@Context UriInfo info){
		
		String parentFolder = this.context.getInitParameter("folder");
		ALPWrapper.setDomain(this.context.getInitParameter("ALP"));
		String criteria = info.getQueryParameters().getFirst("criteria");
		String studentID = info.getQueryParameters().getFirst("studentID");
		String tutorname = info.getQueryParameters().getFirst("folder").split(",")[1];
		String folder = parentFolder+"/"+info.getQueryParameters().getFirst("folder").replace(",", "/");
		String mastery = info.getQueryParameters().getFirst("mastery");
		System.out.println(" Criteria : "+criteria+" Student ID : "+studentID+" Folder : "+folder);
		ProblemSelector ps = new ProblemSelector();
		List<SAI> problemSAI = null;
		if(mastery.equals("true"))
			    problemSAI = ps.getProblemAfterMastery(criteria, studentID, folder, tutorname);
		else
				problemSAI = ps.getProblem(criteria,studentID,folder,tutorname);
		return Response.status(200).entity(problemSAI).build();
	}
	
	@GET
	@Path("/checkPreviousState")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPreviousState(@Context UriInfo info){
		ALPWrapper alp = new ALPWrapper();
		ALPWrapper.setDomain(this.context.getInitParameter("ALP"));
		String studentID = info.getQueryParameters().getFirst("studentID");
		String tutorName = info.getQueryParameters().getFirst("tutor_name");
		String previousState = alp.getPreviousState(studentID, tutorName);
		System.out.println("Previous State : "+previousState);
		return Response.status(200).entity(previousState).build();
	}
	
	@GET
	@Path("/updateStatus")
	@Consumes(MediaType.TEXT_PLAIN)
	public Response updateStatus(@Context UriInfo info){
		ALPWrapper alp = new ALPWrapper();
		ALPWrapper.setDomain(this.context.getInitParameter("ALP"));
		String studentID = info.getQueryParameters().getFirst("studentID");
		String tutorName = info.getQueryParameters().getFirst("tutorname");
		String status = info.getQueryParameters().getFirst("status");
		alp.updateState(studentID, tutorName, status);
		return Response.ok().entity("success").build();
	}
	
	@GET
	@Path("/getStatus")
	@Produces(MediaType.TEXT_PLAIN)
	public Response getStatus(@Context UriInfo info){
		String status = "";
		ALPWrapper alp = new ALPWrapper();
		ALPWrapper.setDomain(this.context.getInitParameter("ALP"));
		ProblemBank problemBank = new ProblemBank();
		ProblemSelector ps = new ProblemSelector();
		String studentID = info.getQueryParameters().getFirst("studentID");
		String tutorname = info.getQueryParameters().getFirst("folder").split(",")[1];
		String parentFolder = this.context.getInitParameter("folder");
		String folder = parentFolder+"/"+info.getQueryParameters().getFirst("folder").replace(",", "/");
		
		String[] problemSolvedList = alp.getProblemSolved(studentID, tutorname);
		List<Problem> problems = problemBank.readProblemBank(folder);
		boolean mastery = ps.isMasteryReached(problems, problemBank, alp, studentID, tutorname);
		
		if(mastery){
			status = "mastery";
			alp.updateState(studentID, tutorname, "mastery");
			alp.clearProblemHistory(studentID, tutorname);
		}
		else{
			boolean wheelSpinning = ps.isWheelSpinning(problemSolvedList, problems);
			status = (wheelSpinning == true) ? "wheelSpinning" : "practice";
			if(status.equals("wheelSpinning")){
				alp.clearProblemHistory(studentID, tutorname);
				alp.updateState(studentID, tutorname, "wheelSpinning");
			}

		}
		System.out.println(" Status : "+status);
		return Response.ok().entity(status).build();
	}
	
}
