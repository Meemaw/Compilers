package analyzer;
import java.util.*;
import analyzer.SymbolTableEntry;

public class SymbolTable {

	private HashMap<SymbolTableEntry, Integer> table;
	private Integer index;

	public SymbolTable() {
		this.table = new HashMap<>();
		this.index = 0;
	}

	public SymbolTableEntry contains(SymbolTableEntry x) {
		return table.containsKey(x) ? x : null;
	}

	public void add(SymbolTableEntry entry) {
		table.put(entry, this.index++);
	}

	public HashMap<SymbolTableEntry, Integer> getTable() {
		return this.table;
	}

	private List<Map.Entry<SymbolTableEntry, Integer>> sortTable() {
		List<Map.Entry<SymbolTableEntry, Integer>> list = new LinkedList<>(table.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<SymbolTableEntry, Integer>>() 
		{
			public int compare(Map.Entry<SymbolTableEntry, Integer> o1, Map.Entry<SymbolTableEntry, Integer> o2)
			{
				return o1.getValue().compareTo(o2.getValue());
			}
		});
		return list;
	}

	public void printSymbolTable() {
		for(Map.Entry<SymbolTableEntry, Integer> entry : sortTable()) {
			System.out.println(entry.getValue() + " " + entry.getKey().getLexeme());
		}
	}

}
