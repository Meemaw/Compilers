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
	private Token previous;
	private List<String> lines;

	public Parser(Lexer lexer, SymbolTable symbolTable, List<String> lines) {
		this.symbolTable = symbolTable;
		this.lexer = lexer;
		this.errorList =  new ArrayList<>();
		this.lines = lines;
	}

	public ArrayList<ParseError> parse() throws Exception {
		try {
			program();
		}
		catch (ParseException e) {
			errorList.add(new ParseError("Expected token ", currentToken));
		}

		return errorList;
	}


	private void program() throws Exception {
		next_token();

		expect(TokenCode.CLASS);
		expectIdentifierProgram(TokenCode.IDENTIFIER);
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
		if(!statement_start())
			return;  // epsilon rule

		statement();
		statement_list();

	}

	private void statement() throws Exception {
		if(match(TokenCode.IDENTIFIER)) {
			next_token();
			assign_incdec_func_call();
			expect(TokenCode.SEMICOLON);
		}
		else if(match(TokenCode.IF)) {
			next_token();
			expect(TokenCode.LPAREN);
			expression();
			expect(TokenCode.RPAREN);
			statement_block();
			optional_else();
		}
		else if(match(TokenCode.FOR)) {
			next_token();
			expect(TokenCode.LPAREN);
			variable_loc();
			expect(TokenCode.ASSIGNOP);
			expression();
			expect(TokenCode.SEMICOLON);
			expression();
			expect(TokenCode.SEMICOLON);
			incr_decr_var();
			expect(TokenCode.RPAREN);
			statement_block();
		}
		else if(match(TokenCode.RETURN)) {
			next_token();
			optional_expression();
			expect(TokenCode.SEMICOLON);
		}
		else if(match(TokenCode.BREAK) || match(TokenCode.CONTINUE)) {
			next_token();
			expect(TokenCode.SEMICOLON);
		}
		else {
			statement_block();
		}
	}


	private void assign_incdec_func_call() throws Exception {
		if(match(TokenCode.LPAREN)) {
			next_token();
			expression_list();
			match(TokenCode.RPAREN);
		}
		else {
			opt_index();
			assign_or_inc();
		}
	}

	private void assign_or_inc() throws Exception {
		if(match(TokenCode.INCDECOP))
			next_token();
		else {
			expect(TokenCode.ASSIGNOP);
			expression();
		}
	}

	private void optional_expression() throws Exception {
		if(!expression_start())
			return; // epsilon rule;

		expression();
	}

	private void statement_block() throws Exception {
		expect(TokenCode.LBRACE);
		statement_list();
		expect(TokenCode.RBRACE);
	}

	private void incr_decr_var() throws Exception {
		variable_loc();
		expect(TokenCode.INCDECOP);
	}

	private void optional_else() throws Exception {
		if(!match(TokenCode.ELSE))
			return;  // epsilon rule
		next_token();
		statement_block();
	}

	private void expression_list() throws Exception {
		if(!expression_start())
			return; // epsilon rule

		expression();
		more_expressions();
	}


	private void more_expressions() throws Exception {
		if(!match(TokenCode.COMMA))
			return; // epsilon rule

		next_token();
		expression();
		more_expressions();
	}

	private void expression() throws Exception {
		simple_expression();
		optional_relop();
	}

	private void optional_relop() throws Exception {
		if(!match(TokenCode.RELOP))
			return; // epsilon rule

		next_token();
		simple_expression();
	}

	private void simple_expression() throws Exception {
		// sign rule
		if(match(OpType.PLUS) || match(OpType.MINUS))
			sign();
		term();
		optional_addops();
	}

	private void optional_addops() throws Exception {
		if(!match(TokenCode.ADDOP))
			return; // epsilon rule

		next_token();
		term();
		optional_addops();
	}

	private void term() throws Exception {
		factor();
		optional_mulop();
	}

	private void optional_mulop() throws Exception {
		if(!match(TokenCode.MULOP))
			return; // epsilon rule

		next_token();
		term();
	}

	private void factor() throws Exception {
		if(match(TokenCode.IDENTIFIER)){
			next_token();
			opt_array_func_call();
		}
		else if(match(TokenCode.NUMBER)) {
			next_token();
		}
		else if(match(TokenCode.LPAREN)) {
			next_token();
			expression();
			expect(TokenCode.RPAREN);
		}
		else if(match(TokenCode.NOT)) {
			next_token();
			factor();
		} else {
			// NEED TO DO ANYTHING?
		}
	}

	private void opt_array_func_call() throws Exception {
		if(match(TokenCode.LPAREN)) {
			next_token();
			expression_list();
			expect(TokenCode.RPAREN);
		}
		else opt_index();
	}

	private void variable_loc() throws Exception {
		expect(TokenCode.IDENTIFIER);
		opt_index();
	}



	private void opt_index() throws Exception {
		if(!match(TokenCode.LBRACKET))
			return; // epsilon rule
		next_token();

		expression();

		expect(TokenCode.RBRACKET);
	}

	private void sign() throws Exception {
		if(!match(OpType.PLUS) && !match(OpType.MINUS))
			throw new ParseException(tokenSyntaxError(TokenCode.ADDOP));

		next_token();
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
		return currentToken.getTokenCode() == code;
	}

	private boolean match(OpType opType) {
		return currentToken.getOpType() == opType;
	}

	private void next_token() throws Exception {
		previous = currentToken;
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

	private boolean expression_start() {
		return currentToken.getTokenCode() == TokenCode.IDENTIFIER
			|| currentToken.getTokenCode() == TokenCode.NUMBER
			|| currentToken.getTokenCode() == TokenCode.LPAREN
			|| currentToken.getTokenCode() == TokenCode.NOT
			|| currentToken.getOpType() == OpType.PLUS
			|| currentToken.getOpType() == OpType.MINUS;
	}

	private String tokenSyntaxError(TokenCode expect) {
		int line = previous.getLine();
		String s = lineOutput(line, lines.get(line), 4);
		s += messageOutput("Expected " + expect, 4);
		s += "Actual " + currentToken.getTokenCode();
		return s;
	}

	private String tokenSyntaxError(OpType expect) {
		int line = previous.getLine();
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

	private void expectIdentifierProgram(TokenCode token) throws Exception {
		if(currentToken.getTokenCode() != token) throw new ParseException(TokenCode.IDENTIFIER + "");
		if(!currentToken.getSymbolTableEntry().getLexeme().equals("Program")) 
			throw new ParseException("Identifier Program");
		next_token();
	}



}
