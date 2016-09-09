

all:
	java -jar jflex-1.6.1.jar analyzer/analyzer.flex
	@echo "==================================="
	javac analyzer/Lexer.java
	javac TokenDumper.java

test:
	java TokenDumper "test_input_file"

tests:
	./runtest.sh LexAnal.jar

clean:
	rm -f analyzer/*.class analyzer/Lexer.java* *.class SourceCode.Zip LexAnal.jar

zip: all
	zip SourceCode.Zip analyzer/*.java analyzer/analyzer.flex jflex-1.6.1.jar TokenDumper.java Makefile

jar:
	jar cfMv LexAnal.jar *.class analyzer/*.class analyzer/analyzer.flex META-INF/