import java.io.*;
import analyzer.*;

public class TokenDumper {


	public static void main(String[] args) throws  IOException {
		Lexer lexer = new Lexer(new FileReader(args[0]));

		while(true) {
			Token t = lexer.yylex();

			TokenCode tokenCode = t.getTokenCode();
			DataType dataType = t.getDataType();
			OpType opType = t.getOpType();


			switch(dataType) {
				case ID:
				case INT:
				case REAL:
					System.out.print(tokenCode.toString() + "(" + t.getSymbolTableEntry().getLexeme() +")");
					break;
				case OP:
					System.out.print(tokenCode.toString()  + "(" + t.getOpType() + ")");
					break;

				default:
					System.out.print(tokenCode.toString());
					break;
			}


			System.out.print(" ");
			if(tokenCode == TokenCode.EOF) {
				break;
			}
		}
	}
}