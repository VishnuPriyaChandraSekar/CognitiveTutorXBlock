package edu.tamu.ieclab.logic.problem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.json.JSONObject;

public class ALPWrapper {
      
	  private final String domain = "https://kona.education.tamu.edu:2401/ALP/";
      
      public double getLValue(String studentID, String skillname){
    	  double lValue = 0.0;
    	  String handler = "getLValue?student_id="+studentID+"&skillname="+skillname+"&callback=";
    	  String jsonP = readJSON(handler);
    	  
    	  /***
    	   * Extracting the data from JSONString
    	   */
    	  try {
    		String jsonString = jsonP.substring(jsonP.indexOf("(")+1,jsonP.lastIndexOf(")"));
			JSONObject json = new JSONObject(jsonString);
			lValue = json.getDouble("probability");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	 
    	  return lValue;
      }
      
      public String[]  getProblemSolved(String studentID, String tutorname){
    	  String[]  solvedProblem = null;
    	  System.out.println(" Student ID : "+studentID+" Tutorname : "+tutorname);
    	  String handler = "getProblemSoloved?student_id="+studentID+"&tutor_name="+tutorname+"&callback=";
    	  String jsonP = readJSON(handler);
    	  
    	  System.out.println(" jsonString : "+jsonP);
    	  /***
    	   * Extracting the data from JSONString
    	   */
    	  try {
      		String jsonString = jsonP.substring(jsonP.indexOf("(")+1,jsonP.lastIndexOf(")"));
			JSONObject json = new JSONObject(jsonString);
			String questionlist = json.getString("questionList");
			solvedProblem = questionlist.split("\\|");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	  
    	 
    	  return solvedProblem;
      }
      
     public String readJSON(String handler){
    	String jsonString = "";
        try {
        	  URL url = new URL(domain+handler);
      	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      	      conn.setRequestMethod("GET");
      	      conn.setRequestProperty("Accept", "application/json");

      	      if(conn.getResponseCode() == 200){
      	    	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
      	    	jsonString = br.readLine();
      				//System.out.println(jsonString);
      	      }
      	      else
      	    	  System.out.println(" Error : "+conn.getResponseCode());
      	    	conn.disconnect();
      		
      	      } catch (MalformedURLException e) {
      			// TODO Auto-generated catch block
      			e.printStackTrace();
      		} catch (IOException e) {
      			// TODO Auto-generated catch block
      			e.printStackTrace();
      		}
          	  
        
        return jsonString;
      }
}
