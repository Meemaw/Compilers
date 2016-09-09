package analyzer;

public class SymbolTableEntry {

	private String lexeme;

	public SymbolTableEntry(String lexeme) {
		this.lexeme = lexeme;
	}

	public String getLexeme() {
		return this.lexeme;
	}
}