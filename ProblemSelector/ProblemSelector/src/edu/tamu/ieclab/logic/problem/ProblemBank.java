package edu.tamu.ieclab.logic.problem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import edu.tamu.ieclab.entity.*;

public class ProblemBank {
		
	   public HashSet<String> getSkillSet() {
		return skillSet;
	}

	public void setSkillSet(HashSet<String> skillSet) {
		this.skillSet = skillSet;
	}


	private HashSet<String> skillSet;
		
		public ProblemBank() {
			super();
			skillSet = new HashSet<String>();
		}
		
		public List<Problem> readProblemBankDummy(String folder,HashSet<String> solvedquestions){
			    List<Problem> problemList = new ArrayList<Problem>();
				BufferedReader br = null;
			try {
				FileReader file = new FileReader(folder+"/ProblemBank.txt");				
				br = new BufferedReader(file);
				String line = "";
				String[] details;
				String[] question;
				String[] skills;
				StringBuilder problemName;
				List<SAI> saiList = new ArrayList<SAI>();
				int problemNo = 1;
					while((line=br.readLine()) != null ){
					if(!solvedquestions.contains(""+problemNo)){
						details=line.split("::");
						question=details[0].split(",");
						skills = details[1].split(",");
						problemName = new StringBuilder("");
						
						/**
						 * abstract the problem name and sai from the beginning of the line
						 */
						for(int i=2; i<question.length; i=i+3){
							problemName.append("#" + question[i]);
							SAI sai = new SAI(question[i-2],question[i-1],question[i]);
							saiList.add(sai);
						}
						
						SAI indexSAI = new SAI("problemIndex","UpdateTextField",String.valueOf(problemNo));
						saiList.add(indexSAI);
						
					    for(int i=0; i<skills.length; i++){
					    	if(!skillSet.contains(skills[i]))
					    		skillSet.add(skills[i]);
					    }
						
					    /**
						 *  form Problem object 
						 */
						
						Problem problem = new Problem(problemName.toString(),new ArrayList<SAI>(saiList),skills,0,1.0);
						problemList.add(problem);
						saiList.clear();
					 }
					problemNo++;
												
					}

			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				if(br != null)
					try {
						br.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
			}
			return problemList;
		}
		
		
		
		public List<Problem> readProblemBank(String folder){
			List<Problem> problems = new ArrayList<Problem>();
			FileReader file ;
			BufferedReader br ;
			String line;
			try {
				file = new FileReader(folder+"/ProblemBank.txt");
				br = new BufferedReader(file);
				line = br.readLine();
				int problemNo = 0;
				System.out.println(" Reading the Problem Bank " );
				while(line != null && line.trim().length() >0){
					String[] problemLine = line.split("::");
					List<SAI> sai = new ArrayList<SAI>();
					List<String> problemSAI = Arrays.asList(problemLine[0].split(","));
					String[] skillList =problemLine[1].split(",");
					Problem problem = new Problem("",null,skillList,0,0.0);
					StringBuilder problemName = new StringBuilder("");
					
					for(int i=2; i<problemSAI.size(); i=i+3){
						sai.add( new SAI(problemSAI.get(i-2),problemSAI.get(i-1),problemSAI.get(i)));
						problemName.append(problemSAI.get(i)+" ");
					}
					
					line = br.readLine();
					problem.setProblemName(problemName.toString());
					sai.add(new SAI("problemIndex","UpdateTextField",String.valueOf(problemNo)));
					problem.setSai(sai);
					problems.add(problem);
					problemNo++;
				}
				
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			return problems;
			
		}
		
		public HashSet<String> getSkillName(List<Problem> problems){
			HashSet<String> skills = new HashSet<String>();
			
			for(int i=0; i<problems.size(); i++){
				String[] skillList = problems.get(i).getSkills();
				for(int j=0; j<skillList.length; j++){
					 if(!skills.contains(skillList[j]))
						 skills.add(skillList[j]);
				}
			}
			return skills;
		}
		
		
		public void getUnsolvedProblem(List<Problem> problemBank, String[] problemSolved){
			// if the student hasn't solved a single problem then go back
	        if(problemSolved == null)
	        	return;
	        else{
	        	int index = 0;
				for(int i=0; i<problemSolved.length; i++){
					if(problemSolved[i].trim().length() > 0){
						index = Integer.parseInt(problemSolved[i]);
						problemBank.get(index).setSolved(true);

					}
				}
	        }
			
		}
		
		
	
		
}
