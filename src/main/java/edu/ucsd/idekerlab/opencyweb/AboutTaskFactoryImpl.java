package edu.ucsd.idekerlab.opencyweb;

import java.awt.Image;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Properties;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JOptionPane;
import edu.ucsd.idekerlab.opencyweb.util.JEditorPaneFactory;
import edu.ucsd.idekerlab.opencyweb.util.Constants;
import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.NetworkTaskFactory;
import org.cytoscape.work.TaskIterator;

/**
 * A Dummy task that just displays a dialog describing the Community Detection
 * Application
 * @author churas
 */
public class AboutTaskFactoryImpl implements NetworkTaskFactory {

    private CySwingApplication _swingApplication;
    private JEditorPaneFactory _editorPaneFactory;
	private ShowDialogUtil _dialogUtil;
    private JEditorPane _editorPane;
    private ImageIcon _aboutIcon;
    
    public AboutTaskFactoryImpl(CySwingApplication swingApplication,
	    JEditorPaneFactory editorPaneFactory, ShowDialogUtil dialogUtil){
		_swingApplication = swingApplication;
		_editorPaneFactory = editorPaneFactory;
		_dialogUtil = dialogUtil;
    }
    
    private void createAboutEditorPaneIfNeeded(){
	if (_editorPane != null){
	    return;
	}
	
	loadImageIcon();
	
	StringBuilder sb = new StringBuilder();
	String version = "Unknown";
	Properties properties = new Properties();
	try {
	    properties.load(getClass().getClassLoader().getResourceAsStream(Constants.PROP_NAME + ".props"));
	    version = properties.getProperty(Constants.PROJECT_VERSION_PROP, version);
	} catch(IOException | NullPointerException ioex){
	    
	}
	
	sb.append("Open in Cytoscape Web (");
	sb.append(version);
	sb.append(") Opens ");
	sb.append("current network in Cytoscape Web<br/>");
	sb.append("https://web.cytoscape.org<br/>");
	sb.append("");
	
	_editorPane = _editorPaneFactory.getDescriptionFrame(sb.toString());
    }

    private void loadImageIcon(){
	    try {
		File imgFile = File.createTempFile("about_icon", "png");
	        InputStream imgStream = getClass().getClassLoader().getResourceAsStream("about_icon.png");
		Files.copy(imgStream, imgFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		_aboutIcon = new ImageIcon(new ImageIcon(imgFile.getPath()).getImage().getScaledInstance(40, 40, Image.SCALE_SMOOTH));
	    }
	    catch (IOException ex){
		    
	    }
	}
    
    /**
     * Brings up an about dialog and then calls a dummy task 
     * that does nothing
     * @param network
     * @return TaskIterator with a dummy task
     */
    @Override
    public TaskIterator createTaskIterator(CyNetwork network) {
		createAboutEditorPaneIfNeeded();
		_dialogUtil.showMessageDialog(_swingApplication.getJFrame(),
			_editorPane, Constants.APP_NAME,
			JOptionPane.INFORMATION_MESSAGE, _aboutIcon);
		return new TaskIterator(new DoNothingTask());
    }

    /**
     * This is always ready
     * @param network
     * @return true
     */
    @Override
    public boolean isReady(CyNetwork network) {
	return true;
    }
    
}
