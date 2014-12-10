package DataTypes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Packet {
	public int source; // The source and destination addresses
	public int dest;
	private int DSCP;
	private boolean OAM = false;
	public String data = "";
	private Queue<MPLS> MPLSheader = new LinkedList<MPLS>(); // all of the MPLS headers in this router
	public HashMap<Integer, Integer> DSCP_AF_Map = new HashMap<Integer, Integer>();
	
	/**
	 * The default constructor for a packet
	 * @param source the source ip address of this packet
	 * @param dest the destination ip address of this packet
	 * @param DSCP Differential Services Code Point
	 * @since 1.0
	 */
	public Packet(int source, int dest, int DSCP){
		try{
			this.source = source;
			this.dest = dest;
			this.DSCP = DSCP;
			this.setupDSCPAFMap();
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public Boolean isOAMPacket() {
		return this.isOAMPacket();
	}
	
	public void setOAM() {
		this.OAM = true;
	}
	
	/**
	 * Adds an MPLS header to a packet
	 * @since 1.0
	 */
	public void addMPLSheader(MPLS header){
		MPLSheader.add(header);
	}
	
	/**
	 * Pops an MPLS header from the packet
	 * @since 1.0
	 */
	public MPLS popMPLSheader(){
		return MPLSheader.poll();
	}
	
	/**
	 * Returns the source ip address of this packet
	 * @return the source ip address of this packet
	 * @since 1.0
	 */
	public int getSource(){
		return this.source;
	}
	
	/**
	 * Returns the destination ip address of this packet
	 * @return the destination ip address of this packet
	 * @since 1.0
	 */
	public int getDest(){
		return this.dest;
	}

	/**
	 * Set the DSCP field
	 * @param DSCP the DSCP field value
	 * @since 1.0
	 */
	public void setDSCP(int dSCP) {
		this.DSCP = dSCP;
	}

	/**
	 * Returns the DSCP field
	 * @return the DSCP field
	 * @since 1.0
	 */
	public int getDSCP() {
		return this.DSCP;
	}
	
	/* Cite the source */
	private void setupDSCPAFMap() {
		
		this.DSCP_AF_Map.put(ConstStrings.PHB_EF,ConstStrings.EF);
		this.DSCP_AF_Map.put(ConstStrings.DSCP11,ConstStrings.AF1);
		this.DSCP_AF_Map.put(ConstStrings.DSCP21,ConstStrings.AF2);
		this.DSCP_AF_Map.put(ConstStrings.DSCP31,ConstStrings.AF3);
		this.DSCP_AF_Map.put(ConstStrings.DSCP41,ConstStrings.AF4);
		this.DSCP_AF_Map.put(ConstStrings.DSCP12,ConstStrings.AF1);
		this.DSCP_AF_Map.put(ConstStrings.DSCP22,ConstStrings.AF2);
		this.DSCP_AF_Map.put(ConstStrings.DSCP32,ConstStrings.AF3);
		this.DSCP_AF_Map.put(ConstStrings.DSCP42,ConstStrings.AF4);
		this.DSCP_AF_Map.put(ConstStrings.DSCP13,ConstStrings.AF1);
		this.DSCP_AF_Map.put(ConstStrings.DSCP23,ConstStrings.AF2);
		this.DSCP_AF_Map.put(ConstStrings.DSCP33,ConstStrings.AF3);
		this.DSCP_AF_Map.put(ConstStrings.DSCP43,ConstStrings.AF4);
	}
}
	
