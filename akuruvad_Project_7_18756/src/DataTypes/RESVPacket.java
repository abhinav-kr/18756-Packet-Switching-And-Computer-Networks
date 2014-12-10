package DataTypes;

import java.util.ArrayList;

public class RESVPacket extends Packet {

	public ArrayList<Integer> pathHops = new ArrayList<Integer>();
	public ArrayList<Integer> resvHops = new ArrayList<Integer>();
	public Integer upstreamLabel, downStreamLabel;

	public ArrayList<Integer> getResvHops() {
		return resvHops;
	}

	public void setResvHops(ArrayList<Integer> resvHops) {
		this.resvHops = resvHops;
	}

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
	public RESVPacket(int source, int dest) {
		super(source, dest);
		this.setOAM(true, "RESV");
	}
	
	public RESVPacket(int source, int dest, OpticalLabel label) {
		super(source, dest, label);
		this.setOAM(true, "RESV");
	}
}
