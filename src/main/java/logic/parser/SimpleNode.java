/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package main.java.logic.parser;
import java.lang.String;

public class SimpleNode implements Node {
  protected Node parent;
  protected Node[] children;
  protected int id;
  protected Object value;
  protected Qbf_parser parser;

	public String debugInfo = "";
	public String op = "";
	public int var = -1;	
	
	/**
	* traverse tree goes through all children of a node and builds a String
	* in .qpro format
	*/	
	public String traverse() {
		int i;	
       	Node child;
       	int numChildren;
		String tmp = "";
		String traversedTree = "";

		// System.out.println(this.getClass().getName());
		if (parent != null) {
			if (parent.getClass().getName() == "main.java.logic.parser.ASTOp") {
				System.out.println("PARENT IS OP");
				Node grandparent = parent.jjtGetParent();
				Node grandgrandparent = grandparent.jjtGetParent();
				System.out.println("this class = " + this.getClass().getName());
				System.out.println("grandparent class = " + grandparent.getClass().getName());
				System.out.println("grandgrandparent class = " + grandgrandparent.getClass().getName());
				System.out.println("this op = " + this.op);
				System.out.println("this var = " + this.var);
				System.out.println		 (((grandparent.op == "" ) ? "" : "gp op " + grandparent.op) + ((grandparent.var == -1) ? "" : "gp var " + grandparent.var));
				System.out.println		 (((parent.op == "" ) ? "" : "gp op " + parent.op) + ((parent.var == -1) ? "" : "gp var " + parent.var));
//				System.out.println("parent op = " + parent.op);
//				System.out.println("parent var = " + parent.var);
			}
			else if (parent.getClass().getName() == "main.java.logic.parser.ASTQuant") {
				System.out.println("PARENT IS QUANTIFIER");
				Node grandparent = parent.jjtGetParent();
				Node grandgrandparent = grandparent.jjtGetParent();
				System.out.println("this class = " + this.getClass().getName());
				System.out.println("grandparent class = " + grandparent.getClass().getName());
				System.out.println("grandgrandparent class = " + grandgrandparent.getClass().getName());
				System.out.println("this op = " + this.op);
				System.out.println("this var = " + this.var);
//				System.out.println("grandparent op = " + grandparent.op);
//				System.out.println("grandparent var = " + grandparent.var);
				System.out.println		 (((grandparent.op == "" ) ? "" : "gp op " + grandparent.op) + ((grandparent.var == -1) ? "" : "gp var " + grandparent.var));
				System.out.println		 (((parent.op == "" ) ? "" : "gp op " + parent.op) + ((parent.var == -1) ? "" : "gp var " + parent.var));
	
			}
//			else if (parent.getClass().getName() == "main.java.logic.parser.ASTNot") {
//				System.out.println("PARENT IS NOT");
//				Node grandparent = parent.jjtGetParent();
//				Node grandgrandparent = grandparent.jjtGetParent();
//				System.out.println("this class = " + this.getClass().getName());
//				System.out.println("grandparent class = " + grandparent.getClass().getName());
//				System.out.println("grandgrandparent class = " + grandgrandparent.getClass().getName());
//				System.out.println("this op = " + this.op);
//				System.out.println("this var = " + this.var);
////				System.out.println("grandparent op = " + grandparent.op);
////				System.out.println("grandparent var = " + grandparent.var);
//				System.out.println		 (((grandparent.op == "" ) ? "" : "gp op " + grandparent.op) + ((grandparent.var == -1) ? "" : "gp var " + grandparent.var));
//				System.out.println		 (((parent.op == "" ) ? "" : "gp op " + parent.op) + ((parent.var == -1) ? "" : "gp var " + parent.var));	
//			}
		}		

		// find children
		numChildren = this.jjtGetNumChildren();
		// recursive through children
		if (numChildren > 0)  {
			for (i = 0; i < numChildren; i++) {
				tmp = jjtGetChild(i).traverse();
				traversedTree += tmp;
			}
		}
		// leaf node
		else {
			if (this.getClass().getName() == "main.java.logic.parser.ASTOp") {
				if (parent.op == "|") { 
					tmp += "d\n"; 
				}
				if (parent.op == "&") { 
					tmp += "c\n"; 
				}
			}
		//	System.out.println(((op == "" ) ? " " + var : op)); // debug
			tmp += (((op == "" ) ? "" : op) + ((var == -1) ? "" : var));
			return tmp;
		}		

		return traversedTree;
	}

	public int getId() {
		return this.id;
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
  
  public void jjtSetParent(Node n) { parent = n; }
  public Node jjtGetParent() { return parent; }

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

  public void jjtSetValue(Object value) { this.value = value; }
  public Object jjtGetValue() { return value; }

  /* You can override these two methods in subclasses of SimpleNode to
     customize the way the node appears when the tree is dumped.  If
     your output uses more than one line you should override
     toString(String), otherwise overriding toString() is probably all
     you need to do. */
	
  public String toString() { return Qbf_parserTreeConstants.jjtNodeName[id]; }
  public String toString(String prefix) { return prefix + toString(); }

  /* Override this method if you want to customize how the node dumps
     out its children. */

  public void dump(String prefix) {
    System.out.println(toString(prefix));
    if (children != null) {
      for (int i = 0; i < children.length; ++i) {
  SimpleNode n = (SimpleNode)children[i];
  if (n != null) {
    n.dump(prefix + " ");
  }
      }
    }
  }
}

/* JavaCC - OriginalChecksum=cd6460b90c70fa000dbb49fc278adf1f (do not edit this line) */
