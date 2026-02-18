package edu.ucsd.idekerlab.opencyweb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.JFrame;

import org.junit.Test;

import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.write.CyNetworkViewWriterManager;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;

public class OpenInCytoscapeWebTaskFactoryImplTest {

    // Mock CyWriter always writes this many bytes — tests vary the threshold property
    private static final int FIXTURE_EXPORT_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

    // Static test fixture for file-size tests (small counts, passes all count checks)
    private static final CyNetworkView FILE_SIZE_FIXTURE = createMockNetworkView(1L, 100, 200);

    private CyFileFilter mockCxFilter;

    private CyNetworkViewWriterManager createMockWriterManager() {
        mockCxFilter = mock(CyFileFilter.class);
        when(mockCxFilter.getExtensions()).thenReturn(new HashSet<>(Arrays.asList("cx")));

        CyNetworkViewWriterManager mgr = mock(CyNetworkViewWriterManager.class);
        when(mgr.getAvailableWriterFilters()).thenReturn(Arrays.asList(mockCxFilter));

        try {
            when(mgr.getWriter(any(CyNetworkView.class), eq(mockCxFilter), any(OutputStream.class)))
                    .thenAnswer(
                            invocation -> {
                                OutputStream os = invocation.getArgument(2);
                                CyWriter writer = mock(CyWriter.class);
                                doAnswer(
                                                run -> {
                                                    os.write(new byte[FIXTURE_EXPORT_SIZE_BYTES]);
                                                    return null;
                                                })
                                        .when(writer)
                                        .run(any());
                                return writer;
                            });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return mgr;
    }

    private CyNetworkViewWriterManager createEmptyWriterManager() {
        CyNetworkViewWriterManager mgr = mock(CyNetworkViewWriterManager.class);
        when(mgr.getAvailableWriterFilters()).thenReturn(Collections.emptyList());
        return mgr;
    }

    @SuppressWarnings("unchecked")
    private OpenInCytoscapeWebTaskFactoryImpl createFactory(
            Properties props,
            Properties coreProps,
            ShowDialogUtil dialogUtil,
            CySwingApplication swingApp,
            CyNetworkViewWriterManager writerManager) {
        CyApplicationManager mockAppManager = mock(CyApplicationManager.class);
        CyProperty<Properties> mockCyProps = mock(CyProperty.class);
        when(mockCyProps.getProperties()).thenReturn(props);
        CyProperty<Properties> mockCoreCyProps = mock(CyProperty.class);
        when(mockCoreCyProps.getProperties()).thenReturn(coreProps);

        return new OpenInCytoscapeWebTaskFactoryImpl(
                mockAppManager, swingApp, dialogUtil, mockCyProps, mockCoreCyProps, writerManager);
    }

    private OpenInCytoscapeWebTaskFactoryImpl createFactory(
            Properties props, ShowDialogUtil dialogUtil, CySwingApplication swingApp) {
        return createFactory(
                props, new Properties(), dialogUtil, swingApp, createMockWriterManager());
    }

    private OpenInCytoscapeWebTaskFactoryImpl createFactory(
            Properties props,
            ShowDialogUtil dialogUtil,
            CySwingApplication swingApp,
            CyNetworkViewWriterManager writerManager) {
        return createFactory(props, new Properties(), dialogUtil, swingApp, writerManager);
    }

    private OpenInCytoscapeWebTaskFactoryImpl createFactory(
            Properties props, Properties coreProps) {
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        return createFactory(
                props, coreProps, mockDialogUtil, mockSwingApp, createMockWriterManager());
    }

    private OpenInCytoscapeWebTaskFactoryImpl createFactory(Properties props) {
        return createFactory(props, new Properties());
    }

    private static CyNetworkView createMockNetworkView(long suid, int nodeCount, int edgeCount) {
        CyNetwork mockNetwork = mock(CyNetwork.class);
        when(mockNetwork.getSUID()).thenReturn(suid);
        when(mockNetwork.getNodeCount()).thenReturn(nodeCount);
        when(mockNetwork.getEdgeCount()).thenReturn(edgeCount);

        CyNetworkView mockView = mock(CyNetworkView.class);
        when(mockView.getModel()).thenReturn(mockNetwork);
        return mockView;
    }

    // --- URL building tests ---

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
        props.setProperty("cytoscapeweb.baseurl", "https://custom.example.com");
        Properties coreProps = new Properties();
        coreProps.setProperty("rest.port", "5678");
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props, coreProps);

        String url = factory.buildCytoscapeWebURI(42L);
        assertEquals(
                "https://custom.example.com?import=http://localhost:5678/v1/networks/42.cx?version=2",
                url);
    }

    @Test
    public void testBuildCytoscapeWebURIWithCustomPort() {
        Properties props = new Properties();
        Properties coreProps = new Properties();
        coreProps.setProperty("rest.port", "9999");
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props, coreProps);

        String url = factory.buildCytoscapeWebURI(100L);
        assertEquals(
                "https://web.cytoscape.org?import=http://localhost:9999/v1/networks/100.cx?version=2",
                url);
    }

    // --- Network element count validation tests ---

    @Test
    public void testCreateTaskIteratorWithinNetworkLimits() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(new Properties(), mockDialogUtil, mockSwingApp);

        CyNetworkView mockView = createMockNetworkView(1L, 100, 200);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil, never()).showMessageDialog(any(), anyString());
        Task task = result.next();
        assertEquals(DoTask.class, task.getClass());
    }

    @Test
    public void testCreateTaskIteratorExceedsMaxElements() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(new Properties(), mockDialogUtil, mockSwingApp);

        // 14000 nodes + 13000 edges = 27000 total elements > 26000 max, but edges under 20000
        CyNetworkView mockView = createMockNetworkView(1L, 14000, 13000);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil)
                .showMessageDialog(any(), contains("Total elements (nodes + edges): 27000"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 26000"));
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

        // 100 nodes + 20001 edges = 20101 total elements < 26000, but edges > 20000
        CyNetworkView mockView = createMockNetworkView(1L, 100, 20001);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil).showMessageDialog(any(), contains("Edges: 20001"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 20000"));
        Task task = result.next();
        assertFalse("Expected no-op task, not DoTask", task instanceof DoTask);
    }

    @Test
    public void testCreateTaskIteratorExceedsBothElementsAndEdges() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(new Properties(), mockDialogUtil, mockSwingApp);

        // 6001 nodes + 20001 edges = 26002 total > 26000, and edges > 20000
        CyNetworkView mockView = createMockNetworkView(1L, 6001, 20001);
        TaskIterator result = factory.createTaskIterator(mockView);

        verify(mockDialogUtil)
                .showMessageDialog(any(), contains("Total elements (nodes + edges): 26002"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("Edges: 20001"));
        Task task = result.next();
        assertFalse("Expected no-op task, not DoTask", task instanceof DoTask);
    }

    @Test
    public void testCreateTaskIteratorWithCustomElementAndEdgeLimits() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));

        Properties props = new Properties();
        props.setProperty("network.max-elements", "1000");
        props.setProperty("network.max-edges", "500");
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(props, mockDialogUtil, mockSwingApp);

        // Within custom limits: 400 nodes + 500 edges = 900 < 1000, edges 500 <= 500
        CyNetworkView withinLimits = createMockNetworkView(1L, 400, 500);
        TaskIterator resultOk = factory.createTaskIterator(withinLimits);
        Task taskOk = resultOk.next();
        assertEquals(DoTask.class, taskOk.getClass());

        // Exceeds custom limits: 600 + 501 = 1101 > 1000, and edges 501 > 500
        CyNetworkView exceedsLimits = createMockNetworkView(2L, 600, 501);
        TaskIterator resultFail = factory.createTaskIterator(exceedsLimits);

        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 1000"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 500"));
        Task taskFail = resultFail.next();
        assertFalse("Expected no-op task, not DoTask", taskFail instanceof DoTask);
    }

    // --- File size validation tests (all use FILE_SIZE_FIXTURE producing 5 MB) ---

    @Test
    public void testCreateTaskIteratorWithinFileSizeLimit() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        // Default max-filesize-mb=10, fixture produces 5 MB → passes
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(new Properties(), mockDialogUtil, mockSwingApp);

        TaskIterator result = factory.createTaskIterator(FILE_SIZE_FIXTURE);

        verify(mockDialogUtil, never()).showMessageDialog(any(), anyString());
        Task task = result.next();
        assertEquals(DoTask.class, task.getClass());
    }

    @Test
    public void testCreateTaskIteratorExceedsMaxFileSize() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));

        Properties props = new Properties();
        props.setProperty("network.max-filesize-mb", "4"); // 5 MB fixture > 4 MB limit
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(props, mockDialogUtil, mockSwingApp);

        TaskIterator result = factory.createTaskIterator(FILE_SIZE_FIXTURE);

        verify(mockDialogUtil).showMessageDialog(any(), contains("CX2 export size"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 4.000 MB"));
        Task task = result.next();
        assertFalse("Expected no-op task, not DoTask", task instanceof DoTask);
    }

    @Test
    public void testCreateTaskIteratorWithCustomFileSizeLimit() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);

        Properties props = new Properties();
        props.setProperty("network.max-filesize-mb", "6"); // 5 MB fixture < 6 MB limit
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(props, mockDialogUtil, mockSwingApp);

        TaskIterator result = factory.createTaskIterator(FILE_SIZE_FIXTURE);

        verify(mockDialogUtil, never()).showMessageDialog(any(), anyString());
        Task task = result.next();
        assertEquals(DoTask.class, task.getClass());
    }

    @Test
    public void testCreateTaskIteratorExceedsDecimalFileSizeLimit() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        when(mockSwingApp.getJFrame()).thenReturn(mock(JFrame.class));

        Properties props = new Properties();
        // 4.999 MB = 5,241,855 bytes, fixture produces 5,242,880 bytes → exceeds
        props.setProperty("network.max-filesize-mb", "4.999");
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(props, mockDialogUtil, mockSwingApp);

        TaskIterator result = factory.createTaskIterator(FILE_SIZE_FIXTURE);

        verify(mockDialogUtil).showMessageDialog(any(), contains("CX2 export size"));
        verify(mockDialogUtil).showMessageDialog(any(), contains("max: 4.999 MB"));
        Task task = result.next();
        assertFalse("Expected no-op task, not DoTask", task instanceof DoTask);
    }

    @Test
    public void testCreateTaskIteratorWithinDecimalFileSizeLimit() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);

        Properties props = new Properties();
        // 5.001 MB = 5,244,929 bytes, fixture produces 5,242,880 bytes → passes
        props.setProperty("network.max-filesize-mb", "5.001");
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(props, mockDialogUtil, mockSwingApp);

        TaskIterator result = factory.createTaskIterator(FILE_SIZE_FIXTURE);

        verify(mockDialogUtil, never()).showMessageDialog(any(), anyString());
        Task task = result.next();
        assertEquals(DoTask.class, task.getClass());
    }

    @Test
    public void testFileSizePropertyNormalizedToThreeDecimalPlaces() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);

        // Integer value should be normalized to 3 decimal places
        Properties props = new Properties();
        props.setProperty("network.max-filesize-mb", "10");
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(props, mockDialogUtil, mockSwingApp);
        factory.createTaskIterator(FILE_SIZE_FIXTURE);
        assertEquals("10.000", props.getProperty("network.max-filesize-mb"));

        // One decimal place should be normalized
        props.setProperty("network.max-filesize-mb", "4.5");
        factory.createTaskIterator(FILE_SIZE_FIXTURE);
        assertEquals("4.500", props.getProperty("network.max-filesize-mb"));

        // Two decimal places should be normalized
        props.setProperty("network.max-filesize-mb", "10.25");
        factory.createTaskIterator(FILE_SIZE_FIXTURE);
        assertEquals("10.250", props.getProperty("network.max-filesize-mb"));

        // Three decimal places should be kept as-is
        props.setProperty("network.max-filesize-mb", "4.999");
        factory.createTaskIterator(FILE_SIZE_FIXTURE);
        assertEquals("4.999", props.getProperty("network.max-filesize-mb"));

        // More than three decimal places should be kept as-is
        props.setProperty("network.max-filesize-mb", "5.12345");
        factory.createTaskIterator(FILE_SIZE_FIXTURE);
        assertEquals("5.12345", props.getProperty("network.max-filesize-mb"));

        // Sub-1.0 value should have leading zero and 3 decimal places
        props.setProperty("network.max-filesize-mb", "0.5");
        factory.createTaskIterator(FILE_SIZE_FIXTURE);
        assertEquals("0.500", props.getProperty("network.max-filesize-mb"));

        // Missing leading digit should be normalized with leading zero
        props.setProperty("network.max-filesize-mb", ".5");
        factory.createTaskIterator(FILE_SIZE_FIXTURE);
        assertEquals("0.500", props.getProperty("network.max-filesize-mb"));

        // Missing leading digit with 3+ decimals should get leading zero only
        props.setProperty("network.max-filesize-mb", ".12345");
        factory.createTaskIterator(FILE_SIZE_FIXTURE);
        assertEquals("0.12345", props.getProperty("network.max-filesize-mb"));
    }

    @Test
    public void testCreateTaskIteratorFileSizeCheckFailsOpen() {
        ShowDialogUtil mockDialogUtil = mock(ShowDialogUtil.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        // Writer manager with no CX filter → measureCx2ExportSize returns -1 → passes
        OpenInCytoscapeWebTaskFactoryImpl factory =
                createFactory(
                        new Properties(), mockDialogUtil, mockSwingApp, createEmptyWriterManager());

        TaskIterator result = factory.createTaskIterator(FILE_SIZE_FIXTURE);

        verify(mockDialogUtil, never()).showMessageDialog(any(), anyString());
        Task task = result.next();
        assertEquals(DoTask.class, task.getClass());
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
