.PHONY: clean test coverage install updateversion docs help
.DEFAULT_GOAL := help

help: ## show this help message
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "%-20s %s\n", $$1, $$2}'

clean: ## run mvn clean
	mvn clean

test: clean ## run tests with mvn test
	mvn test

lint: ## run code linting with Spotless (report: target/site/spotless.html)
	mvn spotless:check	

lint-fix: ## run code linting with Spotless (report: target/site/spotless.html)
	mvn spotless:apply		

coverage: ## check code coverage with jacoco (report: target/site/jacoco/index.html)
	mvn test jacoco:report

install: clean ## install the package to local repo
	mvn install

updateversion: ## updates version in pom.xml via maven command
	mvn versions:set

docs: ## generate Sphinx HTML documentation (output: docs/_build/html/index.html)
	$(MAKE) -C docs clean
	$(MAKE) -C docs html
