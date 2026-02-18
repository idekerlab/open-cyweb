# Open in Cytoscape Web - User Manual

## Overview

Open in Cytoscape Web is a Cytoscape App that opens the currently selected network in [Cytoscape Web](https://web.cytoscape.org), a browser-based network visualization tool. The app validates the network against configurable size limits before opening, and launches the user's default browser with the network loaded.

## Requirements

- Cytoscape 3.10 or later
- Internet connection
- A default web browser configured on your system

## Installation

1. Open Cytoscape.
2. Go to **Apps > App Manager**.
3. Search for **Open in Cytoscape Web**.
4. Click **Install**.

## Usage

### Opening a Network

With a network loaded in Cytoscape, use either method:

- **Context menu**: Right-click a network in the **Network** panel and select **Open in Cytoscape Web**.
- **Toolbar**: Click the **Open in Cytoscape Web** toolbar button.

The app constructs a URL that points Cytoscape Web to your local CyREST server, then opens it in your default browser. The network data is served locally by CyREST in CX2 format.

### Network Validation

Before opening, the app checks the network against size limits to ensure Cytoscape Web can render it. Checks run in this order:

1. **Total elements** - The combined count of nodes and edges must not exceed `network.max-elements` (default: 26,000).
2. **Edge count** - The number of edges must not exceed `network.max-edges` (default: 20,000).
3. **CX2 file size** - The serialized CX2 export size must not exceed `network.max-filesize-mb` (default: 10.000 MB).

If any check fails, a dialog displays which limit was exceeded along with the actual and maximum values. The network will not be opened in the browser.

The file size check involves serializing the network in-memory and is only performed if the count-based checks pass. If the CX2 writer is unavailable (e.g., CX Support app not installed), the file size check is skipped and the network is allowed through.

## Configuration

App properties are accessible via **Edit > Preferences > Properties** by selecting the **opencyweb** group from the dropdown.

### App Properties (opencyweb)

| Property | Default | Description |
|----------|---------|-------------|
| `cytoscapeweb.baseurl` | `https://web.cytoscape.org` | Base URL of the Cytoscape Web instance |
| `network.max-elements` | `26000` | Max total elements (nodes + edges) |
| `network.max-edges` | `20000` | Max edge count |
| `network.max-filesize-mb` | `10.000` | Max CX2 export file size in MB |

The `network.max-filesize-mb` property supports decimal values (e.g., `5.500`) and is automatically normalized to at least 3 decimal places.

### CyREST Port (cytoscape3)

The app reads the CyREST port from Cytoscape's core properties rather than maintaining its own copy. The `rest.port` property is found under the **cytoscape3** group in **Edit > Preferences > Properties** and defaults to `1234`.

## Reporting Issues

To report a bug or request a feature:

1. Go to https://github.com/idekerlab/open-cyweb/issues
2. Click **New Issue**.
3. Describe the issue with steps to reproduce, if possible.
4. Include your Cytoscape version, app version, and operating system.
