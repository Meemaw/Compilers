%%
%class Lexer
%unicode
%line
%column
%standalone
// %debug
%eof{
  System.out.println();
%eof}
Letter     = [A-Za-z]
Digit      = [0-9]
Identifier = {Letter} ({Letter} | {Digit})*
Digits = {Digit}+
WS = [ \n\t]+

%%


{Identifier} { System.out.println("Identifier: ’" + yytext() + "’");   return 1; }
{Digits} { System.out.println("Digits: ’" + yytext() + "’"); return 2;}
{WS} { /* Ignore whitespace */ }
[^\ ]+ { System.out.println("Unknown sequence: ’" + yytext() + "’"); return 3; }




