package net.sf.taverna.t2.activities.wsdlsir.menu;

import java.io.IOException;
import java.util.Map;

import javax.swing.Action;

import net.sf.taverna.t2.activities.wsdlsir.InputPortTypeDescriptorActivity;
import net.sf.taverna.t2.activities.wsdlsir.WSDLActivity;
import net.sf.taverna.t2.activities.wsdlsir.actions.AbstractAddXMLSplitterAction;
import net.sf.taverna.t2.activities.wsdlsir.actions.AddXMLInputSplitterAction;
import net.sf.taverna.t2.workbench.activitytools.AbstractConfigureActivityMenuAction;
import net.sf.taverna.t2.workflowmodel.processor.activity.Activity;
import net.sf.taverna.t2.activities.wsdlsir.InputPortTypeDescriptorActivity;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.parser.UnknownOperationException;

public class AddXMLInputSplitterForWSDLActivityMenuAction extends
			AddXMLInputSplitterMenuAction<WSDLActivity> {

	public AddXMLInputSplitterForWSDLActivityMenuAction() {
		super(WSDLActivity.class);
	}

}
