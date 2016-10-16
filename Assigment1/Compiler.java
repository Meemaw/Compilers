import java.io.*;
import java.util.ArrayList;
import analyzer.*;

public class Compiler {

	public static void main(String[] args) throws IOException {
		Lexer lexer = new Lexer(new FileReader(args[0]));
		SymbolTable table = new SymbolTable();


		Parser p = new Parser(lexer,table);

		ArrayList<ParseException> error_list = p.parse();

		if (error_list.isEmpty())
			System.out.println("No errors\n");

	}

}

