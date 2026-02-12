package edu.ucsd.idekerlab.opencyweb.query;

import java.util.Collection;

import edu.ucsd.idekerlab.opencyweb.DoTask;
import edu.ucsd.idekerlab.opencyweb.util.DesktopUtil;
import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
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

    public static final int MAX_RAW_DATA_LEN = 20;
    public static final int MAX_RAW_COL_LEN = 20;

    // Cytoscape Web URL template - ${network_suid} is placeholder for network SUID
    // TODO - refactor this to externalized property
    private static final String CYTOSCAPE_WEB_URL_TEMPLATE =
            "https://web.cytoscape.org?import=http://localhost:1234/v1/networks/${network_suid}.cx?version=2";
    private CyApplicationManager appManager;
    private DoTask doTask;
    private DesktopUtil deskTopUtil;
    private ShowDialogUtil dialogUtil;
    private CySwingApplication swingApplication;

    /**
     * Constructor for OpenInCytoscapeWebTaskFactoryImpl
     *
     * @param appManager Cytoscape application manager
     * @param swingApplication Cytoscape Swing application
     * @param dialogUtil Utility for showing dialogs
     */
    public OpenInCytoscapeWebTaskFactoryImpl(
            final CyApplicationManager appManager,
            CySwingApplication swingApplication,
            ShowDialogUtil dialogUtil) {
        this.appManager = appManager;
        this.swingApplication = swingApplication;
        this.dialogUtil = dialogUtil;
        deskTopUtil = new DesktopUtil();
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

    protected void setAlternateDesktopUtil(DesktopUtil deskTopUtil) {
        this.deskTopUtil = deskTopUtil;
    }

    public TaskIterator createTaskIterator(CyNetworkView networkView) {
        doTask =
                new DoTask(
                        swingApplication,
                        dialogUtil,
                        deskTopUtil,
                        networkView.getModel(),
                        buildCytoscapeWebURI(appManager.getCurrentNetwork().getSUID()));
        return new TaskIterator(doTask);
    }

    private String buildCytoscapeWebURI(Long networkSuid) {
        return CYTOSCAPE_WEB_URL_TEMPLATE.replace("${network_suid}", networkSuid.toString());
    }
}
