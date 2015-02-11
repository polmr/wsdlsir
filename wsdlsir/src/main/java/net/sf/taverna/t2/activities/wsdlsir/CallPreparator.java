package net.sf.taverna.t2.activities.wsdlsir;

import javax.xml.namespace.QName;


//import java.io.PrintWriter;
//import java.io.BufferedWriter;
//import java.io.FileWriter;






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
	
	public static final boolean debug = Boolean.getBoolean("DEBUG");

	
//	private static int nivel=0; //TODO: que esto no sea variable estatica, sino que se cree en prperaecall y se pase entre los demas

	 /**
	  * Prepara una call para poder acceder a servicio protegido por shibboleth.
	  * La idea es que intente acceder a la URL del servicio, y que responda a las redirecciones que reciba y a los formularios con los datos que sean necesarios
	  * Forwardea las cookies, igual que haría el navegador. 
	  * Finalmente añade un handler a la call para que a la hora de invocar, se establezcan las cookies que se han obtenido de la sessión shibboleth
	  * 	-> En realidad solo nos interesa la cookie _shibsession_XXXXXX
	  * 
	  * NOTA FUTURO: cuando usemos SirDemo, para simular el formulario del WAYF, es interesante tomar como referencia la web de SirDemo deshabilitando JAVASCRIPT
	  * @deprecated
	  * @param call
	  * @param username
	  * @param password
	  * @param idpURLAuthForm Cuando reciba esta URL hará un POST con el user y pass que se le pasen
	  * @return un HandlerConCookies, que puede usarse para que otras calls también usen la cookie de turno 
	  */
//	 public static HandlerConCookies prepareCall4Shib(Call call, String username, String password, String idpURLAuthForm) {
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
	
	//deprecated //TODO: docstrings!
	public static HandlerConCookies createHandlerForCallToEndpoint(String endpoint)
	{
		HandlerConCookies mihandler= new HandlerConCookies();
		Cookie [] cookies=createCookieForCallToEndpoint(endpoint);
		mihandler.setCookies(cookies);
		return mihandler;
	}
	
	
	public static Cookie [] createCookieForCallToEndpoint(String endpoint)
	{
		Integer nivel=new Integer(0);

		try{
			HttpClient client = new HttpClient(); // este cliente va a gestionar las cookies automáticamente. Va a ser nuestro "browser"
			client.getParams().setConnectionManagerTimeout(5000);
			client.getParams().setSoTimeout(5000);
			client.getHttpConnectionManager().getParams().setConnectionTimeout(5000); 
			client.getHttpConnectionManager().getParams().setSoTimeout(5000);
			
			Integer status = null;  
			ResponseFromExecMethod returned=null;
			Map<String,String> parameters=new HashMap<String, String>();
			
			nivel=0;
			String newURL=endpoint;
			do{
				try {
					//newURL = URLDecoder.decode(newURL, "UTF-8");
					//newURL = newURL.replace("%20"," ");
					//newURL = URLEncoder.encode(newURL, "UTF-8");
			     } catch (Exception e) {
			         // TODO Auto-generated catch block
			         e.printStackTrace();
			     }

				if (debug)
					System.out.println(nivel+"-->"+" Accessing: " + newURL);
				String oldURL=newURL;
				
				try{
					returned=ejecutaMetodo(client, newURL,parameters, nivel);
				}
				catch (ConnectTimeoutException e){
					throw new Exception(" Timeout exceeded accesing: "+newURL);
				}
				
				if (debug)
					System.out.println(nivel+"->"+"returned="+returned);
				
				if (debug)
					System.out.println(nivel+"->" + " DEVUELTO: STATUS="+returned.getStatus()+ " + AL acceder a: "+oldURL);
				
				//PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter("pirulo.txt", true)));
				//writer.println(nivel+"->" + " DEVUELTO: "+returned.getStatus()+ " + AL acceder a: "+oldURL);
				//writer.close();
				
				parameters.clear();

				status = returned.getStatus();
				if (status==301 || status==302 || status==303) //REDIRECT: obtenemos la URL de las cabeceras
				{
					newURL= returned.getRedirectLocation();
					if (debug)
						System.out.println(" cambiando url por culpa de redirect a:" + newURL);

				}
				else if (status == 200) //OK: obtenemos la URL del formulario que se nos muestre
				{
					// A - Obtenemos nueva URL
					newURL= getLocationFromForm(returned.getResponseBody());
					//TODO: mejorar seleccion de action (en caso de varios forms!)
					// NOTA: posibilidad de usar xpath para parsear el contenido html.

					if (debug)
						System.out.println(" LOCATION FROM FORM= "+newURL);//TODO: DELETE

					if (newURL==null || "".equals(newURL) || oldURL.endsWith(newURL)) // si no hay un form con otro action, repetiremos la URL a la que estabamos queriendo acceder
						newURL=oldURL;
					else // usamos la url que tenemos en el action del form 
					{
						if (newURL.startsWith("/") ) //si la ruta es relativa, copiamos la ruta base en la que estabamos.
							newURL = oldURL.substring(0, oldURL.lastIndexOf('/'))+newURL;
						else if (! newURL.startsWith("http") ) // copiamos la ruta base en la que estabamos.
							newURL = oldURL.substring(0, oldURL.lastIndexOf('/'))+"/"+newURL;

						if (debug)
							System.out.println(" cambiando url por culpa de formulario a:" + newURL +" de:" + oldURL);
					}

					String responseBody = returned.getResponseBody();

					// B - cogemos los parameters hidden con sus valores para reenviarlos en nueva petición POST
//					System.out.println(" BUSCANDO parametros hidden en cuerpo devuelto por "+ oldURL+ " CUERPO="+responseBody);
					parameters=getParameters_from_response_form(responseBody);
//					System.out.println(" ---> se han encontrado "+parameters.size()+"parametros hidden en cuerpo devuelto por "+ oldURL);

					// incluímos los parámetros 
					//          		if (newURL.contains(WAYFURL))
					if (responseBody.contains("name=\"PAPIHLI\""))
					{
						Map<String,String> opciones = getOptionsFromSelect("PAPIHLI",responseBody);
						// Create an instance of the select
						SelectDialog selectDialog = new SelectDialog( new JFrame(),true,opciones,"", endpoint);
						selectDialog.setModal(true);
						selectDialog.setVisible(true);

						if (selectDialog.isCanceled())
							break;

						if (debug)
							System.out.println("se ha seleccionado:"+selectDialog.idpSelect.getSelectedItem().toString());
						//          				System.exit(1); //TODO: delete
						parameters.put("PAPIHLI", opciones.get(selectDialog.idpSelect.getSelectedItem()));
						//          			parameters.put("PAPIHLI", "FCSCLsirAS");


					}
					//          		if (newURL!=null && newURL.contains(finalIdpURL))
					if (parameterInResponseBody("sso_pass",responseBody) || parameterInResponseBody("password",responseBody) || parameterInResponseBody("j_password",responseBody) )
					{
						//          			System.out.println(" COMPARANDO newURL="+newURL+ " con OLD="+oldURL);
						String mensaje = newURL.equals(oldURL)?"VOLVIENDO A PEDIR (tal vez fuera incorrecta)":"";

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
					System.err.println("bucle terminado sin llegar a puerto. status="+status+ " al acceder a "+oldURL);
					newURL=null; // no queremos seguir
					throw new Exception("bucle terminado sin llegar a puerto. status="+status+ " al acceder a "+oldURL);
					
				}

				if (nivel>25) // demasiadas iteraciones (prevencion de bucle infinito)
				{
					System.err.println("Tras "+nivel+ " iteraciones, no hemos llegado a puerto. Forzamos cancelacion");
					newURL=null;
				}
				nivel++;
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
	 * Crea y genera un PostMethod a ejecutar usando un HttpClient. Recibe la URL y los parámetros (POST) que se le van a pasar.
	 * Guarda los resultados (redirecciones/cookies/status/responsebody) en una variable ResponseFromExecMethod que devuelve
	 * @param client
	 * @param destiny
	 * @param parametros
	 * @return los resultados (redirecciones/cookies/status/responsebody) en una variable ResponseFromExecMethod
	 * @throws IOException
	 * @throws HttpException
	 */
	private static ResponseFromExecMethod ejecutaMetodo(HttpClient client, String destiny, Map<String,String> parametros, Integer nivel) throws IOException, HttpException {
		nivel++;
		ResponseFromExecMethod toret= new ResponseFromExecMethod(nivel);

		
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
		
		method.setFollowRedirects(false); //ESTO PARA CONTROLAR A MANO LO QUE HACE (tal vez se pueda dejar que lo haga solo)!!
		
		//POR ALGUNA EXTRAÑA RAZON, CUANDO HAY VARIAS COOKIES en CLIENT; no parecen llegar en la llamada. POR eso podemos forzarlo "a mano" la inclusion en el metodo
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

		
		
		int status = client.executeMethod(method);
		if (debug)
			System.out.println(" justo TRAS acceder a method");
		toret.setStatus(status);
//		System.out.println(nivel+"->"+" leido status =" +status+ " accediendo a "+ destiny+"\n\t con cookies="+method.getRequestHeader("cookie"));
		Header redirectlocationHeader = method.getResponseHeader("location");
		if (redirectlocationHeader != null) {
			String redirectLocation = redirectlocationHeader.getValue();
			toret.setRedirectLocation(redirectLocation);
			if (method.getResponseHeader("set-cookie")!=null)
			{
				if (debug)
					System.out.println("setting cookies: "+ method.getResponseHeader("set-cookie")); //TODO: delete);
				toret.setCookies(method.getResponseHeader("set-cookie"));
			}

		} else {
//			toret.setResponseBody(method.getResponseBodyAsString());
			BufferedReader br = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
			StringBuilder sb = new StringBuilder();
			String readLine;
			while(((readLine = br.readLine()) != null)) {
				sb.append(readLine+"\n");
			}
			toret.setResponseBody(sb.toString());
		}
		
		method.releaseConnection();

		return toret;
	}
	
	 private static void addParameters_from_response_form(PostMethod method,
			String responseBodyAsString) {
		 Pattern namepatter = Pattern.compile("name=\"([a-zA-Z]*)\"");
		 Pattern valuepattern = Pattern.compile("value=\"(.*)\"");

		 
		 String[] lines = responseBodyAsString.split(System.getProperty("line.separator"));
		 for (String line: lines)
		 {
			 if (line.contains("type=\"hidden\"") )
			 {
				 
				 Matcher mname = namepatter.matcher(line);

				 if (mname.find()) {
					 String name = mname.group(1);
					 if (debug)
							System.out.println( " encontrado parametro=" + name + " -> en linea"+ line);
//					 SafeHtmlUtils.fromString(s);

					 
					 Matcher mvalue = valuepattern.matcher(line);

					 if (mvalue.find()) {
						 String value = mvalue.group(1);
						 if (debug)
								System.out.println( " encontrado value=" + value + " -> en linea"+ line);
//						 System.out.println( " unescaped value=" + unescapeJavaString(value) + " -> en linea"+ line);
						 value=value.replaceAll("&#x3a;",":");
						 if (debug)
								System.out.println( " replaced value=" + value );

						 method.addParameter(name, value);
						 
					 }
					 
				 }
				 
			 }
			 
		 }
		
	}


	 /**
	  * Devuelve un mapa de parametros (key,value) a partir de un contenido html.
	  * Escanea el cuerpo del contenido y devuelve el valor de los input hidden.
	  * 	 
	  * @param responseBodyAsString el contenido html que se desea parsear
	  * @return los inputs hidden con sus valores
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
//					 System.out.println( " leyendo linea en response "+ line);

					 Matcher mname = namepatter.matcher(line);

					 if (mname.find()) {
						 String name = mname.group(1);
//						 System.out.println( " encontrado parametro en response =" + name + " -> en linea"+ line);
//						 SafeHtmlUtils.fromString(s);

						 
						 Matcher mvalue = valuepattern.matcher(line);

						 if (mvalue.find()) {
							 String value = mvalue.group(1);
//							 System.out.println( " encontrado value=" + value + " -> en linea"+ line);
//							 System.out.println( " unescaped value=" + unescapeJavaString(value) + " -> en linea"+ line);
							 value=value.replaceAll("&#x3a;",":");
//							 System.out.println( " replaced value=" + value );
//							 System.out.println( "\t\t adding parametro en response =" + name + " -> "+ value);
							 toret.put(name, value);
						 }


						 
					 }
					 
				 }
				 
			 }
			return toret;
		}
	 
	 /**
	  * Devuelve un mapa de opciones (key,value) a partir de un contenido html.
	  * Escanea el cuerpo del contenido y devuelve el valor de los options
	  *  dentro de un select que recibe como parametro.
	  * 	 
	  * @param select name de el select que contiene las opciones
	  * @param responseBodyAsString el contenido html que se desea parsear
	  * @return
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
								System.out.println( " encontrado option. name=" + name + " -> en linea"+ line);
//						 SafeHtmlUtils.fromString(s);

						 
						 Matcher mvalue = valuepattern.matcher(line);

						 if (mvalue.find()) {
							 String value = mvalue.group(1);
							 if (debug)
									System.out.println( " encontrado value=" + value + " -> en linea"+ line);
//							 System.out.println( " unescaped value=" + unescapeJavaString(value) + " -> en linea"+ line);
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
	  * Devuelve si existe algún input con name="parameter"
	  * @param parameter nombre del parametro
	  * @param responseBodyAsString contenido html donde buscar
	  * @return si se el cuerpo contiene tal parametro
	  */
	 private static boolean parameterInResponseBody( String parameter, String responseBodyAsString) 
	 {
		 
			 Pattern namepatter = Pattern.compile("name=\""+parameter+"\"");
//			 Pattern valuepattern = Pattern.compile("value=\"(.*)\"");

			 
			 String[] lines = responseBodyAsString.split(System.getProperty("line.separator"));
			 for (String line: lines)
			 {
				 if (line.contains("<input") )
				 {
					 Matcher mname = namepatter.matcher(line);
					 if (mname.find()) {
//						 String name = mname.group(1);
						 if (debug)
								System.out.println( " encontrado parametro!=" + parameter + " -> en linea"+ line);
						 return true;
					 }
				 }
			 }
			 return false;
		}
	 
	 

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
//					 System.out.println( " encontrado action=" + action + " -> en linea"+ line);
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
	 



	private static class DefaultTrustManager implements X509TrustManager {

	        @Override
	        public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

	        @Override
	        public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {}

	        @Override
	        public X509Certificate[] getAcceptedIssuers() {
	            return null;
	        }
	    }

	private static class ResponseFromExecMethod
	{
		private Integer status;
		private HttpMethod method;
		private String redirectLocation;
		private Header cookies;
		private String responseBody;
		
		private Integer nivel;

		public ResponseFromExecMethod(Integer nivel) {
			super();
			this.nivel = nivel;
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
				toret+="\t"+nivel+"->"+"status: " + status+"\n";
			if (redirectLocation!=null)
				toret+="\t"+nivel+"->"+"redirect_to: " + redirectLocation+"\n";
			if (cookies!=null)
				toret+="\t"+nivel+"->"+"cookies: " + cookies+"\n";
			if (responseBody!=null)
				toret+="\t"+nivel+"->"+"contents: " + responseBody+"\n";
			toret+="-----------------------\n";


			return toret;

		}


	}

}