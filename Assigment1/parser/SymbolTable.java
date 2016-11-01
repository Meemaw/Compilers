package parser;
import java.util.*;


public class SymbolTable {

	private HashMap<String, SymbolTableEntry> table;

	public SymbolTable() {
		this.table = new HashMap<>();
	}

	public SymbolTableEntry get(String x) {
		return table.containsKey(x) ? table.get(x) : null;
	}

	public void add(String x, SymbolTableEntry e) {
		table.put(x, e);
	}

	public HashMap<String, SymbolTableEntry> getTable() {
		return this.table;
	}
	
	@Override
	public String toString() {
		String s = "";
		for(Map.Entry<String, SymbolTableEntry> entry : this.table.entrySet()) {

			SymbolTableEntry e = entry.getValue();
			s += e.getLexeme() + " : " + e.getEntryType();

			if(e.getEntryType() == EntryType.FUNCTION)
				s += " : " + e.getNumParams();

			s += "\n";
		}
		return s;
	}
}
