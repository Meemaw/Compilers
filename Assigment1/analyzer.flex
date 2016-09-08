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

// Custom java code there
%{
	private Token token(TokenCode tc, DataType dt, OpType ot) {
		return new Token(tc,dt,ot);
	}

	private Token token(TokenCode tc, DataType dt, OpType ot, SymbolTableEntry ste) {
		return new Token(tc, dt, ot, ste);
	}

	private Token token(TokenCode tc, DataType dt) {
		return new Token(tc, dt);
	}

%}

// %debug
%eof{
  System.out.println("\n");
%eof}


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
	{Identifier} { return token(IDENTIFIER, ID); }
	{Int} { return token(NUMBER, INT); }
	{Real} { return token(NUMBER, REAL);}
	{Incdecop} { return token(INCDECOP, OP); }
	{Relop} { return token(RELOP, OP); }
	{Addop} { return token(ADDOP, OP); }
	{MulOp} { return token(MULOP, OP); }
	{Comment} { /* Ignore comment */ }
}









[^\ ]+ {  System.out.println("Unknown(" + yytext() +")"); }







