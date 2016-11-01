package parser;

import analyzer.*;
import java.util.*;
import java.io.IOException;

enum DataType {
    INT, REAL, VOID, NOT_A_TYPE
}


public class Parser{

	private Lexer lexer;
	private SymbolTable globalSymbolTable;
	private SymbolTable localSymbolTable;
	private ArrayList<ParseError> errorList;
	private Token currentToken;
	private Token previousToken;
	private List<String> lines;
	private CodeGenerator codeGenerator;
	private int tempCounter;
	private int labelCounter;


	static private HashSet statement_recovery_set;
	static private HashSet static_semicolon_set;
	static private HashSet semicolon_set;
	static private HashSet static_set;
	static private HashSet lparen_set, rparen_set;
	static private HashSet lbrace_set, rbrace_set;


	public Parser(Lexer lexer, List<String> lines) {
		this.lexer = lexer;
		this.errorList =  new ArrayList<>();
		this.lines = lines;
		this.codeGenerator = new CodeGenerator();
		this.tempCounter = 1;
		this.labelCounter = 1;

		statement_recovery_set = new HashSet(Arrays.asList(new TokenCode[] {TokenCode.SEMICOLON,
																			TokenCode.RBRACE,
																			TokenCode.IF,
																			TokenCode.FOR,
																			TokenCode.BREAK,
																			TokenCode.CONTINUE,
																			TokenCode.LBRACE}));
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
			if(errorList.size() == 0)
				codeGenerator.printCode();
		}
		catch (ParseException e) {
			// do nothing
		}

		return errorList;
	}


	private void program() throws IOException, ParseException {
		globalSymbolTable = new SymbolTable();
		globalSymbolTable.add("writeln", new SymbolTableEntry("writeln", EntryType.FUNCTION, 1));
		globalSymbolTable.add("write", new SymbolTableEntry("write", EntryType.FUNCTION, 1));
		globalSymbolTable.add("0", new SymbolTableEntry("0", EntryType.CONSTANT));
		globalSymbolTable.add("1", new SymbolTableEntry("1", EntryType.CONSTANT));
		next_token();

		expect(TokenCode.CLASS);
		expectIdentifierProgram(TokenCode.IDENTIFIER);
		expect(TokenCode.LBRACE);

		variable_declarations(false);
		codeGenerator.generate(TacCode.GOTO, null, null, new SymbolTableEntry("main", EntryType.FUNCTION));
		method_declarations();
		System.out.println("Global symbol table:");
		System.out.println(globalSymbolTable);

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
			variable_list(method_context);

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
	private void variable_list(boolean method_context) throws IOException, ParseException {
		variable(method_context);


		if (match(TokenCode.COMMA)) {
			next_token();
			variable_list(method_context);
		}
	}

	private void variable(boolean method_context) throws IOException, ParseException {
		String variableName = currentToken.getLexeme();
		SymbolTableEntry entry = new SymbolTableEntry(variableName, EntryType.VARIABLE);
		if(method_context)
			localSymbolTable.add(variableName,entry);
		else
			globalSymbolTable.add(variableName,entry);
		codeGenerator.generate(TacCode.VAR, null, null, entry);

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
		localSymbolTable = new SymbolTable();
		expect(TokenCode.STATIC);
		String name = "";
		if (type() != DataType.NOT_A_TYPE) {
			next_token();
			try {
				name = currentToken.getLexeme();
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
			ArrayList<SymbolTableEntry> params = parameters();
			codeGenerator.functionParameters(params);
			SymbolTableEntry function = new SymbolTableEntry(name, EntryType.FUNCTION, params.size());
			globalSymbolTable.add(name, function);
			expect(TokenCode.RPAREN);
		} catch (ParseException e) {
			consume_all_up_to(lbrace_set);
		}

		expect(TokenCode.LBRACE);

		variable_declarations(true);
		statement_list();

		expect(TokenCode.RBRACE);
		codeGenerator.generate(TacCode.RETURN, null, null, null);
		System.out.println("Local symbol table:");
		System.out.println(localSymbolTable);
	}

	private ArrayList<SymbolTableEntry> parameters() throws IOException, ParseException {
		ArrayList<SymbolTableEntry> params = new ArrayList<>();
		if(match(TokenCode.RPAREN)) return params;

		params.add(parameter());

		if(!match(TokenCode.COMMA) && !match(TokenCode.RPAREN)) {
			expect(TokenCode.COMMA);
		}

		if (match(TokenCode.COMMA)) {
			next_token();
			if(match(TokenCode.RPAREN)) {
				errorList.add(new ParseError("error: illegal start of type" , currentToken));
				throw new ParseException();
			}
			params.addAll(parameters());
		}

		return params;

	}

	private SymbolTableEntry parameter() throws IOException, ParseException {
		if (type() == DataType.INT || type() == DataType.REAL)
			next_token();
		else {
			errorList.add(new ParseError("error: invalid parameters declaration; expected type" , currentToken));
			throw new ParseException();
		}
		SymbolTableEntry e = new SymbolTableEntry(currentToken.getLexeme(), EntryType.VARIABLE);
		localSymbolTable.add(e.getLexeme(), e);
		expect(TokenCode.IDENTIFIER);
		return e;
	}


	private void statement_list() throws IOException, ParseException {
		if(statement_start())
			try {
				statement();
			} catch (ParseException e) {
				consume_all_up_to(statement_recovery_set);
			}
		else if (match(TokenCode.RBRACE))
			return;  // epsilon rule
		else if(currentToken.getTokenCode() == TokenCode.ERR_ILL_CHAR) {
			errorList.add(new ParseError("Invalid character", currentToken));
			consume_all_up_to(statement_recovery_set);
		}
		else {
			errorList.add(new ParseError("error: not a statement" , currentToken));
			consume_all_up_to(statement_recovery_set);
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
			parse_if();
		}
		else if(match(TokenCode.FOR)) {
			parse_for();
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

	private void parse_if() throws IOException, ParseException {

		next_token();

		SymbolTableEntry labelFirst = newLabel();

		try {
			expect(TokenCode.LPAREN);
			SymbolTableEntry exprResult = expression();
			expect(TokenCode.RPAREN);
			codeGenerator.generate(TacCode.EQ, exprResult, globalSymbolTable.get("0"), labelFirst);
		} catch (ParseException e) {
			consume_all_up_to(lbrace_set);
		}

		statement_block();



		if(check_optional_else()) {
			SymbolTableEntry labelSecond = newLabel();
			codeGenerator.generate(TacCode.GOTO, null, null, labelSecond);
			codeGenerator.generate(TacCode.LABEL, null, null, labelFirst);
			labelFirst = labelSecond;
			parse_else();
		}
		codeGenerator.generate(TacCode.LABEL, null, null, labelFirst);
	}

	private boolean check_optional_else() {
		return match(TokenCode.ELSE);
	}

	private void parse_for() throws IOException, ParseException {
		next_token();
		try {
			expect(TokenCode.LPAREN);
			variable_loc();
			expect(TokenCode.ASSIGNOP);
			expression();
			expect(TokenCode.SEMICOLON);
			expression();
			expect(TokenCode.SEMICOLON);
			incr_decr_var();
			expect(TokenCode.RPAREN);
		} catch (ParseException e) {
			consume_all_up_to(lbrace_set);
		}
		statement_block();
	}


	private void assign_incdec_func_call() throws IOException, ParseException {
		if(match(TokenCode.LPAREN)) {
			next_token();
			expression_list();
			expect(TokenCode.RPAREN);
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

	private void parse_else() throws IOException, ParseException {
		next_token();
		statement_block();
	}

	private void expression_list() throws IOException, ParseException {
		if(match(TokenCode.RPAREN))
			return; // epsilon rule
		else
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

	private SymbolTableEntry expression() throws IOException, ParseException {
		simple_expression();
		optional_relop();
		return null; // TODO
	}

	private void optional_relop() throws IOException, ParseException {
		if(!match(TokenCode.RELOP))
			return; // epsilon rule

		next_token();
		simple_expression();
	}

	private SymbolTableEntry simple_expression() throws IOException, ParseException {
		// sign rule

		if(match(OpType.PLUS) || match(OpType.MINUS)) {
			OpType opType = sign(); // generate UMINUS
		}
		
		term();
		optional_addops();
		return null; // TODO
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

	private SymbolTableEntry factor() throws IOException, ParseException {
		SymbolTableEntry entry = null;
		if(match(TokenCode.IDENTIFIER)){
			entry = checkVariable(currentToken.getLexeme());
			if(entry == null) {
				errorList.add(new ParseError("error: cannot find symbol", currentToken));
				throw new ParseException();
			}
			next_token();
			opt_array_func_call();
		}
		else if(match(TokenCode.NUMBER)) {
			entry = new SymbolTableEntry(currentToken.getLexeme(), EntryType.CONSTANT);
			next_token();
		}
		else if(match(TokenCode.LPAREN)) {
			next_token();
			entry = expression();
			expect(TokenCode.RPAREN);
		}
		else if(match(TokenCode.NOT)) {
			next_token();
			entry = factor();
		}
		else if(match(TokenCode.ERR_ILL_CHAR)) {
			errorList.add(new ParseError("error: invalid character", currentToken));
			throw new ParseException();
		} else {
			errorList.add(new ParseError("error: expression expected", currentToken));
			throw new ParseException();
		}
		return entry;
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

	private OpType sign() throws IOException, ParseException {
		if(!match(OpType.PLUS) && !match(OpType.MINUS))
			throw new ParseException();
		OpType opType = currentToken.getOpType();
		next_token();
		return opType;
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
		else if (!currentToken.getLexeme().equals("Program")) {
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
	

	private SymbolTableEntry newTemp() {
		SymbolTableEntry temp = new SymbolTableEntry("t" + this.tempCounter++, EntryType.VARIABLE);
		localSymbolTable.add(temp.getLexeme(), temp);
		codeGenerator.generate(TacCode.VAR, null, null, temp);
		return temp;
	}

	private SymbolTableEntry newLabel() {
		SymbolTableEntry temp = new SymbolTableEntry("lab" + this.labelCounter++, EntryType.LABEL);
		//localSymbolTable.add(temp.getLexeme(), temp);
		return temp;
	}
	

	// TODO checking for name only
	private SymbolTableEntry checkFunction(String x) {
		SymbolTableEntry item = globalSymbolTable.get(x);
		if(item.getEntryType() == EntryType.FUNCTION) 
			return item;
		else 
			return null;
	}

	private SymbolTableEntry checkVariable(String x) {
		SymbolTableEntry localVariable = localSymbolTable.get(x);
		return (localVariable != null) ? localVariable : globalSymbolTable.get(x);
	}


}
