import java.io.*;
import analyzer.*;

public class TokenDumper {

	public static void main(String[] args) throws  IOException {
		Lexer lexer = new Lexer(new FileReader(args[0]));
		SymbolTable table = new SymbolTable();

		while(true) {
			Token t = lexer.yylex();

			TokenCode tokenCode = t.getTokenCode();
			DataType dataType = t.getDataType();
			OpType opType = t.getOpType();
			SymbolTableEntry entry = t.getSymbolTableEntry();


			switch(dataType) {
				case ID:
					if (tokenCode == TokenCode.ERR_LONG_ID) {
						System.out.print(tokenCode.toString());
						break;
					}
					if(table.contains(entry) == null)
						table.add(entry);
					System.out.print(tokenCode.toString() + "(" + entry.getLexeme() +")");
					break;
				case INT:
				case REAL:
					if(table.contains(entry) == null) {
						table.add(entry);
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
