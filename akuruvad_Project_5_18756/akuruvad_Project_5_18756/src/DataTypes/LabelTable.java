package DataTypes;

import java.util.ArrayList;

import NetworkElements.LSRNIC;

class PathInfo {
	private Integer labelIn;
	private NICLabelPair labelDestPair;
	private Integer pathSourceAddress;
	private Integer pathDestAddress;
	
	PathInfo(Integer labelIn, NICLabelPair labelNICPair, Integer sourceAddress, Integer destAddress) {
		this.labelIn = labelIn;
		this.labelDestPair = labelNICPair;
		this.pathSourceAddress = sourceAddress;
		this.pathDestAddress = destAddress;
	}
	
	public Integer getLabelIn() {
		return labelIn;
	}
	public void setLabelIn(Integer labelIn) {
		this.labelIn = labelIn;
	}
	public NICLabelPair getLabelDestPair() {
		return labelDestPair;
	}
	public void setLabelDestPair(NICLabelPair labelDestPair) {
		this.labelDestPair = labelDestPair;
	}
	public Integer getPathSourceAddress() {
		return pathSourceAddress;
	}
	public void setPathSourceAddress(Integer pathSourceAddress) {
		this.pathSourceAddress = pathSourceAddress;
	}
	public Integer getPathDestAddress() {
		return pathDestAddress;
	}
	public void setPathDestAddress(Integer pathDestAddress) {
		this.pathDestAddress = pathDestAddress;
	}	
}

public class LabelTable {
	
	ArrayList<PathInfo> LSPs = new ArrayList<PathInfo>();
	
	public void addNewPathInfo(Integer labelIn, NICLabelPair labelNICPair, Integer sourceAddress, Integer destAddress) {
		PathInfo newPath = new PathInfo(labelIn, labelNICPair, sourceAddress,destAddress);
		LSPs.add(newPath);
	}
	
	public NICLabelPair getNICLabelPair(Integer labelIn) {
		for (PathInfo path : LSPs ) {
			if(path.getLabelIn()==labelIn) {
				return path.getLabelDestPair();
			}
		}
		return null;
	}
	
	public NICLabelPair getNICLabelPair(Integer labelIn, LSRNIC nic) {
		for (PathInfo path : LSPs ) {
			if(path.getLabelIn()==labelIn && path.getLabelDestPair().getNIC() == nic) {
				return path.getLabelDestPair();
			}
		}
		return null;
	}
	
	public NICLabelPair getNICLabelPairForDest(Integer destAddress) {
		for (PathInfo path : LSPs ) {
			if(path.getPathDestAddress()==destAddress) {
				return path.getLabelDestPair();
			}
		}
		return null;
	}
	
	public Boolean pathExists(Integer sourceAddress, Integer destAddress) {
		for (PathInfo path : LSPs ) {
			if(path.getPathDestAddress()==destAddress && path.getPathSourceAddress()==sourceAddress) {
				return true;
			}
		}
		return false;
	}
	
	public Boolean labelExists(Integer labelIn) {
		for (PathInfo path : LSPs ) {
			if(path.getLabelIn() ==  labelIn) {
				return true;
			}
		}
		return false;
	}
	
	public Integer labelInForPath(Integer sourceAddress, Integer destAddress) {
		for (PathInfo path : LSPs ) {
			if(path.getPathDestAddress()==destAddress && path.getPathSourceAddress()==sourceAddress) {
				return path.getLabelIn();
			}
		}
		return -1;
	}
}
