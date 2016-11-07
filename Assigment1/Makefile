# TODO subroutines for compiling

.PHONY: all test tests clean zip jar analyzer parser run

all: analyzer parser
	jar cvfm Compiler.jar META-INF/manifest.Compiler parser/*.class parser/Compiler.class analyzer/*.class analyzer/analyzer.flex

test:
	@java -jar Compiler.jar "tests/longTest.decaf"

run:
	@java -jar Compiler.jar tests/longTest.decaf > temp_file
	@java -jar JTacInt.jar temp_file
	@rm temp_file

tests:
	@./runtest.sh Compiler.jar JTacInt.jar

clean:
	rm -f analyzer/*.class analyzer/Lexer.java* parser/*.class SourceCode.Zip LexAnal.jar Compiler.jar tests/*.real tests/official_tests/*.real

zip: all
	zip SourceCode.Zip META-INF/* analyzer/*.java parser/*.java analyzer/analyzer.flex jflex-1.6.1.jar Compiler.jar Makefile AUTHORS grammar.pdf tests/official_tests/*.decaf tests/official_tests/*.correct runtest.sh libtest.sh JTacInt.jar

analyzer:
	java -jar jflex-1.6.1.jar analyzer/analyzer.flex
	@echo "==================================="
	javac analyzer/Lexer.java
	#javac LexAnal.java
	#jar cvfm LexAnal.jar META-INF/manifest.LexAnal *.class analyzer/*.class analyzer/analyzer.flex

parser:
	javac parser/*.java
