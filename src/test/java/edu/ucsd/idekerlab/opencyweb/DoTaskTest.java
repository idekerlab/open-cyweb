package edu.ucsd.idekerlab.opencyweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;

import org.junit.Test;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;

/**
 * @author churas
 */
public class DoTaskTest {

    private CyNetwork createMockNetwork(long suid) {
        CyNetwork mockNetwork = mock(CyNetwork.class);
        when(mockNetwork.getSUID()).thenReturn(suid);
        return mockNetwork;
    }

    @Test
    public void testRunSuccess() throws Exception {
        Desktop mockDesktop = mock(Desktop.class);
        CyNetwork mockNetwork = createMockNetwork(12345L);
        TaskMonitor mockMonitor = mock(TaskMonitor.class);

        URI uri =
                new URI(
                        "https://web.cytoscape.org?import=http://localhost:1234/v1/networks/12345.cx?version=2");

        DoTask task = new DoTask(mockDesktop, mockNetwork, uri);
        task.run(mockMonitor);

        verify(mockDesktop).browse(uri);
        verify(mockMonitor, never()).showMessage(eq(TaskMonitor.Level.ERROR), anyString());
    }

    @Test
    public void testRunWithBrowserIOException() throws Exception {
        Desktop mockDesktop = mock(Desktop.class);
        CyNetwork mockNetwork = createMockNetwork(12345L);
        TaskMonitor mockMonitor = mock(TaskMonitor.class);

        URI uri = new URI("http://example.com");
        doThrow(new IOException("No browser")).when(mockDesktop).browse(any(URI.class));

        DoTask task = new DoTask(mockDesktop, mockNetwork, uri);
        task.run(mockMonitor);

        verify(mockMonitor)
                .showMessage(eq(TaskMonitor.Level.ERROR), contains("could not be opened"));
    }
}
