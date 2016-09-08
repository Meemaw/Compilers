package analyzer;

public class Token {

	private DataType dataType;
	private TokenCode tokenCode;
	private OpType opType;
	private SymbolTableEntry entry;

	public Token(TokenCode tokenCode, DataType dataType, OpType opType, SymbolTableEntry entry) {
		this.dataType = dataType;
		this.tokenCode = tokenCode;
		this.opType = opType;
		this.entry = entry;
	}

	public Token(TokenCode tokenCode, DataType dataType, OpType opType) {
		this.dataType = dataType;
		this.tokenCode = tokenCode;
		this.opType = opType;
		this.entry = null;
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