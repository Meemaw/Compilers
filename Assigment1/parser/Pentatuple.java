package parser;


public class Pentatuple {

	private SymbolTableEntry label;
	private TacCode code;
	private SymbolTableEntry param1;
	private SymbolTableEntry param2;
	private SymbolTableEntry result;

	public Pentatuple(SymbolTableEntry label, TacCode code, SymbolTableEntry param1, SymbolTableEntry param2, SymbolTableEntry result) {
		this.label = label;
		this.code = code;
		this.param1 = param1;
		this.param2 = param2;
		this.result = result;
	}

	public Pentatuple(TacCode code, SymbolTableEntry param1, SymbolTableEntry param2, SymbolTableEntry result) {
		this(null, code, param1, param2, result);
	}


	public String stringify(int label_format, int param1_format, int param2_format, int result_format) {
		String labelString = (label == null) ? "" : label.getLexeme() + ":";
		String param1String = (param1 == null) ? "" : param1.getLexeme();
		String param2String = (param2 == null) ? "" : param2.getLexeme();
		String resultString = (result == null) ? "" : result.getLexeme();

		String l = String.format("%" + label_format +"s", labelString);
		String t = String.format("%10s", code);
		String p1 = String.format("%" + param1_format +"s", param1String);
		String p2 = String.format("%" + param2_format +"s", param2String);
		String r = String.format("%" + result_format +"s", resultString);

		return l + t + p1 + p2 + r;
	}
}
