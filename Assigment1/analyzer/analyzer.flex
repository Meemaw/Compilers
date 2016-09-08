package analyzer;
/* Imports */
import analyzer.Token;
import analyzer.TokenCode;
import analyzer.SymbolTableEntry;
import analyzer.DataType;
import analyzer.OpType;


%%
%class Lexer
%unicode
%line
%column
%standalone
%type Token

// Custom java code there
%{
	private Token token(TokenCode tc, DataType dt, OpType ot) {
		return new Token(tc,dt,ot, null);
	}

	private Token token(TokenCode tc, DataType dt, OpType ot, SymbolTableEntry ste) {
		return new Token(tc, dt, ot, ste);
	}

	private Token token(TokenCode tc, DataType dt) {
		return new Token(tc, dt, null, null);
	}

%}

// %debug
%eof{
  System.out.println("\n");
%eof}

%eofval{
  return token(TokenCode.EOF, DataType.NONE);
%eofval}


WS = [ \n\t]+
Comment = ("/*" [^*]*  "*/")
Letter     = [A-Za-z]
Digit      = [0-9]
Digits = {Digit}+
Optional_fraction = ([.]{Digits})?
Optional_exponent = ((E[+|-]?){Digits})?
Int = 0 | [1-9]{Digit}*
Real = {Digits}{Optional_fraction}{Optional_exponent}
Number = ({Int} | {Real}) [;]?
Identifier = [:jletter:] [:jletterdigit:]*
Incdecop = ("++"|"--")
Relop = ("=="|"<="|">="|"<"|">"|"!=")
Addop = ("+"|"-"|"||")
Mulop = ("*"|"/"|"%"|"&&")


%%

<YYINITIAL> {
	"break" { /* Ignore for now */ }
	{WS} { /* Ignore whitespace */ }
	{Identifier} { return token(TokenCode.IDENTIFIER, DataType.ID); }
	{Int} { return token(TokenCode.NUMBER, DataType.INT); }
	{Real} { return token(TokenCode.NUMBER, DataType.REAL);}
	{Incdecop} { return token(TokenCode.INCDECOP, DataType.OP); }
	{Relop} { return token(TokenCode.RELOP, DataType.OP); }
	{Addop} { return token(TokenCode.ADDOP, DataType.OP); }
	{Mulop} { return token(TokenCode.MULOP, DataType.OP); }
	{Comment} { /* Ignore comment */ }
}









[^\ ]+ {  System.out.println("Unknown(" + yytext() +")"); }







