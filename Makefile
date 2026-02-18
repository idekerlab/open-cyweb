.PHONY: clean test coverage install install-release updateversion help
.DEFAULT_GOAL := help

help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@echo "  clean          run mvn clean"
	@echo "  test           run tests with mvn test"
	@echo "  lint           check code formatting with Spotless"
	@echo "  lint-fix       auto-fix Spotless formatting"
	@echo "  coverage       check code coverage with jacoco (report: target/site/jacoco/index.html)"
	@echo "  install        install the package to local repo"
	@echo "  install-release  build and install a release jar with VERSION (e.g. make install-release VERSION=1.2.3)"
	@echo "  updateversion  updates version in pom.xml via maven command"

clean:
	mvn clean

test: clean
	mvn test

lint:
	mvn spotless:check	

lint-fix:
	mvn spotless:apply		

coverage:
	mvn test jacoco:report

install: clean
	mvn install

install-release:
	@if [ -z "$(VERSION)" ]; then echo "Error: VERSION is required. Usage: make install-release VERSION=1.2.3"; exit 1; fi
	mvn clean install -Drevision=$(VERSION)

updateversion:
	mvn versions:set
