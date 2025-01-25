[maven]: http://maven.apache.org/
[java]: https://www.oracle.com/java/index.html
[git]: https://git-scm.com/
[make]: https://www.gnu.org/software/make
[cytoscape]: https://cytoscape.org/

Open in Cytoscape Web
=======================================


Open in Cytoscape Web is a Cytoscape App that opens a
selected network in Cytoscape Web

**NOTE:** This service is experimental. The interface is subject to change.

**Publication**

Coming soon...

Requirements to use
=====================

* [Cytoscape][cytoscape] 3.10 or above
* Internet connection to allow App to connect to remote services



Installation via from Cytoscape
======================================

TODO 

Requirements to build (for developers)
========================================

* [Java][java] 17 with jdk
* [Maven][maven] 3.9 or above

To build documentation

* Make
* Python 3+
* Sphinx (install via `pip install sphinx`)
* Sphinx rtd theme (install via `pip install sphinx_rtd_theme`)


Building manually
====================

Commands below assume [Git][git] command line tools have been installed

```Bash
# Can also just download repo and unzip it
git clone https://github.com/idekerlab/open-cyweb

cd webbymcsearch
mvn clean test install
```

The above command will create a jar file under **target/** named
**open-cyweb\<VERSION\>.jar** that can be installed
into [Cytoscape][cytoscape]


Open Cytoscape and follow instructions <TODO> and click on
**Install from File...** button to load the jar created above.


Building documentation
=========================

Documentation is stored under `docs/` directory and
uses Sphinx & Python to generate documentation that
is auto uploaded from **master** branch to Read the docs TODO

```Bash
# The clone and directory change can be
# omitted if done above
git clone https://github.com/idekerlab/open-cyweb

cd open-cyweb
make docs
```
Once `make docs` is run the documentation should automatically
be displayed in default browser, but if not open `docs/_build/html/index.html` in
a web browser
 
COPYRIGHT AND LICENSE
========================

[Click here](LICENSE)

Acknowledgements
=================

* TODO denote funding sources
