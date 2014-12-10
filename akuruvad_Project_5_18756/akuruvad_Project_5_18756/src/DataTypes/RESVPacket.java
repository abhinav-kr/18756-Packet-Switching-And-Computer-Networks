package DataTypes;

import java.util.ArrayList;

public class RESVPacket extends Packet {

	private Integer PHB;
	private Integer bwidthToBeAllocated;
	private Integer AFClass;
	private Integer label;
	ArrayList<Integer> pathHops = new ArrayList<Integer>();

	public RESVPacket(int source, int dest, int DSCP, int PHB,
			int bwidthToBeAllocated, int AFClass, ArrayList<Integer> pathHops) {
		super(source, dest, DSCP);
		this.PHB = PHB;
		this.bwidthToBeAllocated = bwidthToBeAllocated;
		this.AFClass = AFClass;
		this.data = "RESV";
		this.pathHops = pathHops;
	}
	
	public RESVPacket (RESVPacket p) {
		super(p.source, p.dest, p.AFClass);
		this.source = p.source;
		this.dest = p.dest;
		this.PHB = p.PHB;
		this.bwidthToBeAllocated = p.bwidthToBeAllocated;
		this.AFClass = p.AFClass;
		this.data = "PATH";
	}
	
	public RESVPacket(int source, int dest) {
		super(source, dest, 0);
	}
	
	
	public void setAsResvConfirmation() {
		this.data = "RESVCONF";
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

	public Integer getAFClass() {
		return AFClass;
	}

	public void setAFClass(Integer aFClass) {
		AFClass = aFClass;
	}

	public Integer getLabel() {
		return label;
	}

	public void setLabel(Integer label) {
		this.label = label;
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
