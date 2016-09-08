

all:
	java -jar jflex-1.6.1.jar analyzer/analyzer.flex
	@echo "==================================="
	javac analyzer/Lexer.java

test:
	java analyzer/Lexer test_input_file

clean:
	rm analyzer/Lexer.java* analyzer/Lexer.class

zip: all
	zip compiler_project_01.zip analyzer/*.java analyzer/analyzer.flex jflex-1.6.1.jar Makefile 
