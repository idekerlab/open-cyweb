package edu.ucsd.idekerlab.opencyweb;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.AbstractCyAction;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/**
 * Toolbar action to open current network in Cytoscape Web. Enabled only when exactly one network is
 * open.
 */
public class OpenCytoscapeWebToolbar extends AbstractCyAction
        implements NetworkAddedListener, NetworkDestroyedListener {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCytoscapeWebToolbar.class);

    private final CyApplicationManager appManager;
    private final OpenInCytoscapeWebTaskFactoryImpl taskFactory;
    private final DialogTaskManager taskManager;

    public OpenCytoscapeWebToolbar(
            CyApplicationManager appManager,
            CySwingApplication swingApplication,
            OpenInCytoscapeWebTaskFactoryImpl taskFactory,
            DialogTaskManager taskManager) {
        super("Open in Cytoscape Web");

        this.appManager = appManager;
        this.taskFactory = taskFactory;
        this.taskManager = taskManager;

        // Load icons from resources (if available)
        try {
            java.net.URL iconUrl = getClass().getResource("/images/cytoscape-web-icon.png");
            java.net.URL smallIconUrl =
                    getClass().getResource("/images/cytoscape-web-icon-small.png");

            if (iconUrl != null && smallIconUrl != null) {
                ImageIcon icon = new ImageIcon(iconUrl);
                ImageIcon smallIcon = new ImageIcon(smallIconUrl);
                putValue(LARGE_ICON_KEY, icon);
                putValue(SMALL_ICON, smallIcon);
            } else {
                throw new RuntimeException(
                        "Toolbar icons not found in resources/images/. Action will display without custom icons.");
            }
        } catch (Exception ex) {
            LOGGER.warn("Could not load toolbar icons: " + ex.getMessage());
        }

        putValue(SHORT_DESCRIPTION, "Open current network in Cytoscape Web");

        // Set initial enabled state
        updateEnableState();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        CyNetworkView networkView = appManager.getCurrentNetworkView();

        if (networkView == null) {
            // No current network view available
            return;
        }

        LOGGER.info("Opening network in Cytoscape Web from toolbar action");

        // Create and execute task using the existing task factory
        TaskIterator taskIterator = taskFactory.createTaskIterator(networkView);
        taskManager.execute(taskIterator);
    }

    @Override
    public boolean isInToolBar() {
        return true;
    }

    @Override
    public boolean isInMenuBar() {
        return false; // Don't add to menu bar, only toolbar
    }

    /** Update enabled state based on whether exactly one network is available */
    public void updateEnableState() {
        CyNetwork currentNetwork = appManager.getCurrentNetwork();
        setEnabled(currentNetwork != null);
    }

    @Override
    public void handleEvent(NetworkAddedEvent e) {
        updateEnableState();
    }

    @Override
    public void handleEvent(NetworkDestroyedEvent e) {
        updateEnableState();
    }
}
