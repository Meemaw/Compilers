

all:
	java -jar jflex-1.6.1.jar analyzer/analyzer.flex
	@echo "==================================="
	javac analyzer/Lexer.java
	javac TokenDumper.java

test:
	java TokenDumper "test_input_file"

clean:
	rm analyzer/*.class analyzer/Lexer.java* *.class

zip: all
	zip compiler_project_01.zip analyzer.flex 
