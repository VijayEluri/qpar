package main.java.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import main.java.logic.heuristic.HeuristicFactory;
import main.java.master.Mailer;
import main.java.master.console.Shell;

import org.apache.log4j.Logger;

public class LogarithmicEvaluationSuite {

	static Logger logger = Logger.getLogger(LogarithmicEvaluationSuite.class);
	
	protected File directory;
	protected int startCores, stopCores;
	protected long timeout; //in seconds
	protected String solverId;
	
	protected String report = null;
	
	protected Date startedAt = null;
	protected Date stoppedAt = null;
	
	public boolean correctness	= true;
	SerialEvaluation[][]	result		= null; 
	
	protected ArrayList<Integer> coreSet;
	
	public LogarithmicEvaluationSuite(String dir, int startCores, int stopCores, long timeout, String solverId) {
		if(!isBaseTwo(startCores) || !isBaseTwo(stopCores)) {
			IllegalArgumentException e = new IllegalArgumentException("Use only powers of 2");
			throw e;
		}
					
		this.directory = new File(dir);
		this.startCores = startCores;
		this.stopCores	= stopCores;
		this.timeout	= timeout;
		this.solverId	= solverId;
		
		coreSet = getNeededRuns(startCores, stopCores);
		
		result = new SerialEvaluation[coreSet.size()][HeuristicFactory.getAvailableHeuristics().size()];
		
	}
	
	public void run() throws FileNotFoundException {
		if(!directory.exists()) {
			throw new FileNotFoundException(directory.toString());
		}
		Shell.waitforslaves(stopCores, solverId);
		
		this.startedAt = new Date();
		
		int idx = 0;
		for(int cores : coreSet) {
			for(String h : HeuristicFactory.getAvailableHeuristics()) {
				SerialEvaluation e = new SerialEvaluation(directory, h, solverId, timeout, cores);
				result[idx][HeuristicFactory.getAvailableHeuristics().indexOf(h)] = e;
				e.evaluate();
				
			}
			idx += 1;
		}
		this.stoppedAt = new Date();
		
		generateReport();
		
		if(Mailer.email != null && Mailer.server != null && Mailer.user != null && Mailer.pass != null)
			Mailer.send_mail(Mailer.email, Mailer.server, Mailer.user, Mailer.pass, "Evaluation Report", report);
		
		writeReport();
				
	}
	
	protected void writeReport() {
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter(directory.getAbsolutePath() + File.separator + "evaluation.txt"));
			out.write(report);
			out.flush();
		} catch (IOException e) {
			logger.error("While writing report: ", e);
		}
	}

	protected void generateReport() {
		report = "Logarithmic Evaluation Suite Report\n" +
						"Started: " + startedAt + "\n" +
						"Stopped: " + stoppedAt + "\n" +
						"Solver: \t" + solverId + "\n" +
						"Timeout: \t" + timeout + "\n" +
						"Cores Min: \t" + startCores + "\n" +
						"Cores Max: \t" + stopCores + "\n" +
						"Directory: \t" + directory + "\n\n" +
						"cores\t";
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			report += String.format("%s_total\t%s_timeouts\t%s_errors\t", h, h, h);
		}
		report = report.trim() + "\n";
		
		int idx = 0;
		for(int c : coreSet) {
			String line = "" + c + "\t";
			for(String h : HeuristicFactory.getAvailableHeuristics()) {
				line += result[idx][HeuristicFactory.getAvailableHeuristics().indexOf(h)].toString() + "\t";
			}		
			line = line.trim();
			line += "\n";
			report += line;
			idx++;
		}
		
		report += correctnessReport() + "\n";
		
		report += meanTimesReport() + "\n";
		
		report += maxTimesReport() + "\n";
		
		report += overheadTimesReport() + "\n";
		
	}

	public String getReport() {
		return report;
	}
	
	private ArrayList<Integer> getNeededRuns(int start, int end) {
		ArrayList<Integer> runs = new ArrayList<Integer>();
		
		while(start <= end) {
			runs.add(start);			
			start *= 2;
		}
		
		return runs;
	}
	
	private boolean isBaseTwo(int i) {
		double ld = Math.log(i)/Math.log(2);
		if(Math.floor(ld) == ld)
			return true;
		return false;
	}
	
	private String correctnessReport() {
		String correctnessReport = "\n\nDetailed results:\n";
		for(File f : directory.listFiles()) {
			if(f.getName().equals("evaluation.txt"))
				continue;
			correctnessReport += "File: " + f.getName() + "\n";
			Boolean compare = null;
			for(String h : HeuristicFactory.getAvailableHeuristics()) {
				correctnessReport += "Heuristic: " + h + "\n";
				for(int c = 0; c < coreSet.size(); c++) {
					Boolean current = result[c][HeuristicFactory.getAvailableHeuristics().indexOf(h)].getResults().get(f);
					if(current == null) {
						correctnessReport += "x";
					} else if(current.equals(true)) {
						correctnessReport += "t";
					} else if(current.equals(false)) {
						correctnessReport += "f";
					} else { assert(false);}
					
					if(compare == null && current != null) {
						compare = current;
					} else if(compare != null && current != null && !compare.equals(current)) {
						logger.warn("Correctness error detected: File: " + f + ", Cores: " + coreSet.get(c) + ", Heuristic: " + h);
						correctness = false;
					}
				}
				correctnessReport += "\n";
			}
			correctnessReport += "\n";
		}
		
		if(correctness)
			report = report + correctnessReport;
		else
			report = report + "WARNING: INCONSISTENT RESULTS DETECTED! PROGRAM NOT CORRECT!\n\n" + correctnessReport;
		
		return correctnessReport;
	}
	
	public String meanTimesReport() {
		StringBuffer report = new StringBuffer();

		report.append("Mean Solvertimes statistics: \n");
		report.append("cores\t");
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			report.append(String.format("%s_mean\t", h));
		}
		//report = report.trim() + "\n";
		report.append("\n");
		int idx = 0;
		for(int c : coreSet) {
			StringBuffer line = new StringBuffer(); 
			line.append("" + c + "\t");
			for(String h : HeuristicFactory.getAvailableHeuristics()) {
				line.append(result[idx][HeuristicFactory.getAvailableHeuristics().indexOf(h)].meanResultString() + "\t");
			}		
			//line = line.trim();
			line.append("\n");
			report.append(line);
			idx++;
		}
		
		return report.toString();
	}
	
	public String overheadTimesReport() {
		StringBuffer report = new StringBuffer();

		report.append("Mean Overheadtimes statistics: \n");
		report.append("cores\t");
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			report.append(String.format("%s_meanOverhead\t", h));
		}
		//report = report.trim() + "\n";
		report.append("\n");
		int idx = 0;
		for(int c : coreSet) {
			StringBuffer line = new StringBuffer(); 
			line.append("" + c + "\t");
			for(String h : HeuristicFactory.getAvailableHeuristics()) {
				line.append(result[idx][HeuristicFactory.getAvailableHeuristics().indexOf(h)].meanOverheadResultString() + "\t");
			}		
			//line = line.trim();
			line.append("\n");
			report.append(line);
			idx++;
		}
		
		return report.toString();
	}
	
	public String maxTimesReport() {
		StringBuffer report = new StringBuffer();

		report.append("Max Solvertimes statistics: \n");
		report.append("cores\t");
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			report.append(String.format("%s_max\t", h));
		}
		//report = report.trim() + "\n";
		report.append("\n");
		int idx = 0;
		for(int c : coreSet) {
			StringBuffer line = new StringBuffer(); 
			line.append("" + c + "\t");
			for(String h : HeuristicFactory.getAvailableHeuristics()) {
				line.append(result[idx][HeuristicFactory.getAvailableHeuristics().indexOf(h)].maxResultString() + "\t");
			}		
			//line = line.trim();
			line.append("\n");
			report.append(line);
			idx++;
		}
		
		return report.toString();
	}
	
}