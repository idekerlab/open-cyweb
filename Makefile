.PHONY: clean test coverage install updateversion docs help
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
	@echo "  updateversion  updates version in pom.xml via maven command"
	@echo "  docs           generate Sphinx HTML documentation (output: docs/_build/html/index.html)"

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

updateversion:
	mvn versions:set

docs:
	$(MAKE) -C docs clean
	$(MAKE) -C docs html
