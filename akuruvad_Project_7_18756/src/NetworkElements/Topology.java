package NetworkElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

class Edge {

	private Node fromNode;
	private Node toNode;
	private Double weight;
	private Boolean visited;

	Edge(Node NodeA, Node NodeB, Double weight) {
		this.fromNode = NodeA;
		this.toNode = NodeB;
		this.weight = weight;
	}

	public Boolean isBetweenNodes(Node NodeA, Node NodeB) {

		if ((NodeA.equals(this.fromNode) && NodeB.equals(this.toNode))
				|| (NodeA.equals(this.toNode) && NodeB.equals(this.fromNode))) {
			return true;
		}
		return false;
	}

	public Double getWeight() {
		return weight;
	}

	public void setWeight(Double weight) {
		this.weight = weight;
	}

	public Boolean getVisited() {
		return visited;
	}

	public void setVisited(Boolean visited) {
		this.visited = visited;
	}

	public Node getFromNode() {
		return fromNode;
	}

	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}

	public Node getToNode() {
		return toNode;
	}

	public void setToNode(Node toNode) {
		this.toNode = toNode;
	}
	
	public String toString() {
		String s = "Edge from "+ this.fromNode.getAddress() + " to " + this.toNode.getAddress()+" with weight "+this.getWeight();
		return s;
	}
}

class Node implements Comparable<Node> {
	private Integer address;
	private Integer prevNodeAddress;
	private Double currentCost;
	private Boolean visited;
	
	Node(Integer address) {
		this.address = address;
		prevNodeAddress = -1;
		currentCost = Double.MAX_VALUE;
		visited = false;
	}
	
	public void resetNode() {
		this.prevNodeAddress = -1;
		this.currentCost = Double.MAX_VALUE;
		this.visited = false;
	}
	
	public Boolean equals(Node n) {
		if (this.address == n.address) {
			return true;
		} else {
			return false;
		}
	}
	
	public int compareTo(Node otherNode) {
        return Double.compare(currentCost, otherNode.currentCost);
    }
	
	public Integer getAddress() {
		return address;
	}
	public void setAddress(Integer address) {
		this.address = address;
	}
	public Integer getPrevNodeAddress() {
		return prevNodeAddress;
	}
	public void setPrevNodeAddress(Integer prevNodeAddress) {
		this.prevNodeAddress = prevNodeAddress;
	}
	public Double getCurrentCost() {
		return currentCost;
	}
	public void setCurrentCost(Double currentCost) {
		this.currentCost = currentCost;
	}
	public Boolean getVisited() {
		return visited;
	}
	public void setVisited(Boolean visited) {
		this.visited = visited;
	}
}

public class Topology {

	private static HashMap<Node, ArrayList<Edge>> adjacencyList = new HashMap<Node, ArrayList<Edge>>();
	private static HashMap<Edge, Double> edgeWeights = new HashMap<Edge, Double>();
	public static HashMap<Integer, Node> nodeList = new HashMap<Integer, Node>();
	
	private static Boolean trace = false;

	public static void addConnection(Integer LSR1, Integer LSR2, Double weight) {

		Node node1, node2;
		if(nodeList.containsKey(LSR1)) {
			node1 = nodeList.get(LSR1);
		} else {
			node1 = new Node(LSR1);
			nodeList.put(LSR1, node1);
		}
		
		if(nodeList.containsKey(LSR2)) {
			node2 = nodeList.get(LSR2);
		} else {
			node2 = new Node(LSR2);
			nodeList.put(LSR2, node2);
		}
		
		Edge edge12 = new Edge(node1, node2, weight);
		Edge edge21 = new Edge(node2, node1, weight);
		
		Topology.edgeWeights.put(edge12, weight);
		Topology.edgeWeights.put(edge21, weight);
		
		if (Topology.adjacencyList.containsKey(node1)) {
			Topology.adjacencyList.get(node1).add(edge12);
		} else {
			ArrayList<Edge> eList = new ArrayList<Edge>();
			eList.add(edge12);
			Topology.adjacencyList.put(node1, eList);
		}
		if (Topology.adjacencyList.containsKey(node2)) {
			Topology.adjacencyList.get(node2).add(edge21);
		} else {
			ArrayList<Edge> eList = new ArrayList<Edge>();
			eList.add(edge21);
			Topology.adjacencyList.put(node2, eList);
		}	
		
		if(trace) {
			for (Node n: Topology.adjacencyList.keySet()) {
				for (Edge e : Topology.adjacencyList.get(n)) {
					System.out.print(e.toString()+"  ");
				}
				System.out.println("");
			}
		}
		
	}

	public static void updateWeightForEdgeBetween(Node LSR1, Node LSR2,
			Double weight) {
		
		//Will update both the edges - LSR1->LSR2 and LSR2->LSR1.
		for (Edge e : Topology.edgeWeights.keySet()) {
			if (e.isBetweenNodes(LSR1, LSR2)) {
				Topology.edgeWeights.put(e, weight);
			}
		}
	}
	
	public Double getWeightForEdgeBetweenNodes(Node nodeA, Node nodeB) {
		for (Edge e : Topology.edgeWeights.keySet()) {
			if(e.isBetweenNodes(nodeA, nodeB)) {
				return edgeWeights.get(e);
			}
		}
		return 0.0;
	}
	
	public static ArrayList<Integer> pathFromTo(Integer fromNodeAddress, Integer toNodeAddress) {		
		computeShortestPaths(fromNodeAddress);
		return(Topology.bestPathToDestination(toNodeAddress));
	}
	
	public static Integer nextHopFromTo(Integer fromNodeAddress, Integer toNodeAddress) {
		ArrayList <Integer> path = pathFromTo(fromNodeAddress, toNodeAddress);
		if(path.size()>1) {
			int nextHop = path.get(1);
			return(nextHop);
		} else {
			return null;
		}
	}
	
	public static void computeShortestPaths(Integer sourceAddress) {
		
		for(Node n:adjacencyList.keySet()) {
			n.resetNode();
		}
		
		Node source = nodeList.get(sourceAddress);
		PriorityQueue<Node> q = new PriorityQueue<Node>();
		source.setCurrentCost(0.0);
		q.add(source);
		
		while(!q.isEmpty()) {
			Node n = q.poll();
			for (Edge e : adjacencyList.get(n)) {
				if(trace) {
					System.out.println("Considering "+e.getFromNode().getAddress()+" to "+e.getToNode().getAddress());
				}
				Node v = e.getToNode();
				double weight = Topology.edgeWeights.get(e);
				double costThroughn = n.getCurrentCost() + weight;
				if ( costThroughn < v.getCurrentCost()) {
					q.remove(v);
					v.setCurrentCost(costThroughn);
					v.setPrevNodeAddress(n.getAddress());
					q.add(v);
				}
				if(trace) {
					System.out.println("Considering "+v.getAddress());
				}
			}
		}
	}
	
	private static ArrayList<Integer> bestPathToDestination(Integer destAddress) {
		
		Node dest = nodeList.get(destAddress);
		ArrayList<Integer> path = new ArrayList<Integer>();
		for (Node n = dest; n != null; n = nodeList.get(n.getPrevNodeAddress())) {
			path.add(0,	n.getAddress());
		}
		return path;
	}
}