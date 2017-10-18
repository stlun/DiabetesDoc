default: build

.PHONY: build
build:
	@ mvn package

.PHONY: install
install: build
	@cp target/DiabetesDoc*-jar-with-dependencies.jar DiabetesDoc.jar
	@chmod +x DiabetesDoc.jar

.PHONY: clean
clean:
	@ mvn clean

.PHONY: uninstall
uninstall: clean
	@ rm DiabetesDoc.jar

