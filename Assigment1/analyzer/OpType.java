package analyzer;

public enum OpType {
  NONE, INC, DEC, EQUAL, NOT_EQUAL, LT, GT, LTE, GTE, 
  PLUS, MINUS, OR, MULT, DIV, MOD, AND, ASSIGN;

  // TODO: add checks for all operators
  public static OpType opType(String operator) {
  	switch(operator) {
  		case "+":
  			return OpType.PLUS;
  		case "-":
  			return OpType.MINUS;
  		case "++":
  			return OpType.INC;
  		case "--":
  			return OpType.DEC;
  		case "==":
  			return OpType.EQUAL;
  		case "!=":
  			return OpType.NOT_EQUAL;
  		case "*":
  			return OpType.MULT;
  		case "/":
  			return OpType.DIV;
  		case "%":
  			return OpType.MOD;
  		default:
  			return OpType.NONE;

  	} 
  }

}
