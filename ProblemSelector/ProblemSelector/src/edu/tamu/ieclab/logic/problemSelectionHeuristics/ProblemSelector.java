package edu.tamu.ieclab.logic.problemSelectionHeuristics;

import edu.tamu.ieclab.logic.problem.ALPWrapper;
import edu.tamu.ieclab.logic.problem.ProblemBank;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.tamu.ieclab.entity.*;

public class ProblemSelector {
	
	/**
	 * The student reaches mastery when the L value of all the skills is greater than or equal to 0.9 
	 * @param problems
	 * @param problemBank
	 * @param alp
	 * @param studentID
	 * @return
	 */
	public boolean isMasteryReached(List<Problem> problems, ProblemBank problemBank, ALPWrapper alp, String studentID, String tutorname){
		HashSet<String> skills = problemBank.getSkillName(problems);
		Iterator<String> iterator = skills.iterator();
		
		while(iterator.hasNext()){
			if(alp.getLValue(studentID, iterator.next(),tutorname) < 0.9)
				return false;
		}
		return true;
	}
	
	 
	/**
	 *  if the student hasn't mastered (i.e the L values of any of the skill is less than 0.9) but he has completed
	 *  all the problems then wheel spinning is detected
	 * @return
	 */
	public boolean isWheelSpinning(String[] problemSolvedList, List<Problem> problemBank){
		if(problemSolvedList == null || problemSolvedList.length == 0)
			return false;
		 return problemSolvedList.length == problemBank.size();
	}
	
	
	public List<SAI> WheelSpinning(ALPWrapper alp,String studentID,String tutorName){
		System.out.println(" Wheel spinning is detected");
		
		/*
		 * To do : clear the table that stores the problems solved and tell the OpenEdx to provide dynamic link and ask student to review , clear problem solved history

		 */
		
		List<SAI> wheelSpinning = new ArrayList<SAI>();
		alp.clearProblemHistory(studentID, tutorName);
		alp.updateState(studentID, tutorName, "wheelSpinning");
		wheelSpinning.add(new SAI("wheelSpinning","detected","-1"));
		return wheelSpinning;
	}
	
	
	public List<Problem> fillLValue_LCount(List<Problem> problems, ProblemBank problemBank, ALPWrapper alp, String studentID, double threshold, String tutorName){
		HashSet<String> skills = problemBank.getSkillName(problems);
		HashMap<String,Double> skill_LValue = new HashMap<String,Double>();
		List<Problem> unsolved = new ArrayList<Problem>();
		Iterator<String> iterator = skills.iterator();

		while(iterator.hasNext()){
			String skill = iterator.next();
			skill_LValue.put(skill, alp.getLValue(studentID, skill, tutorName));
			System.out.println(" Skill: "+skill+" L-value: "+skill_LValue.get(skill));
		}
		
		System.out.println("\n Unsolved Problems ");
		System.out.println("---------------------");
		 for(int i=0; i<problems.size(); i++){
			    if(!problems.get(i).isSolved()){
			    	String[] skill = problems.get(i).getSkills();
		        	double LValue = 0.0;
		        	int LCount = 0;
		        	for(int j=0; j<skill.length; j++){
		        		double value = skill_LValue.get(skill[j]);
		        		LValue += value;
		        		if(value > threshold)
		        			LCount++;
		        	}
		        	problems.get(i).setLcount(LCount);
		        	problems.get(i).setLvalue(LValue);
		        	unsolved.add(problems.get(i));
		        	System.out.println(problems.get(i).getProblemName()+"   "+" Lvalue : "+LValue+"  LCount : "+LCount);
			    }
	        	
	        }
		 return unsolved;
	}
	
	public List<SAI> pickProblem(List<Problem> problems, String[] problemSolvedList, ProblemBank problemBank, ALPWrapper alp, String studentID, String criteria, double threshold,String tutorName){
		 Problem choosenProblem;
	     ProblemSelectorAlgorithm algorithm  = new ProblemSelectorAlgorithm();
		 problemBank.getUnsolvedProblem(problems, problemSolvedList);		                 //get the list of unsolved problems
		 List<Problem> unsolved = fillLValue_LCount(problems, problemBank,alp,studentID,threshold, tutorName); //find the Lvalue and Lcount for each unsolved problem
		 if(criteria.equalsIgnoreCase("mostL") || criteria.equalsIgnoreCase("leastL")) 	    // pick a problem based on a criteria
			 choosenProblem = algorithm.averageL(criteria, unsolved);
	    else
	    	 choosenProblem = algorithm.masteryL(criteria,unsolved);
		
		System.out.println(" Problem : "+choosenProblem.getProblemName());
		return choosenProblem.getSai();
	}
	
	public List<SAI> getProblem(String criteria, String studentID, String folder, String tutorName){
		/**
		 *  get the list of unsolved problemList
		 *  if the list is empty then check whether the student has reached mastery or not
		 *  		if the student hasn't reached mastery (wheel spinning)then clear the problem solved history and tell the OpenEdx to provide dynamic link
		 *  		if the student has reached the mastery then send a empty arrayList
		 *  if the list is non empty then compute the L-value for each problem and pick a problem based on the given criteria
		 */
		double threshold = 0.9;
		ALPWrapper alp = new ALPWrapper();
		ProblemBank problemBank = new ProblemBank();
		String[] problemSolvedList = alp.getProblemSolved(studentID, tutorName);
		List<Problem> problems = problemBank.readProblemBank(folder);
		boolean mastery = isMasteryReached(problems,problemBank,alp,studentID, tutorName);
		
		if(mastery){
			System.out.println(" The student mastered the skill " );
			alp.clearProblemHistory(studentID, tutorName);
			return new ArrayList<SAI>();
		}
		else if(isWheelSpinning(problemSolvedList, problems)){
			return WheelSpinning(alp, studentID, tutorName);
		}
		else{
			 System.out.println(" There are some unsolved problem ");
			 System.out.println("--------------------------------");
			 return pickProblem(problems, problemSolvedList, problemBank, alp, studentID, criteria, threshold, tutorName);
		}
		
	}
	
	
	
	public List<SAI> getProblemAfterMastery(String criteria, String studentID, String folder, String tutorName){
		ALPWrapper alp = new ALPWrapper();
		ProblemBank problemBank = new ProblemBank();
		String[] problemSolved = alp.getProblemSolved(studentID, tutorName);
		List<Problem> problems = problemBank.readProblemBank(folder);
		int index = 0;
		if(problemSolved == null){
			 Random rand = new Random();
			 index = rand.nextInt(problems.size());
		}
		else
			index = (Integer.parseInt(problemSolved[problemSolved.length-1]) + 1)  % problems.size();
		System.out.println(" Index : "+index);
		return problems.get(index).getSai();
		
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		System.out.println(" Most Mastery ");
		ProblemSelector ps = new ProblemSelector();
		List<SAI> problemSAI = ps.getProblem("mostMastery","5","/Users/simstudent/Documents/Watson/Vishnu/ACT32","ACT32");
		
		for(int i=0; i<problemSAI.size(); i++)
			System.out.println(problemSAI.get(i).getSelection()+"  "+problemSAI.get(i).getAction()+"  "+problemSAI.get(i).getInput());
		
		System.out.println();
		System.out.println("Most L");
		ProblemSelector ps1 = new ProblemSelector();
		List<SAI> problemSAI1 = ps1.getProblem("mostL","5","/Users/simstudent/Documents/Watson/Vishnu/ACT32","ACT32");
		
		for(int i=0; i<problemSAI1.size(); i++)
			System.out.println(problemSAI1.get(i).getSelection()+"  "+problemSAI1.get(i).getAction()+"  "+problemSAI1.get(i).getInput());
		
		
		
	}
	
	
	/*
	public List<SAI> getProblem(String criteria,String studentID,String
	 * folder,String tutorName){ ProblemSelectorAlgorithm algorithm = new
	 * ProblemSelectorAlgorithm(); HashMap<String,Double> Lvalues = new
	 * HashMap<String,Double>(); List<Problem> problemBank = new
	 * ArrayList<Problem>(); ALPWrapper alp = new ALPWrapper(); ProblemBank bank
	 * = new ProblemBank(); Problem choosen = null; double threshold = 0.9;
	 * 
	 * /** get the list of Problems solved from ALP
	 */

	/*
	 * System.out.println(" Getting solved problem list"); String[] problemList
	 * = alp.getProblemSolved(studentID,tutorName); HashSet<String>
	 * problemSolved = new HashSet<String>(); if(problemList != null)
	 * problemSolved = new HashSet<String>(Arrays.asList(problemList));
	 * 
	 * /** Read the problems from the problem bank. We exclude the solved
	 * problems from the problem bank
	 */

	/*
	 * System.out.println("Reading the problem from file"); problemBank =
	 * bank.readProblemBank(folder,problemSolved);
	 * 
	 * 
	 * if(problemBank.size() == 0) return new ArrayList<SAI>();
	 * 
	 * Iterator<String> iterator = bank.getSkillSet().iterator();
	 * while(iterator.hasNext()){ String skillname = iterator.next();
	 * Lvalues.put(skillname, alp.getLValue(studentID, skillname));
	 * System.out.println(Lvalues.get(skillname)+"      "+skillname); }
	 * 
	 * System.out.println(" Fill the LValue and Lcount"); /*** Fill Lvalue &
	 * LCount attribute for each Problem
	 */
	/*
	 * for(int i=0; i<problemBank.size(); i++){ String[] skills =
	 * problemBank.get(i).getSkills(); double LValue = 0.0; int LCount = 0;
	 * for(int j=0; j<skills.length; j++){ double value =
	 * Lvalues.get(skills[j]); LValue += value; if(value > threshold) LCount++;
	 * } problemBank.get(i).setLcount(LCount);
	 * problemBank.get(i).setLvalue(LValue);
	 * System.out.println(problemBank.get(i).getProblemName()+"   "+" Lvalue : "
	 * +LValue+"  LCount : "+LCount); }
	 * 
	 * 
	 * /** Based on the criteria, select a problem
	 */
	/*
	 * if(criteria.equalsIgnoreCase("mostL") ||
	 * criteria.equalsIgnoreCase("leastL")) choosen =
	 * algorithm.averageL(criteria, problemBank);
	 * 
	 * else choosen = algorithm.masteryL(criteria,problemBank);
	 * 
	 * System.out.println(" Problem : "+choosen+" SaI : "+choosen.getSai());
	 * return choosen.getSai();
	 * 
	 * }
	 * 
	 * public List<List<SAI>> getProblemList(String folderName){ String fileName
	 * = folderName + "/ProblemBank.txt"; List<List<SAI>> problemList = new
	 * ArrayList<List<SAI>>();
	 * 
	 * try { FileReader file = new FileReader(fileName); BufferedReader br = new
	 * BufferedReader(file); String line = br.readLine();
	 * 
	 * while(line != null && line.trim().length() > 0){ List<SAI> problemSAI =
	 * new ArrayList<SAI>(); String[] problem = line.split("::"); String[] sai =
	 * problem[0].split(","); for(int i=2; i<sai.length; i=i+3)
	 * problemSAI.add(new SAI(sai[i-2],sai[i-1],sai[i])); problemList.add(new
	 * ArrayList<SAI>(problemSAI)); line = br.readLine(); } br.close();
	 * 
	 * } catch (FileNotFoundException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } catch (IOException e) { // TODO Auto-generated
	 * catch block e.printStackTrace(); }
	 * 
	 * return problemList;
	 * 
	 * }
	 */

}
