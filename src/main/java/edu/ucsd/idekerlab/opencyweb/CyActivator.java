package edu.ucsd.idekerlab.opencyweb;

import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsd.idekerlab.opencyweb.query.OpenInCytoscapeWebTaskFactoryImpl;
import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkCollectionTaskFactory;

import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

public class CyActivator extends AbstractCyActivator {

	private final static Logger LOGGER = LoggerFactory.getLogger(CyActivator.class);

	private BundleContext bundleContext;

	public CyActivator() {
		super();
	}

	@Override
	public void start(BundleContext bc) throws Exception {
		this.bundleContext = bc;
		
		// Register listener for when all apps have finished starting
		// This ensures logging infrastructure is fully initialized in osgi
		// so the initializeApp method can have ability to log messages
		AppsFinishedStartingListener listener = new AppsFinishedStartingListener() {
			@Override
			public void handleEvent(AppsFinishedStartingEvent event) {
				initializeApp();
			}
		};
		
		registerService(bc, listener, AppsFinishedStartingListener.class, new Properties());
	}
	
	private void initializeApp() {
		LOGGER.info("Starting Open in Cytoscape Web App");

		final CySwingApplication swingApplication = getService(bundleContext, CySwingApplication.class);
		final CyApplicationManager appManager = getService(bundleContext, CyApplicationManager.class);

		// sets up the PropertiesHelper and links it to properties that a user can
		// view and edit in Edit => Preferences menu

		ShowDialogUtil dialogUtil = new ShowDialogUtil();

		Properties openMenuProps = new Properties();

		openMenuProps.setProperty(ID, "openInCytoscapeWeb");
		openMenuProps.setProperty(TITLE, "Open in Cytoscape Web");
		openMenuProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
		openMenuProps.setProperty(ENABLE_FOR, "network");
		OpenInCytoscapeWebTaskFactoryImpl openFac = new OpenInCytoscapeWebTaskFactoryImpl(appManager, swingApplication,
				dialogUtil);
		registerService(bundleContext, openFac, NetworkCollectionTaskFactory.class, openMenuProps);
	}

}
