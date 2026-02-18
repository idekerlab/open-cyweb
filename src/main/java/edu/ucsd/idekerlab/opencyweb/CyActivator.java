package edu.ucsd.idekerlab.opencyweb;

import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.ID;
import static org.cytoscape.work.ServiceProperties.IN_NETWORK_PANEL_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.app.event.AppsFinishedStartingEvent;
import org.cytoscape.app.event.AppsFinishedStartingListener;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.property.AbstractConfigDirPropsReader;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

public class CyActivator extends AbstractCyActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(CyActivator.class);
    private static final String CYTOSCAPE3_PROPERTY_GROUP = "cytoscape3.props";

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
        AppsFinishedStartingListener listener =
                new AppsFinishedStartingListener() {
                    @Override
                    public void handleEvent(AppsFinishedStartingEvent event) {
                        initializeApp();
                    }
                };

        registerService(bc, listener, AppsFinishedStartingListener.class, new Properties());
    }

    /**
     * Reads app properties from opencyweb.props bundled in the JAR, then merges any user overrides
     * from the Cytoscape config directory. Properties are visible and editable via Edit >
     * Preferences > Properties under the "opencyweb" dropdown.
     */
    private static class PropsReader extends AbstractConfigDirPropsReader {
        PropsReader(String name, String fileName) {
            super(name, fileName, CyProperty.SavePolicy.CONFIG_DIR);
        }
    }

    private void initializeApp() {
        LOGGER.info("Starting Open in Cytoscape Web App");

        final CySwingApplication swingApplication =
                getService(bundleContext, CySwingApplication.class);
        final CyApplicationManager appManager =
                getService(bundleContext, CyApplicationManager.class);
        final DialogTaskManager taskManager = getService(bundleContext, DialogTaskManager.class);
        final CyNetworkViewWriterManager writerManager =
                getService(bundleContext, CyNetworkViewWriterManager.class);

        // Register app properties so users can view and edit in Edit > Preferences > Properties
        PropsReader propsReader = new PropsReader("opencyweb", "opencyweb.props");
        Properties propsReaderServiceProps = new Properties();
        propsReaderServiceProps.setProperty("cyPropertyName", "opencyweb.props");
        registerAllServices(bundleContext, propsReader, propsReaderServiceProps);

        @SuppressWarnings("unchecked")
        CyProperty<Properties> cyProperties =
                getService(bundleContext, CyProperty.class, "(cyPropertyName=opencyweb.props)");

        @SuppressWarnings("unchecked")
        CyProperty<Properties> coreProperties =
                getService(
                        bundleContext,
                        CyProperty.class,
                        "(cyPropertyName=" + CYTOSCAPE3_PROPERTY_GROUP + ")");

        ShowDialogUtil dialogUtil = new ShowDialogUtil();

        // Create task factory for opening networks in Cytoscape Web
        OpenInCytoscapeWebTaskFactoryImpl openFac =
                new OpenInCytoscapeWebTaskFactoryImpl(
                        appManager,
                        swingApplication,
                        dialogUtil,
                        cyProperties,
                        coreProperties,
                        writerManager);

        // Register right-click context menu action
        Properties openMenuProps = new Properties();
        openMenuProps.setProperty(ID, "openInCytoscapeWeb");
        openMenuProps.setProperty(TITLE, "Open in Cytoscape Web");
        openMenuProps.setProperty(IN_NETWORK_PANEL_CONTEXT_MENU, "true");
        openMenuProps.setProperty(ENABLE_FOR, "network");
        registerService(bundleContext, openFac, NetworkCollectionTaskFactory.class, openMenuProps);

        // Create and register toolbar action
        OpenCytoscapeWebToolbar toolbarAction =
                new OpenCytoscapeWebToolbar(appManager, swingApplication, openFac, taskManager);
        registerService(bundleContext, toolbarAction, CyAction.class, new Properties());

        // Register the toolbar action as a network listener to update enabled state
        registerService(bundleContext, toolbarAction, NetworkAddedListener.class, new Properties());
        registerService(
                bundleContext, toolbarAction, NetworkDestroyedListener.class, new Properties());
    }
}
