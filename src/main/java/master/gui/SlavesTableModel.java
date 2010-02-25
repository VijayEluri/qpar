package main.java.master.gui;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import main.java.Util;
import main.java.master.Job;
import main.java.master.Slave;

public class SlavesTableModel extends AbstractTableModel {

	private String[] columnNames = { "Hostname", "Cores", "Current Jobs" };

	@Override
	public String getColumnName(int col) {
		return columnNames[col];
	}

	
	public int getColumnCount() {
		return columnNames.length;
	}

	
	public int getRowCount() {
		return Slave.getSlaves().size();
	}

	
	public Object getValueAt(int row, int col) {
		Slave slave = Slave.getSlaves().get(row);
		switch (col) {
		case 0:
			return slave.getHostName();
		case 1:
			return slave.getCores();
		case 2:
			return Util.join(slave.getCurrentJobs(), ",");
		default:
			return null;
		}
	}

	@Override
	public void setValueAt(Object value, int row, int col) {
		fireTableCellUpdated(row, col);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Class<String> getColumnClass(int col) {
		switch (col) {
		case 0:
			return String.class;
		case 1:
			return String.class;
		case 2:
			return String.class;
		default:
			return null;
		}
	}

}
