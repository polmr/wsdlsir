/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/

package net.sf.taverna.t2.activities.wsdlsir;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.StringReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;

import net.sf.taverna.t2.activities.wsdlsir.security.SecurityProfiles;
import net.sf.taverna.t2.security.credentialmanager.CMException;
import net.sf.taverna.t2.security.credentialmanager.CredentialManager;
import net.sf.taverna.t2.security.credentialmanager.UsernamePassword;
import net.sf.taverna.wsdl.parser.WSDLParser;
import net.sf.taverna.wsdl.soap.WSDLSOAPInvoker;

import org.apache.axis.AxisProperties;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.MessageContext;
import org.apache.axis.client.Call;
import org.apache.axis.configuration.XMLStringProvider;
import org.apache.axis.encoding.Base64;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.commons.httpclient.Cookie;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.input.SAXBuilder;
import org.jdom.output.DOMOutputter;

/**
 * Invokes SOAP based Web Services from T2.
 * 
 * Subclasses WSDLSOAPInvoker used for invoking Web Services from Taverna 1.x
 * and extends it to provide support for invoking secure Web services.
 * 
 * @author Stuart Owen
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 * 
 */
public class T2WSDLSOAPInvoker extends WSDLSOAPInvoker {
	/**
	 * We use a flag to determine whether to write out some debug information.
	 * The user can use "-DDEBUG=true" as java option to enable debuggin 
	 */
	public static final boolean debug = Boolean.getBoolean("DEBUG");

	private static final String REFERENCE_PROPERTIES = "ReferenceProperties";
	private static final String ENDPOINT_REFERENCE = "EndpointReference";
	private static Logger logger = Logger.getLogger(T2WSDLSOAPInvoker.class);
	private static final Namespace wsaNS = Namespace.getNamespace("wsa",
			"http://schemas.xmlsoap.org/ws/2004/03/addressing");

	private String wsrfEndpointReference = null;
	
	public T2WSDLSOAPInvoker(WSDLParser parser, String operationName,
			List<String> outputNames) {
		super(parser, operationName, outputNames);
	}

	public T2WSDLSOAPInvoker(WSDLParser parser, String operationName,
			List<String> outputNames, String wsrfEndpointReference) {
		this(parser, operationName, outputNames);
		this.wsrfEndpointReference = wsrfEndpointReference;
	}
	
	@SuppressWarnings("unchecked")
	protected void addEndpointReferenceHeaders(
			List<SOAPHeaderElement> soapHeaders) {
		// Extract elements
		// Add WSA-stuff
		// Add elements

		Document wsrfDoc;
		try {
			wsrfDoc = parseWsrfEndpointReference(wsrfEndpointReference);
		} catch (JDOMException e) {
			logger.warn("Could not parse endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		} catch (IOException e) {
			logger.error("Could not read endpoint reference, ignoring:\n"
					+ wsrfEndpointReference, e);
			return;
		}

		Element endpointRefElem = null;
		Element wsrfRoot = wsrfDoc.getRootElement();
		if (wsrfRoot.getNamespace().equals(wsaNS)
				&& wsrfRoot.getName().equals(ENDPOINT_REFERENCE)) {
			endpointRefElem = wsrfRoot;
		} else {
			// Only look for child if the parent is not an EPR
			Element childEndpoint = wsrfRoot
					.getChild(ENDPOINT_REFERENCE, wsaNS);
			if (childEndpoint != null) {
				// Support wrapped endpoint reference for backward compatibility
				// and convenience (T2-677)
				endpointRefElem = childEndpoint;
			} else {
				logger
						.warn("Unexpected element name for endpoint reference, but inserting anyway: "
								+ wsrfRoot.getQualifiedName());
				endpointRefElem = wsrfRoot;
			}
		}

		Element refPropsElem = endpointRefElem.getChild(REFERENCE_PROPERTIES,
				wsaNS);
		if (refPropsElem == null) {
			logger.warn("Could not find " + REFERENCE_PROPERTIES);
			return;
		}

		List<Element> refProps = refPropsElem.getChildren();
		// Make a copy of the list as it would be modified by
		// prop.detach();
		for (Element prop : new ArrayList<Element>(refProps)) {
			DOMOutputter domOutputter = new DOMOutputter();
			SOAPHeaderElement soapElem;
			prop.detach();
			try {
				org.w3c.dom.Document domDoc = domOutputter.output(new Document(
						prop));
				soapElem = new SOAPHeaderElement(domDoc.getDocumentElement());
			} catch (JDOMException e) {
				logger.warn(
						"Could not translate wsrf element to DOM:\n" + prop, e);
				continue;
			}
			soapElem.setMustUnderstand(false);
			soapElem.setActor(null);
			soapHeaders.add(soapElem);
		}

		// soapHeaders.add(new SOAPHeaderElement((Element) wsrfDoc
		// .getDocumentElement()));
	}

	protected void configureSecurity(Call call,
			WSDLActivityConfigurationBean bean) throws Exception {

		// If security settings require WS-Security - configure the axis call
		// with appropriate properties
		String securityProfile = bean.getSecurityProfile();
		if (securityProfile
				.equals(SecurityProfiles.WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD)
				|| securityProfile
						.equals(SecurityProfiles.WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD)
				|| securityProfile
						.equals(SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTEXTPASSWORD)
				|| securityProfile
						.equals(SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD)) {
			
			UsernamePassword usernamePassword = getUsernameAndPasswordForService(bean, false);
			call.setProperty(Call.USERNAME_PROPERTY, usernamePassword.getUsername());
			call.setProperty(Call.PASSWORD_PROPERTY, usernamePassword.getPasswordAsString());
			usernamePassword.resetPassword();
		} else if (securityProfile.equals(SecurityProfiles.HTTP_BASIC_AUTHN)){
			// Basic HTTP AuthN - set HTTP headers
			// pathrecursion allowed
			UsernamePassword usernamePassword = getUsernameAndPasswordForService(bean, true);
			MessageContext context = call.getMessageContext();
			context.setUsername(usernamePassword.getUsername());
			context.setPassword(usernamePassword.getPasswordAsString());
			usernamePassword.resetPassword();
		}
		else if (securityProfile.equals(SecurityProfiles.SAMLWEBSSOAUTH))
		{
			// set handlers for the call.
			WSDLParser parser = new WSDLParser(bean.getWsdl()); 
			List<String> endpoints = parser.getOperationEndpointLocations(bean.getOperation());
			for (String endpoint : endpoints) //Actually i am only expecting one endpoint
			{
				HandlerConCookies ahandler= new HandlerConCookies();

				CredentialManager credman = CredentialManager.getInstance();
				
				// we query the credential manager for the password (actually that would be the cookies)
				if (credman.containsAlias(CredentialManager.KEYSTORE, "password#" + new URI(endpoint).toASCIIString()) ){ 
					UsernamePassword usernamepass=credman.getUsernameAndPasswordForService(new URI(endpoint), false, "nomatter");
					
					//get the cookies from Credential Mangaer and put in on the handler
					Cookie[] cookies = (Cookie[])deserializefromString(usernamepass.getPasswordAsString());
					ahandler.setCookies( cookies);
					
					if (debug)
						for (Cookie cookie : cookies) 
						{
							System.out.println(" in configureSecurity with profile=SAMLWEBSSOAUTH . cookie in cookies: "+cookie);
						}
					
					//TODO: a failure due to a wrong cookie should be considered and a new handler should de obtained (if profile is SAMLWEBSSOAUTH...).
					//HOW? a GET might do. maybe a "testconectivity(String endpoint, Cookie[] cookies)" method should be included in CallPreparator class
				}
				else // nothing on credential manager, we perform the algorithm for obtaining a new one.  
				{
					System.out.println("Credential manager does not contain the session cookie");
					Cookie[] cookiesTODAS= CallPreparator.createCookieForCallToEndpoint(endpoint);
					ahandler.setCookies(cookiesTODAS);
					
					//Notice that unlike when the call is configured via UI, we do not save the cookies on the credential manager
				}

				// Finally we set the new handler to the call
				call.setClientHandlers(ahandler, null);
			}
		}
		else {
			logger.error("Unknown security profile " + securityProfile);
		}
	}


	/**
	 * Read the object from Base64 string. 
	 * @param s
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	 private static Object deserializefromString( String s ) throws IOException ,
	                                                     ClassNotFoundException {
	      byte [] data = Base64.decode( s );
	      ObjectInputStream ois = new ObjectInputStream( 
	                                      new ByteArrayInputStream(  data ) );
	      Object o  = ois.readObject();
	      ois.close();
	      return o;
	 }

	 
	/**
	 * Get username and password from Credential Manager or ask user to supply
	 * one. Username is the first element of the returned array, and the
	 * password is the second.
	 */
	protected UsernamePassword getUsernameAndPasswordForService(
			WSDLActivityConfigurationBean bean, boolean usePathRecursion) throws CMException {

		// Try to get username and password for this service from Credential
		// Manager (which should pop up UI if needed)
		CredentialManager credManager = null;
		credManager = CredentialManager.getInstance();
		String wsdl = bean
				.getWsdl();
		URI serviceUri = URI.create(wsdl); 
		UsernamePassword username_password = credManager.getUsernameAndPasswordForService(serviceUri, usePathRecursion, null);
		if (username_password == null) {
			throw new CMException("No username/password provided for service " + bean.getWsdl());
		} 
		return username_password;
	}

	@Override
	protected List<SOAPHeaderElement> makeSoapHeaders() {
		List<SOAPHeaderElement> soapHeaders = new ArrayList<SOAPHeaderElement>(
				super.makeSoapHeaders());
		if (wsrfEndpointReference != null && getParser().isWsrfService()) {
			addEndpointReferenceHeaders(soapHeaders);
		}
		return soapHeaders;
	}

	protected org.jdom.Document parseWsrfEndpointReference(
			String wsrfEndpointReference) throws JDOMException, IOException {
		SAXBuilder builder = new SAXBuilder();
		return builder.build(new StringReader(wsrfEndpointReference));
	}

	public Map<String, Object> invoke(Map<String, Object> inputMap,
			WSDLActivityConfigurationBean bean) throws Exception {

		String securityProfile = bean.getSecurityProfile();
		EngineConfiguration wssEngineConfiguration = null;
		if (securityProfile != null) {
			// If security settings require WS-Security and not just e.g. Basic HTTP
			// AuthN - configure the axis engine from the appropriate config strings
			if (securityProfile
					.equals(SecurityProfiles.WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD)) {
				wssEngineConfiguration = new XMLStringProvider(
						SecurityProfiles.WSSECURITY_USERNAMETOKEN_PLAINTEXTPASSWORD_CONFIG);
			} else if (securityProfile
					.equals(SecurityProfiles.WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD)) {
				wssEngineConfiguration = new XMLStringProvider(
						SecurityProfiles.WSSECURITY_USERNAMETOKEN_DIGESTPASSWORD_CONFIG);
			} else if (securityProfile
					.equals(SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTEXTPASSWORD)) {
				wssEngineConfiguration = new XMLStringProvider(
						SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_PLAINTETPASSWORD_CONFIG);
			} else if (securityProfile
					.equals(SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD)) {
				wssEngineConfiguration = new XMLStringProvider(
						SecurityProfiles.WSSECURITY_TIMESTAMP_USERNAMETOKEN_DIGESTPASSWORD_CONFIG);
			}
		}

		// This does not work
//		ClassUtils.setClassLoader("net.sf.taverna.t2.activities.wsdlsir.security.TavernaAxisCustomSSLSocketFactory",TavernaAxisCustomSSLSocketFactory.class.getClassLoader());
		
		// Setting Axis property only works when we also set the Thread's classloader as below 
		// (we do it from the net.sf.taverna.t2.workflowmodel.processor.dispatch.layers.Invoke.requestRun())
//		Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
		if (AxisProperties.getProperty("axis.socketSecureFactory")== null || !AxisProperties.getProperty("axis.socketSecureFactory").equals("net.sf.taverna.t2.activities.wsdlsir.security.TavernaAxisCustomSSLSocketFactory")){
			AxisProperties.setProperty("axis.socketSecureFactory", "net.sf.taverna.t2.activities.wsdlsir.security.TavernaAxisCustomSSLSocketFactory");
			logger.info("Setting axis.socketSecureFactory property to " + AxisProperties.getProperty("axis.socketSecureFactory"));
		}
        
		// This also does not work
		//AxisProperties.setClassDefault(SecureSocketFactory.class, "net.sf.taverna.t2.activities.wsdlsir.security.TavernaAxisCustomSSLSocketFactory");
        
		Call call = super.getCall(wssEngineConfiguration);
		
		// Now that we have an axis Call object, configure any additional
		// security properties on it (or its message context or its Transport
		// handler),
		// such as WS-Security UsernameToken or HTTP Basic AuthN
		if (securityProfile != null) {
			configureSecurity(call, bean);
		}
		
		

		

		return invoke(inputMap, call);
	}

}
