package analyzer;

import parser.SymbolTableEntry;

public class Token {

	private DataType dataType;
	private TokenCode tokenCode;
	private OpType opType;
	private SymbolTableEntry entry;
	private int line;
	private int column;

	public Token(DataType dataType, TokenCode tokenCode, OpType opType, SymbolTableEntry entry, int line, int column) {
		this.dataType = dataType;
		this.tokenCode = tokenCode;
		this.opType = opType;
		this.entry = entry;
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

	public SymbolTableEntry getSymbolTableEntry() {
		return this.entry;
	}

	public int getLine() {
		return this.line;
	}

	public int getColumn() {
		return this.column;
	}


}