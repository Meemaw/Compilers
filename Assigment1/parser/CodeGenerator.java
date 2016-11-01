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


	public void functionParameters(ArrayList<Token> params) {
		for(Token parameter : params) {
			generate(TacCode.FPARAM, null, null, parameter.getSymbolTableEntry());
		}
	}



	public void printCode() {
		for(Quadruple quadruple : code.getQuadruples()) {
			System.out.println(quadruple);
		}
	}



	

}