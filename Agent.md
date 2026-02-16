# Agent Instructions for opencyweb

## Project Overview
Cytoscape desktop app (OSGi bundle) that opens the current network in a Cytoscape Web browser instance. Java 8 target, built with Maven, packaged as an OSGi bundle via `maven-bundle-plugin`.

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
├── OpenInCytoscapeWebTaskFactoryImpl.java     # Creates tasks for opening networks in Cytoscape Web
├── OpenCytoscapeWebToolbar.java               # Toolbar button UI action
├── DoTask.java                                # Task that opens browser with constructed URL
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
`CyActivator` extends `AbstractCyActivator`. Services are registered in `initializeApp()` which is called after `AppsFinishedStartingEvent` to ensure logging infrastructure is ready. Use `getService()` to retrieve and `registerService()`/`registerAllServices()` to register.

### App Properties (CyProperty)
Properties are managed via the Cytoscape `CyProperty` pattern:
- `PropsReader` inner class in `CyActivator` extends `AbstractConfigDirPropsReader` with `SavePolicy.CONFIG_DIR`
- Defaults are in `src/main/resources/opencyweb.props`
- Registered with `cyPropertyName=opencyweb.props` so they appear under the `opencyweb` group in Edit > Preferences > Properties
- Property keys use short names (e.g. `cyrest.port`, `cytoscapeweb.baseurl`) — the group scope is provided by the CyProperty service name
- Pass `CyProperty<Properties>` references (not raw values) so runtime edits via Preferences take effect immediately

### URL Template
`OpenInCytoscapeWebTaskFactoryImpl` builds the Cytoscape Web URL from a template with three placeholders (`${cytoscape_web_base_url}`, `${cyrest_port}`, `${network_suid}`). The first two are resolved from app properties; the third from the network SUID at runtime.

### Testing
- JUnit 4 + Mockito 3.2
- Tests are in the same package as source for package-private access
- Mock all Cytoscape services (`CyApplicationManager`, `CySwingApplication`, `CyProperty`, etc.)
- `DoTask` tests mock `java.awt.Desktop` to avoid opening real browsers

## Dependencies
- Cytoscape API 3.7.0 (`property-api`, `swing-application-api`, `service-api`, `model-api`, etc.) — all `provided` scope
- Resource filtering is enabled in `pom.xml` for `src/main/resources` (Maven variable substitution in `.props` files)

## CI
GitHub Actions workflow (`.github/workflows/ci.yml`) runs `make test` on every push and PR to main, using JDK 17 + Temurin.
