

.PHONY: all test tests clean zip jar

all:
	java -jar jflex-1.6.1.jar analyzer/analyzer.flex
	@echo "==================================="
	javac analyzer/Lexer.java
	javac TokenDumper.java
	jar cfMv LexAnal.jar *.class analyzer/*.class analyzer/analyzer.flex META-INF/

test:
	java -jar LexAnal.jar "test.decaf"

tests:
	@./runtest.sh LexAnal.jar

clean:
	rm -f analyzer/*.class analyzer/Lexer.java* *.class SourceCode.Zip LexAnal.jar tests/*.real

zip: all
	zip SourceCode.Zip META-INF/ analyzer/analyzer.flex analyzer/*.java TokenDumper.java jflex-1.6.1.jar LexAnal.jar Makefile test.decaf

