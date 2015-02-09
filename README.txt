Plugin for WSDL services protected using SAML WEB SSO Profile
=============================================================

Taverna's WSDL activity is compatible with different security mechanisms (Basic HTTP Auth, WS-Security, ...), but lacks of compatibility with web browser based mechanisms for federation, such as the SAML WEB SSO Profile. 

We have developed a plugin that helps accessing services protected within a federation. Concretely the `SIR federation <http://www.rediris.es/sir/index.html.es>`_.

Notice, that the WSDL describing the service need to be accessible without any security mechanisms. If the services provider is protecting it, we would need to get its contents using a browser and save them in a file in order to include it as WSDL location. 

The source code can be found at: ::
	
	https://github.com/polmr/wsdlsir

Requisites
----------

A taverna workbench 2.5 installation is required

Installation
------------

In order to install WSDL SIR Plugin, we proceed this way:

1. Click on Advanced/Updates and plugins

 
2. Click on Find New Plugins 
 
		
3. Add update site 
		 
		
4. We give a name to our new site and the URL:
		
5. Then click on Install
		  

Using Taverna Workbench, we go to the menu and select 

Usage
-----

Once we have the plugin installed, we will be able to import a new kind os service:
 
We select the service and then introduce the URL to the WSDL description of some services that are protected within a federation (with SAML Web SSO profile).
 

Then we import the service into the workflow (as we would normally do with a WSDL service)		

		
Now it is time to configure the security. We right-click onto the imported service and select Configure security... 
		
We select SAML WEB SSO profile authentication and click on the botton to authenticate
		
We will be prompted for the Idp to authenticate against: 
				
We enter our credentials (as we would to using a browser)
		
And then we are ready to go. The authentication is already performed and we would have obtained a cookie that will be stored within our taverna's credential manager. In further invocations of the service, the cookie will be used to authenticate. In case it stops working, please delete the entry in your Credential Manager or re-authenticate repeating the previous proceedings. 

