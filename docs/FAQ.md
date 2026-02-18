# Open in Cytoscape Web - FAQ

### How do I open a network in Cytoscape Web?

Right-click on a network in the **Network** panel and select **Open in Cytoscape Web**, or click the toolbar button. Your default browser will open with the network loaded.

### How large of a network can I open on web.cytoscape.org?

Cytoscape Web has limits on the size of networks it can render. By default, the app enforces the following thresholds before opening a network:

| Limit | Default | Description |
|-------|---------|-------------|
| Max total elements | 26,000 | Maximum combined number of nodes and edges |
| Max edges | 20,000 | Maximum number of edges |
| Max CX2 file size | 10.000 MB | Maximum file size of the exported network |

These defaults match the current limits defined in Cytoscape Web. If your network exceeds any of these limits, a dialog will appear explaining which threshold was exceeded.

### Can I adjust the network size limits?

Yes. Go to **Edit > Preferences > Properties** and select the **opencyweb** group from the dropdown. You can modify:

- `network.max-elements` - Max total elements (nodes + edges)
- `network.max-edges` - Max edge count
- `network.max-filesize-mb` - Max CX2 export file size in MB (supports decimal values, e.g. `5.500`)

Note that increasing these limits beyond the Cytoscape Web defaults may result in errors or poor performance in the browser.

### Can I change the Cytoscape Web URL?

Yes. In **Edit > Preferences > Properties** under the **opencyweb** group, change the `cytoscapeweb.baseurl` property to your desired URL.

### What CyREST port does the app use?

The app reads the `rest.port` property from the Cytoscape core properties (the **cytoscape3** group in **Edit > Preferences > Properties**). The default is `1234`. This is the same port setting used by all Cytoscape apps that interact with CyREST.

### Why do I see a "network exceeds threshold limits" error?

Your network is larger than what Cytoscape Web can render. The error dialog will tell you which specific limit was exceeded (total elements, edges, or file size) and show the current values vs. the configured maximums. You can adjust the limits in **Edit > Preferences > Properties** under the **opencyweb** group, but be aware that Cytoscape Web may not be able to handle networks beyond its built-in limits.

### Why did the browser not open?

Ensure your operating system has a default browser configured. The app uses Java's `Desktop.browse()` to open the URL, which requires a system default browser to be set.

### Does the app require an internet connection?

Yes. The network data is served locally via CyREST, but Cytoscape Web itself is a web application hosted at `web.cytoscape.org` (by default) and requires internet access to load.
