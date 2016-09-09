package analyzer;

public enum TokenCode {
  IDENTIFIER, NUMBER, INCDECOP, RELOP, MULOP, ADDOP,
  ASSIGNOP,
  CLASS, STATIC, VOID, IF, ELSE, FOR, RETURN, BREAK, CONTINUE, 
  LBRACE, RBRACE, LBRACKET, RBRACKET, LPAREN, RPAREN,
  SEMICOLON, COMMA, NOT, INT, REAL,
  EOF, ERR_ILL_CHAR, ERR_LONG_ID;

  public static TokenCode tokenCode(String token) {
  	// TODO: add switch statements
  	switch(token) {
  		case "class":
  			return TokenCode.CLASS;
  		case "static":
  			return TokenCode.STATIC;
  		case "void":
  			return TokenCode.VOID;
  		case "if":
  			return TokenCode.IF;
  		case "else":
  			return TokenCode.ELSE;
  		case "for":
  			return TokenCode.FOR;
  		case "return":
  			return TokenCode.RETURN;
  		case "break":
  			return TokenCode.BREAK;
  		case "continue":
  			return TokenCode.CONTINUE;
  		case "{":
  			return TokenCode.LBRACE;
  		case "}":
  			return TokenCode.RBRACE;
  		case "[":
  			return TokenCode.LBRACKET;
  		case "]":
  			return TokenCode.RBRACKET;
  		case "(":
  			return TokenCode.LPAREN;
		case ")":
			return TokenCode.RPAREN;
		case ";":
			return TokenCode.SEMICOLON;
		case ",":
			return TokenCode.COMMA;
		case "!":
			return TokenCode.NOT;
		case "int":
			return TokenCode.INT;
		case "real":
			return TokenCode.REAL;
  		default:
  			return TokenCode.ERR_LONG_ID;

  	}
  }
}