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
Optional_fraction = ([.]{Digits})?
Optional_exponent = ((E[+|-]?){Digits})?
Number = {Digits}{Optional_fraction}{Optional_exponent}
Incdecop = ("++"|"--")
Relop = ("=="|"<="|">="|"<"|">"|"!=")
Addop = ("+"|"-"|"||")
Mulop = ("*"|"/"|"%"|"&&")
Operator = ({Incdecop}| {Relop} | {Addop} | {Mulop})


WS = [ \n\t]+

%%


{Identifier} { System.out.println("Identifier: ’" + yytext() + "’");   return 1; }
{Operator}  {System.out.println("Operator: ’" + yytext() + "’"); return 3;}
{Number} { System.out.println("Number: ’" + yytext() + "’"); return 2;}
{WS} { /* Ignore whitespace */ }
[^\ ]+ { System.out.println("Unknown sequence: ’" + yytext() + "’"); return 4; }






