

.PHONY: all test tests clean zip jar

all:
	java -jar jflex-1.6.1.jar analyzer/analyzer.flex
	@echo "==================================="
	javac analyzer/Lexer.java
	#javac LexAnal.java
	#jar cvfm LexAnal.jar META-INF/manifest.LexAnal *.class analyzer/*.class analyzer/analyzer.flex
	javac Compiler.java
	jar cvfm Parser.jar META-INF/manifest.Parser *.class Compiler.class analyzer/*.class analyzer/analyzer.flex

test:
	java -jar Parser.jar "test.decaf"

tests:
	@./runtest.sh Parser.jar

clean:
	rm -f analyzer/*.class analyzer/Lexer.java* *.class SourceCode.Zip LexAnal.jar Parser.jar tests/*.real

zip: all
	zip SourceCode.Zip META-INF/* analyzer/analyzer.flex analyzer/*.java *.java jflex-1.6.1.jar Parser.jar Makefile test.decaf AUTHORS

