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
		globalSymbolTable.add("writeln", new SymbolTableEntry("writeln", EntryType.FUNCTION, 1, DataType.VOID));
		globalSymbolTable.add("write", new SymbolTableEntry("write", EntryType.FUNCTION, 1, DataType.VOID));
		globalSymbolTable.add("0", new SymbolTableEntry("0", EntryType.CONSTANT));
		globalSymbolTable.add("1", new SymbolTableEntry("1", EntryType.CONSTANT));
		next_token();

		expect(TokenCode.CLASS);
		expectIdentifierProgram(TokenCode.IDENTIFIER);
		expect(TokenCode.LBRACE);

		variable_declarations(false);
		codeGenerator.generate(TacCode.GOTO, null, null, new SymbolTableEntry("main", EntryType.FUNCTION));
		method_declarations();
		//System.out.println("Global symbol table:");
		//System.out.println(globalSymbolTable);

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
		if(method_context)
			add_if_not_defined(localSymbolTable, "method");
		else
			add_if_not_defined(globalSymbolTable, "class");

		expect(TokenCode.IDENTIFIER);

		if (match(TokenCode.LBRACKET)) {
			next_token();
			expect(TokenCode.NUMBER);
			expect(TokenCode.RBRACKET);
		}
	}

	private void add_if_not_defined(SymbolTable table, String context) throws IOException, ParseException {
		String variableName = currentToken.getLexeme();
		SymbolTableEntry entry = new SymbolTableEntry(variableName, EntryType.VARIABLE);
		SymbolTableEntry var = table.get(variableName);
		if(var == null) {
			table.add(variableName,entry);
			codeGenerator.generate(TacCode.VAR, null, null, entry);
		}
		else {
			errorList.add(new ParseError("error: variable " + variableName +  " is already defined inside " + context, currentToken));
			throw new ParseException();
		}
	}

	private void method_declarations() throws IOException, ParseException {
		try {
			method_declaration();

			if (!match(TokenCode.STATIC)) {
				if(checkFunction("main") == null) {
					errorList.add(new ParseError("error: main function should allways be declared", currentToken));
					throw new ParseException();
				}
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
		Token identifier = null;
		SymbolTableEntry function = null;
		DataType return_type = null;
		String name = "";
		if (type() != DataType.NOT_A_TYPE) {
			return_type = type();
			next_token();
			try {
				name = currentToken.getLexeme();
				identifier = currentToken;
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
		else {
			throw new ParseException();
		}

		expect(TokenCode.LPAREN);

		try {
			ArrayList<SymbolTableEntry> params = parameters();
			if(checkFunction(name) != null) {
				if (!name.equals(""))
					errorList.add(new ParseError("error: method " + name +  " is already defined in class", identifier));
				throw new ParseException();
			}
			function = new SymbolTableEntry(name, EntryType.FUNCTION, params.size(), return_type);
			globalSymbolTable.add(name, function);
			codeGenerator.generate(TacCode.LABEL, null, null, function);
			codeGenerator.functionParameters(params);
			expect(TokenCode.RPAREN);
		} catch (ParseException e) {
			consume_all_up_to(lbrace_set);
		}

		expect(TokenCode.LBRACE);

		variable_declarations(true);
		statement_list(null, null, function, null);

		expect(TokenCode.RBRACE);
		codeGenerator.generate(TacCode.RETURN, null, null, null);
		//System.out.println("Local symbol table:");
		//System.out.println(localSymbolTable);
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


	private void statement_list(SymbolTableEntry afterForLabel, SymbolTableEntry beforeForLabel, SymbolTableEntry function, Quadruple inc_decr_quadruple) throws IOException, ParseException {
		if(statement_start())
			try {
				statement(afterForLabel, beforeForLabel, function, inc_decr_quadruple);
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

		statement_list(afterForLabel, beforeForLabel, function, inc_decr_quadruple);
	}

	// TODO - return true if it was finished by return statement
	private void statement(SymbolTableEntry afterForLabel, SymbolTableEntry beforeForLabel, SymbolTableEntry function, Quadruple inc_decr_quadruple) throws IOException, ParseException {
		if(match(TokenCode.IDENTIFIER)) {
			next_token();
			assign_incdec_func_call(previousToken); // semantic check inside
			expect(TokenCode.SEMICOLON);
		}
		else if(match(TokenCode.IF)) {
			parse_if(afterForLabel, beforeForLabel, function, inc_decr_quadruple);
		}
		else if(match(TokenCode.FOR)) {
			parse_for(function);
		}
		else if(match(TokenCode.RETURN)) {
			next_token(); // consuming keyword return
			SymbolTableEntry return_value = optional_expression();

			if (function.getReturnType() != DataType.VOID) {
				// function should return something
				if (return_value == null) {
					errorList.add(new ParseError("error: missing return value", previousToken));
					throw new ParseException();
				}
				codeGenerator.generate(TacCode.ASSIGN, return_value, null, function);
			}
			else if (function.getReturnType() == DataType.VOID && return_value != null) {
				errorList.add(new ParseError("error: cannot return a value from method whose result type is void", previousToken));
				throw new ParseException();
			}
			expect(TokenCode.SEMICOLON);
			codeGenerator.generate(TacCode.RETURN, null, null, null);
		}
		else if(match(TokenCode.BREAK) || match(TokenCode.CONTINUE)) {
			if(match(TokenCode.BREAK) && afterForLabel != null)
				codeGenerator.generate(TacCode.GOTO, null, null, afterForLabel);
			else if(match(TokenCode.CONTINUE) && beforeForLabel != null) {
				codeGenerator.generate(inc_decr_quadruple);
				codeGenerator.generate(TacCode.GOTO, null, null, beforeForLabel);
			}

			next_token();
			expect(TokenCode.SEMICOLON);
		}
		else if (match(TokenCode.LBRACE)){
			statement_block(afterForLabel, beforeForLabel, function, inc_decr_quadruple);
		}
		else {
			throw new IOException("something is wrong");
		}
	}


	private void parse_if(SymbolTableEntry afterForLabel, SymbolTableEntry beforeForLabel, SymbolTableEntry function, Quadruple inc_decr_quadruple) throws IOException, ParseException {

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

		statement_block(afterForLabel, beforeForLabel, function, inc_decr_quadruple);

		if(check_optional_else()) {
			SymbolTableEntry labelSecond = newLabel();
			codeGenerator.generate(TacCode.GOTO, null, null, labelSecond);
			codeGenerator.generate(TacCode.LABEL, null, null, labelFirst);
			labelFirst = labelSecond;
			parse_else(afterForLabel, beforeForLabel, function, inc_decr_quadruple);
		}
		codeGenerator.generate(TacCode.LABEL, null, null, labelFirst);
	}

	private boolean check_optional_else() {
		return match(TokenCode.ELSE);
	}

	private void parse_for(SymbolTableEntry function) throws IOException, ParseException {
		next_token();
		Quadruple inc_decr_quadruple = null;
		SymbolTableEntry labelCondition = newLabel();
		SymbolTableEntry labelForEnd = newLabel();
		try {
			expect(TokenCode.LPAREN);
			SymbolTableEntry var = variable_loc();
			expect(TokenCode.ASSIGNOP);
			codeGenerator.generate(TacCode.ASSIGN, expression(), null, var);
			expect(TokenCode.SEMICOLON);
			codeGenerator.generate(TacCode.LABEL, null, null ,labelCondition);
			SymbolTableEntry conditionResult = expression();
			codeGenerator.generate(TacCode.EQ, conditionResult, globalSymbolTable.get("0"), labelForEnd);
			expect(TokenCode.SEMICOLON);
			inc_decr_quadruple = incr_decr_var();
			expect(TokenCode.RPAREN);
		} catch (ParseException e) {
			consume_all_up_to(lbrace_set);
		}
		statement_block(labelForEnd, labelCondition, function, inc_decr_quadruple);
		codeGenerator.generate(inc_decr_quadruple);
		codeGenerator.generate(TacCode.GOTO, null, null, labelCondition);
		codeGenerator.generate(TacCode.LABEL, null, null, labelForEnd);
	}


	private void assign_incdec_func_call(Token identifier) throws IOException, ParseException {
		if(match(TokenCode.LPAREN)) {
			func_call(identifier);
		}
		else {
			opt_index();
			SymbolTableEntry entry = checkVariable(identifier.getLexeme());
			if(entry == null) {
				errorList.add(new ParseError("error: cannot find symbol", identifier));
				throw new ParseException();
			}
			assign_or_inc(entry);
		}
	}

	// done
	private SymbolTableEntry func_call (Token identifier) throws IOException, ParseException {
		if(match(TokenCode.LPAREN)) {

			SymbolTableEntry function = checkFunction(identifier.getLexeme());
			if(function == null) {
				errorList.add(new ParseError("error: cannot find symbol", identifier));
				throw new ParseException();
			}
			next_token();
			ArrayList<SymbolTableEntry> arguments = expression_list();
			int numArguments = arguments.size();
			if(function.getNumParams() != numArguments) {
				errorList.add(new ParseError("error: actual and formal argument lists differ in length", identifier));
				throw new ParseException();
			}
			expect(TokenCode.RPAREN);
			codeGenerator.pushArguments(arguments); // Generate APARAM instruction for each argument
			codeGenerator.generate(TacCode.CALL, function, null, null);
			if (function.getReturnType() != DataType.VOID) {
				SymbolTableEntry temp_var = newTemp();
				codeGenerator.generate(TacCode.ASSIGN, function, null, temp_var);
				return temp_var;
			}
			else
				return null;

		}
		else
			return null;
	}

	// done
	private void assign_or_inc(SymbolTableEntry entry) throws IOException, ParseException {
		if(match(TokenCode.INCDECOP)) {
			if (currentToken.getOpType() == OpType.INC)
				codeGenerator.generate(TacCode.ADD, entry, globalSymbolTable.get("1"), entry);
			else if (currentToken.getOpType() == OpType.DEC)
				codeGenerator.generate(TacCode.SUB, entry, globalSymbolTable.get("1"), entry);

			next_token();
		}
		else {
			expect(TokenCode.ASSIGNOP);
			codeGenerator.generate(TacCode.ASSIGN, expression(), null, entry);
		}
	}

	private SymbolTableEntry optional_expression() throws IOException, ParseException {
		if(!expression_start())
			return null; // epsilon rule;

		return expression();
	}

	private void statement_block(SymbolTableEntry afterForLabel, SymbolTableEntry beforeForLabel, SymbolTableEntry function, Quadruple inc_decr_quadruple) throws IOException, ParseException {
		expect(TokenCode.LBRACE);
		statement_list(afterForLabel, beforeForLabel, function, inc_decr_quadruple);
		expect(TokenCode.RBRACE);
	}

	private Quadruple incr_decr_var() throws IOException, ParseException {
		SymbolTableEntry entry = variable_loc();
		expect(TokenCode.INCDECOP);
		if(previousToken.getOpType() == OpType.INC)
			return new Quadruple(TacCode.ADD, entry, globalSymbolTable.get("1"), entry);
		else
			return new Quadruple(TacCode.SUB, entry, globalSymbolTable.get("1"), entry);
	}

	private void parse_else(SymbolTableEntry afterForLabel, SymbolTableEntry beforeForLabel, SymbolTableEntry function, Quadruple inc_decr_quadruple) throws IOException, ParseException {
		next_token();
		statement_block(afterForLabel, beforeForLabel, function, inc_decr_quadruple);
	}

	private ArrayList<SymbolTableEntry> expression_list() throws IOException, ParseException {
		ArrayList<SymbolTableEntry> args = new ArrayList();
		if(match(TokenCode.RPAREN))
			return args; // epsilon rule
		else {
			args.add(expression());
		}

		if(match(TokenCode.COMMA))  {
			next_token();
			args.addAll(expression_list());
		}

		return args;
	}

	/*
	private ArrayList<SymbolTableEntry> more_expressions() throws IOException, ParseException {
		ArrayList<SymbolTableEntry> args = new ArrayList();
		if(!match(TokenCode.COMMA))
			return; // epsilon rule

		next_token();
		args.add(expression());
		args.addAll(more_expressions());
		return args;
	}
	*/

	// done
	private SymbolTableEntry expression() throws IOException, ParseException {
		SymbolTableEntry entry = simple_expression();
		return optional_relop(entry);
	}

	// done
	private SymbolTableEntry optional_relop(SymbolTableEntry entry_1) throws IOException, ParseException {
		if(!match(TokenCode.RELOP))
			return entry_1; // epsilon rule

		OpType operator = currentToken.getOpType();
		next_token(); // consuming operator

		SymbolTableEntry entry_2 = simple_expression();

		SymbolTableEntry temp_var = newTemp();
		SymbolTableEntry label_1 = newLabel();
		SymbolTableEntry label_2 = newLabel();

		TacCode relop;

		switch(operator) {
			case EQUAL:
				relop = TacCode.EQ;
				break;
			case LTE:
				relop = TacCode.LE;
				break;
			case GTE:
				relop = TacCode.GE;
				break;
			case LT:
				relop = TacCode.LT;
				break;
			case GT:
				relop = TacCode.GT;
				break;
			case NOT_EQUAL:
				relop = TacCode.NE;
				break;
			default:
				relop = TacCode.NOOP;
				break;
		}

		codeGenerator.generate(relop, entry_1, entry_2, label_1);
		codeGenerator.generate(TacCode.ASSIGN, globalSymbolTable.get("0"), null, temp_var);
		codeGenerator.generate(TacCode.GOTO, null, null, label_2);
		codeGenerator.generate(TacCode.LABEL, null, null, label_1);
		codeGenerator.generate(TacCode.ASSIGN, globalSymbolTable.get("1"), null, temp_var);
		codeGenerator.generate(TacCode.LABEL, null, null, label_2);

		return temp_var;
	}

	// done
	private SymbolTableEntry simple_expression() throws IOException, ParseException {

		OpType opt_unary_oprator = null;
		// sign rule
		if(match(OpType.PLUS) || match(OpType.MINUS)) {
			opt_unary_oprator = sign();
		}

		SymbolTableEntry entry = term();

		if (opt_unary_oprator == OpType.MINUS) { // generate UMINUS
			SymbolTableEntry temp;
			temp = newTemp();
			codeGenerator.generate(TacCode.UMINUS, entry, null, temp);
			entry = temp;
		}

		return optional_addops(entry);
	}

	// done
	private SymbolTableEntry optional_addops(SymbolTableEntry entry_1) throws IOException, ParseException {
		if(!match(TokenCode.ADDOP))
			return entry_1; // epsilon rule

		OpType operator = currentToken.getOpType();
		next_token();

		SymbolTableEntry entry_2 = term();
		SymbolTableEntry temp_var = newTemp();

		TacCode addop;

		switch(operator) {
			case PLUS:
				addop = TacCode.ADD;
				break;
			case MINUS:
				addop = TacCode.SUB;
				break;
			case OR:
				addop = TacCode.OR;
				break;
			default:
				addop = TacCode.NOOP;
				break;
		}

		codeGenerator.generate(addop, entry_1, entry_2, temp_var);

		return optional_addops(temp_var);
	}

	// done
	private SymbolTableEntry term() throws IOException, ParseException {
		SymbolTableEntry entry = factor();

		return optional_mulop(entry);
	}

	// done
	private SymbolTableEntry optional_mulop(SymbolTableEntry entry_1) throws IOException, ParseException {
		if(!match(TokenCode.MULOP))
			return entry_1; // epsilon rule

		OpType operator = currentToken.getOpType();
		next_token(); // consuming operator

		SymbolTableEntry entry_2 = factor();
		SymbolTableEntry temp_var = newTemp();

		TacCode mulop;

		switch(operator) {
			case MULT:
				mulop = TacCode.MULT;
				break;
			case DIV:
				mulop = TacCode.DIV;
				break;
			case MOD:
				mulop = TacCode.MOD;
				break;
			case AND:
				mulop = TacCode.AND;
				break;
			default:
				mulop = TacCode.NOOP;
				break;
		}

		codeGenerator.generate(mulop, entry_1, entry_2, temp_var);

		return optional_mulop(temp_var);
	}

	// done
	private SymbolTableEntry factor() throws IOException, ParseException {
		SymbolTableEntry entry = null;
		if(match(TokenCode.IDENTIFIER)){
			next_token(); // consume identifier
			entry = opt_array_func_call(previousToken); // semantic check is inside
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
			SymbolTableEntry temp = newTemp();
			entry = factor();
			codeGenerator.generate(TacCode.NOT, entry, null, temp);
			entry = temp;
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

	// done
	private SymbolTableEntry opt_array_func_call(Token identifier) throws IOException, ParseException {
		SymbolTableEntry entry;

		if(match(TokenCode.LPAREN))
			entry = func_call(identifier);
		else {
			// semantic check - variable declaration
			entry = checkVariable(identifier.getLexeme());
			if(entry == null) {
				errorList.add(new ParseError("error: cannot find symbol", identifier));
				throw new ParseException();
			}
			opt_index();
		}

		return entry;
	}

	// done
	private SymbolTableEntry variable_loc() throws IOException, ParseException {
		SymbolTableEntry entry = checkVariable(currentToken.getLexeme());
		if(entry == null) {
			errorList.add(new ParseError("error: cannot find symbol", currentToken));
			throw new ParseException();
		}
		expect(TokenCode.IDENTIFIER);
		opt_index();
		return entry;
	}


	private void opt_index() throws IOException, ParseException {
		if(!match(TokenCode.LBRACKET))
			return; // epsilon rule
		next_token();

		expression();

		expect(TokenCode.RBRACKET);
	}

	// done
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
		if(item != null && item.getEntryType() == EntryType.FUNCTION)
			return item;
		else
			return null;
	}

	private SymbolTableEntry checkVariable(String x) {
		SymbolTableEntry localVariable = localSymbolTable.get(x);
		if(localVariable != null) return localVariable;
		else {
			SymbolTableEntry globalVariable = globalSymbolTable.get(x);
			if(globalVariable != null && globalVariable.getEntryType() == EntryType.VARIABLE)
				return globalVariable;
			else
				return null;
		}
	}
}
