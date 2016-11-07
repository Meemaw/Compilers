package parser;

import java.util.ArrayList;

public class QuadrupleList {

	private ArrayList<Quadruple> quadruples;

	public QuadrupleList() {
		this.quadruples = new ArrayList<>();
	}

	public ArrayList<Quadruple> getQuadruples() {
		return this.quadruples;
	}

	public void addQuadruple(Quadruple quadruple) {
		quadruples.add(quadruple);
	}

	public void setQuadrupleList(ArrayList<Quadruple> quadrupleList) {
		this.quadruples = quadrupleList;
	}
}
