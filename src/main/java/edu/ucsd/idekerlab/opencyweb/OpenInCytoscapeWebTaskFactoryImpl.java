package edu.ucsd.idekerlab.opencyweb;

import java.awt.Desktop;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JOptionPane;

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

    // Network size limit property keys and defaults
    static final String PROP_MAX_NODES = "network.max-nodes";
    static final String PROP_MAX_EDGES = "network.max-edges";
    private static final int DEFAULT_MAX_NODES = 10000;
    private static final int DEFAULT_MAX_EDGES = 20000;

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
        CyNetwork network = networkView.getModel();

        String validationError = validateNetworkSize(network);
        if (validationError != null) {
            dialogUtil.showMessageDialog(
                    swingApplication.getJFrame(),
                    validationError,
                    "Open in Cytoscape Web",
                    JOptionPane.ERROR_MESSAGE);
            return new TaskIterator();
        }

        DoTask doTask =
                new DoTask(
                        swingApplication,
                        dialogUtil,
                        Desktop.getDesktop(),
                        network,
                        buildCytoscapeWebURI(network.getSUID()));
        return new TaskIterator(doTask);
    }

    /**
     * Validates that the network does not exceed the configured node and edge limits for web-based
     * rendering.
     *
     * @param network The network to validate
     * @return null if the network is within limits, or an error message string if exceeded
     */
    String validateNetworkSize(CyNetwork network) {
        Properties props = cyProperties.getProperties();
        int maxNodes =
                Integer.parseInt(
                        props.getProperty(PROP_MAX_NODES, String.valueOf(DEFAULT_MAX_NODES)));
        int maxEdges =
                Integer.parseInt(
                        props.getProperty(PROP_MAX_EDGES, String.valueOf(DEFAULT_MAX_EDGES)));

        int nodeCount = network.getNodeCount();
        int edgeCount = network.getEdgeCount();

        boolean nodesExceeded = nodeCount > maxNodes;
        boolean edgesExceeded = edgeCount > maxEdges;

        if (!nodesExceeded && !edgesExceeded) {
            return null;
        }

        StringBuilder msg = new StringBuilder();
        msg.append("The selected network exceeds the threshold limits for web-based rendering.");
        if (nodesExceeded) {
            msg.append("\n  Nodes: " + nodeCount + " (max: " + maxNodes + ")");
        }
        if (edgesExceeded) {
            msg.append("\n  Edges: " + edgeCount + " (max: " + maxEdges + ")");
        }
        msg.append(
                "\n\nYou can adjust these limits in Edit > Preferences > Properties (opencyweb).");
        return msg.toString();
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
