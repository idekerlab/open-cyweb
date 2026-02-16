package edu.ucsd.idekerlab.opencyweb;

import java.awt.Desktop;
import java.util.Collection;
import java.util.Properties;

import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * Implementation of {@link NetworkViewTaskFactory} and {@link AbstractNodeViewTaskFactory} to send
 * members of specified node to iQuery
 */
public class OpenInCytoscapeWebTaskFactoryImpl extends AbstractTaskFactory
        implements NetworkCollectionTaskFactory {

    // Cytoscape Web URL template - placeholders are substituted at runtime from app properties
    private static final String CYTOSCAPE_WEB_URL_TEMPLATE =
            "${cytoscape_web_base_url}?import=http://localhost:${cyrest_port}/v1/networks/${network_suid}.cx?version=2";

    // Property keys (short names, scoped under the "opencyweb" CyProperty group)
    static final String PROP_CYREST_PORT = "cyrest.port";
    static final String PROP_CYTOSCAPE_WEB_BASE_URL = "cytoscapeweb.baseurl";

    // Default values matching opencyweb.props defaults
    private static final String DEFAULT_CYREST_PORT = "1234";
    private static final String DEFAULT_CYTOSCAPE_WEB_BASE_URL = "https://web.cytoscape.org";

    private CyApplicationManager appManager;
    private ShowDialogUtil dialogUtil;
    private CySwingApplication swingApplication;
    private final CyProperty<Properties> cyProperties;

    /**
     * Constructor for OpenInCytoscapeWebTaskFactoryImpl
     *
     * @param appManager Cytoscape application manager
     * @param swingApplication Cytoscape Swing application
     * @param dialogUtil Utility for showing dialogs
     * @param cyProperties App properties from opencyweb.props (editable via Edit > Preferences)
     */
    public OpenInCytoscapeWebTaskFactoryImpl(
            final CyApplicationManager appManager,
            CySwingApplication swingApplication,
            ShowDialogUtil dialogUtil,
            CyProperty<Properties> cyProperties) {
        this.appManager = appManager;
        this.swingApplication = swingApplication;
        this.dialogUtil = dialogUtil;
        this.cyProperties = cyProperties;
    }

    @Override
    public boolean isReady() {
        return appManager.getCurrentNetwork() != null;
    }

    @Override
    public TaskIterator createTaskIterator() {
        return this.createTaskIterator(appManager.getCurrentNetworkView());
    }

    @Override
    public boolean isReady(Collection<CyNetwork> clctn) {
        return appManager.getCurrentNetwork() != null && clctn.size() == 1;
    }

    @Override
    public TaskIterator createTaskIterator(Collection<CyNetwork> clctn) {
        return this.createTaskIterator(appManager.getCurrentNetworkView());
    }

    /**
     * Creates a TaskIterator for opening the current network in Cytoscape Web. The URL is
     * constructed using the network's SUID and the predefined URL template. If the URL is
     * malformed, an error dialog will be shown.
     *
     * @param networkView The CyNetworkView for which to create the task
     * @return TaskIterator containing the DoTask to open the network in Cytoscape Web
     */
    public TaskIterator createTaskIterator(CyNetworkView networkView) {
        DoTask doTask =
                new DoTask(
                        swingApplication,
                        dialogUtil,
                        Desktop.getDesktop(),
                        networkView.getModel(),
                        buildCytoscapeWebURI(networkView.getModel().getSUID()));
        return new TaskIterator(doTask);
    }

    String buildCytoscapeWebURI(Long networkSuid) {
        Properties props = cyProperties.getProperties();
        String cyrestPort = props.getProperty(PROP_CYREST_PORT, DEFAULT_CYREST_PORT);
        String baseUrl =
                props.getProperty(PROP_CYTOSCAPE_WEB_BASE_URL, DEFAULT_CYTOSCAPE_WEB_BASE_URL);

        return CYTOSCAPE_WEB_URL_TEMPLATE
                .replace("${cytoscape_web_base_url}", baseUrl)
                .replace("${cyrest_port}", cyrestPort)
                .replace("${network_suid}", networkSuid.toString());
    }
}
