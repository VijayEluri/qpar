package main.java.evaluation;

import java.io.File;
import java.util.HashMap;

import main.java.QPar;
import main.java.master.Job;
import main.java.master.Job.Status;

import org.apache.log4j.Logger;

/**
 * Encapsulates solving all formulas in a directory with a specified heuristic
 * @author thomasm
 *
 */
public class SerialEvaluation implements Evaluation {

	static 	Logger 	logger = Logger.getLogger(SerialEvaluation.class);
	protected File 	directory;
	protected String 	heuristicId, solverId;
	protected long	timeout; // timeout in seconds
	protected int		cores;
	
	protected int 					timeouts		= 0;
	protected int 					errors			= 0;
	protected long 					elapsedTotal 	= 0;
	protected HashMap<File, Boolean> 	results 		= new HashMap<File, Boolean>();
	public double meanSolverTime;
	public double meanMaxSolverTime;
	public double meanOverheadTime;
	
	public SerialEvaluation(	File 	directory,
						String 	heuristicId,
						String 	solverId,
						long 	timeout,
						int 	cores) {
		this.directory 			= directory;
		this.heuristicId		= heuristicId;
		this.solverId			= solverId;
		this.timeout			= timeout;
		this.cores				= cores;
	}
	
	public void evaluate() {
		for(File f : this.directory.listFiles()) {
			if(f.getName().equals("evaluation.txt"))
				continue;
			try {
				Job job = new Job(f.getAbsolutePath(), null, solverId, heuristicId, timeout, cores);
							
				job.startBlocking();
				
				synchronized(job.status) {
					if(job.getStatus() == Job.Status.COMPLETE) {
						//logger.info("JOB TOTAL MILLIS " + job.totalMillis());
						//logger.info("ELAPSED TOAL " + elapsedTotal);
						elapsedTotal += job.totalMillis();
						results.put(f, job.getResult());
					} else if(job.getStatus() == Job.Status.TIMEOUT){ 
						elapsedTotal += timeout*1000;
						timeouts++;
					} else if(job.getStatus() == Job.Status.ERROR) {
						elapsedTotal += timeout*1000;
						errors++;
					} else {
						assert(false);
					}
				}
							
				// Do the stats
				synchronized(job.solverTimes) {
					if (!job.solverTimes.isEmpty()) {
						this.meanSolverTime += job.meanSolverTime();
						this.meanMaxSolverTime += job.maxSolverTime();
					}
				}
				synchronized(job.overheadTimes) {
					if (!job.overheadTimes.isEmpty()) {
						this.meanOverheadTime += job.meanOverheadTime();
					}
				}
				
			} catch(Throwable t) {
				logger.error("Evaluation.java", t);
				QPar.sendExceptionMail(t);
			}
		}
		
	}

	public String meanOverheadResultString() {
		return String.format("%.2f", (double)this.meanOverheadTime/1000.00);
	}
	
	public String meanResultString() {
		return String.format("%.2f", (double)this.meanSolverTime/1000.00);
	}
	
	public String maxResultString() {
		return String.format("%.2f", (double)this.meanMaxSolverTime/1000.00);
	}
	
	public String toString() {
		return String.format("%.2f\t%d\t%d", this.elapsedTotal/1000.00, this.timeouts, this.errors);
	}
	
	public int getTimeouts() {
		return timeouts;
	}

	public void setTimeouts(int timeouts) {
		this.timeouts = timeouts;
	}

	public int getErrors() {
		return errors;
	}

	public void setErrors(int errors) {
		this.errors = errors;
	}

	public long getElapsedTotal() {
		return elapsedTotal;
	}

	public void setElapsedTotal(long elapsedTotal) {
		this.elapsedTotal = elapsedTotal;
	}

	public void setResults(HashMap<File, Boolean> results) {
		this.results = results;
	}

	public HashMap<File, Boolean> getResults() {
		return results;
	}
	
}