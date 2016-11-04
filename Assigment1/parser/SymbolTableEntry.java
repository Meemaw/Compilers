package parser;

import java.util.Objects;

public class SymbolTableEntry {

	private String lexeme;
	private EntryType entryType;
	private int numParams;
	private DataType returnType;

	public SymbolTableEntry(String lexeme, EntryType entryType) {
		this.lexeme = lexeme;
		this.entryType = entryType;
	}

	public SymbolTableEntry(String lexeme, EntryType entryType, int numParams, DataType returnType) {
		this(lexeme,entryType);
		this.numParams = numParams;
		this.returnType = returnType;
	}

	public int getNumParams() {
		return this.numParams;
	}

	public EntryType getEntryType() {
		return this.entryType;
	}

	public DataType getReturnType() {
		return this.returnType;
	}

	public String getLexeme() {
		return this.lexeme;
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(this.lexeme);
	}

	@Override
	public boolean equals(Object obj) {
	    return this.lexeme.equals((String) obj);
	}


}
