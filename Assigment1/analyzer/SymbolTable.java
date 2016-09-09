package analyzer;
import analyzer.SymbolTableEntry;
import java.util.*;
import analyzer.SymbolTableEntry;

public class SymbolTable {

	private TreeMap<Integer, String> table;

	public SymbolTable() {
		this.table = new TreeMap<>();
	}

	public SymbolTableEntry contains(SymbolTableEntry x) {
		if(table.containsValue(x.getLexeme())) return x;
		else return null;
	}

	public void put(SymbolTableEntry entry, Integer index) {
		table.put(index, entry.getLexeme());
	}

	public void printSymbolTable() {
		for(Map.Entry<Integer, String> entry : table.entrySet()) {
			System.out.println(entry.getKey() + " " + entry.getValue());
		}
	}

}