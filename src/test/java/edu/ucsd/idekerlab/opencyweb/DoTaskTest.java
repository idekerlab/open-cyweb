package edu.ucsd.idekerlab.opencyweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Desktop;
import java.net.URI;

import org.junit.Test;

import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

/**
 * @author churas
 */
public class DoTaskTest {

    @Test
    public void testRunSuccess() throws Exception {
        // Set up mocks
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        Desktop mockDesktop = mock(Desktop.class);
        CyNetwork mockNetwork = mock(CyNetwork.class);
        TaskMonitor mockMonitor = mock(TaskMonitor.class);

        // Configure mock behavior
        when(mockNetwork.getSUID()).thenReturn(12345L);

        String validUrl =
                "https://web.cytoscape.org?import=http://localhost:1234/v1/networks/12345.cx?version=2";

        // Create task and run
        DoTask task = new DoTask(mockSwingApp, mockDialogUtil, mockDesktop, mockNetwork, validUrl);
        task.run(mockMonitor);

        // Verify desktop.browse was called with correct URI
        verify(mockDesktop).browse(new URI(validUrl));
        verify(mockDialogUtil, never()).showMessageDialog(any(), anyString());
    }

    @Test
    public void testRunWithInvalidUrl() throws Exception {
        // Set up mocks
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        Desktop mockDesktop = mock(Desktop.class);
        CyNetwork mockNetwork = mock(CyNetwork.class);
        TaskMonitor mockMonitor = mock(TaskMonitor.class);

        // Configure mock behavior
        when(mockNetwork.getSUID()).thenReturn(12345L);

        // Invalid URL with spaces (will trigger URISyntaxException)
        String invalidUrl = "https://invalid url with spaces.com";

        // Create task and run
        DoTask task =
                new DoTask(mockSwingApp, mockDialogUtil, mockDesktop, mockNetwork, invalidUrl);
        task.run(mockMonitor);

        // Verify that dialog was shown with error message (exception was caught internally)
        verify(mockDialogUtil).showMessageDialog(any(), anyString());
        verify(mockDesktop, never()).browse(any(URI.class));
    }
}
