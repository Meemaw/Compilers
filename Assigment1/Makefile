# TODO subroutines for compiling

.PHONY: all test tests clean zip jar analyzer parser

all: analyzer parser
	jar cvfm Parser.jar META-INF/manifest.Parser parser/*.class parser/Compiler.class analyzer/*.class analyzer/analyzer.flex

test:
	java -jar Parser.jar "tests/Parser_tests/test.decaf"
	java -jar Parser.jar "tests/Parser_tests/algebra.decaf"
	java -jar Parser.jar "tests/Parser_tests/err.decaf"

tests:
	@./runtest.sh Parser.jar

clean:
	rm -f analyzer/*.class analyzer/Lexer.java* parser/*.class SourceCode.Zip LexAnal.jar Parser.jar tests/*.real

zip: all
	zip SourceCode.Zip META-INF/* analyzer/analyzer.flex analyzer/*.java *.java jflex-1.6.1.jar Parser.jar Makefile AUTHORS grammar.pdf

analyzer:
	java -jar jflex-1.6.1.jar analyzer/analyzer.flex
	@echo "==================================="
	javac analyzer/Lexer.java
	#javac LexAnal.java
	#jar cvfm LexAnal.jar META-INF/manifest.LexAnal *.class analyzer/*.class analyzer/analyzer.flex

parser:
	javac parser/*.java