package qpar.master;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import org.apache.log4j.Logger;

import qpar.master.heuristic.DependencyNode;
import qpar.master.heuristic.Heuristic;
import qpar.master.logic.parser.ParseException;
import qpar.master.logic.parser.Qbf_parser;
import qpar.master.logic.parser.SimpleNode;
import qpar.master.logic.parser.TokenMgrError;

/**
* A QBF object contains one QBF as well as methods to split it up into subQBFs
* and merging subresults back together
*
*/
public class Qbf {
	
    static Logger logger = Logger.getLogger(Qbf.class);

	Heuristic h = null;
//	private static int idCounter = 0;
	
	private HashMap<Integer, Integer> literalCount = new HashMap<Integer, Integer>();	
	public Vector<Integer> eVars = new Vector<Integer>();
	public Vector<Integer> aVars = new Vector<Integer>();
	public Vector<Integer> vars  = new Vector<Integer>();
	public SimpleNode root = null;
	
	public DependencyNode dependencyGraphRoot;
		
	/**
	* constructor
	* @param filename The file containing the QBF that will be stored in this object
	* @throws IOException 
	*/
	public Qbf(String filename) throws IOException {
		assert(filename != null);
//		this.id = Qbf.getUniqueId();
		
		long start = System.currentTimeMillis();
		// parse the formula, get various vectors of vars
		try {
			Qbf_parser parser = new Qbf_parser(new FileInputStream(filename));
			parser.ReInit(new FileInputStream(filename), null);
			logger.debug("Begin parsing...");
			parser.Input();	
			logger.debug("Succesful parse");
			literalCount = parser.getLiteralCount();
			this.eVars = parser.getEVars();
			this.aVars = parser.getAVars();
			this.vars = parser.getVars();
			root = parser.getRootNode();

			//root.dump("> ");
			parser.doPostprocessing();
			logger.debug("postprocessing...");
			// root.dump("> ");
			
			//logger.debug("Postprocessed Formula: \n" + root.subTreetoString("  "));
		}
		catch (ParseException e) {
			logger.error("Parse error", e);
			return;
		}
		catch (TokenMgrError e) {
			logger.error(filename, e);
			throw e;
		}
		long end = System.currentTimeMillis();
		logger.debug("Existential quantified Variables: " + eVars);
		logger.debug("Number of e.q.vs: " + eVars.size());
		logger.debug("Universally quantified Variables: " + aVars);
		logger.debug("Number of u.q.vs: " + aVars.size());
		logger.debug("All variables: " + vars);
		logger.debug("Number of all v.: " + vars.size());
		logger.debug("Finished parsing QBF from " + filename + ", Took: " + (end-start)/1000 + " seconds.");
		logger.debug("Generating dependency graph...");
		start = System.currentTimeMillis();
		dependencyGraphRoot = this.root.dependencyTree()[0];
		end = System.currentTimeMillis();
		logger.debug("Dependency graph generated. Took " + (end-start)/1000 + " seconds.");
		// This hampers performance severely!! only use if debugging
		//logger.debug("Dependency Tree: \n" + dependencyGraphRoot.dump());
	}

//	synchronized private static int getUniqueId() {
//		idCounter++;
//		return idCounter;
//	}
	
	public boolean isUniversalQuantified(Integer v) {
		return aVars.contains(v);
	}
	
	public HashMap<Integer, Integer> getLiteralCount() {
		return literalCount;
	}

	public void setHeuristic(Heuristic h) {
		this.h = h;
	}
}
