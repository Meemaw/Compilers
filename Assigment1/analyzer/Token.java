package analyzer;


public class Token {

	private DataType dataType;
	private TokenCode tokenCode;
	private OpType opType;
	private int line;
	private int column;
	private String lexeme;

	public Token(DataType dataType, TokenCode tokenCode, OpType opType, String lexeme, int line, int column) {
		this.dataType = dataType;
		this.tokenCode = tokenCode;
		this.opType = opType;
		this.lexeme = lexeme;
		this.line = line;
		this.column = column;
	}

	public TokenCode getTokenCode() {
		return this.tokenCode;
	}

	public DataType getDataType() {
		return this.dataType;
	}

	public OpType getOpType() {
		return this.opType;
	}

	public int getLine() {
		return this.line;
	}

	public int getColumn() {
		return this.column;
	}

	public String getLexeme() {
		return this.lexeme;
	}


}