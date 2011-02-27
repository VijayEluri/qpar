package main.java.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import main.java.logic.heuristic.HeuristicFactory;
import main.java.master.Job;
import main.java.master.Job.Status;
import main.java.master.Mailer;
import main.java.master.console.Shell;
import main.java.scheduling.BestFitScheduler;
import main.java.scheduling.Scheduler;

public class ParLogEvalSuite implements EvaluationSuite {

	/**
	 * [file][cores][heuristic]
	 */
	Job results[][][];
	
	int coresStart;
	int coresEnd;
	String solver;
	List<String> heuristics;
	long timeout;
	List<File> files = new ArrayList<File>();
	
	List<Job> jobsTodo = new ArrayList<Job>();
	Date startedAt = null;
	Date stoppedAt = null;
	File dir;
	
	public ParLogEvalSuite(File dir, int coresStart, int coresEnd, String solver, List<String> heuristics, long timeout) {
		if(!isBaseTwo(coresStart) || !isBaseTwo(coresEnd)) {
			IllegalArgumentException e = new IllegalArgumentException("Use only powers of 2");
			throw e;
		}
		this.coresStart = coresStart;
		this.coresEnd   = coresEnd;
		this.solver		= solver;
		this.heuristics	= heuristics;
		this.timeout 	= timeout;
		this.dir		= dir;
		
		for(File f : dir.listFiles()) {
			if(f.getName().equals("evaluation.txt"))
				continue;
			files.add(f);
		}
		
		results = new Job[files.size()]
		                 [this.getNeededRuns(coresStart, coresEnd).size()]
		                 [heuristics.size()];
		
		setupJobs();
	}
	
	private void setupJobs() {
		for(int f = 0; f < files.size(); f++) {
			for(int c = 0; c < getNeededRuns(coresStart, coresEnd).size(); c++) {
				for(int h = 0; h < heuristics.size(); h++) {
					Job j = new Job(files.get(f).getAbsolutePath(), null, solver, heuristics.get(h), timeout, getNeededRuns(coresStart, coresEnd).get(c));
					results[f][c][h] = j;
					jobsTodo.add(j);
				}	
			}
		}
	}

	@Override
	public void evaluate() {
		this.startedAt = new Date();
		Scheduler sched = new BestFitScheduler(jobsTodo);
		sched.startExecution();
		this.stoppedAt = new Date();
	}

	@Override
	public boolean isCorrect() {
		for(int f = 0; f < files.size(); f++) {
			Boolean fileIs = null;
			for(int c = 0; c < getNeededRuns(coresStart, coresEnd).size(); c++) {
				for(int h = 0; h < heuristics.size(); h++) {
					if(fileIs == null) {
						fileIs = results[f][c][h].getResult();
						continue;
					}
					if(!fileIs.equals(results[f][c][h].getResult()))
							return false;
				}	
			}
		}
		return true;
	}

	@Override
	public String getReport() {
		String report = "Logarithmic Evaluation Suite Report\n" +
						"Started: " + startedAt + "\n" +
						"Stopped: " + stoppedAt + "\n" +
						"Solvers: \t" + heuristics + "\n" +
						"Timeout: \t" + timeout + "\n" +
						"Cores Min: \t" + coresStart + "\n" +
						"Cores Max: \t" + coresEnd + "\n" +
						"Directory: \t" + dir + "\n";
		try {
			String hostname = InetAddress.getLocalHost().getHostName();
			report += "Host: \t" + hostname + "\n\n";
		} catch (UnknownHostException e) {
			report += "Host: \t UNKNOWN \n\n";
		}
		
		if(!this.isCorrect()) {
			report += "RESULTS INCONSISTENT. SOLVER NOT CORRECT\n\n";
		}
		
		report += runtimesReport() + "\n\n";
		report += timeoutErrorsReport() + "\n\n";
		report += detailedReport() + "\n\n";
		report += meanSolvertimesReport() + "\n\n";
		report += maxSolvertimesReport() + "\n\n";
		report += meanOverheadReport() + "\n\n";
		
		return report;
	}

	private String timeoutErrorsReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Timeouts and Errors \n");
		String line = "";
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s_timeouts\t%s_errors\t", h, h);
		}
		sbuf.append(line + "\n");
		
		for(int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i).toString();
			line += cores + "\t";
			line += timeoutErrorsLine(i) + "\n";
			sbuf.append(line);
		}
		
		return sbuf.toString();
	}

	private String timeoutErrorsLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for(int h = 0; h < heuristics.size(); h++) {
			int cumulatedTimeouts 	= 0;
			int cumulatedErrors		= 0;
			
			for(int f = 0; f < files.size(); f++) {
				if(results[f][i][h].status == Status.ERROR) {
					cumulatedErrors++;
				} else if(results[f][i][h].status == Status.TIMEOUT) {
					cumulatedTimeouts++;
				}
				
			}
			sbuf.append(String.format("%d\t%d", cumulatedTimeouts, cumulatedErrors) + "\t");
		}
		
		String ret = sbuf.toString();
		return ret.trim();
	}

	private String meanOverheadReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Added mean overheadtimes \n");
		String line = "";
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s\t", h);
		}
		sbuf.append(line + "\n");
		
		for(int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i).toString();
			line += cores + "\t";
			line += meanOverheadtimeLine(i) + "\n";
			sbuf.append(line);
		}
		
		return sbuf.toString();
	}

	private String meanOverheadtimeLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for(int h = 0; h < heuristics.size(); h++) {
			double cumulatedTime = 0;
			
			for(int f = 0; f < files.size(); f++) {
				cumulatedTime += results[f][i][h].meanOverheadTime();
			}
			sbuf.append(String.format("%.2f", cumulatedTime/1000.00) + "\t");
		}
		
		String ret = sbuf.toString();
		return ret.trim();
	}

	private String maxSolvertimesReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Added max solvertimes \n");
		String line = "";
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s\t", h);
		}
		sbuf.append(line + "\n");
		
		for(int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i).toString();
			line += cores + "\t";
			line += maxSolvertimeLine(i) + "\n";
			sbuf.append(line);
		}
		
		return sbuf.toString();
	}

	private String maxSolvertimeLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for(int h = 0; h < heuristics.size(); h++) {
			double cumulatedTime = 0;
			
			for(int f = 0; f < files.size(); f++) {
				cumulatedTime += results[f][i][h].maxSolverTime();
			}
			sbuf.append(String.format("%.2f", cumulatedTime/1000.00) + "\t");
		}
		
		String ret = sbuf.toString();
		return ret.trim();
	}

	private String meanSolvertimesReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Added mean solvertimes \n");
		String line = "";
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s\t", h);
		}
		sbuf.append(line + "\n");
		
		for(int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i).toString();
			line += cores + "\t";
			line += meanSolvertimeLine(i) + "\n";
			sbuf.append(line);
		}
		
		return sbuf.toString();
	}
	
	private String meanSolvertimeLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for(int h = 0; h < heuristics.size(); h++) {
			double cumulatedTime = 0;
			
			for(int f = 0; f < files.size(); f++) {
				cumulatedTime += results[f][i][h].meanSolverTime();
			}
			sbuf.append(String.format("%.2f", cumulatedTime/1000.00) + "\t");
		}
		
		String ret = sbuf.toString();
		return ret.trim();
	}

	private String detailedReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Detailed Report\n");
		
		for(int f = 0; f < files.size(); f++) {
			sbuf.append("File: " + files.get(f) + "\n");
			sbuf.append(detailedFileReport(f) + "\n\n");
		}
		
		return sbuf.toString().trim();
	}

	private String detailedFileReport(int f) {
		StringBuffer sbuf = new StringBuffer();
		
		for(int h = 0; h < heuristics.size(); h++) {
			sbuf.append("Heuristic: " +heuristics.get(h) + "\n");
			for(int c = 0; c < getNeededRuns(coresStart, coresEnd).size(); c++) {
				switch(results[f][c][h].status) {
					case TIMEOUT:
						sbuf.append("t");
						break;
					case ERROR:
						sbuf.append("e");
						break;
					case COMPLETE:
						if(results[f][c][h].getResult())
							sbuf.append("t");
						else
							sbuf.append("f");
						break;
					default:
						sbuf.append("!");
						break;
				}
			}
			sbuf.append("\n");
		}
		
		return sbuf.toString().trim();		
	}

	private String runtimesReport() {
		StringBuffer sbuf = new StringBuffer();
		sbuf.append("Run times \n");
		String line = "";
		for(String h : HeuristicFactory.getAvailableHeuristics()) {
			line += String.format("%s_total\t", h);
		}
		sbuf.append(line + "\n");
		
		for(int i = 0; i < getNeededRuns(coresStart, coresEnd).size(); i++) {
			line = "";
			String cores = getNeededRuns(coresStart, coresEnd).get(i).toString();
			line += cores + "\t";
			line += runtimeLine(i) + "\n";
			sbuf.append(line);
		}
		
		return sbuf.toString();
	}

	private String runtimeLine(int i) {
		StringBuffer sbuf = new StringBuffer();
		for(int h = 0; h < heuristics.size(); h++) {
			long cumulatedTime = 0;
			
			for(int f = 0; f < files.size(); f++) {
				cumulatedTime += results[f][i][h].totalMillis();
			}
			sbuf.append(String.format("%.2f", cumulatedTime/1000.00) + "\t");
		}
		
		String ret = sbuf.toString();
		return ret.trim();
	}

	private boolean isBaseTwo(int i) {
		double ld = Math.log(i)/Math.log(2);
		if(Math.floor(ld) == ld)
			return true;
		return false;
	}
	
	private ArrayList<Integer> getNeededRuns(int start, int end) {
		ArrayList<Integer> runs = new ArrayList<Integer>();
		
		while(start <= end) {
			runs.add(start);			
			start *= 2;
		}
		
		return runs;
	}

}