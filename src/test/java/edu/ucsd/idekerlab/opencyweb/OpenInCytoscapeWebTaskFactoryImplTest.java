package edu.ucsd.idekerlab.opencyweb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Properties;

import org.junit.Test;

import edu.ucsd.idekerlab.opencyweb.util.ShowDialogUtil;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.property.CyProperty;

public class OpenInCytoscapeWebTaskFactoryImplTest {

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

    private CyNetwork createMockNetwork(int nodeCount, int edgeCount) {
        CyNetwork mockNetwork = mock(CyNetwork.class);
        when(mockNetwork.getNodeCount()).thenReturn(nodeCount);
        when(mockNetwork.getEdgeCount()).thenReturn(edgeCount);
        return mockNetwork;
    }

    @Test
    public void testValidateNetworkSizeWithinLimits() {
        Properties props = new Properties();
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props);

        assertNull(factory.validateNetworkSize(createMockNetwork(100, 200)));
    }

    @Test
    public void testValidateNetworkSizeExceedsMaxNodes() {
        Properties props = new Properties();
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props);

        String error = factory.validateNetworkSize(createMockNetwork(10001, 100));
        assertNotNull(error);
        assertTrue(error.contains("Nodes: 10001"));
        assertTrue(error.contains("max: 10000"));
    }

    @Test
    public void testValidateNetworkSizeExceedsMaxEdges() {
        Properties props = new Properties();
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props);

        String error = factory.validateNetworkSize(createMockNetwork(100, 20001));
        assertNotNull(error);
        assertTrue(error.contains("Edges: 20001"));
        assertTrue(error.contains("max: 20000"));
    }

    @Test
    public void testValidateNetworkSizeExceedsBoth() {
        Properties props = new Properties();
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props);

        String error = factory.validateNetworkSize(createMockNetwork(10001, 20001));
        assertNotNull(error);
        assertTrue(error.contains("Nodes: 10001"));
        assertTrue(error.contains("Edges: 20001"));
    }

    @Test
    public void testValidateNetworkSizeWithCustomLimits() {
        Properties props = new Properties();
        props.setProperty("network.max-nodes", "500");
        props.setProperty("network.max-edges", "1000");
        OpenInCytoscapeWebTaskFactoryImpl factory = createFactory(props);

        // Within custom limits
        assertNull(factory.validateNetworkSize(createMockNetwork(500, 1000)));

        // Exceeds custom limits
        String error = factory.validateNetworkSize(createMockNetwork(501, 1001));
        assertNotNull(error);
        assertTrue(error.contains("max: 500"));
        assertTrue(error.contains("max: 1000"));
    }
}
