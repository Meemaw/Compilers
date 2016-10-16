import analyzer.*;
import java.util.ArrayList;
import java.io.IOException;

public class Parser{

	private Lexer lexer;
	private SymbolTable symbolTable;
	private ArrayList<ParseException> errorList;
	private Token currentToken;

	public Parser(Lexer lexer, SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
		this.lexer = lexer;
		this.errorList =  new ArrayList<>();
	}

	public ArrayList<ParseException> parse() throws IOException {
		currentToken = lexer.yylex();
		program();


		return errorList;
	}


	private void program() throws IOException {
		expect(TokenCode.CLASS);
		expect(TokenCode.IDENTIFIER);
		expect(TokenCode.LBRACE);

		variable_declarations();
		method_declarations();

		expect(TokenCode.LBRACE);
	}

	private void variable_declarations() {

	}

	private void method_declarations() {

	}


	private boolean accept(TokenCode code) throws IOException{
		if(currentToken.getTokenCode() == code) {
			currentToken = lexer.yylex();
			return true;
		}
		return false;
	}

	private boolean expect(TokenCode code) throws IOException{
		if(accept(code)) return true;
		errorList.add(new ParseException(currentToken+""));
		return false;
	}




}
