import java.io.*;
import analyzer.*;

public class TokenDumper {




	public static void main(String[] args) throws  IOException {
		Lexer lexer = new Lexer(new FileReader(args[0]));
		SymbolTable table = new SymbolTable();
		int index = 0;

		while(true) {
			Token t = lexer.yylex();

			TokenCode tokenCode = t.getTokenCode();
			DataType dataType = t.getDataType();
			OpType opType = t.getOpType();


			switch(dataType) {
				case ID:
				case INT:
				case REAL:
					SymbolTableEntry entry = t.getSymbolTableEntry();
					if(table.contains(entry) == null) {
						table.put(entry, index);
						index++;
					}
					System.out.print(tokenCode.toString() + "(" + entry.getLexeme() +")");
					break;
				case OP:
					if(opType == OpType.NONE || opType == OpType.ASSIGN) 
						System.out.print(tokenCode.toString());
					else 
						System.out.print(tokenCode.toString()  + "(" + t.getOpType() + ")");
					break;
				default:
					System.out.print(tokenCode.toString());
					break;
			}

			
			if(tokenCode == TokenCode.EOF) {
				System.out.println("\n");
				table.printSymbolTable();
				break;
			}
			System.out.print(" ");

		}
	}
}
