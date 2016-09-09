package analyzer;

public class SymbolTableEntry {

	private String lexeme;

	public SymbolTableEntry(String lexeme) {
		this.lexeme = lexeme;
	}

	public Object getLexeme() {
		return this.lexeme;
	}
}