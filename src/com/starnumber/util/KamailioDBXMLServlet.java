package com.starnumber.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jdom.input.SAXBuilder;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *  This servlet is local to a Kamailio instance, and gives kamailio something to talk to.  When kamailio finds that it lacks a user profile, the 
 *  kamailio agent sends XML to this servlet, asking for the user profile.  This servlet contacts the CentralDBServlet, which does the actual DB lookup and returns XML.
 *  If everything goes right then it returns the User Profile as XML along with a 200 OK, otherwise it will return a 404 NOT FOUND or a 500 INTERNAL SERVER ERROR.
 * 
 * 
 * @author Bruce
 *
 */
public class KamailioDBXMLServlet extends HttpServlet {
	  private String responseFromCentralDBServlet ="";
	  private String calledURI; // will be extracted from request
	  private String result; // the response from Central DB Servlet.  E.g.  200 OK   or 404 NOT FOUND
	  private String user_profile; // the user_profile returned from CentralDB Servlet, if it is returned
	  
	  
	@Override
	public void doGet (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	  {
		  PrintWriter out = res.getWriter();
		  out.println("Hello! This is KamailioDBXMLServlet doGet()");
		  out.close();
	  }
	
	  @Override
/**
 * This is the main method of this servlet.  Don't use doGet().
 *  It expects to receive an HTTP POST with simple XML embedded in a variable named "message".  
 * This servlet parses the incoming XML, figures out what to do, then returns an XML response.
 * 
 * 
 * 
 */
	public void doPost (HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException
	  {

		  PrintWriter out = res.getWriter();
		  boolean requestForProfileSucceeded = false;
		  boolean requestForProfileFailedDueToNotFound = false;
		  boolean isActualUserProfilePresent=false;
		  String centralServletResponse = "";
		  // was a user profile found, such that KamailioServlet should pass it on  

		  try {
				// get the XML request, probably sent from Kamailio agent
				String reqMessage = req.getParameter("message");
		     	  System.out.println("KS message|" + reqMessage + "|");
				// print out XML request for debug purposes
				//out.println("message=|"+reqMessage+"|\n");
				  // put req XML into a Doc to parse
				
				///////////////////////////////
				// Start parsing XML request with SAX parser
				SAXParserFactory factory = SAXParserFactory.newInstance();
				SAXParser saxParser = factory.newSAXParser();				
				// input stream for SAXParser that represents the servlet request 
				byte[] reqMessageBA = reqMessage.getBytes();
				ByteArrayInputStream  reqMessagebyteArrayInputStream = new ByteArrayInputStream(reqMessageBA);
				
				DefaultHandler reqHandler = new DefaultHandler() {
					 
					boolean isKamailioRequest = false;
					boolean isCalledURI = false;
					boolean isResult = false;

					@Override
					public void startElement(String uri, String localName,String qName, 
				                Attributes attributes) throws SAXException {
				 
						System.out.println("KamailioSevletrequestStart Element :" + qName);
				 
						if (qName.equalsIgnoreCase("KamailioRequest")) {
							isKamailioRequest = true;
						}
				 
						if (qName.equalsIgnoreCase("calledURI")) {
							isCalledURI = true;
						}
				 
						if (qName.equalsIgnoreCase("result")) {
							isResult = true;
						}
					}
				 
					@Override
					public void endElement(String uri, String localName,
						String qName) throws SAXException {				 
						System.out.println("KamailioSevletRequest End Element :" + qName);
					}
				 
					@Override
					public void characters(char ch[], int start, int length) throws SAXException {				 

						// TODO: Implement a security/sanity check here to make sure we are talking to 
						// the correct agent.  Will involve a cypto-token
						if (isKamailioRequest) {
							isKamailioRequest = false;
						}						

						// Extract the calledURI from the request, if it's there
						if (isCalledURI) {
							System.out.println("Called URI: " + new String(ch, start, length));							
							calledURI = new String(ch, start, length);
							isCalledURI = false;
						}

						// extract the result field (an enumerated value of 200 OK , 404 NOT FOUND, or 500 INTERNAL SERVER ERROR)
						if (isResult) {
							//System.out.println("Called URI: " + new String(ch, start, length));							
							result = new String(ch, start, length);
							isResult = false;
						}
				 
					}				 
				};				
				
				// do the actual SAXparse of the incoming  XML message.  Any problems will throw an exception, which should return a 500 INTERNAL SERVER ERROR			
				saxParser.parse(reqMessagebyteArrayInputStream, reqHandler); 
				System.out.println("kamServlet bp#1");
				
				// TODO: implement a requestType token.  Currently we just do one thing: extract calledURI and request user profile.
				// check to see if calledURI extracted from XML is non-zero length 
				// if we got a called URI then proceed and request a User Profile from the CentralDBServlet
	
				if (calledURI!=null && calledURI.length() >0 ) {
					
					// we have a calledURI, so we can assemble an XML request to the CentralDBServlet.  Do this.
					reqMessage = "<xml  version=\"1.0\" encoding=\"UTF-8\"  schema=\"com.starnumberV.1.0\"><KamServletRequest><calledURI>"+calledURI+"</calledURI></KamServletRequest></xml>";

					// encode the message for safe transit.  
					String message = URLEncoder.encode("message", "UTF-8") + "=" + URLEncoder.encode(reqMessage, "UTF-8");

					System.out.println("KS request from KamServlet to CentralDBServlet:\n" + message);

					// contact CentralDBServlet and pass along a request.  This is the actual network traffic request
					centralServletResponse = postToURL(message);

					System.out.println("KS response from CentralDBServlet to KS:\n" + centralServletResponse);
					
					
					// check that centralServletResponse XML is non-null, to prevent crashes and NPEs
					if (centralServletResponse != null && centralServletResponse.length() > 0)
					{
						// parse centralServletResponse, look for success or failure
						// input stream for SAXParser that represents the CentralDB servlet response to parse 
						// we re-use the same ByteArrayInputStream we created to parse the initial request message
						centralServletResponse = centralServletResponse.replace("\n", "");
						
						
						byte[] resMessageBA = centralServletResponse.getBytes();
						reqMessagebyteArrayInputStream = new ByteArrayInputStream(resMessageBA);

						// TODO: Remove all System.out.println statements
						// TODO: replace all System.out.println with logging

						// create a SAX handler to handle the response doc from CentralDBServlet, which 
						// hopefully includes a UserProfile
						DefaultHandler resHandler = new DefaultHandler() {
							 
							boolean isCentralDBServletResponse = false;
							boolean isResult = false;
							boolean isUserProfile = false;
						 
							@Override
							public void startElement(String uri, String localName,String qName, 
						                Attributes attributes) throws SAXException {
						 
								System.out.println("KS CentralDBServletResponse Start Element :" + qName);
						 
								// TODO: add a check to verify it's the correct reponder, will involve crypto token
								// TODO: perform a check with CentralDBServletResponse: currently we only do one thing, which is pass on the User Profile XML
								if (qName.equalsIgnoreCase("CentralDBServletResponse")) {
									isCentralDBServletResponse = true;
								}

								// TODO: switch user_profile processing from String manipulation to SAXParse
								if (qName.equalsIgnoreCase("user_profile")) {
									isUserProfile = true;
								}

								if (qName.equalsIgnoreCase("result")) {
									isResult = true;
								}
							}
						 
							@Override
							public void endElement(String uri, String localName,
								String qName) throws SAXException {

								System.out.println("KS CentralDBServletResponse End Element :" + qName);
						 
							}
						 
							@Override
							public void characters(char ch[], int start, int length) throws SAXException {
								if (isResult) {
									result = new String(ch, start, length);
									System.out.println("KamServlet Result: " + result);							
									isResult=false;
								}								
								// TODO: do user profile parsing here.  For now we just pass it on.
								if (isUserProfile) {
									isUserProfile=false;
									// SAX parsing details for User Profile follows								
								}
								
							}
						 
						     };				
						// end of the CentralDBServlet response handler inner class      	
						  
						// do the actual SAXparse
						saxParser.parse(reqMessagebyteArrayInputStream, resHandler); 

						// we extract user profile from the response.  To do this we must track where it starts and ends in the character array. 
						int user_profile_start=0;
						int user_profile_end=0;
						// TODO: extract all the Strings to the start of file
						// mark the start tag
						user_profile_start = centralServletResponse.indexOf("<user_profile")  ;
						// mark the end of the end tag
						user_profile_end = centralServletResponse.indexOf("</user_profile>") + "</user_profile>".length();

						// if we received 200 OK and are supposed to get a user profile
						System.out.println("KS result==|"+result+"|");
						if ("200 OK".equalsIgnoreCase(result)) {
							System.out.println("KS user_profile_start="+user_profile_start+" & user_profile_end=" + user_profile_end + "\n");
							// TODO: protect this against ArrayIndexOutOfBounds
							if (user_profile_start > 0 && user_profile_end > user_profile_start) {
								user_profile=centralServletResponse.substring(user_profile_start,  user_profile_end);							
								isActualUserProfilePresent = true;
							}
							//System.out.println("response from CentralDBServlet to KamServlet:\n\n" + centralServletResponse + "\n\n");
							System.out.println("KS user_profile:\n\n|" + user_profile + "|\n\n");
						}
						
						// reset our counters
						requestForProfileSucceeded=false;
						requestForProfileFailedDueToNotFound=false;
						//  detect for 200 OK and pass on user profile
						//System.out.println("KamServlet: result = |" + result + "|");
						if (result != null && "200 OK".equals(result)) {
							// if result is 200 OK then presume it succeeded
							requestForProfileSucceeded = true;
							
							// TODO: put in a check that verifies the User Profile is valid!  belwo lines is a start
							// since we got a 200 OK Result we shall try to build a JDOM object with user_profile
							SAXBuilder sb=new SAXBuilder();
							//org.jdom.Document doc=sb.build("<xml><node/></xml>");							
							//System.out.println("kamServlet: created a JDOM user_profile:\n" + doc.toString() );

						}
						// detect 404 and pass on 404
						else if (result != null && "404 NOT FOUND".equals(result)) {
							// pretend it succeeded
							requestForProfileFailedDueToNotFound = true;
							
						}						
						// detect error pass on error
						
					}
					
				}
				
				
				
				
				// Build the XML reply to the kamailaio agent
				
				/////////////////////////////
				//Creating an empty XML Document
				
				//We need a Document
				DocumentBuilderFactory dbfac = DocumentBuilderFactory.newInstance();
				DocumentBuilder docBuilder = dbfac.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
	            ////////////////////////
	            //Creating the XML tree

	            //create the root element and add it to the document
	            Element root = doc.createElement("xml");
	            root.setAttribute("schema", "com.StarnumberV1.0");
	            doc.appendChild(root);
	            //create a comment and put it in the root element
	            //Comment comment = doc.createComment("Just a thought");
	            //root.appendChild(comment);

	            //create child element, add an attribute, and add to root
	            Element kamServletResponse = doc.createElement("KamServletResponse");
	            root.appendChild(kamServletResponse);

				// if requestForProfileSucceeded we got a valid user profile back, replied with 200 OK
				if (requestForProfileSucceeded) {
		            System.out.println("KamailioDBServlet: building 200 OK response and a User Profile");
		            //create grandchild element, add to child
		            Element grandchild2 = doc.createElement("result");
		            kamServletResponse.appendChild(grandchild2);

		            //add a text element to the child
		            Text text2 = doc.createTextNode("200 OK");
		            grandchild2.appendChild(text2);	            

		            Element calledURINode = doc.createElement("calledURI");
		            kamServletResponse.appendChild(calledURINode);

		            //add a text element to the child
		            Text text3 = doc.createTextNode(calledURI);
		            calledURINode.appendChild(text3);	            
		            System.out.println("KamailioDBServlet: building 200 OK response calledURI = " + calledURI);

		            Element grandchild3 = doc.createElement("user_profile");
		            kamServletResponse.appendChild(grandchild3);

		            CDATASection cdata = doc.createCDATASection(user_profile);
		            grandchild3.appendChild(cdata);
		            System.out.println("KamailioDBServlet: building 200 OK response user_profile = " + user_profile);
				}
				// if everything works but Central DB replies with a 404 NOT FOUND
				else if (requestForProfileFailedDueToNotFound) {
		            System.out.println("KamailioDBServlet: building 404 NOT FOUND response");
		            //create grandchild element, add to child
		            Element grandchild2 = doc.createElement("result");
		            kamServletResponse.appendChild(grandchild2);

		            //add a text element to the child
		            Text text2 = doc.createTextNode("404 Not Found");
		            grandchild2.appendChild(text2);	            
					
				}
				// if something went wrong other than 404 NOT FOUND
				else {
		            System.out.println("KamailioDBServlet: building 500 INTERNAL SERVER ERROR response");
		            //create grandchild element, add to child
		            Element grandchild2 = doc.createElement("result");
		            kamServletResponse.appendChild(grandchild2);

		            //add a text element to the child
		            Text text2 = doc.createTextNode("500 INTERNAL SERVER ERROR");
		            grandchild2.appendChild(text2);	            
					
				}

				// This next part is only required for TESTING, to make the JSP test page work right.
				
				
				//create grandchild element, add to child
	            Element grandchild10 = doc.createElement("RqstToCentralDBServ");
	            kamServletResponse.appendChild(grandchild10);

	            
	            // this is actually building the Request to CentralServerDB that shows in JSP
	            //create grandchild element, add to child
	            Element grandchild11 = doc.createElement("KamailioServletRequest");
	            grandchild10.appendChild(grandchild11);
	            
	            //create grandchild element, add to child
	            Element grandchild12 = doc.createElement("calledURI");
	            grandchild11.appendChild(grandchild12);
	            
	            //add a text element to the child
	            Text text12 = doc.createTextNode(calledURI);
	            grandchild12.appendChild(text12);	            
	            // end of JSP test page part
	            
	            /////////////////
	            //Output the XML

	            //set up a transformer
	            TransformerFactory transfac = TransformerFactory.newInstance();
	            Transformer trans = transfac.newTransformer();
	            trans.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	            trans.setOutputProperty(OutputKeys.INDENT, "yes");

	            //create string from xml tree
	            
	            
	            StringWriter sw = new StringWriter();
	            StreamResult result = new StreamResult(sw);
	            DOMSource source = new DOMSource(doc);
	            trans.transform(source, result);
	            String xmlString = sw.toString();				
	            System.out.println("KS about to return XML to KamAgent: " + xmlString);	            
	            out.println(xmlString);
			} 
		  // This catch encloses all of doPost() and returns an ERROR message if any un-caught exceptions are thrown.
			catch (Exception e) {
				e.printStackTrace();
				String XMLToReturnOnError = "<xml  version=\"1.0\" encoding=\"UTF-8\"  schema=\"com.StarnumberV1.0\"><KamServletResponse><result>500 INTERNAL SERVER ERROR</result></KamServletResponse><></xml>";
				out.println(XMLToReturnOnError);
				
			}
		  
			
		  
		  //out.println( this.getHTMLContent() );
		  //String messageFromKam = parseRequest(req.toString());
		  // contact CentralDBServlet and show its response.
		  //out.println(this.postToURL("test message"));
		  out.close();
	  }
	

	private String postToURL(String message) {
		// initialize the response to blank
		responseFromCentralDBServlet = "";
		try {
		    // Construct data

		    // Send data
		    URL url = new URL("http://localhost:8888/SNWebApp/CentralDBServlet");
		    URLConnection conn = url.openConnection();
		    conn.setDoOutput(true);
		    OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
		    wr.write(message);
		    wr.flush();

		    // Get the response
		    BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		    String line;
		    while ((line = rd.readLine()) != null) {
		        // Process line...
		    	responseFromCentralDBServlet += line;
		    }
		    wr.close();
		    rd.close();
		} catch (Exception e) {
		}
		return responseFromCentralDBServlet;
	}
	


	// TODO: make this next bit work properly, and use it to validate and handle User profile XML.  Possibly move this to other location.
	//Parses a string containing XML and returns a DocumentFragment
	//containing the nodes of the parsed XML.
	public static DocumentFragment parseXml(Document doc, String fragment) {
	 // Wrap the fragment in an arbitrary element
	 fragment = "<fragment>"+fragment+"</fragment>";
	 try {
	     // Create a DOM builder and parse the fragment
	     DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	     Document d = factory.newDocumentBuilder().parse(
	         new InputSource(new StringReader(fragment)));
	
	     // Import the nodes of the new document into doc so that they
	     // will be compatible with doc
	     Node node = doc.importNode(d.getDocumentElement(), true);
	
	     // Create the document fragment node to hold the new nodes
	     DocumentFragment docfrag = doc.createDocumentFragment();
	
	     // Move the nodes into the fragment
	     while (node.hasChildNodes()) {
	         docfrag.appendChild(node.removeChild(node.getFirstChild()));
	     }
	
	     // Return the fragment
	     return docfrag;
	 } catch (SAXException e) {
	     // A parsing error occurred; the xml input is not valid
	 } catch (ParserConfigurationException e) {
	 } catch (IOException e) {
	 }
	 return null;
	}

	
	private String calledURIRequiringProfile="defaults to 3";

	public String getCalledURIRequiringProfile() {
		return calledURIRequiringProfile;
	}

	public void setCalledURIRequiringProfile(String calledURIRequiringProfile) {
		this.calledURIRequiringProfile = calledURIRequiringProfile;
	}
	
	
	  
}
