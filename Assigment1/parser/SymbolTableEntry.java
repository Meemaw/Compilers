package parser;

import java.util.Objects;

public class SymbolTableEntry {

	private String lexeme;
	private EntryType entryType;
	private int numParams;

	public SymbolTableEntry(String lexeme, EntryType entryType) {
		this.lexeme = lexeme;
		this.entryType = entryType;
	}

	public SymbolTableEntry(String lexeme, EntryType entryType, int numParams) {
		this(lexeme,entryType);
		this.numParams = numParams;
	}

	public int getNumParams() {
		return this.numParams;
	}

	public EntryType getEntryType() {
		return this.entryType;
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