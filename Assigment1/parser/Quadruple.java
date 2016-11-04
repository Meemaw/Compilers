package parser;


public class Quadruple {

	private TacCode code;
	private SymbolTableEntry param1;
	private SymbolTableEntry param2;
	private SymbolTableEntry result;

	public Quadruple(TacCode code, SymbolTableEntry param1, SymbolTableEntry param2, SymbolTableEntry result) {
		this.code = code;
		this.param1 = param1;
		this.param2 = param2;
		this.result = result;
	}

	public TacCode getTacCode() {
		return this.code;
	}

	public SymbolTableEntry getParam1() {
		return this.param1;
	}

	public SymbolTableEntry getParam2() {
		return this.param2;
	}

	public SymbolTableEntry getResult() {
		return this.result;
	}


	public String stringify(int longestLabel) {
		if(code == TacCode.LABEL)
			return result.getLexeme();

		String param1String = (param1 == null) ? "" : param1.getLexeme();
		String param2String = (param2 == null) ? "" : param2.getLexeme();
		String resultString = (result == null) ? "" : result.getLexeme();

		int param1_intendetion = longestLabel > 11 ? param1_intendetion = longestLabel + 4 : 15;



		return String.format("%10s", code) +
		 		String.format("%" + param1_intendetion + "s", param1String) +
		 		String.format("%15s", param2String) +
		 		String.format("%15s", resultString) + "\n";
	}
}
