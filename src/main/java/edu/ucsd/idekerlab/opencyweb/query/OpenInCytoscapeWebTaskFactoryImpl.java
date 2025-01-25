package edu.ucsd.idekerlab.opencyweb.query;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import edu.ucsd.idekerlab.opencyweb.DoNothingTask;
import edu.ucsd.idekerlab.opencyweb.util.DesktopUtil;
import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;
import java.util.Collection;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NetworkCollectionTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link NetworkViewTaskFactory} and
 * {@link AbstractNodeViewTaskFactory} to send members of specified
 * node to iQuery
 *
 */
public class OpenInCytoscapeWebTaskFactoryImpl extends AbstractNodeViewTaskFactory implements NetworkViewTaskFactory, NetworkCollectionTaskFactory {

	public static final int MAX_RAW_DATA_LEN = 20;
	public static final int MAX_RAW_COL_LEN = 20;
	private final static Logger LOGGER = LoggerFactory.getLogger(OpenInCytoscapeWebTaskFactoryImpl.class);
	private final CySwingApplication _swingApplication;
	private final ShowDialogUtil _dialogUtil;
	private DesktopUtil _deskTopUtil;
	private CyApplicationManager _appManager;
 

	public OpenInCytoscapeWebTaskFactoryImpl(final CyApplicationManager appManager, CySwingApplication swingApplication,
			ShowDialogUtil dialogUtil) {
		_appManager = appManager;
		_swingApplication = swingApplication;
		_dialogUtil = dialogUtil;
		_deskTopUtil = new DesktopUtil();

	}
	
	protected void setAlternateDesktopUtil(DesktopUtil deskTopUtil){
		this._deskTopUtil = deskTopUtil;
	}

	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		
        boolean openWebBrowserRes = true;
		CyNetwork network = networkView.getModel();
		String suidStr = Long.toString(network.getSUID());
        LOGGER.info("Network SUID: " + suidStr);
		try {
			openWebBrowserRes = runQueryOnWebBrowser(new URI("https://web.cytoscape.org?import=http://localhost:1234/v1/networks/" + suidStr + ".cx?version=2"));
			if (openWebBrowserRes == false){
				LOGGER.error("Unable to open browser");
			}
			
		} catch(URISyntaxException uEx){
			LOGGER.error("Caught URISyntaxException: " + uEx.getMessage());
		}
		return new TaskIterator(new DoNothingTask());
	}

	private boolean runQueryOnWebBrowser(URI theQueryURI){
		URL theQueryURL = null;
		try {
			theQueryURL = theQueryURI.toURL();
			LOGGER.debug("Opening " + theQueryURL + " in default browser");
			_deskTopUtil.getDesktop().browse(theQueryURI);
		} catch (Exception e) {
			LOGGER.info("Unable to open default browser window to pass terms to iQuery", e);
			_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
					"Default browser window could not be opened. Please copy/paste this link to your browser: "
						+ (theQueryURL == null ? "NA" : theQueryURL));
			return false;
		}
		return true;
	}
	
	/**
	 * Lets caller know if this task can be invoked via 
	 * {@link #createTaskIterator(org.cytoscape.view.model.CyNetworkView) }
	 * @param networkView
	 * @return true if network is in view
	 */
	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (networkView != null && networkView.getModel() != null) {

			return true;
		}
		return false;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return this.createTaskIterator(networkView);
	}

	/**
	 * Just calls {@link #isReady(org.cytoscape.view.model.CyNetworkView) } ignoring
	 * {@code nodeView}
	 * @param nodeView This is ignored
	 * @param networkView
	 * @return See {@link #isReady(org.cytoscape.view.model.CyNetworkView) } for 
	 *         return information
	 */
	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return this.isReady(networkView);
	}

	@Override
	public TaskIterator createTaskIterator(Collection<CyNetwork> clctn) {
		throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
	}

	@Override
	public boolean isReady(Collection<CyNetwork> clctn) {
		return _appManager.getCurrentNetwork() != null && clctn.size() == 1;
	}
	
}
