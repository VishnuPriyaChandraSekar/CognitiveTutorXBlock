package edu.tamu.ieclab.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class ProblemSelectorClient {
	
	
	public void getProblem(){
		 try {
       	  URL url = new URL("http://localhost:8080/ProblemSelector/getProblem?criteria=MostL&studentID=1a2b3c&folder=VishnuAPLUS");
     	      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
     	      conn.setRequestMethod("GET");
     	      conn.setRequestProperty("Accept", "application/json");

     	      if(conn.getResponseCode() == 200){
     	    	BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
     	    	String jsonString = br.readLine();
     	    	System.out.println(" JSON : "+jsonString);
     	    	JSONArray s = new JSONArray(jsonString);
     	    	
     	    	for(int i=0; i<s.length(); i++){
     	    		JSONObject json = (JSONObject) s.get(i);
     	    		System.out.println(json.get("selection"));
     	         	System.out.println(json.getString("input"));
     	    		System.out.println(json.getString("action"));

     	    	}
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

	}
	public static void main(String[] args) {
		// TODO Auto-generated method stub
       ProblemSelectorClient ps = new ProblemSelectorClient();
       ps.getProblem();
	}

}
