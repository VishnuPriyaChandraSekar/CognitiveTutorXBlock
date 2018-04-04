package edu.tamu.ieclab.logic.problem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.mysql.jdbc.Connection;
import com.mysql.jdbc.PreparedStatement;
import com.mysql.jdbc.Statement;
import org.json.JSONException;
import org.json.JSONObject;

public class ALPWrapper {
      
	  private static String domain;
	  
	  public static void setDomain(String _domain){
		   if(ALPWrapper.domain == null)
			   ALPWrapper.domain = _domain;
	  }
	  
      public double getLValue(String studentID, String skillname, String tutorname){
    	  double lValue = 0.0;
    	  skillname = skillname +"_"+tutorname;
    	  String handler = "getLValue?student_id="+studentID+"&skillname="+skillname+"&callback=";
    	  String jsonP = readJSON(handler);
    	  /***
    	   * Extracting the data from JSONString
    	   */
    	  if(jsonP != null && jsonP.length() > 0){
    		  try {
    	    		String jsonString = jsonP.substring(jsonP.indexOf("(")+1,jsonP.lastIndexOf(")"));
    				JSONObject json = new JSONObject(jsonString);
    				lValue = json.getDouble("probability");
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
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
    	  if(jsonP != null && jsonP.length() > 0){
    		  try {
    	      		String jsonString = jsonP.substring(jsonP.indexOf("(")+1,jsonP.lastIndexOf(")"));
    				JSONObject json = new JSONObject(jsonString);
    				String questionlist = json.has("questionList") ? json.getString("questionList") : "";
    				if(questionlist.trim().length() > 0)
    				solvedProblem = questionlist.split("\\|");
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    	  }
    	  
    	  
    	 
    	  return solvedProblem;
      }
      
     public void clearProblemHistory(String studentID, String tutorname){
    	 //String handler = "";
    	// delete(handler); 
    	 String handler = "clearProblemSolvedHistory?studentID="+studentID+"&tutorName="+tutorname+"&callback=";
    	 String jsonP = readJSON(handler);

   	  System.out.println(" jsonString : "+jsonP);
   	  /***
   	   * Extracting the data from JSONString
   	   */
   	  if(jsonP != null && jsonP.length() > 0){
   		  try {
   	      		String jsonString = jsonP.substring(jsonP.indexOf("(")+1,jsonP.lastIndexOf(")")).replace("\"", "");
   				JSONObject jsonStatus = new JSONObject(jsonString);
   				String status = jsonStatus.has("status") ? String.valueOf(jsonStatus.getInt("status"))  : "500";
   	      		if(!status.equals("200"))
   					throw new Exception("Failure while clearing the database");
   			} catch (JSONException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   	    }
   	  
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
    
     public void updateState(String studentId, String tutorName, String status){
   	  	 String handler = "updateStatus?studentID="+studentId+"&tutorname="+tutorName+"&status="+status+"&callback=";
   	  	 String jsonP = readJSON(handler);   	     
   	     System.out.println(" jsonString : "+jsonP);
   	     /***
   	      * Extracting the data from JSONString
   	      */
   	   if(jsonP != null && jsonP.length() > 0){
   		  try {
   			    String jsonString = jsonP.substring(jsonP.indexOf("(")+1,jsonP.lastIndexOf(")")).replace("\"", "");
				JSONObject jsonStatus = new JSONObject(jsonString);
				String statusCode = jsonStatus.has("status") ? String.valueOf(jsonStatus.getInt("status"))  : "500";
	      		if(!statusCode.equals("200"))
   	      			throw new Exception("Error while updating the status");
   			} catch (JSONException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
   	    }
   	  

     }
     
     public String getPreviousState(String studentId, String tutorName) {
   	  	 String handler = "checkPreviousStatus?studentID="+studentId+"&tutor_name="+tutorName+"&callback=";
   	  	 String jsonP = readJSON(handler);
   	     String previousStatus = "";
   	     
   	     System.out.println(" jsonString : "+jsonP);
   	     /***
   	      * Extracting the data from JSONString
   	      */
   	   if(jsonP != null && jsonP.length() > 0){
   		  try {
   			String jsonString = jsonP.substring(jsonP.indexOf("(")+1,jsonP.lastIndexOf(")")).replace("\"", "");
			JSONObject jsonStatus = new JSONObject(jsonString);
			previousStatus = jsonStatus.has("state") ? jsonStatus.getString("state")  : "";   	      		
   			} catch (JSONException e) {
   				// TODO Auto-generated catch block
   				e.printStackTrace();
   			}
   	    }
   	  
        return previousStatus;
     }

}
