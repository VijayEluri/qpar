/* Generated By:JJTree: Do not edit this line. SimpleNode.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package main.java.logic.parser;

import java.lang.String;
import java.util.Arrays;
import java.io.Serializable;

public class SimpleNode implements Node, Serializable {
	protected Object value;
	protected Qbf_parser parser;
	protected Node parent;
	protected Node[] children;

	public int id;
	public int var = -1;	
	public String op = "";
	public String truthValue = "";
	
	/**
	* assign a truth value to a specific var
	* @param v the var to assign a truth value to
	* @param b the truth value to assign
	*/
	public void assignTruthValue(int v, boolean b) {
		int i = 0;
		int numChildren = this.jjtGetNumChildren();
		 
		// not in a leaf node, nothing to set
		if (numChildren > 0) {
			for (i = 0; i < numChildren; i++) {
				jjtGetChild(i).assignTruthValue(v, b);
			}
		}
		// in a leaf node now
		else {
			if (var == v) {
				if (b) truthValue = "TRUE"; else truthValue = "FALSE";
			}
		}	
	}
	
	/**
	* traverse tree goes through all children of a node and builds a String
	* in .qpro format
	* @return A String in qpro format
	*/	
	public String traverse() {	
       	Node child;
		String tmp = "";
		String traversedTree = "";
		String partialTree = "";
		String negatedPartialTree = "";
		String enclosedPartialTree = "";
		int i = 0;	
       	int numChildren = this.jjtGetNumChildren();
					
		if (numChildren > 0) { // we're not in a leaf node
			if (op == "|") {
				traversedTree += "d\n";
				for (i = 0; i < numChildren; i++) {
					if (jjtGetChild(i).getVar() > -1) {
						partialTree += jjtGetChild(i).traverse();						
					}					
					else {
						enclosedPartialTree += jjtGetChild(i).traverse();	
					}
				}	
				traversedTree += partialTree + "\n" + negatedPartialTree + "\n" + enclosedPartialTree + "/d\n";
			}
			else if (op == "&") {
				traversedTree += "c\n";
				for (i = 0; i < numChildren; i++) {
					if (jjtGetChild(i).getVar() > -1) {
						partialTree += jjtGetChild(i).traverse();						
					}					
					else {
						enclosedPartialTree += jjtGetChild(i).traverse();	
					}
				}	
				traversedTree += partialTree + "\n" + negatedPartialTree + "\n" + enclosedPartialTree + enclosedPartialTree + "/c\n";
			}
		}
		else { // we're in a leaf node...
			if (truthValue == "") {
				// ...but not a truth-assigned one
				traversedTree += var + " ";
			}
		}
		return traversedTree;
	}

	/** 
	* reduces a tree containung truth-assigned variables to a tree without them
	* @return true if tree is still traversable, false if not
	*/
	public boolean reduceTree() {
       	Node parentNode = null;
		Node grandparentNode = null;
       	Node siblingNode = null;
		int i = 0;	
       	int numChildren = this.jjtGetNumChildren();
       	boolean reducable = false;
					
		if (this.jjtGetNumChildren() > 0) { // we're not in a leaf node...
			for (i = 0; i < this.jjtGetNumChildren(); i++) { // ... so we just traverse through all it's children
				reducable = jjtGetChild(i).reduceTree();
			}
		}
		else { // we're in a leaf node...
			if (truthValue != "") {
			// we're in a truth-assigned leaf node, let's see what to do
				parentNode = jjtGetParent();
				// if we're in the logical root node, then there's no more reducing
				// even if it has a truth value assigned, else the tree might be
				// even more reducable
				if (parentNode.getClass().getName() == "main.java.logic.parser.ASTInput") {
					return false;
				} else {
					reducable = true;
				}

				// not x, set the parent to not x
				if (parentNode.getOp() == "!") {
					parentNode.setOp("");
					if (truthValue == "FALSE") {
						parentNode.setTruthValue("TRUE");
					}
					else {
						parentNode.setTruthValue("FALSE");
					}
					parentNode.deleteChildren();
					jjtSetParent(null);
				}

				// false & x = false, so set parent to false and make it a leaf node
				if ((parentNode.getOp() == "&") && (truthValue == "FALSE")) {
					parentNode.setOp("");
					parentNode.setTruthValue("FALSE");
					parentNode.deleteChildren();
					jjtSetParent(null);
				}

				// true & x = x, so delete this node, replace the parent node with
				// the sibling
				if ((parentNode.getOp() == "&") && (truthValue == "TRUE")) {
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
				}

				// false | x = x, so delete this node, replace the parent node with
				// the sibling
				if ((parentNode.getOp() == "|") && (truthValue == "FALSE")) {
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
				}

				// true | x = true, so set the parent node to true and make it a leaf
				if ((parentNode.getOp() == "|") && (truthValue == "TRUE")) {
					parentNode.setOp("");
					parentNode.setTruthValue("TRUE");
					parentNode.deleteChildren();
					jjtSetParent(null);				
				}
			}
		}
		return reducable;
	}

	/**
	* removes all children of a node by setting the childrens parent to null and
	* cleaning the children[] array. Hopefully the garbage collector will really
	* delete them
	*/
	public void deleteChildren() {
		for (int i = 0; i < jjtGetNumChildren(); i++) {
			jjtGetChild(i).jjtSetParent(null);
		}
		children = null;
	}
	
	/**
	* replaces node old with node new in the parent list of a node
	* @param oldNode the node to be replaced
	* @param newNode the node that will take the old nodes place
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

	// mostly auto-generated stuff from here plus some simple getter/setter methods
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
	
  public String toString() { return Qbf_parserTreeConstants.jjtNodeName[id] + " (op= " + op + ", var= " + var + " "  + truthValue + ")"; }
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
