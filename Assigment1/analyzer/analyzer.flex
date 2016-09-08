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
	private Token token(DataType dt, TokenCode tc,  OpType ot) {
		return new Token(dt, tc, ot, null);
	}

	private Token token(DataType dt, TokenCode tc, OpType ot, SymbolTableEntry ste) {
		return new Token(dt, tc, ot, ste);
	}

	private OpType opType(String x) {
		// TODO - Add checks for operator
		return OpType.PLUS;
	}
%}

// %debug
%eof{
  System.out.println("\n");
%eof}

%eofval{
  return token(DataType.NONE, TokenCode.EOF, OpType.NONE);
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
	{Identifier} { return token( DataType.ID, TokenCode.IDENTIFIER, OpType.NONE); }
	{Int} { return token(DataType.INT, TokenCode.NUMBER, OpType.NONE); }
	{Real} { return token(DataType.REAL, TokenCode.NUMBER, OpType.NONE);}
	{Incdecop} { return token(DataType.OP, TokenCode.INCDECOP, opType(yytext())); }
	{Relop} { return token(DataType.OP, TokenCode.RELOP, opType(yytext())); }
	{Addop} { return token(DataType.OP, TokenCode.ADDOP, opType(yytext())); }
	{Mulop} { return token(DataType.OP, TokenCode.MULOP, opType(yytext())); }
	{Comment} { /* Ignore comment */ }
}









[^\ ]+ {  System.out.println("Unknown(" + yytext() +")"); }







