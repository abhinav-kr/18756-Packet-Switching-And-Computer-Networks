package DataTypes;

import java.util.ArrayList;

public class PATHPacket extends Packet {
	
	public ArrayList<Integer> pathHops = new ArrayList<Integer>();
	public Integer upstreamLabel, downStreamLabel;

	public ArrayList<Integer> getPathHops() {
		return pathHops;
	}

	public void setPathHops(ArrayList<Integer> pathHops) {
		this.pathHops = pathHops;
	}

	public Integer getUpstreamLabel() {
		return upstreamLabel;
	}

	public Integer getDownStreamLabel() {
		return downStreamLabel;
	}

	public void setUpstreamLabel(Integer upstreamLabel) {
		this.upstreamLabel = upstreamLabel;
	}

	public void setDownStreamLabel(Integer downStreamLabel) {
		this.downStreamLabel = downStreamLabel;
	}

	public PATHPacket(int source, int dest) {
		super(source, dest);
		this.setOAM(true, "PATH");
		// TODO Auto-generated constructor stub
	}
	
	public PATHPacket(int source, int dest, OpticalLabel label) {
		super(source, dest, label);
		this.setOAM(true, "PATH");
		// TODO Auto-generated constructor stub
	}
	
	public void addHopToPath(Integer address) {
		this.pathHops.add(address);
	}
}
