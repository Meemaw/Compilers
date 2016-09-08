package analyzer;

public enum OpType {
  NONE, INC, DEC, EQUAL, NOT_EQUAL, LT, GT, LTE, GTE, 
  PLUS, MINUS, OR, MULT, DIV, MOD, AND, ASSIGN;

  // TODO: add checks for all operators
  public static OpType opType(String x) {
  	if(x.equals("+")) return OpType.PLUS;
  	else return OpType.MINUS;
  }

}
