package parser;

import java.util.ArrayList;
import analyzer.Token;


public class CodeGenerator {

	private QuadrupleList code;
	private int longestLabelLength;

	public CodeGenerator() {
		code = new QuadrupleList();
		longestLabelLength = 0;
	}

	public void generate(TacCode tacCode, SymbolTableEntry param1, SymbolTableEntry param2, SymbolTableEntry result) {
		code.addQuadruple(new Quadruple(tacCode, param1, param2, result));
		if(tacCode == TacCode.LABEL && result.getLexeme().length() > longestLabelLength)
			longestLabelLength = result.getLexeme().length() + 1;
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
		for(int i = 0; i < code.getQuadruples().size(); i++) {
			Quadruple temp = code.getQuadruples().get(i);
			if(temp.getTacCode() != TacCode.LABEL) {
				if(longestLabelLength <= 8) {
					System.out.print(String.format("%64s", temp.toString()));
				}
				else {
					int actual = 64 + (longestLabelLength - 8);
					System.out.print(String.format("%" + actual +"s", temp.toString()));
				}
			} else {
				Quadruple next = code.getQuadruples().get(++i);
				if(longestLabelLength <= 8) {
					System.out.print(String.format("%8s%55s", temp.toString() + ":", next.toString()));
				}
				else {
					System.out.print(String.format("%" + longestLabelLength + "s%55s", temp.toString() + ":", next.toString()));
				}
			}
		}
	}


}
