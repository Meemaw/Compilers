package parser;

import java.util.ArrayList;
import analyzer.Token;


public class CodeGenerator {

	private QuadrupleList code;
	private int longestLabelLength;
	private int longestParam1Length;
	private int longestParam2Length;
	private int longestResultLength;

	public CodeGenerator() {
		code = new QuadrupleList();
		longestLabelLength = 0;
		longestResultLength = 0;
		longestParam1Length = 0;
		longestParam2Length = 0;
	}

	public void generate(TacCode tacCode, SymbolTableEntry param1, SymbolTableEntry param2, SymbolTableEntry result) {
		code.addQuadruple(new Quadruple(tacCode, param1, param2, result));
		if(tacCode == TacCode.LABEL && result.getLexeme().length() > longestLabelLength)
			longestLabelLength = result.getLexeme().length() + 1;
		if(param1 != null && param1.getLexeme().length() > longestParam1Length)
			longestParam1Length = param1.getLexeme().length();
		if(param2 != null && param2.getLexeme().length() > longestParam2Length)
			longestParam2Length = param2.getLexeme().length();
		if(result != null && result.getLexeme().length() > longestResultLength)
			longestResultLength = result.getLexeme().length();
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
		int label_format = (longestLabelLength <= 8) ? 8 : longestLabelLength;
		int param1_format = (longestParam1Length < 11) ? 15 : longestParam1Length + 4;
		int param2_format = (longestParam2Length < 11) ? 15 : longestParam2Length + 4;
		int result_format = (longestResultLength < 11) ? 15 : longestResultLength + 4;

		for(int i = 0; i < code.getQuadruples().size(); i++) {
			Quadruple temp = code.getQuadruples().get(i);
			Pentatuple current;
			if(temp.getTacCode() != TacCode.LABEL) 
				current = new Pentatuple(temp.getTacCode(),
										 temp.getParam1(),
										 temp.getParam2(),
										 temp.getResult());
			else {
				Quadruple next = code.getQuadruples().get(++i);
				current = new Pentatuple(temp.getResult(),
										 next.getTacCode(),
										 next.getParam1(),
										 next.getParam2(),
										 next.getResult());
			}

			System.out.println(current.stringify(label_format,
												param1_format,
												param2_format,
												result_format));
		}
	}
}
