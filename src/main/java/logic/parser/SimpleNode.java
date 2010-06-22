/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package main.java.logic.parser;

import java.lang.String;
import java.util.Arrays;
import java.util.Vector;
import java.io.Serializable;
import main.java.QPar;


import main.java.master.MasterDaemon;
import org.apache.log4j.Logger;
import org.apache.log4j.Level;

// All nodes in the formula tree are derived from SimpleNode.
public class SimpleNode implements Node, Serializable {

	static Logger logger = Logger.getLogger(SimpleNode.class);
	static { 
		logger.setLevel(QPar.logLevel);
	}
	
	protected Object value;
	protected Qbf_parser parser;
	protected Node parent;
	protected Node[] children;
	protected NodeType nodeType = null;

	public int id; // TODO check if needed
	public int var = -1; // -1 = not a var node
	public String op = ""; // "" = not an operator node
	public String truthValue = ""; // "" = not truth assigned
	public enum NodeType {
		START, VAR, FORALL, EXISTS, AND, OR, NOT, TRUE, FALSE
	}

	/**
	 * constructor
	 */
	public SimpleNode() {
		logger.setLevel(QPar.logLevel);
	}

	public void setNodeType(NodeType nt) {
		this.nodeType = nt;
	}

	public NodeType getNodeType() {
		return nodeType;
	}

	public int getNodeVariable() {
		return var;
	}

	public void setNodeVariable(int v) {
		this.var = v;
	}

	/**
	 * assign a truth value to a specific var
	 * 
	 * @param v
	 *            the var to assign a truth value to
	 * @param b
	 *            the truth value to assign
	 */
	public void assignTruthValue(int v, boolean b) {
		int numChildren = this.jjtGetNumChildren();

		// not in a leaf node, nothing to set
		if (numChildren > 0) {
			for (int i = 0; i < numChildren; i++) {
				jjtGetChild(i).assignTruthValue(v, b);
			}
		}
		// in a leaf node now
		else {
			if (var == v) {
				if (b) {
					truthValue = "TRUE";
				} else {
					truthValue = "FALSE";
				}
			}
		}
	}

	/**
	 * Checks if a node is somehow connected to the root node.
	 * 
	 * @return True if there's a path from this node to the root, false
	 *         otherwise
	 */
	public boolean checkConnectionToRoot() {
		// this might be the root node itself, so obviously there's a connection
		if (this.getClass().getName().equals("main.java.logic.parser.ASTInput"))
			return true;

		// the node may also be an orphan, so no connection to root
		if (this.jjtGetParent() == null)
			return false;

		// or, if the node is neither root itself nor orphaned, check the parent
		return this.jjtGetParent().checkConnectionToRoot();
	}

	/**
	 * Collects negative literals as long as they're children of nodes with the
	 * given && or || operator
	 * 
	 * @return A vector of negative literals
	 * @param op
	 *            The operator the negative literals should be children of
	 * @param v
	 *            A vector of all already collected negative literals
	 */
	public Vector<Integer> getPositiveLiterals(String op, Vector<Integer> v) {
		for (int i = 0; i < this.jjtGetNumChildren(); i++) {
			// if the child is a var node, just add the var number
			if (this.jjtGetChild(i).getVar() > -1) {
				v.add(this.jjtGetChild(i).getVar());
			}

			// nested con/disjunction, go deeper in the tree and collect
			// literals there
			if (this.jjtGetChild(i).getOp().equals(op)) {
				v = (this.jjtGetChild(i).getPositiveLiterals(op, v));
			}
		}
		return v;
	}

	/**
	 * Collects positive literals as long as they're children of nodes with the
	 * given && or || operator
	 * 
	 * @return A vector of positive literals
	 * @param op
	 *            The operator the positive literals should be children of
	 * @param v
	 *            A vector of all already collected positive literals
	 */
	public Vector<Integer> getNegativeLiterals(String op, Vector<Integer> v) {
		for (int i = 0; i < this.jjtGetNumChildren(); i++) {
			// if the child is a var node, just add the var number
			if (this.jjtGetChild(i).getOp().equals("!")) {
				v.add(this.jjtGetChild(i).jjtGetChild(0).getVar());
			}

			// nested con/disjunction, go deeper in the tree and collect
			// literals there
			if (this.jjtGetChild(i).getOp().equals(op)) {
				v = (this.jjtGetChild(i).getNegativeLiterals(op, v));
			}
		}
		return v;
	}

	/**
	 * This gets enclosed formulas
	 * 
	 * @return A qpro formatted subformula
	 * @param op
	 *            ICH KENN MICH GRAD SELBST NICHT MEHR AUS :) TODO
	 */
	public String getEnclosedFormula(String op) {
		String tmp = "";
		for (int i = 0; i < this.jjtGetNumChildren(); i++) {
			if (this.jjtGetChild(i).getOp().equals(op)) {
				tmp += this.jjtGetChild(i).traverse();
			} else {
				tmp += this.jjtGetChild(i).getEnclosedFormula(op);
			}
		}
		return tmp;
	}

	/**
	 * traverse tree goes through all children of a node and builds a String in
	 * .qpro format
	 * 
	 * @return A String in qpro format
	 */
	public String traverse() {
		Node child;
		String tmp = "";
		String[] tmpList;
		String traversedTree = "";
		String partialTree = "";
		String negatedPartialTree = "";
		String enclosedPartialTree = "";
		Vector<Integer> posLiterals = new Vector<Integer>();
		Vector<Integer> negLiterals = new Vector<Integer>();

		if (this.getOp().equals("&")) {
			traversedTree += "c\n";
			posLiterals = (this.getPositiveLiterals("&", posLiterals));
			negLiterals = (this.getNegativeLiterals("&", negLiterals));

			for (int var : posLiterals)
				traversedTree += var + " ";
			traversedTree += "\n";

			for (int var : negLiterals)
				traversedTree += var + " ";
			traversedTree += "\n";

			traversedTree += this.getEnclosedFormula("|");

			traversedTree += "/c\n";
		}

		if (this.getOp().equals("|")) {
			traversedTree += "d\n";
			posLiterals = (this.getPositiveLiterals("|", posLiterals));
			negLiterals = (this.getNegativeLiterals("|", negLiterals));

			for (int var : posLiterals)
				traversedTree += var + " ";
			traversedTree += "\n";

			for (int var : negLiterals)
				traversedTree += var + " ";
			traversedTree += "\n";

			traversedTree += this.getEnclosedFormula("&");

			traversedTree += "/d\n";
		}

		return traversedTree;
	}

	/**
	 * reduces a tree containung truth-assigned variables to a tree without them
	 * 
	 * @return true if tree is still traversable, false if not
	 */
	public boolean reduce() {
		Node parentNode = null;
		Node grandparentNode = null;
		Node siblingNode = null;
		int i = 0;
		boolean reducable = false;

		if (this.jjtGetNumChildren() > 0) { // we're not in a leaf node...
			for (i = 0; i < this.jjtGetNumChildren(); i++) { // ... so we just
																// traverse
																// through all
																// it's children
				if (jjtGetChild(i).checkConnectionToRoot())
					reducable = jjtGetChild(i).reduce() || reducable;
			}
		} else { // we're in a leaf node...
			parentNode = this.jjtGetParent();

			if ((this.truthValue.equals("TRUE"))
					|| (this.truthValue.equals("FALSE"))) {
				// we're in a truth-assigned leaf node, let's see what to do

				// if we're in the logical root node, then there's no more
				// reducing
				// even if it has a truth value assigned, else the tree might be
				// even more reducable
				if (parentNode.getClass().getName().equals(
						"main.java.logic.parser.ASTInput")) {
					logger.debug("RETURNING FALSE");
					return false;
				}
				reducable = true;

				// not x, set the parent to not x
				if (parentNode.getOp().equals("!")) {
					logger.debug("NEGATION occured");
					parentNode.setOp("");
					if (truthValue.equals("FALSE")) {
						parentNode.setTruthValue("TRUE");
					} else {
						parentNode.setTruthValue("FALSE");
					}
					parentNode.deleteChildren();
					jjtSetParent(null);
					return reducable;
				}

				// false & x = false, so set parent to false and make it a leaf
				// node
				if ((parentNode.getOp().equals("&"))
						&& (truthValue.equals("FALSE"))) {
					logger.debug("AND FALSE occured");
					parentNode.setOp("");
					parentNode.setTruthValue("FALSE");
					parentNode.deleteChildren();
					jjtSetParent(null);
					logger.debug("AND FALSE occured end");
					return reducable;
				}

				// true & x = x, so delete this node, replace the parent node
				// with
				// the sibling
				if ((parentNode.getOp().equals("&"))
						&& (truthValue.equals("TRUE"))) {
					logger.debug("AND TRUE occured");
					// get grandparent
					grandparentNode = parentNode.jjtGetParent();
					// find sibling
					for (i = 0; i < parentNode.jjtGetNumChildren(); i++) {
						if (parentNode.jjtGetChild(i) != this) {
							siblingNode = parentNode.jjtGetChild(i);
						}
					}
					// make sibling grandparents child
					grandparentNode.replaceChild(parentNode, siblingNode);
					// make grandparent siblings parent
					siblingNode.jjtSetParent(grandparentNode);
					// remove old parents children and parent
					parentNode.deleteChildren();
					parentNode.jjtSetParent(null);
					// remove current nodes parent
					jjtSetParent(null);
					logger.debug("AND TRUE occured end");
					return reducable;
				}

				// false | x = x, so delete this node, replace the parent node
				// with
				// the sibling
				if ((parentNode.getOp().equals("|"))
						&& (truthValue.equals("FALSE"))) {
					logger.debug("OR FALSE occured");
					// get grandparent
					grandparentNode = parentNode.jjtGetParent();
					// find sibling
					for (i = 0; i < parentNode.jjtGetNumChildren(); i++) {
						if (parentNode.jjtGetChild(i) != this) {
							siblingNode = parentNode.jjtGetChild(i);
						}
					}
					// make sibling grandparents child
					grandparentNode.replaceChild(parentNode, siblingNode);
					// make grandparent siblings parent
					siblingNode.jjtSetParent(grandparentNode);
					// remove old parents children and parent
					parentNode.deleteChildren();
					parentNode.jjtSetParent(null);
					// remove current nodes parent
					jjtSetParent(null);
					logger.debug("OR FALSE occured end");
					return reducable;
				}

				// true | x = true, so set the parent node to true and make it a
				// leaf
				if ((parentNode.getOp().equals("|"))
						&& (truthValue.equals("TRUE"))) {
					logger.debug("OR TRUE occured");
					parentNode.setOp("");
					parentNode.setTruthValue("TRUE");
					parentNode.deleteChildren();
					jjtSetParent(null);
					logger.debug("OR TRUE occured end");
					return reducable;
				}
			}
		}
		return reducable;
	}

	/**
	 * removes all children of a node by setting the childrens parent to null
	 * and cleaning the children[] array. Hopefully the garbage collector will
	 * really delete them
	 */
	public void deleteChildren() {
		for (int i = 0; i < jjtGetNumChildren(); i++) {
			jjtGetChild(i).jjtSetParent(null);
		}
		children = null;
	}

	/**
	 * replaces node old with node new in the parent list of a node
	 * 
	 * @param oldNode
	 *            the node to be replaced
	 * @param newNode
	 *            the node that will take the old nodes place
	 * @return true if success, false if the node to replace was not found
	 */
	public boolean replaceChild(Node oldNode, Node newNode) {
		for (int i = 0; i < jjtGetNumChildren(); i++) {
			if (jjtGetChild(i) == oldNode) {
				children[i] = newNode;
				return true;
			}
		}
		return false;
	}

	/**
	 * search for at least one occurance of car v in the tree
	 * 
	 * @param v
	 *            the var to search for
	 * @return true if at least one occurance, false otherwise
	 */
	public boolean findVar(int v) {
		int i;
		boolean found = false;

		if (this.jjtGetNumChildren() > 0) {
			for (i = 0; i < this.jjtGetNumChildren(); i++) {
				found = found || this.jjtGetChild(i).findVar(v);
			}
		} else {
			if (this.var == v) {
				found = true;
			}
		}
		return found;
	}

	// mostly auto-generated stuff from here plus some simple getter/setter
	// methods
	// one doesn't really need because all vars are public anyway :)
	public void setTruthValue(String t) {
		this.truthValue = t;
	}

	public String getTruthValue() {
		return truthValue;
	}

	public void setVar(int v) {
		this.var = v;
	}

	public int getVar() {
		return var;
	}

	public void setOp(String o) {
		this.op = o;
	}

	public String getOp() {
		return op;
	}

	public int getId() {
		return id;
	}

	public SimpleNode(int i) {
		id = i;
	}

	public SimpleNode(Qbf_parser p, int i) {
		this(i);
		parser = p;
	}

	public void jjtOpen() {
	}

	public void jjtClose() {
	}

	public void jjtSetParent(Node n) {
		parent = n;
	}

	public Node jjtGetParent() {
		return parent;
	}

	public void jjtAddChild(Node n, int i) {
		if (children == null) {
			children = new Node[i + 1];
		} else if (i >= children.length) {
			Node c[] = new Node[i + 1];
			System.arraycopy(children, 0, c, 0, children.length);
			children = c;
		}
		children[i] = n;
	}

	public Node jjtGetChild(int i) {
		return children[i];
	}

	public int jjtGetNumChildren() {
		return (children == null) ? 0 : children.length;
	}

	public void jjtSetValue(Object value) {
		this.value = value;
	}

	public Object jjtGetValue() {
		return value;
	}

	/*
	 * You can override these two methods in subclasses of SimpleNode to
	 * customize the way the node appears when the tree is dumped. If your
	 * output uses more than one line you should override toString(String),
	 * otherwise overriding toString() is probably all you need to do.
	 */

	public String toString() {
		return Qbf_parserTreeConstants.jjtNodeName[id] + " (op= " + op
				+ ", var= " + var + " " + truthValue + ")";
	}

	public String toString(String prefix) {
		return prefix + toString();
	}

	/*
	 * Override this method if you want to customize how the node dumps out its
	 * children.
	 */

	public void dump(String prefix) {
		System.out.println(toString(prefix));
		if (children != null) {
			for (int i = 0; i < children.length; ++i) {
				SimpleNode n = (SimpleNode) children[i];
				if (n != null) {
					n.dump(prefix + " ");
				}
			}
		}
	}

	public double getTruthProbability() {
		if (children != null) {
			if(this.getOp().equals("|")){
				assert (children.length == 2);
				return 1-((1-children[0].getTruthProbability())*(1-children[1].getTruthProbability()));
			} else if(this.getOp().equals("&")){
				assert (children.length == 2);
				return (children[0].getTruthProbability() * children[1].getTruthProbability());
			} else {
				assert (children.length == 1);				// We are a quantifier node
				return children[0].getTruthProbability();
			}
				
		}
		// We are a leaf(variable) node. So our P(T) = 0.5
		return 0.5;
	}
}

/*
 * JavaCC - OriginalChecksum=cd6460b90c70fa000dbb49fc278adf1f (do not edit this
 * line)
 */
