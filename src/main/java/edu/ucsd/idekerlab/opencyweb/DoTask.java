package edu.ucsd.idekerlab.opencyweb;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

/**
 * Task to open current network in Cytoscape Web. Validation is performed by the factory before this
 * task is created; this task only handles browser launching.
 *
 * @author churas
 */
public class DoTask extends AbstractTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DoTask.class);

    private final Desktop desktop;
    private final CyNetwork network;
    private final URI uri;

    /**
     * Constructor for DoTask
     *
     * @param desktop Desktop instance for browser operations
     * @param network The network to be used
     * @param uri The validated URI for Cytoscape Web
     */
    public DoTask(Desktop desktop, CyNetwork network, URI uri) {
        this.desktop = desktop;
        this.network = network;
        this.uri = uri;
    }

    @Override
    public void run(TaskMonitor taskMonitor) {
        String suidStr = Long.toString(network.getSUID());
        LOGGER.info("Opening Network SUID: " + suidStr);

        try {
            LOGGER.info("Opening " + uri + " in default browser");
            desktop.browse(uri);
        } catch (IOException e) {
            LOGGER.error("Unable to open default browser window", e);
            taskMonitor.showMessage(
                    TaskMonitor.Level.ERROR,
                    "Default browser window could not be opened."
                            + " Please copy/paste this link to your browser: "
                            + uri);
        }
    }
}
