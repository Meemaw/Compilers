import analyzer.*;
import java.util.ArrayList;
import java.io.IOException;


enum DataType {
    INT, REAL, VOID, NOT_A_TYPE
}


public class Parser{

	private Lexer lexer;
	private SymbolTable symbolTable;
	private ArrayList<ParseException> errorList;
	private Token currentToken;

	public Parser(Lexer lexer, SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
		this.lexer = lexer;
		this.errorList =  new ArrayList<>();
	}

	public ArrayList<ParseException> parse() throws Exception {
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
			throw new ParseException(currentToken+"");

		expect(TokenCode.IDENTIFIER);
		expect(TokenCode.LPAREN);

		paremeters();

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
			throw new ParseException(currentToken+"");

		expect(DataType.IDENTIFIER);
	}


	private void statement_list() throws Exception {
		// TODO
	}



	private boolean expect(TokenCode code) throws Exception {
		if(currentToken.getTokenCode() == code) {
			next_token();
			return true;
		}
		errorList.add(new ParseException(currentToken+""));
		throw new ParseException(currentToken+"");
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




}
