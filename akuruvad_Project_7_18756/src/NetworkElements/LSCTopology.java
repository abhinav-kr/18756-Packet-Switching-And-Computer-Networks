package NetworkElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;

public class LSCTopology {

	private static HashMap<Node, ArrayList<Edge>> adjacencyList = new HashMap<Node, ArrayList<Edge>>();
	private static HashMap<Edge, Double> edgeWeights = new HashMap<Edge, Double>();
	public static HashMap<Integer, Node> nodeList = new HashMap<Integer, Node>();
	
	private static Boolean trace = true;

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
		
		edgeWeights.put(edge12, weight);
		edgeWeights.put(edge21, weight);
		
		if (adjacencyList.containsKey(node1)) {
			adjacencyList.get(node1).add(edge12);
		} else {
			ArrayList<Edge> eList = new ArrayList<Edge>();
			eList.add(edge12);
			adjacencyList.put(node1, eList);
		}
		if (adjacencyList.containsKey(node2)) {
			adjacencyList.get(node2).add(edge21);
		} else {
			ArrayList<Edge> eList = new ArrayList<Edge>();
			eList.add(edge21);
			adjacencyList.put(node2, eList);
		}	
		
		for (Node n: adjacencyList.keySet()) {
			for (Edge e : adjacencyList.get(n)) {
				System.out.print(e.toString()+"  ");
			}
			System.out.println("");
		}
		
	}

	public static void updateWeightForEdgeBetween(Node LSR1, Node LSR2,
			Double weight) {
		
		//Will update both the edges - LSR1->LSR2 and LSR2->LSR1.
		for (Edge e : edgeWeights.keySet()) {
			if (e.isBetweenNodes(LSR1, LSR2)) {
				edgeWeights.put(e, weight);
			}
		}
	}
	
	public Double getWeightForEdgeBetweenNodes(Node nodeA, Node nodeB) {
		for (Edge e : edgeWeights.keySet()) {
			if(e.isBetweenNodes(nodeA, nodeB)) {
				return edgeWeights.get(e);
			}
		}
		return 0.0;
	}
	
	public static ArrayList<Integer> pathFromTo(Integer fromNodeAddress, Integer toNodeAddress) {		
		computeShortestPaths(fromNodeAddress);
		return(bestPathToDestination(toNodeAddress));
	}
	
	public static Integer nextHopFromTo(Integer fromNodeAddress, Integer toNodeAddress) {
		return(pathFromTo(fromNodeAddress, toNodeAddress).get(1));
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
				double weight = edgeWeights.get(e);
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