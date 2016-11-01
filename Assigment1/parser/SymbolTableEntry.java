package parser;

import java.util.Objects;

public class SymbolTableEntry {

	private String lexeme;
	private EntryType entryType;

	public SymbolTableEntry(String lexeme) {
		this.lexeme = lexeme;
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
	    return this.lexeme.equals(((SymbolTableEntry)obj).getLexeme());
	}


}