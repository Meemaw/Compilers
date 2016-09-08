package analyzer;

public class SymbolTableEntry {

	private Object value;

	public SymbolTableEntry(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return this.value;
	}
}