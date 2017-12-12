package edu.tamu.ieclab.logic.problemSelectionHeuristics;

import edu.tamu.ieclab.logic.problem.ALPWrapper;
import edu.tamu.ieclab.logic.problem.ProblemBank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import edu.tamu.ieclab.entity.*;

public class ProblemSelector {

    private ProblemSelectorAlgorithm algorithm;
    private HashMap<String,Double> Lvalues;
    private List<Problem> problemBank;
    private ALPWrapper alp;
    private ProblemBank bank;
    
    public ProblemSelector(){
    	algorithm = new ProblemSelectorAlgorithm();
    	Lvalues = new HashMap<String,Double>();
    	problemBank = new ArrayList<Problem>();
    	alp = new ALPWrapper();
    	bank = new ProblemBank();
    }
    
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println(" Most Mastery ");
		ProblemSelector ps = new ProblemSelector();
		List<SAI> problemSAI = ps.getProblem("mostMastery","1a2b3c","Vishnu/APLUS","");
		
		for(int i=0; i<problemSAI.size(); i++)
			System.out.println(problemSAI.get(i).getSelection()+"  "+problemSAI.get(i).getAction()+"  "+problemSAI.get(i).getInput());
		
		System.out.println();
		System.out.println("Most L");
		ProblemSelector ps1 = new ProblemSelector();
		List<SAI> problemSAI1 = ps1.getProblem("mostL","1a2b3c","Vishnu/APLUS","");
		
		for(int i=0; i<problemSAI1.size(); i++)
			System.out.println(problemSAI1.get(i).getSelection()+"  "+problemSAI1.get(i).getAction()+"  "+problemSAI1.get(i).getInput());
		
		
		
	}
	
	
	public List<SAI> getProblem(String criteria,String studentID,String folder,String tutorName){
		
		Problem choosen = null;
		double threshold = 0.9;

		/**
		 *  get the list of Problems solved from ALP
		 */
		System.out.println(" Getting solved problem list");
		String[] problemList = this.alp.getProblemSolved(studentID,tutorName);
		HashSet<String> problemSolved = new HashSet<String>();
		if(problemList != null)
			problemSolved = new HashSet<String>(Arrays.asList(problemList));
		
		/** 
		 *  Read the problems from the problem bank. We exclude the solved problems from the problem bank
		 */
		
		System.out.println("Reading the problem from file");
		this.problemBank = this.bank.readProblemBank(folder,problemSolved);
		

		if(this.problemBank.size() == 0)
			 return new ArrayList<SAI>();
		
        Iterator<String> iterator = this.bank.getSkillSet().iterator();
        while(iterator.hasNext()){
        	String skillname = iterator.next();
        	this.Lvalues.put(skillname, this.alp.getLValue(studentID, skillname));
        	System.out.println(this.Lvalues.get(skillname)+"      "+skillname);
        }
        
        System.out.println(" Fill the LValue and Lcount");
        /***
         * Fill Lvalue & LCount attribute for each Problem  
         */
        for(int i=0; i<this.problemBank.size(); i++){
        	String[] skills = this.problemBank.get(i).getSkills();
        	double LValue = 0.0;
        	int LCount = 0;
        	for(int j=0; j<skills.length; j++){
        		double value = this.Lvalues.get(skills[j]);
        		LValue += value;
        		if(value > threshold)
        			LCount++;
        	}
        	this.problemBank.get(i).setLcount(LCount);
        	this.problemBank.get(i).setLvalue(LValue);
        	System.out.println(this.problemBank.get(i).getProblemName()+"   "+" Lvalue : "+LValue+"  LCount : "+LCount);
        }
        
        
        /**
         * Based on the criteria, select a problem 
         */
	    if(criteria.equalsIgnoreCase("mostL") || criteria.equalsIgnoreCase("leastL"))
	    	  choosen = this.algorithm.averageL(criteria, this.problemBank);
	    
	    else
	    	  choosen = this.algorithm.masteryL(criteria,this.problemBank);
		
		System.out.println(" Problem : "+choosen+" SaI : "+choosen.getSai());
		return choosen.getSai();
		
	}
	
}
