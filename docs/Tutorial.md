# Open in Cytoscape Web - Tutorial

## Introduction

Open in Cytoscape Web is a Cytoscape App that opens the currently selected network in [Cytoscape Web](https://web.cytoscape.org), a browser-based network visualization tool. This tutorial walks through the basic workflow of opening a network from Cytoscape desktop in your web browser.

### Prerequisites

- Cytoscape 3.10 or later installed
- Open in Cytoscape Web app installed
- An active internet connection

## Step 1: Install the App

1. Open Cytoscape.
2. Go to **Apps > App Manager**.
3. Search for **Open in Cytoscape Web** in the list of apps.
4. Click **Install**.

## Step 2: Load a Network

Load any network in Cytoscape. For this tutorial, open one of the built-in sample networks:

1. On the Starter Panel, click on **Affinity Purification** (or any sample network).
2. Wait for the network to load and display in the network view.

If the Starter Panel is not visible, go to **View > Show Starter Panel**.

## Step 3: Open the Network in Cytoscape Web

There are two ways to open the network:

### Option A: Right-Click Context Menu

1. In the **Network** panel (left sidebar), right-click on the network name.
2. Select **Open in Cytoscape Web** from the context menu.

### Option B: Toolbar Button

1. Click the **Open in Cytoscape Web** button in the toolbar.

Your default web browser will open with the network loaded in Cytoscape Web.

## Step 4: View the Network in Cytoscape Web

The network will be rendered in Cytoscape Web at `web.cytoscape.org`. From there you can interact with the network using the browser-based visualization tools.

## Troubleshooting

If the network does not open:

- **Network too large**: If the network exceeds the size limits for web-based rendering, a dialog will explain which limit was exceeded. See the [FAQ](FAQ.md) for details on network size limits.
- **Browser does not open**: Ensure your system has a default browser configured.
- **CyREST not running**: The app requires CyREST to serve the network data. CyREST is included with Cytoscape by default and runs automatically.
