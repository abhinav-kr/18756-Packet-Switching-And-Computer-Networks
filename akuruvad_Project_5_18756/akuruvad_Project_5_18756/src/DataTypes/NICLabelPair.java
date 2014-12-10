package DataTypes;

import NetworkElements.*;

public class NICLabelPair implements Comparable<NICLabelPair>{
	private LSRNIC nic; // The nic of the pair
	private int label; // the label of the pair
	public Boolean waitingForPath = false;
	
	/**
	 * Constructor for a pair of (nic, label)
	 * @param nic the nic that is in the pair
	 * @param label the label that is in the pair
	 * @since 1.0
	 */
	public NICLabelPair(LSRNIC nic, int label){
		this.nic = nic;
		this.label = label;
	}
	
	/**
	 * Returns the nic that makes up half of the pair
	 * @return the nic that makes up half of the pair
	 * @since 1.0
	 */
	public LSRNIC getNIC(){
		return this.nic;
	}
	
	/**
	 * Returns the nic that makes up half of the pair
	 * @return the nic that makes up half of the pair
	 * @since 1.0
	 */
	public int getLabel(){
		return this.label;
	}
	
	/**
	 * Returns whether or not a given object is the same as this pair. I.e. it is a pair containing the same nic and vc
	 * @return true/false the given object of the same as this object
	 * @since 1.0
	 */
	public boolean equals(Object o){
		if(o instanceof NICLabelPair){
			NICLabelPair other = (NICLabelPair) o;
			
			if(other.getNIC()==this.getNIC() && other.getLabel()==this.getLabel())
				return true;
		}
		
		return false;
	}
	
	/**
	 * Allows this object to be used in a TreeMap
	 * @returns if this object is less than, equal to, or greater than a given object
	 * @since 1.0
	 */
	public int compareTo(NICLabelPair o){
		return this.getLabel()-o.getLabel();
	}

	public LSRNIC getNic() {
		return nic;
	}

	public void setNic(LSRNIC nic) {
		this.nic = nic;
	}

	public void setLabel(int label) {
		this.label = label;
	}
}