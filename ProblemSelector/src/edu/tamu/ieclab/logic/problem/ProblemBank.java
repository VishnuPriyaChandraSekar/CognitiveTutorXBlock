package edu.tamu.ieclab.logic.problem;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
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
		
		public List<Problem> readProblemBank(String folder,HashSet<String> solvedquestions){
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
						//System.out.println(" question : "+details.length);
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
		
		
		public void printHashTables(){
			
			System.out.println(" Problem Bank ");
			
			
		}
		
		
		/*public void fillLValues(HashMap<String,Double> LValues){
			 for(Map.Entry<String, List<Skill>> entry : problemSkillTable.entrySet()){
				 for(int i=0; i<entry.getValue().size(); i++)
					 entry.getValue().get(i).setLValue(LValues.get(entry.getValue().get(i).getSkillName()));
			 }
		}*/
		
}
