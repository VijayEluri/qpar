/* Generated By:JJTree: Do not edit this line. ASTVar.java Version 4.1 */
/* JavaCCOptions:MULTI=true,NODE_USES_PARSER=false,VISITOR=true,TRACK_TOKENS=false,NODE_PREFIX=AST,NODE_EXTENDS=,NODE_FACTORY= */
package main.java.logic.parser;

public class ASTVar extends SimpleNode {
	private String id="";

	public ASTVar(int id) {
	super(id);
	}

	public ASTVar(Qbf_parser p, int id) {
	super(p, id);
	}


	public void setId(String id) {
		this.id = id;
	}
	public String getId() {
		return id;
	}


  /** Accept the visitor. **/
  public Object jjtAccept(Qbf_parserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }
}
/* JavaCC - OriginalChecksum=0d1a7da57530148d3e27382816a4c9ca (do not edit this line) */
