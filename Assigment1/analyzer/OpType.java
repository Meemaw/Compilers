package analyzer;

public enum OpType {
  NONE, INC, DEC, EQUAL, NOT_EQUAL, LT, GT, LTE, GTE, 
  PLUS, MINUS, OR, MULT, DIV, MOD, AND, ASSIGN;

  // TODO: add checks for all operators
  public static OpType opType(String operator) {

    // TODO: add them
  	switch(operator) {
      case "++":
        return OpType.INC;
      case "--":
        return OpType.DEC;
      case "==":
        return OpType.EQUAL;
      case "!=":
        return OpType.NOT_EQUAL;
      case "<":
        return OpType.LT;
      case ">":
        return OpType.GT;
      case "<=":
        return OpType.LTE;
      case ">=":
        return OpType.GTE;
  		case "+":
  			return OpType.PLUS;
  		case "-":
  			return OpType.MINUS;
      case "||":
        return OpType.OR;
  		case "*":
  			return OpType.MULT;
  		case "/":
  			return OpType.DIV;
  		case "%":
  			return OpType.MOD;
      case "&&":
        return OpType.AND;
      case "=":
        return OpType.ASSIGN;
  		default:
  			return OpType.NONE;

  	} 
  }
}
