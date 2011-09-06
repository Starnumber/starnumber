/**
 * 
 */
package com.starnumber.util;

import static org.junit.Assert.*;

import java.io.File;

import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.SAXException;

/**
 * @author User
 *
 */
public class KamailioDBXMLServletTest {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		KamailioDBXMLServlet kamailioDBXMLServlet = new KamailioDBXMLServlet();
		
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		//fail("Not yet implemented");
		assertTrue(true);
		// test for getter and setter
	}

	@Test
	public void testGetXMLRequest() {
		//fail("Not yet implemented");
		assertTrue(true);
		// test for getter and setter
	}

	// test whether SampleXMLIsValidAccordingToXSD
	@Test
	public void testDoPost() {
        // 1. Lookup a factory for the W3C XML Schema language
        SchemaFactory factory = 
            SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
        
        // 2. Compile the schema. 
        // Here the schema is loaded from a java.io.File, but you could use 
        // a java.net.URL or a javax.xml.transform.Source instead.
        File schemaLocation = new File("/data/StarnumberSchemaV1.xsd");
        Schema schema = null; 
        Validator validator = null;
        try {
        	schema = factory.newSchema(schemaLocation);
        	
        }
        catch (Exception e) {
        	assertTrue(false);
        }
    
        // 3. Get a validator from the schema.
        validator = schema.newValidator();
        
        // 4. Parse the document you want to check.
        // set args[0]  
        String XMLFileToValidate = "/data/SN_DMTokamailio_simple.xml";
        
        Source source = new StreamSource(XMLFileToValidate);
        
        
        // 5. Check the document
        try {
            validator.validate(source);
            System.out.println(XMLFileToValidate + " is valid.");
            assertTrue(true);
        }
        catch (SAXException ex) {
            System.out.println(XMLFileToValidate + " is not valid because ");
            System.out.println(ex.getMessage());
            assertTrue(false);
        }  
        catch (Exception ex) {
            System.out.println(XMLFileToValidate + " is VERY VERY not valid because ");
            System.out.println(ex.getMessage());
            assertTrue(false);
        }  
		
	}
	
	
}
