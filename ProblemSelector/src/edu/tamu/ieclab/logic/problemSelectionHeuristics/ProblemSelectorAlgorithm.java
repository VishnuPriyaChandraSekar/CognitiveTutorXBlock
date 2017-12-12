package edu.tamu.ieclab.logic.problemSelectionHeuristics;


import java.util.ArrayList;
import java.util.List;


import edu.tamu.ieclab.entity.Problem;

public class ProblemSelectorAlgorithm {
	
	
	public Problem averageL(String criteria, List<Problem> problemBank){
		double min = Double.MAX_VALUE;
		double max = 0.0;
		List<Problem> minEqual = new ArrayList<Problem>();
	    List<Problem> maxEqual = new ArrayList<Problem>();
	    
		for(int i=0; i<problemBank.size(); i++){
			 double average = problemBank.get(i).getLvalue() / problemBank.get(i).getSkills().length;
			System.out.println(" average : "+average+" max: "+max+"  min : "+min);
			 if(max <= average ){
				 if(maxEqual.size() > 0 && max < average)
					 maxEqual.clear();
				 max = average;
				 maxEqual.add(problemBank.get(i));
				 System.out.println(maxEqual.get(maxEqual.size()-1));
			 }
			 if(min >= average){
				 if(minEqual.size() > 0 && min > average)
					 minEqual.clear();
				 min = average;
				 minEqual.add(problemBank.get(i));
			 }
		}
		
		System.out.println(" The mostL average");
		for(int i=0; i< maxEqual.size(); i++)
			System.out.println(maxEqual.get(i).getProblemName()+"   "+max);
		System.out.println();

		System.out.println(" The leastL average");
		for(int i=0; i< minEqual.size(); i++)
			System.out.println(minEqual.get(i).getProblemName()+"   "+min);
		System.out.println();

        
		
		if(criteria.equalsIgnoreCase("mostL"))
			return maxEqual.size() > 1 ? maxEqual.get(getRandomNumber(maxEqual.size())) : maxEqual.get(0);
		else
			return minEqual.size() > 1 ? minEqual.get(getRandomNumber(minEqual.size())) : minEqual.get(0);
	}
	
	public Problem masteryL(String criteria, List<Problem> problemBank){
		int minCount = Integer.MAX_VALUE, maxCount = Integer.MIN_VALUE;
		List<Problem> minLProblem = new ArrayList<Problem>();
		List<Problem> maxLProblem = new ArrayList<Problem>();
		
		for(int i=0; i<problemBank.size(); i++){
			int Lcount = problemBank.get(i).getLcount();
			if(maxCount <= Lcount){
				if(maxLProblem.size() > 0 && maxCount < Lcount)
					maxLProblem.clear();
				maxCount = Lcount;
				maxLProblem.add(problemBank.get(i));
			}
			if(minCount >= Lcount) {
				if(minLProblem.size() > 0 && minCount > Lcount)
					minLProblem.clear();
				minCount = Lcount;
				minLProblem.add(problemBank.get(i));
			}
		}
		
		System.out.println(" The most Mastery ");
		for(int i=0; i< maxLProblem.size(); i++)
			System.out.println(maxLProblem.get(i).getProblemName()+"   "+maxLProblem.get(i).getLcount());
		System.out.println();

		System.out.println(" The least Mastery ");
		for(int i=0; i< minLProblem.size(); i++)
			System.out.println(minLProblem.get(i).getProblemName()+"   "+minLProblem.get(i).getLcount());
		System.out.println();

		
		if(criteria.equalsIgnoreCase("mostMastery"))
			return maxLProblem.size() > 1 ? maxLProblem.get(getRandomNumber(maxLProblem.size())) : maxLProblem.get(0);
		else
			return minLProblem.size() > 1 ? minLProblem.get(getRandomNumber(minLProblem.size())) : minLProblem.get(0);
				
	}
     
	
	public int getRandomNumber(int length){
		int rand = (int) (Math.random() * length) ;
		return rand;
	}
}
