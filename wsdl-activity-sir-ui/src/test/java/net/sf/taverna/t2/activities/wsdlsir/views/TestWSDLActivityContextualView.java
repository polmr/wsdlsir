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
package net.sf.taverna.t2.activities.wsdlsir.views;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;

import net.sf.taverna.t2.activities.wsdlsir.WSDLActivity;
import net.sf.taverna.t2.activities.wsdlsir.WSDLActivityConfigurationBean;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactory;
import net.sf.taverna.t2.workbench.ui.views.contextualviews.activity.ContextualViewFactoryRegistry;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;

import org.junit.Before;
import org.junit.Test;

public class TestWSDLActivityContextualView {

	Activity<?> a;
	
	@Before
	public void setUp() throws Exception {
		a=new WSDLActivity();
		WSDLActivityConfigurationBean b=new WSDLActivityConfigurationBean();
		b.setOperation("getReport");
		String wsdlUrl=TestWSDLActivityContextualView.class.getResource("/GMService.wsdl").toExternalForm();
		b.setWsdl(wsdlUrl);
		((WSDLActivity)a).configure(b);
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testDisovery() throws Exception {
		List<ContextualViewFactory> viewFactoriesForBeanType = ContextualViewFactoryRegistry.getInstance().getViewFactoriesForObject(a);
		assertTrue("The WSDL view factory should not be empty", !viewFactoriesForBeanType.isEmpty());
		WSDLActivityViewFactory factory = null;
		for (ContextualViewFactory cvf : viewFactoriesForBeanType) {
			if (cvf instanceof WSDLActivityViewFactory) {
				factory = (WSDLActivityViewFactory) cvf;
			}
		}
		assertTrue("No WSDL view factory", factory != null);
	}
	
	public void testConfigurationAction() {
		WSDLActivityContextualView view = new WSDLActivityContextualView(a);
		assertNull("WSDL has no configure action, so should be null",view.getConfigureAction(null));
	}
}
