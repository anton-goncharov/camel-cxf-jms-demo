package com.windmilled.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Random;

import junit.framework.Assert;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestClient {
	
	private final static String URL = "http://localhost:8180";
	private final static String TEST_SPECIFIC_STORE = "/alpha";
	private final static String TEST_GENERAL_STORE = "/general";
	private final static String TEST_PING = "/ping";

	private static final String CONTENT_SPECIFIC = "{\"type\":\"collection\",\"items\":[{\"type\":\"Item\",\"itemType\": \"type1\", \"properties\": { \"p1\": \"aaa\", \"p2\": 222 }} , {\"type\":\"Item\",\"G2\": \"data\", \"properties\": { \"p1\": \"bbb\", \"p2\": 222 }}]}";
	private static final String CONTENT_GENERAL = "{\"type\":\"collection\",\"items\":[{\"type\":\"Item\",\"itemType\": \"type2\", \"properties\": { \"p1\": \"ggg\", \"p2\": 777 }}]}";
	private static final String CONTENT_EXPECTED_MERGE = "{\"type\":\"collection\",\"items\":[{\"type\":\"Item\",\"itemType\":\"type2\",\"properties\":{\"p1\":\"ggg\",\"p2\":777}},{\"type\":\"Item\",\"itemType\":\"type1\",\"properties\":{\"p1\":\"aaa\",\"p2\":222}},{\"type\":\"Item\",\"G2\":\"data\",\"properties\":{\"p1\":\"bbb\",\"p2\":222}}]}";
	
	Logger logger = LoggerFactory.getLogger(this.getClass().getName());
	
	private Server server;
	
	/**
	 * Start embedded web server
	 * @throws Exception
	 */
	@Before
	public void before() throws Exception {
		logger.info("--------------------- START HTTP SERVER -----------------------");
		server = new Server(8180);
		WebAppContext context = new WebAppContext();
        context.setDescriptor("/WEB-INF/web.xml");
        context.setResourceBase("../camel-cxf-jms-demo/src/main/webapp");
        context.setContextPath("/");
        context.setParentLoaderPriority(true);
        server.setHandler(context);
        server.start();
	}
	
	/**
	 * Stop embedded web server
	 * @throws Exception
	 */
	@After
	public void after() throws Exception {
		logger.info("--------------------- STOP HTTP SERVER -----------------------");
		if (server != null) {
			server.stop();
		}
	}
	
	private Response sendPOST(String resource, String content) throws IOException {
		URL url = new URL(URL + resource);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		con.setRequestMethod("POST");
		
		OutputStream os = con.getOutputStream();
		os.write(content.getBytes("UTF-8"));
		os.flush();
		os.close();
		
		//send data
		Response response = new Response();
		int responseCode = con.getResponseCode();
		response.setCode(responseCode);
		
		logger.info("Sending POST request to URL : " + url);
		logger.info("Response Code : " + responseCode);					
			
		if (responseCode == HttpURLConnection.HTTP_OK) {
			StringBuilder sb = new StringBuilder();  		
		    BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream(),"UTF-8"));  
		    String line = null;  
		    while ((line = br.readLine()) != null) {  
		    	sb.append(line + "\n");  
		    }  
		    br.close();  
		    response.setContent(sb.toString());
		    if (sb.length() > 10000) {
				logger.info("Response: " + sb.toString().substring(0, 10000));	
			} else {
				logger.info("Response: " + sb.toString());
			}  
		} else {
			response.setContent(con.getResponseMessage());
			logger.info("Response: " + con.getResponseMessage());
			
		}
		return response;
	}
	
	private Response sendGET(String resource) throws IOException {
		URL url = new URL(URL + resource);
		HttpURLConnection con = (HttpURLConnection) url.openConnection();
		con.setDoOutput(true);
		con.setDoInput(true);
		con.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
		con.setRequestMethod("GET");			
 
		Response response = new Response();
		int responseCode = con.getResponseCode();
		response.setCode(responseCode);
		logger.info("Sending GET request to URL : " + url);
		logger.info("Response Code : " + responseCode);
 
		if (responseCode == HttpURLConnection.HTTP_OK) {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
			br.close();
			response.setContent(sb.toString());
			if (sb.length() > 10000) {
				logger.info("Response: " + sb.toString().substring(0, 10000));	
			} else {
				logger.info("Response: " + sb.toString());
			}			 
		} else {
			response.setContent(con.getResponseMessage());
			logger.info("Response: " + con.getResponseMessage());
		}
		return response;
	}
	
	/**
	 * Test if service is available
	 * @throws IOException
	 */
	@Test
	public void testPingService() throws IOException {
		logger.info("--------------------- TEST PING SERVICE -----------------------");
		Response response;
		response = sendGET(TEST_PING);
		Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
	}
	
	/**
	 * Try to upload data and check HTTP response status
	 * @throws IOException
	 */
	@Test
	public void testUploadData() throws IOException {
		logger.info("--------------------- TEST UPLOAD DATA -----------------------");
		Response response;
		response = sendPOST(TEST_SPECIFIC_STORE, CONTENT_SPECIFIC);
		Assert.assertNotNull(response);
		Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
		response = sendPOST(TEST_GENERAL_STORE, CONTENT_GENERAL);
		Assert.assertNotNull(response);
		Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
	}
	
	/**
	 * Try to get data from user resource 
	 * @throws IOException 
	 */
	@Test
	public void testGetUserData() throws IOException {
		logger.info("--------------------- TEST GET USER+GENERAL DATA -----------------------");
		sendPOST(TEST_SPECIFIC_STORE, CONTENT_SPECIFIC);
		sendPOST(TEST_GENERAL_STORE, CONTENT_GENERAL);
		
		Response response = sendGET(TEST_SPECIFIC_STORE);
		Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
		Assert.assertEquals((CONTENT_EXPECTED_MERGE).trim(), response.getContent().trim());
	}
	
	/**
	 * Try to get data from general resource 
	 * @throws IOException 
	 */
	@Test
	public void testGetGeneralData() throws IOException {
		logger.info("--------------------- TEST GET GENERAL DATA -----------------------");
		sendPOST(TEST_SPECIFIC_STORE, CONTENT_SPECIFIC);
		sendPOST(TEST_GENERAL_STORE, CONTENT_GENERAL);
		
		Response response = sendGET(TEST_GENERAL_STORE);
		Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
        Assert.assertEquals(CONTENT_GENERAL.trim(), response.getContent().trim());
	}

	/**
	 * Try to upload and download big chunks of data (10mb of user data + 10mb of general data)
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void testGetBigChunkData() throws IOException, InterruptedException {
		logger.info("--------------------- TEST WITH BIG CHUNKS OF DATA -----------------------");
		// ~10mb file		
		String bigchunk1 = createStringWithSize(10000);
        String bigMessage1 = CONTENT_SPECIFIC.replace("aaa",bigchunk1);
		// ~10mb file		
		String bigchunk2 = createStringWithSize(10000);
        String bigMessage2 = CONTENT_GENERAL.replace("aaa",bigchunk2);
		sendPOST(TEST_SPECIFIC_STORE, bigMessage1);
		sendPOST(TEST_GENERAL_STORE, bigMessage2);

        // wait until cache will be updated
		Thread.sleep(2000);
		
		Response response = sendGET(TEST_SPECIFIC_STORE);
		logger.info("message 1 length: " + bigMessage1.length());
		logger.info("message 2 length: " + bigMessage2.length());
		logger.info("response length: " + response.getContent().length());
		Assert.assertEquals(HttpURLConnection.HTTP_OK, response.getCode());
		
	}	
	
	/**
	 * Generates String
	 * @param msgSize - string size in KB
	 */
	private static String createStringWithSize(int msgSize) {
		// java chars are 2 bytes
		msgSize = msgSize / 2;
		msgSize = msgSize * 1024;
		StringBuilder sb = new StringBuilder(msgSize);
		Random r = new Random();
		for (int i = 0; i < msgSize; i++) {			
			// get random char from 'a' to 'z'
			char c = (char)(r.nextInt(26) + 'a'); 
			sb.append(c);
		}
		return sb.toString();
	}
	

}
