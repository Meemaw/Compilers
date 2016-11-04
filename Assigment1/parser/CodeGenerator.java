package parser;

import java.util.ArrayList;
import analyzer.Token;


public class CodeGenerator {

	private QuadrupleList code;

	public CodeGenerator() {
		code = new QuadrupleList();
	}

	public void generate(TacCode tacCode, SymbolTableEntry param1, SymbolTableEntry param2, SymbolTableEntry result) {
		code.addQuadruple(new Quadruple(tacCode, param1, param2, result));
	}

	public void generate(Quadruple quadruple) {
		code.addQuadruple(quadruple);
	}

	public void functionParameters(ArrayList<SymbolTableEntry> params) {
		for(SymbolTableEntry entry : params) {
			generate(TacCode.FPARAM, null, null, entry);
		}
	}

	public void pushArguments(ArrayList<SymbolTableEntry> args) {
		for(SymbolTableEntry entry : args) {
			generate(TacCode.APARAM, null, null, entry);
		}
	}

	public void printCode() {
		for(Quadruple quadruple : code.getQuadruples()) {
			System.out.println(quadruple);
		}
	}


}
