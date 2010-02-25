package main.java.master.Console;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import main.java.Util;
import main.java.master.Job;
import main.java.master.MasterDaemon;
import main.java.master.Slave;

public class Shell implements Runnable{

	static Logger logger = Logger.getLogger(MasterDaemon.class);
	{
		logger.setLevel(Level.INFO);
	}
	
	private String prompt 	= ">";
	BufferedReader in 		= new BufferedReader(new InputStreamReader(System.in));
	boolean run				= true;
	
	
	public void run() {	
		String line;
		while(run) {
			put(prompt);
			line = read();
			if(line.length() < 1) continue;
			parseLine(line);

		}
	}

	private void parseLine(String line) {
		StringTokenizer token = new StringTokenizer(line);
		switch (Command.toCommand(token.nextToken().toUpperCase()))
		{
			case NEWJOB:
				newjob(token);
				break;
			case STARTJOB:
				startjob(token);
				break;
			case ABORTJOB:
				abortjob(token);
				break;
			case VIEWJOBS:
				viewjobs();
				break;
			case VIEWSLAVES:
				viewslaves();
				break;
			case KILLSLAVE:
				killslave(token);
				break;
			case SOURCE:
				source(token);
				break;
			case HELP:
				help();
				break;
			case QUIT:
				quit();
				break;
			case NOVALUE:
				puts("Command not found (try \"help\"");
		        break;
		    default:
		        assert(false);
		}
		
	}

	/**
	 * Syntax: SOURCE path
	 * @param token
	 */
	private void source(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			try {
				BufferedReader in = new BufferedReader(new FileReader(token.nextToken()));
				String line;
				while((line = in.readLine()) != null) {
					parseLine(line);
				}
			} catch (FileNotFoundException e) {
				logger.error("Error while reading formula file: " + e);
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			puts("Syntax: SOURCE path");
		}	
		
		
	}

	private void help() {
		puts("Allowed comands are NEWJOB, STARTJOB, ABORTJOB, VIEWJOBS, VIEWSLAVES, KILLSLAVE, HELP, SOURCE, QUIT (Case insensitive)");
	}

	/**
	 * Syntax: KILLSLAVE hostname
	 * @param token
	 */
	private void killslave(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			String hostname = token.nextToken();
			Slave.getSlaves().get(hostname).kill("User command");
		} else {
			puts("Syntax: KILLSLAVE hostname");
		}		
	}

	/**
	 * Syntax: VIEWJOBS
	 */
	private void viewjobs() {
		puts("JOBID\tSTARTED\tFINISHED\tSTATUS");
		for(Job j : Job.getJobs().values()) {
			puts(j.getId() + "\t" + j.getStartedAt() + "\t" + j.getStoppedAt() + "\t" + j.getStatus());
		}
	}
	
	/**
	 * Syntax: VIEWSLAVES
	 */
	private void viewslaves() {
		puts("HOSTNAME\tCORES\tCURRENT_JOBS");
		for(Slave s : Slave.getSlaves().values()) {
			puts(s.getHostName() + "\t" + s.getCores() + "\t" + Util.join(s.getCurrentJobs(), ","));
		}
	}

	/**
	 * Syntax: ABORTJOB jobid
	 * @param token
	 */
	private void abortjob(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			Job.getJobs().get(token.nextToken()).abort();
		} else {
			puts("Syntax: ABORTJOB jobid");
		}	
	}

	/**
	 * Syntax: STARTJOB jobid
	 * @param token
	 */
	private void startjob(StringTokenizer token) {
		if(token.hasMoreTokens()) {
			try {
				Job.getJobs().get(token.nextToken()).start();
			} catch(FileNotFoundException e) {
				logger.error("Error while reading formula file: " + e);
			} catch (IOException e) {
				logger.error(e);
			}
		} else {
			puts("Syntax: STARTJOB jobid");
		}			
	}

	/**
	 * Syntax: NEWJOB path_to_formula path_to_outputfile solverid heuristic
	 * @param token
	 */
	private void newjob(StringTokenizer token) {
		try{
			String input_path = token.nextToken();
			String output_path = token.nextToken();
			String solverid = token.nextToken();
			String heuristic = token.nextToken();
			Job.createJob(input_path, output_path, solverid, heuristic);
			
		} catch(NoSuchElementException e) {
			puts("Syntax: NEWJOB path_to_formula path_to_outputfile solverid heuristic");
		}
	}

	private void quit() {
		this.run = false;
	}
	
	private void put(String s) {
		System.out.print(s);
	}
	
	private void puts(String s) {
		System.out.println(s);
	}
	
	private String read() {
		String line = null;
		try {
			line = in.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return line;
	}
	
}