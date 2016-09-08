package analyzer;

public class Token {

	private DataType dataType;
	private TokenCode tokenCode;
	private OpType opType;
	private SymbolTableEntry entry;

	public Token(DataType dataType, TokenCode tokenCode, OpType opType, SymbolTableEntry entry) {
		this.dataType = dataType;
		this.tokenCode = tokenCode;
		this.opType = opType;
		this.entry = entry;
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


}