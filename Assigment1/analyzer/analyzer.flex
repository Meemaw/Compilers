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
%public
%type Token

// Custom java code there
%{
	private Token token(DataType dt, TokenCode tc,  OpType ot) {
		return new Token(dt, tc, ot, null);
	}

	private Token token(DataType dt, TokenCode tc, OpType ot, SymbolTableEntry ste) {
		return new Token(dt, tc, ot, ste);
	}

	private SymbolTableEntry entry(String x) {
		return new SymbolTableEntry(x);
	}
%}

// %debug
%eof{
%eof}

%eofval{
  return token(DataType.NONE, TokenCode.EOF, OpType.NONE);
%eofval}


WS = [ \n\t\r]+

InputCharacter = [^\r\n]
LineTerminator = \r|\n|\r\n
MultiLineComment = ("/*" [^*]*  "*/")
NormalComment = "//" {InputCharacter}* {LineTerminator}?
Comment = {NormalComment} | {MultiLineComment}
WS = {LineTerminator} | [ \t\f]
Letter_ = ([A-Za-z]|"_")
Digit      = [0-9]
Id = {Letter_}({Letter_}|{Digit})*
Digits = {Digit}+
Optional_fraction = ([.]{Digits})?
Optional_exponent = ((E[+|-]?){Digits})?
Int = 0 | [1-9]{Digit}*
Real = {Digits}{Optional_fraction}{Optional_exponent}

%%

{WS} { /* Ignore whitespace */ }
"+" { return token(DataType.OP, TokenCode.ADDOP, OpType.PLUS); }
"-" { return token(DataType.OP, TokenCode.ADDOP, OpType.MINUS); }
"||" { return token(DataType.OP, TokenCode.ADDOP, OpType.OR); }
"==" { return token(DataType.OP, TokenCode.RELOP, OpType.EQUAL); }
"<=" { return token(DataType.OP, TokenCode.RELOP, OpType.LTE); }
">=" { return token(DataType.OP, TokenCode.RELOP, OpType.GTE); }
"<" { return token(DataType.OP, TokenCode.RELOP, OpType.LT); }
">" { return token(DataType.OP, TokenCode.RELOP, OpType.GT); }
"!=" { return token(DataType.OP, TokenCode.RELOP, OpType.NOT_EQUAL); }
"++" { return token(DataType.OP, TokenCode.INCDECOP, OpType.INC); }
"--" { return token(DataType.OP, TokenCode.INCDECOP, OpType.DEC); }
"*" { return token(DataType.OP, TokenCode.MULOP, OpType.MULT); }
"/" { return token(DataType.OP, TokenCode.MULOP, OpType.DIV); }
"%" { return token(DataType.OP, TokenCode.MULOP, OpType.MOD); }
"&&" { return token(DataType.OP, TokenCode.MULOP, OpType.AND); }
"=" { return token(DataType.OP, TokenCode.ASSIGNOP, OpType.ASSIGN); }
";" { return token(DataType.KEYWORD, TokenCode.SEMICOLON, OpType.NONE); }
"," { return token(DataType.KEYWORD, TokenCode.COMMA, OpType.NONE); }
"!" { return token(DataType.KEYWORD, TokenCode.NOT, OpType.NONE); }
")" { return token(DataType.KEYWORD, TokenCode.RPAREN, OpType.NONE); }
"(" { return token(DataType.KEYWORD, TokenCode.LPAREN, OpType.NONE); }
"[" { return token(DataType.KEYWORD, TokenCode.LBRACKET, OpType.NONE); }
"]" { return token(DataType.KEYWORD, TokenCode.RBRACKET, OpType.NONE); }
"{" { return token(DataType.KEYWORD, TokenCode.LBRACE, OpType.NONE); }
"}" { return token(DataType.KEYWORD, TokenCode.RBRACE, OpType.NONE); }
"break" { return token(DataType.KEYWORD, TokenCode.BREAK, OpType.NONE); }
"class" { return token(DataType.KEYWORD, TokenCode.CLASS, OpType.NONE); }
"static" { return token(DataType.KEYWORD, TokenCode.STATIC, OpType.NONE); }
"void" { return token(DataType.KEYWORD, TokenCode.VOID, OpType.NONE); }
"int" { return token(DataType.KEYWORD, TokenCode.INT, OpType.NONE); }
"real" { return token(DataType.KEYWORD, TokenCode.REAL, OpType.NONE); }
"if" { return token(DataType.KEYWORD, TokenCode.IF, OpType.NONE); }
"else" { return token(DataType.KEYWORD, TokenCode.ELSE, OpType.NONE); }
"for" { return token(DataType.KEYWORD, TokenCode.FOR, OpType.NONE); }

{Id} { return token(DataType.ID, TokenCode.IDENTIFIER, OpType.NONE, entry(yytext())); }
{Int} { return token(DataType.INT, TokenCode.NUMBER, OpType.NONE, entry(yytext())); }
{Real} { return token(DataType.REAL, TokenCode.NUMBER, OpType.NONE, entry(yytext()));}
{Comment} { /* Ignore comment */ }








. {  System.out.println("Unknown(" + yytext() +")"); }







