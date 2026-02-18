package edu.ucsd.idekerlab.opencyweb;

import java.awt.Desktop;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 * Implementation of {@link NetworkCollectionTaskFactory} to open the current network in Cytoscape
 * Web. Performs all validation (network element counts, file size, and URL) before task creation so
 * that error dialogs are shown outside the task lifecycle.
 */
public class OpenInCytoscapeWebTaskFactoryImpl extends AbstractTaskFactory
        implements NetworkCollectionTaskFactory {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(OpenInCytoscapeWebTaskFactoryImpl.class);

    // Cytoscape Web URL template - placeholders are substituted at runtime from app properties
    private static final String CYTOSCAPE_WEB_URL_TEMPLATE =
            "${cytoscape_web_base_url}?import=http://localhost:${cyrest_port}/v1/networks/${network_suid}.cx?version=2";

    // Property key for CyREST port (read from Cytoscape core "cytoscape3" properties)
    static final String PROP_CYREST_PORT = "rest.port";
    static final String PROP_CYTOSCAPE_WEB_BASE_URL = "cytoscapeweb.baseurl";

    // Network validation property keys and defaults
    static final String PROP_MAX_ELEMENTS = "network.max-elements";
    static final String PROP_MAX_EDGES = "network.max-edges";
    static final String PROP_MAX_FILESIZE_MB = "network.max-filesize-mb";
    private static final int DEFAULT_MAX_ELEMENTS = 26000;
    private static final int DEFAULT_MAX_EDGES = 20000;
    private static final double DEFAULT_MAX_FILESIZE_MB = 10.0;

    // Default values matching opencyweb.props defaults
    private static final String DEFAULT_CYREST_PORT = "1234";
    private static final String DEFAULT_CYTOSCAPE_WEB_BASE_URL = "https://web.cytoscape.org";

    private final CyApplicationManager appManager;
    private final ShowDialogUtil dialogUtil;
    private final CySwingApplication swingApplication;
    private final CyProperty<Properties> cyProperties;
    private final CyProperty<Properties> coreProperties;
    private final CyNetworkViewWriterManager writerManager;

    /**
     * Constructor for OpenInCytoscapeWebTaskFactoryImpl
     *
     * @param appManager Cytoscape application manager
     * @param swingApplication Cytoscape Swing application
     * @param dialogUtil Utility for showing dialogs
     * @param cyProperties App properties from opencyweb.props (editable via Edit > Preferences)
     * @param coreProperties Cytoscape core properties from cytoscape3.props (provides rest.port)
     * @param writerManager Cytoscape network view writer manager for measuring export size
     */
    public OpenInCytoscapeWebTaskFactoryImpl(
            final CyApplicationManager appManager,
            CySwingApplication swingApplication,
            ShowDialogUtil dialogUtil,
            CyProperty<Properties> cyProperties,
            CyProperty<Properties> coreProperties,
            CyNetworkViewWriterManager writerManager) {
        this.appManager = appManager;
        this.swingApplication = swingApplication;
        this.dialogUtil = dialogUtil;
        this.cyProperties = cyProperties;
        this.coreProperties = coreProperties;
        this.writerManager = writerManager;
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
     * Creates a TaskIterator for opening the current network in Cytoscape Web. Validates network
     * element counts, file size, and URL before creating the task. If validation fails, an error
     * dialog is shown and a no-op task is returned (the framework requires a valid TaskIterator).
     *
     * @param networkView The CyNetworkView for which to create the task
     * @return TaskIterator containing the DoTask to open the network, or a no-op task on validation
     *     failure
     */
    public TaskIterator createTaskIterator(CyNetworkView networkView) {
        CyNetwork network = networkView.getModel();

        // Validate network
        String validationError = validateNetwork(networkView);
        if (validationError != null) {
            dialogUtil.showMessageDialog(swingApplication.getJFrame(), validationError);
            return new TaskIterator(new NoOpTask());
        }

        // Build and validate URL
        String cytowebUrl = buildCytoscapeWebURI(network.getSUID());
        URI uri;
        try {
            uri = new URL(cytowebUrl).toURI();
        } catch (MalformedURLException | URISyntaxException e) {
            LOGGER.error("Invalid Cytoscape Web URL: " + cytowebUrl, e);
            dialogUtil.showMessageDialog(
                    swingApplication.getJFrame(),
                    "Invalid URL: "
                            + cytowebUrl
                            + "\n\nPlease check your settings in"
                            + " Edit > Preferences > Properties (opencyweb).");
            return new TaskIterator(new NoOpTask());
        }

        return new TaskIterator(new DoTask(Desktop.getDesktop(), network, uri));
    }

    /**
     * Validates network against Cytoscape Web thresholds: total elements (nodes+edges), edge count,
     * and CX2 export file size. Count checks run first (cheap, O(1)) to gate the expensive file
     * size check.
     *
     * @param networkView the network view to validate
     * @return error message string if any threshold is exceeded, null if OK
     */
    String validateNetwork(CyNetworkView networkView) {
        CyNetwork network = networkView.getModel();
        Properties props = cyProperties.getProperties();
        int maxElements =
                Integer.parseInt(
                        props.getProperty(PROP_MAX_ELEMENTS, String.valueOf(DEFAULT_MAX_ELEMENTS)));
        int maxEdges =
                Integer.parseInt(
                        props.getProperty(PROP_MAX_EDGES, String.valueOf(DEFAULT_MAX_EDGES)));

        int nodeCount = network.getNodeCount();
        int edgeCount = network.getEdgeCount();
        int elementCount = nodeCount + edgeCount;

        boolean elementsExceeded = elementCount > maxElements;
        boolean edgesExceeded = edgeCount > maxEdges;

        // Check count-based thresholds first (cheap checks)
        if (elementsExceeded || edgesExceeded) {
            StringBuilder msg = new StringBuilder();
            msg.append(
                    "The selected network exceeds the threshold limits for web-based rendering.");
            if (elementsExceeded) {
                msg.append(
                        "\n  Total elements (nodes + edges): "
                                + elementCount
                                + " (max: "
                                + maxElements
                                + ")");
            }
            if (edgesExceeded) {
                msg.append("\n  Edges: " + edgeCount + " (max: " + maxEdges + ")");
            }
            msg.append(
                    "\n\nYou can adjust these limits in"
                            + " Edit > Preferences > Properties (opencyweb).");
            return msg.toString();
        }

        // Check file size threshold (expensive — requires CX2 serialization)
        String rawFileSizeMb =
                props.getProperty(PROP_MAX_FILESIZE_MB, String.valueOf(DEFAULT_MAX_FILESIZE_MB));
        double maxFileSizeMb = Double.parseDouble(rawFileSizeMb);
        normalizeFileSizeProperty(props, rawFileSizeMb);
        long maxFileSizeBytes = (long) (maxFileSizeMb * 1024.0 * 1024.0);
        long exportSize = measureCx2ExportSize(networkView);

        if (exportSize > maxFileSizeBytes) {
            double sizeMb = exportSize / (1024.0 * 1024.0);
            StringBuilder msg = new StringBuilder();
            msg.append(
                    "The selected network exceeds the threshold limits for web-based rendering.");
            msg.append(
                    String.format(
                            "\n  CX2 export size: %.3f MB (max: %.3f MB)", sizeMb, maxFileSizeMb));
            msg.append(
                    "\n\nYou can adjust these limits in"
                            + " Edit > Preferences > Properties (opencyweb).");
            return msg.toString();
        }

        return null;
    }

    /**
     * Measures the CX2 export size of the given network view by serializing through the registered
     * CX writer. Uses {@link CountingOutputStream} to count bytes without storing data on the heap.
     *
     * @param networkView the network view to measure
     * @return the export size in bytes, or -1 if the CX writer is unavailable or serialization
     *     fails (fail-open: validation passes)
     */
    long measureCx2ExportSize(CyNetworkView networkView) {
        try {
            CyFileFilter cxFilter = findCxFileFilter();
            if (cxFilter == null) {
                LOGGER.warn("CX file filter not found — skipping file size check");
                return -1;
            }

            CountingOutputStream countingStream = new CountingOutputStream();
            CyWriter writer = writerManager.getWriter(networkView, cxFilter, countingStream);
            writer.run(null);
            return countingStream.getByteCount();
        } catch (Exception e) {
            LOGGER.warn("Failed to measure CX2 export size: " + e.getMessage(), e);
            return -1;
        }
    }

    /**
     * Ensures the file size property is stored with at least 3 decimal places and a leading digit
     * before the decimal point. Values with fewer than 3 decimal places are reformatted (e.g. "10"
     * → "10.000", ".5" → "0.500"). Values with 3 or more decimal places are kept but ensured to
     * have a leading digit (e.g. ".12345" → "0.12345").
     */
    private void normalizeFileSizeProperty(Properties props, String rawValue) {
        BigDecimal value = new BigDecimal(rawValue);
        if (value.scale() < 3) {
            value = value.setScale(3, RoundingMode.HALF_UP);
        }
        String normalized = value.toPlainString();
        if (!normalized.equals(rawValue)) {
            props.setProperty(PROP_MAX_FILESIZE_MB, normalized);
        }
    }

    private CyFileFilter findCxFileFilter() {
        List<CyFileFilter> filters = writerManager.getAvailableWriterFilters();
        for (CyFileFilter filter : filters) {
            if (filter.getExtensions().contains("cx")) {
                return filter;
            }
        }
        return null;
    }

    String buildCytoscapeWebURI(Long networkSuid) {
        Properties coreProps = coreProperties.getProperties();
        String cyrestPort = coreProps.getProperty(PROP_CYREST_PORT, DEFAULT_CYREST_PORT);
        Properties props = cyProperties.getProperties();
        String baseUrl =
                props.getProperty(PROP_CYTOSCAPE_WEB_BASE_URL, DEFAULT_CYTOSCAPE_WEB_BASE_URL);

        return CYTOSCAPE_WEB_URL_TEMPLATE
                .replace("${cytoscape_web_base_url}", baseUrl)
                .replace("${cyrest_port}", cyrestPort)
                .replace("${network_suid}", networkSuid.toString());
    }

    /** A no-op task returned when validation fails, to satisfy the TaskIterator contract. */
    private static class NoOpTask extends AbstractTask {
        @Override
        public void run(TaskMonitor taskMonitor) {
            // Intentionally empty — validation error was already shown via dialog
        }
    }
}
