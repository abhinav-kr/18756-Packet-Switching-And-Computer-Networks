package DataTypes;

import java.util.ArrayList;

public class PathPacket extends Packet {
	
	Integer PHB;
	Integer bwidthToBeAllocated;
	Integer AFClass;
	
	public ArrayList<Integer> pathHops = new ArrayList<Integer>();
	
	public PathPacket(int source, int destination, int PHB, int bwidthToBeAllocated, int AFClass) {
		super(source, destination, AFClass);
		this.source = source;
		this.dest = destination;
		this.PHB = PHB;
		this.bwidthToBeAllocated = bwidthToBeAllocated;
		this.AFClass = AFClass;
		this.data = "PATH";
	}
	
	public PathPacket (PathPacket p) {
		super(p.source, p.dest, p.AFClass);
		this.source = p.source;
		this.dest = p.dest;
		this.PHB = p.PHB;
		this.bwidthToBeAllocated = p.bwidthToBeAllocated;
		this.AFClass = p.AFClass;
		this.data = "PATH";
	}
	
	public String toString() {
		return "PATH";
	}
	
	public void addPathHop(Integer address){
		this.pathHops.add(address);
	}

	public Integer getAFClass() {
		return AFClass;
	}

	public void setAFClass(Integer aFClass) {
		AFClass = aFClass;
	}

	public Integer getPHB() {
		return PHB;
	}

	public void setPHB(Integer pHB) {
		PHB = pHB;
	}

	public Integer getBwidthToBeAllocated() {
		return bwidthToBeAllocated;
	}

	public void setBwidthToBeAllocated(Integer bwidthToBeAllocated) {
		this.bwidthToBeAllocated = bwidthToBeAllocated;
	}

	public ArrayList<Integer> getPathHops() {
		return pathHops;
	}
	
	public Integer getNextHop() {
		return pathHops.remove(pathHops.size()-1);
	}

	public void setPathHops(ArrayList<Integer> pathHops) {
		this.pathHops = pathHops;
	}

}
