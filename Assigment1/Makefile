

all:
	java -jar jflex-1.6.1.jar analyzer.flex
	@echo "==================================="
	javac Lexer.java

test:
	java Lexer test_input_file

clean:
	rm Lexer.java

zip: all
	zip compiler_project_01.zip analyzer.flex 
