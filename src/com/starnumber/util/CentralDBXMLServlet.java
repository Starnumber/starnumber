
package com.starnumber.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


/**
 * CentralDBXMLServlet is a Starnumber servlet.  It is part of the kamailio to DB interface.  Kamailio does not speak directly to a remote database: 
 * instead kamailio communicates with a local servlet, which then contacts THIS servlet, which does a central DB lookup and returns an XML respons
 *  to the Kamailio servlet 
 * 
 * Use only the doPost() method on this servlet.
 * 
 * @author Bruce
 *
 */

public class CentralDBXMLServlet extends HttpServlet {
	private String messageFromkamailioServlet = "";
	private String responseToKamailioServlet = "<CentralDMServlet><CentralDMServlet>";
	private String calledURI;
	private String returnedXML = "<result>500 INTERNAL ERROR</result>";
	
	  public String getCalledURI() {
		return calledURI;
	}

	public void setCalledURI(String calledURI) {
		this.calledURI = calledURI;
	}


	/** HttpServletRequest 
	 *  HttpServletResponse 
	 * 
	 * This is the servlets primary method.  It expects to receive an HTTP POST with the XML embedded in a variable named "message".  
	 * This servlet parses incoming XML, figures out what to do, does a DB lookup, then returns the XML response it receives from the DB.
	 * After stress testing we might look into moving the XML conversion from DB to Tomcat layer, but this currently lives in the DB layer.
	 * 
	 */
	public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	  {
		  // we shall respond with XML
		  res.setContentType("text/xml");
		  // The PrintWriter writes the response, which sends back an XML response via HTTP 
		  PrintWriter out = res.getWriter();
		  messageFromkamailioServlet = req.getParameter("message");
		  // put call to database here
		  // transform to XML and return 
		  try {
				///////////////////////////////
				// Start parsing XML request with SAX parser
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();				
				System.out.println("CentralDBServletRequest messageFromkamailioServlet:" + messageFromkamailioServlet);
				// input stream for SAXParser that represents the servlet request 
				byte[] reqMessageBA = messageFromkamailioServlet.getBytes();
				ByteArrayInputStream  reqMessagebyteArrayInputStream = new ByteArrayInputStream(reqMessageBA);
				
				
				/**
				 * This is an inner class that handle SAX parsing.  Since this servlet only passes on XML without looking at it,
				 * SAX parsing is the most efficient way to do this.  This class contains the logic for parsing the XML 
				 * in the request, to see what was requested.  It will probably be a request for a UserProfile, and should provide a called URI.
				 * 
				 */
				DefaultHandler reqHandler = new DefaultHandler() {
					 
					
					boolean isKamailioServletRequest = false;
					boolean isCalledURI = false;
					
				 
					@Override
					public void startElement(String uri, String localName,String qName, 
				                Attributes attributes) throws SAXException {
				 
						System.out.println("CentralDBServletRequest Start Element :" + qName);
				 
						if (qName.equalsIgnoreCase("KamailioServletRequest")) {
							isKamailioServletRequest = true;
						}
						if (qName.equalsIgnoreCase("calledURI")) {
							isCalledURI = true;
						}												
					}
				 
					@Override
					public void endElement(String uri, String localName,
						String qName) throws SAXException {				 
						System.out.println("CentralDBServletRequest  End Element :" + qName);
					}
				 
					@Override
					public void characters(char ch[], int start, int length) throws SAXException {				 
						if (isCalledURI) {
							calledURI = new String(ch, start, length);
							System.out.println("CentralDBServletRequest  Called URI: " + new String(ch, start, length));							
							isCalledURI = false;
						}
				 
					}				 
				};				
				
				// do the actual SAXparse
				saxParser.parse(reqMessagebyteArrayInputStream, reqHandler); 
			  
				  // call the DB with calledURI
				  
				  if ("7827*7829@starnumber.net".equals(calledURI)) {
					  returnedXML = "<result>200 OK</result><calledURI>"+calledURI+"</calledURI>" + defaultXMLresponse ;
				  }
				  // case 404 not found
				  else {
					  returnedXML = "<result>404 NOT FOUND</result><calledURI>"+calledURI+"</calledURI>";			  
				  }
		  }
		  catch (Exception e) {
			  //TODO: Add better error checking to all SAXParse catch statements
			  // an error occurred, so return XML for ERROR OCCURRED
			  returnedXML = "<result>500 SERVER ERROR</result>";			  
			  
			  e.printStackTrace();			  
		  }
		  
		  
		  String returnString = "<xml schema='com.StarnumberV1.0'>\n<CentralDBServletResponse>"+returnedXML+"</CentralDBServletResponse></xml>";
		  
		  out.println(returnString);
		  out.close();
	  }
	
	  public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	  {
		  PrintWriter out = res.getWriter();
		  out.println("this is CentralDBServlet doGet().  You probably meant to call doPost()");
		  out.close();
	  }
	
	//  this is just some sample XML for testing.
	private String defaultXMLresponse = "<user_profile user_profile_id=\"3\" user_id=\"3\"><name>testuser profile 3</name><user_name>testuser2</user_name>"+								
	"  <line id=\"1\" is_active=\"true\" line_type_number=\"1\">"+								
	"    <description>Inbound SN</description>"+								
	"    <name>My first starnumber</name>"+								
	"	<uri>7827*7829@starnumber.net</uri>"+								
	"	  <line_profile id=\"1\">"+								
	"	    <name>Default</name>"+								
	"	      <line_profile_setting id=\"1\" setting_number=\"1\" setting_value_type=\"Int\">"+								
	"	  	    <name>Ring On Destination Line ID</name>"+								
	"  		    <value>2</value>"+								
	"	      </line_profile_setting>"+								
	"	      <line_profile_setting id=\"4\" setting_number=\"1\" setting_value_type=\"Int\">"+								
	"	  	    <name>Ring On Destination Line ID</name>"+								
	"  		    <value>3</value>"+								
	"	      </line_profile_setting>"+								
	"	      <line_profile_setting id=\"5\" setting_number=\"1\" setting_value_type=\"Int\">"+								
	"	  	    <name>Ring On Destination Line ID</name>"+								
	"  		    <value>4</value>"+								
	"	      </line_profile_setting>"+								
	"      </line_profile>"+								
	"  </line>"+								
	"</user_profile>";								


}
