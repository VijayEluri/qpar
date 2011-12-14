/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
package qpar.master.heuristic;

import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Set;

import org.apache.log4j.Logger;

import qpar.master.Qbf;
import qpar.master.logic.parser.Node;


public class ShallowHeuristic extends Heuristic {

	static Logger logger = Logger.getLogger(ShallowHeuristic.class);
	
	@Override
	public LinkedHashSet<Integer> sortGroup(Set<Integer> group, Qbf qbf) {
		LinkedHashSet<Integer> order = new LinkedHashSet<Integer>();
		Deque<Node> nodes = new LinkedList<Node>();
		
		nodes.add(qbf.root);
		
		while(order.size() < group.size()) {
			Node currentNode = nodes.poll();
			
			switch(currentNode.getNodeType()) {
				case FORALL:
				case EXISTS:
					nodes.addFirst(currentNode.jjtGetChild(0));
					break;
				case VAR:
					if(group.contains(Integer.valueOf(currentNode.getVar()))){
						order.add(currentNode.getVar());
					}							
					break;
				default:
					for(int i = 0; i < currentNode.jjtGetNumChildren(); i++) {
						nodes.add(currentNode.jjtGetChild(i));
					}
			}
			
		}	
		
//		Permuter p = new Permuter(group);
//		return new LinkedHashSet<Integer>(p.next());
		
		return order;
	}

	@Override
	public String getId() {
		return "shallow";
	}

}
