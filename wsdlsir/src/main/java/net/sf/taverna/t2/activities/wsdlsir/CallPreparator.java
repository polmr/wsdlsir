package net.sf.taverna.t2.activities.wsdlsir;

import javax.xml.namespace.QName;

import java.awt.Dimension;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Cookie;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.DataHandler;
import javax.net.ssl.X509TrustManager;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.wsdl.WSDLException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.rpc.ServiceException;
import javax.xml.soap.SOAPException;

import org.apache.axis.AxisFault;
import org.apache.axis.EngineConfiguration;
import org.apache.axis.attachments.AttachmentPart;
import org.apache.axis.client.Call;
import org.apache.axis.message.SOAPBodyElement;
import org.apache.axis.message.SOAPEnvelope;
import org.apache.axis.message.SOAPHeaderElement;
import org.apache.axis.transport.http.HTTPTransport;
import org.apache.commons.httpclient.ConnectTimeoutException;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.net.URLDecoder;

/**
 * 
 * @author Pablo Martin
 *
 */
public class CallPreparator {
	/**
	 * We use a flag to determine whether to write out some debug information.
	 * The user can use "-DDEBUG=true" as java option to enable debuggin 
	 */
	public static final boolean debug = Boolean.getBoolean("DEBUG");


	/**
	 * Prepares a org.apache.axis.client.Call to be able to access a web service service protected with shibboleth (SAML Web SSO Profile)
	 * 
	 * The idea is that the URL to the service is accessed (before the actual service invocation), and the behaviour of a brower is mimiced:
	 * redirection, autosubmitting forms, cookies forwarding, and also forms with inputs for the users are presented (via GUI).
	 * The goal is to obtain the cookie session for shibboleth autentication.
	 * 
	 * In the end, a handler is appended to the call, so that when the final invocation is performed, the cookies obtained (namely _shibsession_XXXXX cookie),
	 * are sent with the call.  
	 * 
	 * @deprecated
	 * @param call the call to prepare
	 * @param mihandler a handler (if a non null value is received, this handler is set (the algorithm does not take place)
	 * @return a HandlerConCookies, a handler for the call, that will override the invoke method in order to include the cookies 
	 */
	public static HandlerConCookies prepareCall4Shib(Call call, HandlerConCookies mihandler) {
		if (debug)
			System.out.println(" en prepareCall4Shib . mihandler="+mihandler);
		String endpoint = call.getTargetEndpointAddress();
		
		if (mihandler==null)
		{
			mihandler=createHandlerForCallToEndpoint( endpoint);
		}

		call.setClientHandlers(mihandler, null);

		 return mihandler;
	 }
	
	 /**
	  * Creates a handler that can be asigned to a a org.apache.axis.client.Call to be able to access a web service service protected with shibboleth (SAML Web SSO Profile)
	  * 
	  * The idea is that the URL to the service is accessed (before the actual service invocation), and the behaviour of a brower is mimiced:
	  * redirection, autosubmitting forms, cookies forwarding, and also forms with inputs for the users are presented (via GUI).
	  * The goal is to obtain the cookie session for shibboleth autentication.
	  * 
	  * @deprecated
	  * @param endpoint the URL of the protected service
	  * @return a HandlerConCookies, a handler for a call, that will override the invoke method in order to include the cookies 
	  */
	public static HandlerConCookies createHandlerForCallToEndpoint(String endpoint)
	{
		HandlerConCookies mihandler= new HandlerConCookies();
		Cookie [] cookies=createCookieForCallToEndpoint(endpoint);
		mihandler.setCookies(cookies);
		return mihandler;
	}
	
	/**
	 * Returns the cookies obtained when a web service service protected with shibboleth (SAML Web SSO Profile) is addressed.
	 * 
	 * The idea is that the URL to the service is accessed, and the behaviour of a brower is mimiced:
	 * redirection, autosubmitting forms, cookies forwarding, and also forms with inputs for the users are presented (via GUI).
	 * The goal is to obtain the cookie session for shibboleth autentication.
	 * 
	 * 
	 * @param endpoint the URL of the protected service
	 * @return an array of Cookies obtained. 
	 */
	public static Cookie [] createCookieForCallToEndpoint(String endpoint)
	{
		Integer level=new Integer(0);

		try{
			HttpClient client = new HttpClient(); // This client will deal with cookies automatically. It will be our "browser"
			client.getParams().setConnectionManagerTimeout(5000);
			client.getParams().setSoTimeout(5000);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000); 
			client.getHttpConnectionManager().getParams().setSoTimeout(5000);
			
			Integer status = null;  
			ResponseFromExecMethod returned=null;
			Map<String,String> parameters=new HashMap<String, String>();
			
			level=0;
			String newURL=endpoint;
			
			//We have a loop that will perform different HTTP methods, mimicing what the browser would do.
			do{ 
				if (debug)
					System.out.println(level+"-->"+" Accessing: " + newURL);
				String oldURL=newURL;
				
				try{
					returned=ejecutaMetodo(client, newURL,parameters, level);
				}
				catch (ConnectTimeoutException e){
					throw new Exception(" Timeout exceeded accesing: "+newURL);
				}
				
				if (debug)
					System.out.println(level+"->"+"returned="+returned);
				
				if (debug)
					System.out.println(level+"->" + " RETURNED: STATUS="+returned.getStatus()+ " + Accessing to: "+oldURL);
				
				parameters.clear();

				status = returned.getStatus();
				if (status==301 || status==302 || status==303) //REDIRECT: we get the redirect URL from the headers
				{
					newURL= returned.getRedirectLocation();
					if (debug)
						System.out.println(" changing URL due to a redirect to:" + newURL);

				}
				else if (status == 200) //OK: we need to get the URL from the presented form
				{
					// A - Get new URL from form
					newURL= getLocationFromForm(returned.getResponseBody());
					//TODO: Enhance the selection of the action (in case of several forms)
					//NOTE: xpath could be used to parse HTML contents

					if (debug)
						System.out.println(" LOCATION FROM FORM= "+newURL);

					if (newURL==null || "".equals(newURL) || oldURL.endsWith(newURL)) //if there is action is found in a form, we will use the previous URL (so does a browser) 
						newURL=oldURL;
					else // we use the URL from the action 
					{
						//if a relative path is given, we use the base URL we were using and append the relative path
						if (newURL.startsWith("/") ) // if it starts with "/", we will not append "/" 
							newURL = oldURL.substring(0, oldURL.lastIndexOf('/'))+newURL;
						else if (! newURL.startsWith("http") ) // we will need an extra "/"
							newURL = oldURL.substring(0, oldURL.lastIndexOf('/'))+"/"+newURL;

						if (debug)
							System.out.println(" changing URL due to a form to:" + newURL +" from:" + oldURL);
					}

					String responseBody = returned.getResponseBody();

					// B - we will gather the hidden inputs in order to re-sent them in a new POST pettition. 
					parameters=getParameters_from_response_form(responseBody);
					//System.out.println(" ---> found "+parameters.size()+" hidden parameters in responseBody from "+ oldURL); //too verbose

					//this tricks helps determines that the URL corresponds to SIR's WAYF.
					//TODO: In order to work with other federations similar stuff must be considered
					if (responseBody.contains("name=\"PAPIHLI\"")) 
					{
						// We would offer a dialog to let the user select which Idp requires.
						// TODO: this solution is only suitable for taverna-workbench as it is an interactive solution.
						
						Map<String,String> opciones = getOptionsFromSelect("PAPIHLI",responseBody);
						// Create an instance of the select dialog
						SelectDialog selectDialog = new SelectDialog( new JFrame(),true,opciones,"", endpoint);
						selectDialog.setModal(true);
						selectDialog.setVisible(true);

						if (selectDialog.isCanceled())
							break;

						if (debug)
							System.out.println("Idp selected:"+selectDialog.idpSelect.getSelectedItem().toString());
						
						// add the parameter with the option selected
						parameters.put("PAPIHLI", opciones.get(selectDialog.idpSelect.getSelectedItem()));

					}
					
					// We do this trick to determine that the response correspond to an Idp user/pass form. 
					// TODO: this has been successfully tested in some IDPs, but SAML Web SSO does not determine the implementation of those. 
					//   therefore, a generic implementation is unlikely, but some more Idps could be tested
					if (parameterInResponseBody("sso_pass",responseBody) || parameterInResponseBody("password",responseBody) || parameterInResponseBody("j_password",responseBody) )
					{
						
						String mensaje = newURL.equals(oldURL)?"NEW PETITION (maybe credentials given were incorrect)":"";

						// Create an instance of the test dialog
						TestDialog testDialog = new TestDialog( new JFrame(),true, mensaje);
						testDialog.setModal(true);
						testDialog.setVisible(true);

						if (testDialog.isCanceled())
							break;


						String username=testDialog.userField.getText();
						String password=testDialog.passwordField.getText();
						//password=testDialog.passwordField.getPassword().toString();

						parameters.put("j_username", username);
						parameters.put("j_password", password);
						parameters.put("username", username);
						parameters.put("password", password);
						parameters.put("sso_user", username);
						parameters.put("sso_pass", password);					
					}

				}
				else
				{
					System.err.println("loop finish unsucessfully. status="+status+ " accessing "+oldURL);
					newURL=null; // We do not want to continue
					throw new Exception("loop finish unsucessfully. status="+status+ " accessing "+oldURL);
					
				}

				if (level>25) // too much iterations (preventing infinite loop)
				{
					System.err.println("After "+level+ " iterations, we have been not succesfull. Cancellation forced");
					newURL=null;
				}
				level++;
			} while (status !=null && newURL!=null && !endpoint.equals(newURL) );

			
			Cookie [] cookies=client.getState().getCookies();
			return cookies;

		} catch (Exception e) {
			//System.err.println(e.toString());
			e.printStackTrace();
			String message = "Something went wrong trying to accomplish SAML Web SSO protocol. "
					+ "\n"
					+ "If the problem persists, use the option -DDEBUG=true in the taverna "
					+ "\n"
					+ "script and some extra information will be printed in the log files. "
					+ "\n"
					+ " Problem encountered: "
					+ "\n"+e.getMessage();
//			String message = "<html>Something went wrong trying to accomplish SAML Web SSO protocol. "
//					+ "If the problem persists use the options -DDEBUG=true in the taverna script "
//					+ "and some extra information will be printed in the log files. "
//					+ "<br>"
//					+ " Problem encountered: "+e.getMessage() + "</html>";
			
//			    JOptionPane.showMessageDialog(new JFrame(), message, "Dialog",
//			        JOptionPane.ERROR_MESSAGE);
			    
	            JTextArea jta = new JTextArea(message);
	            JScrollPane jsp = new JScrollPane(jta){
	                @Override
	                public Dimension getPreferredSize() {
	                    return new Dimension(480, 120);
	                }
	            };
	            JOptionPane.showMessageDialog(
	            		new JFrame(), jsp, "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		return null;
	}

	/**
	 * Creates a PostMethod to be executed using an HttpClient. Receives the URL and (POST) parameters to be passed along.
	 * Saves the results (redirects/cookies/status/responsebody) in a ResponseFromExecMethod variable that will be returned
	 * 
	 * @param client HttpClient client
	 * @param destiny destiny URL
	 * @param parametros parameters
	 * @param level number of invocation. Used for debugging information.
	 * @return the results (redirects/cookies/status/responsebody) in a ResponseFromExecMethod variable
	 * @throws IOException
	 * @throws HttpException
	 */
	private static ResponseFromExecMethod ejecutaMetodo(HttpClient client, String destiny, Map<String,String> parametros, Integer level) throws IOException, HttpException {
		level++;
		ResponseFromExecMethod toret= new ResponseFromExecMethod(level);

		
		PostMethod method = new PostMethod(destiny);
		if (parametros!=null)
		{
			Iterator<Entry<String, String>> it = parametros.entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry<String,String> e =  it.next();
				method.addParameter(e.getKey(),e.getValue());
			}
		}
		
		method.setRequestHeader("User-Agent", "CallPreparatorAgent"); //this prevents some Idps from failing (they might reject a petition with no user-agent header)
		
		method.setFollowRedirects(false); // to control redirections
		
		// For some odd reason, when there are several cookies in the client, they do not seem to reach the method call. Thus, we force the inclusion in the method
		String newcookie = "";
		
		if (method.getRequestHeader("Cookie")!=null)
			newcookie=method.getRequestHeader("Cookie")+";";
		for (Cookie c: client.getState().getCookies())
		{
			if (c.toString().contains("_shibsession") || c.toString().contains("path") || c.toString().contains("expires") )
			{
				if (debug)
					System.out.println(" cookie: "+c + " method.getRequestHeader(\"Cookie\") = "+method.getRequestHeader("Cookie"));
				newcookie+=c+";";
			}
		}

		method.setRequestHeader("Cookie",newcookie);

		
		//do the actual execution
		int status = client.executeMethod(method);
		
		if (debug)
			System.out.println(" justo after accesing method");
		
		//save the status
		toret.setStatus(status);

		Header redirectlocationHeader = method.getResponseHeader("location");
		if (redirectlocationHeader != null) {//if a redirection is given, we gather the cookies from the response "set-cookie" headers. 

			String redirectLocation = redirectlocationHeader.getValue();
			//save redirect location
			toret.setRedirectLocation(redirectLocation);
			if (method.getResponseHeader("set-cookie")!=null)
			{
				if (debug)
					System.out.println("setting cookies: "+ method.getResponseHeader("set-cookie"));
				//save the cookies
				toret.setCookies(method.getResponseHeader("set-cookie"));
			}
		} else { // we gather the response body to be returned
			BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			StringBuilder sb = new StringBuilder();
			String readLine;
			while(((readLine = br.readLine()) != null)) {
				sb.append(readLine+"\n");
			}
			//save the body
			toret.setResponseBody(sb.toString());
		}
		
		method.releaseConnection();

		return toret;
	}
	
	//TODO: seguir por aqui comentarios y por SelectDialog y demas....

	 /**
	  * Returns the parameters in a map (key,value) parsing the contents of an HTML response
	  * gathering the hidden inputs.
	  * 	 
	  * TODO The parsing is done by lines, so depending on the html structure
	  * this method would fail. xpath could be used to parse HTML contents.
	  * @param responseBodyAsString the html content to parse
	  * @return the hidden inputs with their values
	  */
	 private static Map<String,String> getParameters_from_response_form( String responseBodyAsString) {
		 
		 	Map <String,String> toret= new HashMap<String, String>();
		 	
			 Pattern namepatter = Pattern.compile("name=\"([a-zA-Z_]*)\"");
			 Pattern valuepattern = Pattern.compile("value=\"(.*)\"");

			 
			 String[] lines = responseBodyAsString.split(System.getProperty("line.separator"));
			 for (String line: lines)
			 {
				 if (line.contains("type=\"hidden\"") )
				 {
					 Matcher mname = namepatter.matcher(line);

					 if (mname.find()) {
						 String name = mname.group(1);
						 
						 Matcher mvalue = valuepattern.matcher(line);

						 if (mvalue.find()) {
							 String value = mvalue.group(1);
							 
							 //we need to aply some correction with some characters codification (TODO: a general solution might be necessary as similar problems may occur)
							 value=value.replaceAll("&#x3a;",":"); 
							 toret.put(name, value);
						 }


						 
					 }
					 
				 }
				 
			 }
			return toret;
		}
	 
	 /**
	  * Returns a Map (key,value) with options from an html response body 
	  * parsing the contents. It looks for the options within a select element (received as parameter)
	  * 
	  * 	 
	  * @param select name from the select with the options
	  * @param responseBodyAsString the html content to parse
	  * @return the map with the options in the select
	  */
	 private static Map<String,String> getOptionsFromSelect( String select, String responseBodyAsString) {
		 
		 	Map <String,String> toret= new LinkedHashMap<String, String>();
		 	
			 Pattern valuepattern = Pattern.compile("value=\"(.*)\"");
//			 Pattern optionpattern = Pattern.compile("<option");
			 Pattern namepattern = Pattern.compile(">([^<]*)<");

			 String[] lines = responseBodyAsString.split(System.getProperty("line.separator"));
			 for (String line: lines)
			 {
				 if (line.contains("<option") )
				 {
					 Matcher mname = namepattern.matcher(line);

					 if (mname.find()) {
						 String name = mname.group(1);
						 if (debug)
								System.out.println( " found option. name=" + name + " -> in linewether"+ line);

						 
						 Matcher mvalue = valuepattern.matcher(line);

						 if (mvalue.find()) {
							 String value = mvalue.group(1);
							 if (debug)
									System.out.println( " found value=" + value + " -> in line: "+ line);
							 // we need to aply some correction with some characters codification (TODO: a general solution might be necessary as similar problems may occur)
							 // SafeHtmlUtils.fromString(s); //I think this does not work
							 value=value.replaceAll("&#x3a;",":");
							 if (debug)
									System.out.println( " replaced value=" + value );

							 toret.put(name, value);
						 }
						 
					 }
					 
				 }
				 
			 }
			return toret;
		}
	 
	 
	 /**
	  * Returns if an input with certain name if found in the body contents
	  * @param parameter the name of the parameter
	  * @param responseBodyAsString the html content to parse
	  * @return whether the body contains such parameter
	  */
	 private static boolean parameterInResponseBody( String parameter, String responseBodyAsString) 
	 {
			 Pattern namepatter = Pattern.compile("name=\""+parameter+"\"");
			 
			 String[] lines = responseBodyAsString.split(System.getProperty("line.separator"));
			 for (String line: lines)
			 {
				 if (line.contains("<input") )
				 {
					 Matcher mname = namepatter.matcher(line);
					 if (mname.find()) {
						 if (debug)
								System.out.println( " found parameter!=" + parameter + " -> in line: "+ line);
						 return true;
					 }
				 }
			 }
			 return false;
		}
	 
	 
	 /**
	  * Returns the location of of the action in the forms an http body
	  * Some Idps might have addicional forms (besides the user/pass one). 
	  * 
	  * We need to ensure that the action returned is that of the form with those inputs
	  * TODO: this has been successfully tested in some IDPs, but SAML Web SSO does not determine the implementation of those. 
	  *  therefore, a generic implementation is unlikely, but some more Idps could be tested
	  * 
	  * @param responseBodyAsString the html content to parse
	  * @return the location on the action
	  */
	 private static String getLocationFromForm(String responseBodyAsString) {
		 Pattern namepatter = Pattern.compile("action=\"([^\"]*)\"");
		 
		 String toret=null;
		 String newtoret=null;
		 String[] lines = responseBodyAsString.split(System.getProperty("line.separator"));
		 
		 boolean userinputfound=false;
		 for (String line: lines)
		 {
			 if (line.contains("<form") )
			 {
				 Matcher mname = namepatter.matcher(line);
				 if (mname.find()) {
					 String action = mname.group(1);
					 // we need to aply some correction with some characters codification (TODO: a general solution might be necessary as similar problems may occur)
					 newtoret=action.replaceAll("&#x3a;", ":").replaceAll("&#x2f;", "/");
					 if (toret==null) toret=newtoret;
				 }
			 }
			 else if (line.contains("</form"))
				 newtoret=null;
			 if ( newtoret!=null && line.contains("input") 
					 && 
					(
							line.contains("name=\"user")
							|| line.contains("name='user")
							|| line.contains("name='sso_user")
							|| line.contains("name=\"sso_user")
					 )
					 )
				 toret=newtoret;
		 }
		 return toret;
	}
	 

//  This was used to prevent sites protected using non valid certificates to fail. 
//  I think that this is necessary for testing the code alone. When using this within taverna, the workbench will prompt for acceptation of certificates	 
//
//	 private static class DefaultTrustManager implements X509TrustManager {
//
//		 @Override
//		 public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
//
//		 @Override
//		 public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}
//
//		 @Override
//		 public X509Certificate[] getAcceptedIssuers() {
//			 return null;
//		 }
//	 }

	 /**
	  * This class is used to hold information on the execution of an HTTP method.
	  * 
	  * @author Pablo Martin
	  *
	  */
	private static class ResponseFromExecMethod
	{
		private Integer status;
		private HttpMethod method;
		private String redirectLocation;
		private Header cookies;
		private String responseBody;
		
		private Integer level;

		public ResponseFromExecMethod(Integer level) {
			super();
			this.level = level;
		}
		public String getResponseBody() {
			return responseBody;
		}
		public void setResponseBody(String responseBody) {
			this.responseBody = responseBody;
		}
		public Header getCookies() {
			return cookies;
		}
		public void setCookies(Header cookies) {
			this.cookies = cookies;
		}
		public Integer getStatus() {
			return status;
		}
		public void setStatus(Integer status) {
			this.status = status;
		}
		public HttpMethod getMethod() {
			return method;
		}
		public void setMethod(HttpMethod method) {
			this.method = method;
		}
		public String getRedirectLocation() {
			return redirectLocation;
		}
		public void setRedirectLocation(String redirectLocation) {
			this.redirectLocation = redirectLocation;
		}

		public String toString(){
			String toret="ResponseFromExecMethod ( "+" "+") \n";
			if (status!=null)
				toret+="\t"+level+"->"+"status: " + status+"\n";
			if (redirectLocation!=null)
				toret+="\t"+level+"->"+"redirect_to: " + redirectLocation+"\n";
			if (cookies!=null)
				toret+="\t"+level+"->"+"cookies: " + cookies+"\n";
			if (responseBody!=null)
				toret+="\t"+level+"->"+"contents: " + responseBody+"\n";
			toret+="-----------------------\n";


			return toret;

		}


	}

}