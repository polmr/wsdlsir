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

.. figure:: images/wsdlplugin/01_updates_plugins.png 
		:scale: 60 %
		:alt: WSDL Plugin installation: Updates and plugins. 
		:align: center
	
		Updates and plugins

2. Click on Find New Plugins 

.. figure:: images/wsdlplugin/02_find_new_plugins.png
		:scale: 60 %
		:alt: WSDL Plugin installation: 01_updates_plugins. 
		:align: center
	
		Find new plugins
		
3. Add update site 
		
.. figure:: images/wsdlplugin/03_add_update_site.png
		:scale: 60 %
		:alt: WSDL Plugin installation: Add update site. 
		:align: center
	
		Add update site
		
4. We give a name to our new site and the URL:

	https://srv-prj-wsamiga-sir.fcsc.es/plugin/
		
.. figure:: images/wsdlplugin/04_add_plugin_site.png
		:scale: 60 %
		:alt: WSDL Plugin installation: Add plugin site. 
		:align: center
	
		Add plugin site
		
5. Then click on Install
		
.. figure:: images/wsdlplugin/05_install_plugin.png 
		:scale: 60 %
		:alt: WSDL Plugin installation: Install the plugin. 
		:align: center
	
		Install the plugin

We will be told to restart taverna. So shall we.


Usage
-----

Once we have the plugin installed, we will be able to import a new kind os service:

.. figure:: images/wsdlplugin/usage/01_import_wsdl_federated_service.png 
		:scale: 60 %
		:alt: WSDL Plugin Usage: Import new service. 
		:align: center
	
		Using the plugin. Import new service. 

We select the service and then introduce the URL to the WSDL description of some services that are protected within a federation (with SAML Web SSO profile).

.. figure:: images/wsdlplugin/usage/02_add_URL.png 
		:scale: 60 %
		:alt: WSDL Plugin Usage: Add the URL of some protectes services
		:align: center
	
		Using the plugin. Add the URL of some protectes services

Then we import the service into the workflow (as we would normally do with a WSDL service)		

.. figure:: images/wsdlplugin/usage/03_add_to_workflow.png
		:scale: 60 %
		:alt: WSDL Plugin Usage: Add service to workflow
		:align: center
	
		Using the plugin. Add service to workflow
		
Now it is time to configure the security. We right-click onto the imported service and select Configure security... 
		
.. figure:: images/wsdlplugin/usage/04_configure_security.png 
		:scale: 60 %
		:alt: WSDL Plugin Usage: Configure security 
		:align: center
	
		Using the plugin. Configure security
		
We select SAML WEB SSO profile authentication and click on the botton to authenticate
		
.. figure:: images/wsdlplugin/usage/05_select_security.png
		:scale: 60 %
		:alt: WSDL Plugin Usage: Select security 
		:align: center
	
		Using the plugin. Select security

We will be prompted for the Idp to authenticate against: 
				
.. figure:: images/wsdlplugin/usage/06_select_idp.png
		:scale: 60 %
		:alt: WSDL Plugin Usage: Select the Identity provider. 
		:align: center
	
		Using the plugin.  Select the Identity provider
		
We enter our credentials (as we would to using a browser)
		
.. figure:: images/wsdlplugin/usage/07_enter_user_pass.png
		:scale: 60 %
		:alt: WSDL Plugin Usage: Enter user credentials. 
		:align: center
	
		Using the plugin. Enter user credentials.
		
And then we are ready to go. The authentication is already performed and we would have obtained a cookie that will be stored within our taverna's credential manager. In further invocations of the service, the cookie will be used to authenticate. In case it stops working, please delete the entry in your Credential Manager or re-authenticate repeating the previous proceedings. 

