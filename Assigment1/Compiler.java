import java.io.*;
import java.util.ArrayList;
import analyzer.*;
import java.nio.file.*;
import java.util.List;
import java.nio.charset.Charset;

public class Compiler {

	public static void main(String[] args) throws Exception {
		if(args.length == 0) {
			System.out.println("Please provide a filename!");
			return;
		}

		String fileName = args[0];
		Path filePath = Paths.get(fileName);
		List<String> lines = Files.readAllLines(filePath, Charset.forName("UTF-8"));


		Lexer lexer = new Lexer(new FileReader(fileName));
		SymbolTable table = new SymbolTable();


		Parser p = new Parser(lexer,table, lines);

		ArrayList<ParseError> error_list = p.parse();


		if (error_list.isEmpty()) {
			System.out.println("No errors\n");
			return;
		}


		// print errors
		for(int i = 0; i < error_list.size(); i++) {
			ParseError current = error_list.get(i);
			int lineNumber = current.getToken().getLine();
			int columnNumber = current.getToken().getColumn();
			String line = lines.get(lineNumber);
			String leftRemoved = line.replaceAll("^\\s+", "");
			int diff = line.length() - leftRemoved.length();


			System.out.println(lineOutput(lineNumber, leftRemoved, 4));
			System.out.println(messageOutput(current.getMessage(), columnNumber, current.getPointAfterToken(), leftRemoved, diff, current.getToken()));
		}

		System.out.println("Number of errors: " +error_list.size());

	}

	private static String lineOutput(int lineNumber, String line, int formatLength) {
		return String.format("%1$" + formatLength + "d", lineNumber) + " : " + line;
	}

	private static String messageOutput(String message, int columnNumber, boolean pointAfterToken, String line, int diff, Token token) {
		String s = "";
		for(int i = 0; i < 7; i++) s += " "; // default whitespaces

		if(pointAfterToken) 
			for(int i = 0; i < columnNumber - diff + token.getSymbolTableEntry().getLexeme().length(); i++) s+= " "; // go to end of the line
		else 
			for(int i = 0; i < columnNumber - diff; i++) s+= " "; // whitespaces token column

		return s + "^ " + message;
	}


/*
	private String tokenSyntaxError(TokenCode expect) {
		int line = previousToken.getLine();
		String s = lineOutput(line, lines.get(line), 4);
		s += messageOutput("Expected " + expect, 4);
		s += "Actual " + currentToken.getTokenCode();
		return s;
	}

	private String tokenSyntaxError(OpType expect) {
		int line = previousToken.getLine();
		String s = lineOutput(line, lines.get(line), 4);
		s += messageOutput("Expected " + expect, 4);
		s += "Actual " + currentToken.getTokenCode();
		return s;
	}

	private String lineOutput(int lineNumber, String line, int formatLength) {
		return String.format("%1$" + formatLength + "d", lineNumber) + " : " + line + "\n" ;
	}

	private String messageOutput(String message, int formatLength) {
		return String.format("%1$" + formatLength + "s", "^") + " " + message + "\n";
	}*/


}

