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
package net.sf.taverna.t2.activities.wsdlsir.xmlsplitter;

import java.io.IOException;
import java.io.StringReader;

import java.util.List;

import net.sf.taverna.t2.workflowmodel.health.HealthCheck;
import net.sf.taverna.t2.workflowmodel.health.HealthChecker;
import net.sf.taverna.t2.visit.VisitReport;
import net.sf.taverna.t2.visit.VisitReport.Status;
import net.sf.taverna.wsdl.parser.TypeDescriptor;
import net.sf.taverna.wsdl.xmlsplitter.XMLSplitterSerialisationHelper;

import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

public class XMLOutputSplitterHealthChecker implements HealthChecker<XMLOutputSplitterActivity> {

	public boolean canVisit(Object subject) {
		return subject!=null && subject instanceof XMLOutputSplitterActivity;
	}

	public VisitReport visit(XMLOutputSplitterActivity activity, List<Object> ancestors) {
		String xml = activity.getConfiguration().getWrappedTypeXML();
		Element element;
		try {
			element = new SAXBuilder().build(new StringReader(xml)).getRootElement();
		} catch (JDOMException e) {
		    VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "Error reading the configuration of XML Splitter", HealthCheck.INVALID_CONFIGURATION, Status.SEVERE);
		    report.setProperty("exception", e);
		    return report;
		} catch (IOException e) {
		    VisitReport report = new VisitReport(HealthCheck.getInstance(), activity, "Error reading the configuration of XML Splitter", HealthCheck.INVALID_CONFIGURATION, Status.SEVERE);
		    report.setProperty("exception", e);
		    return report;
		}
		TypeDescriptor typeDescriptor = XMLSplitterSerialisationHelper.extensionXMLToTypeDescriptor(element);
		if (typeDescriptor==null) {
			return new VisitReport(HealthCheck.getInstance(), activity, "Unknown datatype for port", HealthCheck.NULL_DATATYPE, Status.SEVERE);
		}
		else {
			return new VisitReport(HealthCheck.getInstance(), activity, "Recognized datatype", HealthCheck.NO_PROBLEM, Status.OK);
		}
	}

	public boolean isTimeConsuming() {
		return false;
	}

}
