package edu.ucsd.idekerlab.opencyweb;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import edu.ucsd.idekerlab.opencyweb.util.JEditorPaneFactoryImpl;
import edu.ucsd.idekerlab.opencyweb.query.OpenInCytoscapeWebTaskFactoryImpl;
import edu.ucsd.idekerlab.opencyweb.util.Constants;
import edu.ucsd.idekerlab.opencyweb.util.IconJLabelDialogFactory;
import edu.ucsd.idekerlab.opencyweb.util.ImageIconHolderFactory;
import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CyActivator extends AbstractCyActivator {

	private final static Logger LOGGER = LoggerFactory.getLogger(CyActivator.class);
	public CyActivator() {
		super();
	}

	
	@Override
	public void start(BundleContext bc) throws Exception {

		final CySwingApplication swingApplication = getService(bc, CySwingApplication.class);
		final CyApplicationManager appManager = getService(bc, CyApplicationManager.class);
		
		// sets up the PropertiesHelper and links it to properties that a user can
		// view and edit in Edit => Preferences menu
	
		
		ShowDialogUtil dialogUtil = new ShowDialogUtil();
		ImageIconHolderFactory iconHolderFactory = new ImageIconHolderFactory();
		JEditorPaneFactoryImpl editorPaneFac = new JEditorPaneFactoryImpl();
		IconJLabelDialogFactory iconJLabelFactory = new IconJLabelDialogFactory(dialogUtil,
				iconHolderFactory, editorPaneFac);
			
		Properties openMenuProps = new Properties();
		openMenuProps.setProperty(PREFERRED_MENU, Constants.CONTEXT_MENU);
		openMenuProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
		openMenuProps.setProperty(TITLE, "Open in Cytoscape Web");
		openMenuProps.put(IN_MENU_BAR, false);
		openMenuProps.put(IN_CONTEXT_MENU, true);
		openMenuProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
		OpenInCytoscapeWebTaskFactoryImpl openFac = new OpenInCytoscapeWebTaskFactoryImpl(appManager, swingApplication, dialogUtil);
		registerAllServices(bc, openFac, openMenuProps);
		
	}

}
