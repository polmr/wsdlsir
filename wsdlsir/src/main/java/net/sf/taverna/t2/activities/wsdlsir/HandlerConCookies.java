package net.sf.taverna.t2.activities.wsdlsir;
import javax.xml.namespace.QName;

import org.apache.axis.AxisFault;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.BasicHandler;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.commons.httpclient.Cookie;

public class HandlerConCookies extends BasicHandler {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
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

    @Override
    public void invoke(MessageContext mc) throws AxisFault {
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
		    	   System.out.println("HANDLERCOOKIES:  appending cookie en hd: "+HTTPConstants.HEADER_COOKIE+"-> "+ cookies[i]);
		    	   }
		       }
	       }
        
	        System.out.println("HANDLERCOOKIES: getting out of invoked");

    }

//    public QName[] getHeaders() {
//        System.out.println("getHeaders");
//        return new QName[1];
//    }

	public void setCookies(Cookie[] cookies) {
		this.cookies = cookies;
		
	}
}