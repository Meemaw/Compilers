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
}
