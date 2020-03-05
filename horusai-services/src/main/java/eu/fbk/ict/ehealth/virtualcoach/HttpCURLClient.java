package eu.fbk.ict.ehealth.virtualcoach;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;


public class HttpCURLClient {

  private static Logger logger = LoggerFactory.getLogger(HttpCURLClient.class);
  private static final String USER_AGENT = "Mozilla/5.0";
  
  private HttpCURLClient() {}
  
  
  public static String get(String url, HashMap<String, String> header, String data) throws Exception {
    
    /*
    String url = "http://www.google.com/search?q=mkyong";

    URL obj = new URL(url);
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    // optional default is GET
    con.setRequestMethod("GET");

    //add request header
    con.setRequestProperty("User-Agent", USER_AGENT);

    int responseCode = con.getResponseCode();
    System.out.println("\nSending 'GET' request to URL : " + url);
    System.out.println("Response Code : " + responseCode);

    BufferedReader in = new BufferedReader(
            new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
        response.append(inputLine);
    }
    in.close();

    //print result
    System.out.println(response.toString());
    
    return null;
    */
    
    URL obj = new URL(url);
    HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
    //HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    /* Add request header */
    con.setRequestMethod("GET");
    con.setRequestProperty("User-Agent", USER_AGENT);
    
    Iterator<String> it = header.keySet().iterator();
    while(it.hasNext()) {
      String key = (String) it.next();
      String value = header.get(key);
      con.setRequestProperty(key, value);
      //con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    }
    
    //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

    /* Send POST request */
    /*
    con.setDoOutput(true);
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(data);
    wr.flush();
    wr.close();
    */

    int responseCode = con.getResponseCode();
    logger.info("Sending 'GET' request to URL : " + url);
    logger.info("GET parameters : " + data);
    logger.info("Response Code : " + responseCode);

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    
    return response.toString();
  }
  
  
  
  
  public static String post(String url, HashMap<String, String> header, String data) throws Exception {
    
    URL obj = new URL(url);
    //HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
    HttpURLConnection con = (HttpURLConnection) obj.openConnection();

    /* Add request header */
    con.setRequestMethod("POST");
    con.setRequestProperty("User-Agent", USER_AGENT);
    
    Iterator<String> it = header.keySet().iterator();
    while(it.hasNext()) {
      String key = (String) it.next();
      String value = header.get(key);
      con.setRequestProperty(key, value);
      //con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
    }
    
    //String urlParameters = "sn=C02G8416DRJM&cn=&locale=&caller=&num=12345";

    /* Send POST request */
    con.setDoOutput(true);
    DataOutputStream wr = new DataOutputStream(con.getOutputStream());
    wr.writeBytes(data);
    wr.flush();
    wr.close();

    int responseCode = con.getResponseCode();
    logger.info("Sending 'POST' request to URL : " + url);
    logger.info("Post parameters : " + data);
    logger.info("Response Code : " + responseCode);

    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
    String inputLine;
    StringBuffer response = new StringBuffer();

    while ((inputLine = in.readLine()) != null) {
      response.append(inputLine);
    }
    in.close();
    
    return response.toString();
  }
}
