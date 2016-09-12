package analyzer;
import java.util.*;
import analyzer.SymbolTableEntry;

public class SymbolTable {

	private TreeMap<Integer, String> table;
	private Integer index;

	public SymbolTable() {
		this.table = new TreeMap<>();
		this.index = 0;
	}

	public SymbolTableEntry contains(SymbolTableEntry x) {
		if(table.containsValue(x.getLexeme())) return x;
		else return null;
	}

	public void add(SymbolTableEntry entry) {
		table.put(this.index++, entry.getLexeme());
	}

	public void printSymbolTable() {
		for(Map.Entry<Integer, String> entry : table.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}

}
