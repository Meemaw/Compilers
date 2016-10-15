import java.io.*;
import analyzer.*;

public class Compiler {

	public static void main(String[] args) throws IOException {
		Lexer lexer = new Lexer(new FileReader(args[0]));
		SymbolTable table = new SymbolTable();


		Parser p = new Parser(lexer,table);


	}

}

