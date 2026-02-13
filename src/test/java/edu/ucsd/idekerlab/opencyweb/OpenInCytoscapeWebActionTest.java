package edu.ucsd.idekerlab.opencyweb;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.awt.event.ActionEvent;

import org.junit.Test;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.swing.DialogTaskManager;

/** Unit tests for OpenInCytoscapeWebAction. */
public class OpenInCytoscapeWebActionTest {

    @Test
    public void testActionPerformedWithNullCurrentNetworkView() {
        // Set up mocks
        CyApplicationManager mockAppManager = mock(CyApplicationManager.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        OpenInCytoscapeWebTaskFactoryImpl mockTaskFactory =
                mock(OpenInCytoscapeWebTaskFactoryImpl.class);
        DialogTaskManager mockTaskManager = mock(DialogTaskManager.class);
        ActionEvent mockEvent = mock(ActionEvent.class);

        // Configure mock to return null for current network view
        when(mockAppManager.getCurrentNetworkView()).thenReturn(null);

        // Create action
        OpenCytoscapeWebToolbar action =
                new OpenCytoscapeWebToolbar(
                        mockAppManager, mockSwingApp, mockTaskFactory, mockTaskManager);

        // Execute actionPerformed
        action.actionPerformed(mockEvent);

        // Verify that taskFactory was never called since network view was null
        verify(mockTaskFactory, never()).createTaskIterator(any(CyNetworkView.class));
        verify(mockTaskManager, never()).execute(any(TaskIterator.class));
    }

    @Test
    public void testActionPerformedExecutesTask() {
        // Set up mocks
        CyApplicationManager mockAppManager = mock(CyApplicationManager.class);
        CySwingApplication mockSwingApp = mock(CySwingApplication.class);
        OpenInCytoscapeWebTaskFactoryImpl mockTaskFactory =
                mock(OpenInCytoscapeWebTaskFactoryImpl.class);
        DialogTaskManager mockTaskManager = mock(DialogTaskManager.class);
        CyNetworkView mockNetworkView = mock(CyNetworkView.class);
        ActionEvent mockEvent = mock(ActionEvent.class);

        // Create a real TaskIterator (final class, cannot be mocked)
        TaskIterator realTaskIterator = new TaskIterator();

        // Configure mock behavior
        when(mockAppManager.getCurrentNetworkView()).thenReturn(mockNetworkView);
        when(mockTaskFactory.createTaskIterator(mockNetworkView)).thenReturn(realTaskIterator);

        // Create action
        OpenCytoscapeWebToolbar action =
                new OpenCytoscapeWebToolbar(
                        mockAppManager, mockSwingApp, mockTaskFactory, mockTaskManager);

        // Execute actionPerformed
        action.actionPerformed(mockEvent);

        // Verify that task was created and executed
        verify(mockTaskFactory).createTaskIterator(mockNetworkView);
        verify(mockTaskManager).execute(realTaskIterator);
    }
}
