package net.sf.taverna.t2.activities.wsdlsir;
import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Cookie;

/**
 * This extends BasicHandler and is used to append cookies (concretelly the shibsession cookie) to a service invocation (a call)
 * @author pol
 *
 */
public class HandlerConCookies extends BasicHandler {
	/**
	 * We use a flag to determine whether to write out some debug information.
	 * The user can use "-DDEBUG=true" as java option to enable debuggin 
	 */
	public static final boolean debug = Boolean.getBoolean("DEBUG");
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Cookies to be appended
	 */
	private Cookie[] cookies;
	
    @Override
    public void init() {
        System.out.println("init called");
        super.init();
        System.out.println("init called");
    }

    @Override
    public void cleanup() {
        super.cleanup();
        System.out.println("cleanup called");
    }

    /**
     * Previous to the invokation the shibsession cookie is appended. 
     * Although the handler keeps all the cookies, we just append the shibbsession one. 
     */
    @Override
    public void invoke(MessageContext mc) throws AxisFault {
    	if (debug)
    		System.out.println("HANDLERCOOKIES: invoke called");
        
        mc.setMaintainSession(true); // enable axis session
        javax.xml.soap.MimeHeaders hd = mc.getCurrentMessage().getMimeHeaders();
        
	       for (int i = 0; i < cookies.length; i++) 
	       {
		       if (cookies[i] != null) 
		       {
		    	   if (cookies[i].getName().contains("shibsession"))
		    	   {
		    		   hd.addHeader(HTTPConstants.HEADER_COOKIE,  cookies[i].getName() + "=" + cookies[i].getValue() );
		    		   if (debug)
		    			   System.out.println("HANDLERCOOKIES:  appending cookie in hd: "+HTTPConstants.HEADER_COOKIE+"-> "+ cookies[i]);
		    	   }
		       }
	       }
	       if (debug)
	    	   System.out.println("HANDLERCOOKIES: getting out of invoked");

    }

	public void setCookies(Cookie[] cookies) {
		this.cookies = cookies;
		
	}
}