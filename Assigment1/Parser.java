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
	private Token previousToken;
	private List<String> lines;

	static private HashSet statement_recovery_set;
	static private HashSet static_semicolon_set;
	static private HashSet semicolon_set;
	static private HashSet static_set;
	static private HashSet lparen_set, rparen_set;
	static private HashSet lbrace_set, rbrace_set;


	public Parser(Lexer lexer, SymbolTable symbolTable, List<String> lines) {
		this.symbolTable = symbolTable;
		this.lexer = lexer;
		this.errorList =  new ArrayList<>();
		this.lines = lines;

		statement_recovery_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.SEMICOLON, TokenCode.RBRACE}));
		static_semicolon_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.SEMICOLON, TokenCode.STATIC}));
		semicolon_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.SEMICOLON}));

		static_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.STATIC}));
		lparen_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.LPAREN}));
		rparen_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.RBRACE}));
		lbrace_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.LBRACE}));
		rbrace_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.RBRACE}));
	}

	public ArrayList<ParseError> parse() throws IOException {
		try {
			program();
		}
		catch (ParseException e) {
			// do nothing
		}

		return errorList;
	}


	private void program() throws IOException, ParseException {
		next_token();

		expect(TokenCode.CLASS);
		expectIdentifierProgram(TokenCode.IDENTIFIER);
		expect(TokenCode.LBRACE);

		variable_declarations(false);
		method_declarations();

		expect(TokenCode.RBRACE);
	}

	private void variable_declarations(boolean method_context) throws IOException, ParseException {
		if (!method_context && match(TokenCode.STATIC)) {
			return; // epsilon rule
		}
		if (method_context && (statement_start() || match(TokenCode.RBRACE))) {
			return; // epsilon rule
		}

		try {
			if (type() != DataType.INT && type() != DataType.REAL) {
				errorList.add(new ParseError("error: type expected" , currentToken));
				throw new ParseException();
			}

			next_token(); // consume INT | REAL
			variable_list();

			expect(TokenCode.SEMICOLON);
		} catch (ParseException e) {
			if (method_context)
				consume_all_up_to(semicolon_set);
			else
				consume_all_up_to(static_semicolon_set);

		}

		variable_declarations(method_context);
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

	// this method implement also more_variables (nonterminal) rule
	private void variable_list() throws IOException, ParseException {
		variable();

		if (match(TokenCode.COMMA)) {
			next_token();
			variable_list();
		}
	}

	private void variable() throws IOException, ParseException {
		expect(TokenCode.IDENTIFIER);

		if (match(TokenCode.LBRACKET)) {
			next_token();
			expect(TokenCode.NUMBER);
			expect(TokenCode.RBRACKET);
		}
	}

	private void method_declarations() throws IOException, ParseException {
		try {
			method_declaration();

			if (!match(TokenCode.STATIC)) {
				return; // epsilon rule
			}
		} catch (ParseException e) {
			consume_all_up_to(static_set);
		}
		method_declarations();
	}

	private void method_declaration() throws IOException, ParseException {
		expect(TokenCode.STATIC);
		if (type() != DataType.NOT_A_TYPE) {
			next_token();
			try {
				expect(TokenCode.IDENTIFIER);
			} catch (ParseException e) {
				consume_all_up_to(lparen_set);
			}
		}
		else if (match(TokenCode.IDENTIFIER)) {
			// java: "error: invalid method declaration; return type required"
			errorList.add(new ParseError("error: invalid method declaration; return type required", currentToken));
			consume_all_up_to(lparen_set);
		}
		else if (match(TokenCode.LPAREN)) {
		}
		else {
			throw new ParseException();
		}

		expect(TokenCode.LPAREN);

		try {
			parameters();
			expect(TokenCode.RPAREN);
		} catch (ParseException e) {
			consume_all_up_to(lbrace_set);
		}

		expect(TokenCode.LBRACE);

		variable_declarations(true);
		statement_list();

		expect(TokenCode.RBRACE);
	}

	private void parameters() throws IOException, ParseException {
		if(match(TokenCode.RPAREN)) return;

		parameter();

		if(!match(TokenCode.COMMA) && !match(TokenCode.RPAREN)) {
			expect(TokenCode.COMMA);
		}

		if (match(TokenCode.COMMA)) {
			next_token();
			if(match(TokenCode.RPAREN)) {
				errorList.add(new ParseError("error: illegal start of type" , currentToken));
				throw new ParseException();
			}
			parameters();
		}
	}

	private void parameter() throws IOException, ParseException {
		if (type() == DataType.INT || type() == DataType.REAL)
			next_token();
		else {
			errorList.add(new ParseError("error: invalid parameters declaration; expected type" , currentToken));
			throw new ParseException();
		}

		expect(TokenCode.IDENTIFIER);
	}


	private void statement_list() throws IOException, ParseException {
		if(statement_start())
			statement();
		else if (match(TokenCode.RBRACE))
			return;  // epsilon rule
		else {
			errorList.add(new ParseError("error: not a statement" , currentToken));
			consume_all_up_to(statement_recovery_set);
			throw new ParseException();
		}

		statement_list();
	}

	private void statement() throws IOException, ParseException {
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
		else if (match(TokenCode.LBRACE)){
			statement_block();
		}
		else {
			throw new IOException("something is wrong");
		}
	}


	private void assign_incdec_func_call() throws IOException, ParseException {
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

	private void assign_or_inc() throws IOException, ParseException {
		if(match(TokenCode.INCDECOP))
			next_token();
		else {
			expect(TokenCode.ASSIGNOP);
			expression();
		}
	}

	private void optional_expression() throws IOException, ParseException {
		if(!expression_start())
			return; // epsilon rule;

		expression();
	}

	private void statement_block() throws IOException, ParseException {
		expect(TokenCode.LBRACE);
		statement_list();
		expect(TokenCode.RBRACE);
	}

	private void incr_decr_var() throws IOException, ParseException {
		variable_loc();
		expect(TokenCode.INCDECOP);
	}

	private void optional_else() throws IOException, ParseException {
		if(!match(TokenCode.ELSE))
			return;  // epsilon rule
		next_token();
		statement_block();
	}

	private void expression_list() throws IOException, ParseException {
		if(!expression_start())
			return; // epsilon rule

		expression();
		more_expressions();
	}


	private void more_expressions() throws IOException, ParseException {
		if(!match(TokenCode.COMMA))
			return; // epsilon rule

		next_token();
		expression();
		more_expressions();
	}

	private void expression() throws IOException, ParseException {
		simple_expression();
		optional_relop();
	}

	private void optional_relop() throws IOException, ParseException {
		if(!match(TokenCode.RELOP))
			return; // epsilon rule

		next_token();
		simple_expression();
	}

	private void simple_expression() throws IOException, ParseException {
		// sign rule
		if(match(OpType.PLUS) || match(OpType.MINUS))
			sign();
		term();
		optional_addops();
	}

	private void optional_addops() throws IOException, ParseException {
		if(!match(TokenCode.ADDOP))
			return; // epsilon rule

		next_token();
		term();
		optional_addops();
	}

	private void term() throws IOException, ParseException {
		factor();
		optional_mulop();
	}

	private void optional_mulop() throws IOException, ParseException {
		if(!match(TokenCode.MULOP))
			return; // epsilon rule

		next_token();
		term();
	}

	private void factor() throws IOException, ParseException {
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
			errorList.add(new ParseError("error: expression expected", currentToken));
			throw new ParseException();
		}
	}

	private void opt_array_func_call() throws IOException, ParseException {
		if(match(TokenCode.LPAREN)) {
			next_token();
			expression_list();
			expect(TokenCode.RPAREN);
		}
		else opt_index();
	}

	private void variable_loc() throws IOException, ParseException {
		expect(TokenCode.IDENTIFIER);
		opt_index();
	}



	private void opt_index() throws IOException, ParseException {
		if(!match(TokenCode.LBRACKET))
			return; // epsilon rule
		next_token();

		expression();

		expect(TokenCode.RBRACKET);
	}

	private void sign() throws IOException, ParseException {
		if(!match(OpType.PLUS) && !match(OpType.MINUS))
			throw new ParseException();

		next_token();
	}






	private void expect(TokenCode code) throws IOException, ParseException {
		if(currentToken.getTokenCode() == code) {
			next_token();
			return;
		}
		if(currentToken.getTokenCode() == TokenCode.ERR_ILL_CHAR)
			errorList.add(new ParseError("Invalid character", currentToken));
		else if(currentToken.getTokenCode() == TokenCode.EOF)
			errorList.add(new ParseError("error: reached end of file while parsing", previousToken, true));
		else if (code == TokenCode.SEMICOLON || code == TokenCode.COMMA) // expected semicolon
			errorList.add(new ParseError(String.format("error: %s expected", code.stringifyTokenCode()), previousToken, true));
		else
			errorList.add(new ParseError(String.format("error: %s expected", code.stringifyTokenCode()), currentToken));
		throw new ParseException();
	}

	private boolean match(TokenCode code) {
		return currentToken.getTokenCode() == code;
	}

	private boolean match(OpType opType) {
		return currentToken.getOpType() == opType;
	}

	private void next_token() throws IOException,ParseException {
		previousToken = currentToken;
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


	private void expectIdentifierProgram(TokenCode code) throws IOException, ParseException {
		if(currentToken.getTokenCode() != code) {
			errorList.add(new ParseError("error: %s expected".format(code.stringifyTokenCode()), currentToken));
			throw new ParseException();
		}
		else if (!currentToken.getSymbolTableEntry().getLexeme().equals("Program")) {
			errorList.add(new ParseError("error: Programs in Decaf are written in a single class, that should by convention be named ’Program’", currentToken));
			throw new ParseException();
		}

		else next_token();
	}

	private void consume_all_up_to(Set <TokenCode> token_set) throws IOException, ParseException {
		while (!token_set.contains(currentToken.getTokenCode())) {
			if (match(TokenCode.EOF)) {
				throw new ParseException();
			}
			next_token();
			if (match(TokenCode.LPAREN) && !token_set.contains(TokenCode.LPAREN)) {
				consume_all_up_to(rparen_set);
			}
			else if (match(TokenCode.LBRACE) && !token_set.contains(TokenCode.LBRACE)) {
				consume_all_up_to(rbrace_set);
			}
		}
		if (match(TokenCode.SEMICOLON))
			next_token();
	}
}
