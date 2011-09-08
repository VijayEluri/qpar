/* Generated By:JJTree: Do not edit this line. Node.java Version 4.3 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=false,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY=,SUPPORT_CLASS_VISIBILITY_PUBLIC=true */
package qpar.master.logic.parser;

/* All AST nodes must implement this interface.  It provides basic
   machinery for constructing the parent and child relationships
   between nodes. */

import java.util.Set;

public interface Node {

	// custom(ized) methods
	public double getTruthProbability();
	public boolean	checkConnectionToRoot();
	public void		deleteChildren();
	public void		dump(String prefix);
	public int		getId();
	public int		getVar();
	public int		getNodeVariable();
	public boolean	replaceChild(Node oldNode, Node newNode);
	public void		setTruthValue(boolean t);
	public void		setVar(int v);
	public void		setNodeType(qpar.master.logic.parser.SimpleNode.NodeType n);
	public qpar.master.logic.parser.SimpleNode.NodeType getNodeType();
	public Set<Integer> getPositiveLiterals(SimpleNode.NodeType op, Set<Integer> v);
	public Set<Integer> getNegativeLiterals(SimpleNode.NodeType op, Set<Integer> v);
	
	
  /** This method is called after the node has been made the current
    node, it indicates that child nodes can now be added to it. */
  public void jjtOpen();

  /** This method is called after all the child nodes have been
    added. */
  public void jjtClose();

  /** This pair of methods are used to inform the node of its
    parent. */
  public void jjtSetParent(Node n);
  public Node jjtGetParent();

  /** This method tells the node to add its argument to the node's
    list of children.  */
  public void jjtAddChild(Node n, int i);

  /** This method returns a child node.  The children are numbered
     from zero, left to right. */
  public Node jjtGetChild(int i);

  /** Return the number of children the node has. */
  public int jjtGetNumChildren();
}/* JavaCC - OriginalChecksum=d0e4097f6955c516c10a7cd4776b0f97 (do not edit this line) */