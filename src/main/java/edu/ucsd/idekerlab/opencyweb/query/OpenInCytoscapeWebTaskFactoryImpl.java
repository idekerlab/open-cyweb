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
public class OpenInCytoscapeWebTaskFactoryImpl extends AbstractTaskFactory implements NetworkCollectionTaskFactory {

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

	@Override
	public boolean isReady() {
		return _appManager.getCurrentNetwork() != null;
	}
	

	@Override
	public TaskIterator createTaskIterator() {
		return this.createTaskIterator(_appManager.getCurrentNetworkView());
	}

	@Override
	public boolean isReady(Collection<CyNetwork> clctn) {
		return _appManager.getCurrentNetwork() != null && clctn.size() == 1;
	}

	@Override
	public TaskIterator createTaskIterator(Collection<CyNetwork> clctn) {
		return this.createTaskIterator(_appManager.getCurrentNetworkView());
	}
	
}
