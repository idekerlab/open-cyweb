package edu.ucsd.idekerlab.opencyweb;

import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import edu.ucsd.idekerlab.opencyweb.query.OpenInCytoscapeWebTaskFactoryImpl;
import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import org.cytoscape.work.TaskFactory;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyActivator extends AbstractCyActivator {
	

	private final static Logger LOGGER = LoggerFactory.getLogger(CyActivator.class);
	
		private static CyProperty<Properties> cyProps;

	
	public CyActivator() {
		super();
	}

	
	@Override
	public void start(BundleContext bc) throws Exception {

		final CySwingApplication swingApplication = getService(bc, CySwingApplication.class);
		final CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
		
		// sets up the PropertiesHelper and links it to properties that a user can
		// view and edit in Edit => Preferences menu
	
		cyProps = getService(bc, CyProperty.class, "(cyPropertyName=cytoscape3.props)");
		
		ShowDialogUtil dialogUtil = new ShowDialogUtil();

			
		Properties openMenuProps = new Properties();
		
		openMenuProps.setProperty(ID, "openInCytoscapeWeb");
		openMenuProps.setProperty(TITLE, "Open in Cytoscape Web");
		openMenuProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
		openMenuProps.setProperty(ENABLE_FOR, "network");
		OpenInCytoscapeWebTaskFactoryImpl openFac = new OpenInCytoscapeWebTaskFactoryImpl(appManager, swingApplication, dialogUtil);
		registerAllServices(bc, openFac, openMenuProps);
		

		final Properties networkToCytoWeb = new Properties();

		networkToCytoWeb.setProperty(PREFERRED_MENU, "File.Export");
		networkToCytoWeb.setProperty(MENU_GRAVITY, "0.0");
		networkToCytoWeb.setProperty(TITLE, "Network to Cytoscape Web");
		registerService(bc, openFac, TaskFactory.class, networkToCytoWeb);
		
	}

}
