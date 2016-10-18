import analyzer.*;
import java.util.*;
import java.io.IOException;


enum DataType {
    INT, REAL, VOID, NOT_A_TYPE
}


public class Parser{

	private Lexer lexer;
	private SymbolTable symbolTable;
	private ArrayList<ParseError> errorList;
	private Token currentToken;
	private List<String> lines;

	public Parser(Lexer lexer, SymbolTable symbolTable, List<String> lines) {
		this.symbolTable = symbolTable;
		this.lexer = lexer;
		this.errorList =  new ArrayList<>();
		this.lines = lines;
	}

	public ArrayList<ParseError> parse() throws Exception {
		program();

		return errorList;
	}


	private void program() throws Exception {
		next_token();

		expect(TokenCode.CLASS);
		expect(TokenCode.IDENTIFIER);
		expect(TokenCode.LBRACE);

		variable_declarations();
		method_declarations();

		expect(TokenCode.RBRACE);
	}

	private void variable_declarations() throws Exception {
		if (type() != DataType.INT && type() != DataType.REAL)
			return; // epsilon rule

		next_token(); // consume INT | REAL
		variable_list();

		expect(TokenCode.SEMICOLON);

		variable_declarations();
	}

	// return also void as a type
	private DataType type() {
		if (currentToken.getTokenCode() == TokenCode.INT)
			return DataType.INT;
		if (currentToken.getTokenCode() == TokenCode.REAL)
			return DataType.REAL;
		if (currentToken.getTokenCode() == TokenCode.VOID)
			return DataType.VOID;
		return DataType.NOT_A_TYPE;
	}

	// method implement also more_variables rule
	private void variable_list() throws Exception {
		variable();

		if (match(TokenCode.COMMA)) {
			next_token();
			variable_list();
		}
	}

	private void variable() throws Exception {
		expect(TokenCode.IDENTIFIER);


		if (match(TokenCode.LBRACKET)) {
			next_token();
			expect(TokenCode.NUMBER);
			expect(TokenCode.RBRACKET);
		}
	}

	private void method_declarations() throws Exception {
		method_declaration();

		if (match(TokenCode.STATIC)) {
			method_declarations();
		}
	}

	private void method_declaration() throws Exception {
		expect(TokenCode.STATIC);
		if (type() != DataType.NOT_A_TYPE)
			next_token();
		else
			throw new ParseException(tokenSyntaxError(TokenCode.STATIC));

		expect(TokenCode.IDENTIFIER);
		expect(TokenCode.LPAREN);

		parameters();

		expect(TokenCode.RPAREN);
		expect(TokenCode.LBRACE);

		variable_declarations();
		statement_list();

		expect(TokenCode.RBRACE);
	}

	private void parameters() throws Exception {

		if (type() != DataType.INT && type() != DataType.REAL)
			return; // epsilon rule

		parameter();

		if (match(TokenCode.COMMA)) {
			next_token();
			parameters();
		}
	}

	private void parameter() throws Exception {
		if (type() == DataType.INT || type() == DataType.REAL)
			next_token();
		else
			throw new ParseException(tokenSyntaxError(TokenCode.INT));

		expect(TokenCode.IDENTIFIER);
	}


	private void statement_list() throws Exception {
		// TODO
		if(!statement_start())
			return;  // epsilong rule

	}



	private boolean expect(TokenCode code) throws Exception {
		if(currentToken.getTokenCode() == code) {
			next_token();
			return true;
		}
		errorList.add(new ParseError("error", currentToken));
		throw new ParseException(tokenSyntaxError(code));
		//return false;
	}

	private boolean match(TokenCode code) {
		if (currentToken.getTokenCode() == code)
			return true;
		return false;

	}

	private void next_token() throws Exception {
		currentToken = lexer.yylex();
	}

	private boolean statement_start() {
		return currentToken.getTokenCode() == TokenCode.IDENTIFIER 
			|| currentToken.getTokenCode() == TokenCode.IF
			|| currentToken.getTokenCode() == TokenCode.FOR
			|| currentToken.getTokenCode() == TokenCode.RETURN
			|| currentToken.getTokenCode() == TokenCode.BREAK
			|| currentToken.getTokenCode() == TokenCode.CONTINUE
			|| currentToken.getTokenCode() == TokenCode.LBRACE;
	}

	private String tokenSyntaxError(TokenCode expect) {
		int line = currentToken.getLine();
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
	}

}
