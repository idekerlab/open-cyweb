# Agent Instructions for opencyweb

## Project Overview
Cytoscape desktop app (OSGi bundle) that opens the current network in a Cytoscape Web browser instance. Before opening, the app validates the network against Cytoscape Web's import limits (element count, edge count, and CX2 file size). Java 8 target, built with Maven, packaged as an OSGi bundle via `maven-bundle-plugin`.

## Build Commands
Use the Makefile targets — they wrap Maven:

- `make test` — clean + compile + run tests + spotless check (use this as the primary verification command)
- `make lint` — check formatting only (no compile/test)
- `make lint-fix` — auto-fix all Spotless formatting violations
- `make install` — clean + install JAR to local Maven repo
- `make coverage` — run tests + generate JaCoCo report at `target/site/jacoco/index.html`

## Fixing Spotless Errors
Spotless (Google Java Format, AOSP style) runs during the `test` phase. If the build fails on spotless:check:
1. Run `make lint-fix` (or `mvn spotless:apply`) to auto-fix
2. Re-run `make test` to confirm

Do NOT manually fix formatting — always use `make lint-fix`.

## Project Structure
```
src/main/java/edu/ucsd/idekerlab/opencyweb/
├── CyActivator.java                          # OSGi bundle activator, entry point
├── OpenInCytoscapeWebTaskFactoryImpl.java     # Validation + task creation for opening networks
├── OpenCytoscapeWebToolbar.java               # Toolbar button UI action
├── DoTask.java                                # Task that opens browser with constructed URL
├── CountingOutputStream.java                  # Lightweight OutputStream that counts bytes without storing data
└── util/ShowDialogUtil.java                   # Dialog utility wrapper

src/main/resources/
├── opencyweb.props                            # App properties (defaults, filtered by Maven)
└── images/                                    # Toolbar icons

src/test/java/edu/ucsd/idekerlab/opencyweb/
├── DoTaskTest.java
├── OpenInCytoscapeWebActionTest.java
└── OpenInCytoscapeWebTaskFactoryImplTest.java
```

## Key Patterns

### OSGi Service Registration
`CyActivator` extends `AbstractCyActivator`. Services are registered in `initializeApp()` which is called after `AppsFinishedStartingEvent` to ensure logging infrastructure is ready. Use `getService()` to retrieve and `registerService()`/`registerAllServices()` to register. The activator retrieves `CyNetworkViewWriterManager` for file size measurement and the Cytoscape core `CyProperty` (via `CYTOSCAPE3_PROPERTY_GROUP` constant, filter `"(cyPropertyName=cytoscape3.props)"`) for reading `rest.port`, and passes both to the task factory.

### App Properties (CyProperty)
Properties are managed via the Cytoscape `CyProperty` pattern:
- `PropsReader` inner class in `CyActivator` extends `AbstractConfigDirPropsReader` with `SavePolicy.CONFIG_DIR`
- Defaults are in `src/main/resources/opencyweb.props`
- Registered with `cyPropertyName=opencyweb.props` so they appear under the `opencyweb` group in Edit > Preferences > Properties
- Property keys use short names — the group scope is provided by the CyProperty service name
- Pass `CyProperty<Properties>` references (not raw values) so runtime edits via Preferences take effect immediately

**App Properties (opencyweb group):**

| Key | Default | Description |
|-----|---------|-------------|
| `cytoscapeweb.baseurl` | `https://web.cytoscape.org` | Cytoscape Web base URL |
| `network.max-elements` | `26000` | Max total elements (nodes + edges) allowed |
| `network.max-edges` | `20000` | Max edge count allowed |
| `network.max-filesize-mb` | `10.000` | Max CX2 export file size in MB (supports decimal values) |

**Core Cytoscape Properties (cytoscape3 group, read-only from this app):**

| Key | Default | Description |
|-----|---------|-------------|
| `rest.port` | `1234` | CyREST server port (managed by Cytoscape, not by this app) |

The `network.max-filesize-mb` property is automatically normalized to at least 3 decimal places using `BigDecimal` (e.g. `10` → `10.000`, `.5` → `0.500`). Values with 3+ decimal places are kept as-is.

### Network Validation
`OpenInCytoscapeWebTaskFactoryImpl.validateNetwork()` performs all validation in the factory before task creation. Validation runs in order of cost:
1. **Total elements check** (O(1)) — `nodeCount + edgeCount > max-elements`
2. **Edge count check** (O(1)) — `edgeCount > max-edges`
3. **CX2 file size check** (expensive, gated behind count checks) — serializes the network via `CyNetworkViewWriterManager` using `CountingOutputStream` (counts bytes without heap allocation) and compares against `max-filesize-mb`

If any check fails, an error dialog is shown with specific threshold details and a `NoOpTask` is returned. If the CX2 writer is unavailable (e.g. CX Support app not installed) or serialization fails, the file size check is skipped (fail-open).

### URL Template
`OpenInCytoscapeWebTaskFactoryImpl` builds the Cytoscape Web URL from a template with three placeholders (`${cytoscape_web_base_url}`, `${cyrest_port}`, `${network_suid}`). The base URL is resolved from app properties (opencyweb), the CyREST port from Cytoscape core properties (cytoscape3), and the network SUID from the network at runtime.

### Testing
- JUnit 4 + Mockito 3.2
- Tests are in the same package as source for package-private access
- Mock all Cytoscape services (`CyApplicationManager`, `CySwingApplication`, `CyProperty`, etc.)
- `CyNetworkViewWriterManager` is mocked at the service boundary — the mock `CyWriter` writes a deterministic 5 MB to the `OutputStream` passed by `measureCx2ExportSize`, exercising the full code path (filter lookup → writer creation → serialization → size measurement)
- File size tests reuse a static `FILE_SIZE_FIXTURE` network view and vary the `max-filesize-mb` property threshold to test above/below limits
- `DoTask` tests mock `java.awt.Desktop` to avoid opening real browsers
- Tests require an X11 display because `Desktop.getDesktop()` is called in the production code path. On headless Linux (e.g. CI), use `xvfb-run` to provide a virtual framebuffer (e.g. `xvfb-run make test`)

## Dependencies
- Cytoscape API 3.7.0 (`property-api`, `swing-application-api`, `service-api`, `model-api`, `io-api`, etc.) — all `provided` scope
- `io-api` provides `CyNetworkViewWriterManager`, `CyWriter`, and `CyFileFilter` for file size measurement
- Resource filtering is enabled in `pom.xml` for `src/main/resources` (Maven variable substitution in `.props` files)

## CI
GitHub Actions workflow (`.github/workflows/ci.yml`) runs `xvfb-run make test` on every push and PR to main, using JDK 17 + Temurin. `xvfb-run` is required because tests invoke `Desktop.getDesktop()` which needs an X11 display; `xvfb-run` is pre-installed on `ubuntu-latest` runners.
