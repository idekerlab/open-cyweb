package edu.ucsd.idekerlab.opencyweb;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.ucsd.idekerlab.opencyweb.util.DesktopUtil;
import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;

/**
 * Task to open iQuery with current network in Cytoscape Web
 * 
 * @author churas
 */
public class DoTask extends AbstractTask{
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DoTask.class);
	private final CySwingApplication swingApplication;
	private final ShowDialogUtil dialogUtil;
	private DesktopUtil deskTopUtil;
    private CyNetwork network;
    private String cytowebUrl;

    /**
     * Constructor for DoTask   
     * @param swingApplication Cytoscape Swing application
     * @param dialogUtil Utility for showing dialogs
     * @param deskTopUtil Utility for desktop operations
     * @param network The network to be used
     * @param cytowebUrl The URL for Cytoscape Web
     */
    public DoTask(CySwingApplication swingApplication, ShowDialogUtil dialogUtil, DesktopUtil deskTopUtil, CyNetwork network, String cytowebUrl) {
        this.swingApplication = swingApplication;
        this.dialogUtil = dialogUtil;
        this.deskTopUtil = deskTopUtil;
        this.network = network;
        this.cytowebUrl = cytowebUrl;
    }

    @Override
    public void run(TaskMonitor taskMonitor) throws Exception {
        String suidStr = Long.toString(network.getSUID());
        LOGGER.info("Opening Network SUID: " + suidStr);
        runQueryOnWebBrowser(cytowebUrl);    
    }

    @Override
    public void cancel() {
        super.cancel();
    }

    @Override
    public TaskIterator getTaskIterator() {
        return super.getTaskIterator(); 
    }

    private void runQueryOnWebBrowser(String cytowebUrl) throws Exception {
        try {
            LOGGER.info("Opening " + cytowebUrl+ " in default browser");
            deskTopUtil.getDesktop().browse(new URI(cytowebUrl));
        } catch (Exception e) {
            LOGGER.error("Unable to open default browser window to pass terms to iQuery", e);
            dialogUtil.showMessageDialog(swingApplication.getJFrame(),
                    "Default browser window could not be opened. Please copy/paste this link to your browser: " + cytowebUrl);
            throw e;                
        }
    } 
}
