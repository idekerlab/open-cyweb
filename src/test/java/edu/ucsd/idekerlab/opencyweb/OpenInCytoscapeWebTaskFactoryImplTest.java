package edu.ucsd.idekerlab.opencyweb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.swing.JFrame;

import org.junit.Test;

import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;

public class OpenInCytoscapeWebTaskFactoryImplTest {

    @SuppressWarnings("unchecked")
    private OpenInCytoscapeWebTaskFactoryImpl createFactory(
            Properties props, ShowDialogUtil dialogUtil, CySwingApplication swingApp) {
        CyApplicationManager mockAppManager = mock(CyApplicationManager.class);
        CyProperty<Properties> mockCyProps = mock(CyProperty.class);
        when(mockCyProps.getProperties()).thenReturn(props);

        return new OpenInCytoscapeWebTaskFactoryImpl(
                mockAppManager, swingApp, dialogUtil, mockCyProps);
    }

    @SuppressWarnings("unchecked")
    private OpenInCytoscapeWebTaskFactoryImpl createFactory(Properties props) {
        CyApplicationManager mockAppManager = mock(CyApplicationManager.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CyProperty<Properties> mockCyProps = mock(CyProperty.class);
        when(mockCyProps.getProperties()).thenReturn(props);

        return new OpenInCytoscapeWebTaskFactoryImpl(
                mockAppManager, mockSwingApp, mockDialogUtil, mockCyProps);
    }

    private CyNetworkView createMockNetworkView(long suid, int nodeCount, int edgeCount) {
        CyNetwork mockNetwork = mock(CyNetwork.class);
        when(mockNetwork.getSUID()).thenReturn(suid);
        when(mockNetwork.getNodeCount()).thenReturn(nodeCount);
        when(mockNetwork.getEdgeCount()).thenReturn(edgeCount);

        CyNetworkView mockView = mock(CyNetworkView.class);
        when(mockView.getModel()).thenReturn(mockNetwork);
        return mockView;
    }

    // --- URL building tests (existing) ---

    @Test
    public void testBuildCytoscapeWebURIWithDefaults() {
        Properties props = new Properties();
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props);

        String url = factory.buildCytoscapeWebURI(12345L);
        assertEquals(
                "https://web.cytoscape.org?import=http://localhost:1234/v1/networks/12345.cx?version=2",
                url);
    }

    @Test
    public void testBuildCytoscapeWebURIWithCustomProperties() {
        Properties props = new Properties();
        props.setProperty("cyrest.port", "5678");
        props.setProperty("cytoscapeweb.baseurl", "https://custom.example.com");
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props);

        String url = factory.buildCytoscapeWebURI(42L);
        assertEquals(
                "https://custom.example.com?import=http://localhost:5678/v1/networks/42.cx?version=2",
                url);
    }

    @Test
    public void testBuildCytoscapeWebURIWithPartialCustomProperties() {
        Properties props = new Properties();
        props.setProperty("cyrest.port", "9999");
        // cytoscapeweb.baseurl not set - should use default
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props);

        String url = factory.buildCytoscapeWebURI(100L);
        assertEquals(
                "https://web.cytoscape.org?import=http://localhost:9999/v1/networks/100.cx?version=2",
                url);
    }

    // --- Network size validation tests ---

    @Test
    public void testCreateTaskIteratorWithinNetworkSizeLimits() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(new Properties(), mockDialogUtil, mockSwingApp);

        CyNetworkView mockView = createMockNetworkView(1L, 100, 200);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil, never()).showMessageDialog(any(), anyString());
        // Should contain a DoTask (not a NoOpTask)
        Task task = result.next();
        assertEquals(DoTask.class, task.getClass());
    }

    @Test
    public void testCreateTaskIteratorExceedsMaxNodes() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(new Properties(), mockDialogUtil, mockSwingApp);

        CyNetworkView mockView = createMockNetworkView(1L, 10001, 100);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil).showMessageDialog(any(), contains("Nodes: 10001"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 10000"));
        // Should contain a no-op task (not DoTask)
        Task task = result.next();
        assertFalse("Expected no-op task, not DoTask", task instanceof DoTask);
    }

    @Test
    public void testCreateTaskIteratorExceedsMaxEdges() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(new Properties(), mockDialogUtil, mockSwingApp);

        CyNetworkView mockView = createMockNetworkView(1L, 100, 20001);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil).showMessageDialog(any(), contains("Edges: 20001"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 20000"));
        Task task = result.next();
        assertFalse("Expected no-op task, not DoTask", task instanceof DoTask);
    }

    @Test
    public void testCreateTaskIteratorExceedsBothLimits() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(new Properties(), mockDialogUtil, mockSwingApp);

        CyNetworkView mockView = createMockNetworkView(1L, 10001, 20001);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil).showMessageDialog(any(), contains("Nodes: 10001"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("Edges: 20001"));
        Task task = result.next();
        assertFalse("Expected no-op task, not DoTask", task instanceof DoTask);
    }

    @Test
    public void testCreateTaskIteratorWithCustomLimits() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));

        Properties props = new Properties();
        props.setProperty("network.max-nodes", "500");
        props.setProperty("network.max-edges", "1000");
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(props, mockDialogUtil, mockSwingApp);

        // Within custom limits
        CyNetworkView withinLimits = createMockNetworkView(1L, 500, 1000);
        TaskIterator resultOk = factory.createTaskIterator(withinLimits);
        Task taskOk = resultOk.next();
        assertEquals(DoTask.class, taskOk.getClass());

        // Exceeds custom limits
        CyNetworkView exceedsLimits = createMockNetworkView(2L, 501, 1001);
        TaskIterator resultFail = factory.createTaskIterator(exceedsLimits);

        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 500"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 1000"));
        Task taskFail = resultFail.next();
        assertFalse("Expected no-op task, not DoTask", taskFail instanceof DoTask);
    }

    // --- URL validation test ---

    @Test
    public void testCreateTaskIteratorWithInvalidUrl() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));

        Properties props = new Properties();
        props.setProperty("cytoscapeweb.baseurl", "not a valid url");
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(props, mockDialogUtil, mockSwingApp);

        CyNetworkView mockView = createMockNetworkView(1L, 100, 200);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil).showMessageDialog(any(), contains("Invalid URL"));
        Task task = result.next();
        assertFalse("Expected no-op task, not DoTask", task instanceof DoTask);
    }
}
