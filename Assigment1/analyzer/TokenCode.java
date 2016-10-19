package analyzer;

public enum TokenCode {
	IDENTIFIER, NUMBER, INCDECOP, RELOP, MULOP, ADDOP,
	ASSIGNOP,
	CLASS, STATIC, VOID, IF, ELSE, FOR, RETURN, BREAK, CONTINUE,
	LBRACE, RBRACE, LBRACKET, RBRACKET, LPAREN, RPAREN,
	SEMICOLON, COMMA, NOT, INT, REAL,
	EOF, ERR_ILL_CHAR, ERR_LONG_ID;

	public String stringifyTokenCode() {
		switch (this) {
			case IDENTIFIER:
				// java: "error: <identifier> expected"
				return "<identifier>";
			case SEMICOLON:
				// java: "error: ';' expected"
				return "';'";
			case CLASS:
				return "class";
			case STATIC:
				return "static";
			case RETURN:
				return "return";
			case RBRACE:
				return "}";
			case LBRACE:
				return "{";
			case RPAREN:
				return ")";
			case LPAREN:
				return "(";
			case COMMA:
				return "','";


			//case NOT:
			// we won't probably expect "NOT" token anywhere, so put here only tokens we need to stringify like braces

		}

		return "'something is wrong'";
	}
}
