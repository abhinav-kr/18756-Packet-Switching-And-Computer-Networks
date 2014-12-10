package NetworkElements;

import java.util.ArrayList;
import java.util.HashMap;

import DataTypes.ConstStrings;
import DataTypes.LabelTable;
import DataTypes.MPLS;
import DataTypes.NICLabelPair;
import DataTypes.Packet;
import DataTypes.PathPacket;
import DataTypes.RESVPacket;

public class LSR {
	private int address; // The AS address of this router
	private ArrayList<LSRNIC> nics = new ArrayList<LSRNIC>(); // all of the nics
																// in this
																// router
	private LabelTable labelTable = new LabelTable();
	private HashMap<Integer, LSRNIC> routingTable = new HashMap<Integer, LSRNIC>();
	private ArrayList<Packet> waitQueue = new ArrayList<Packet>();

	/**
	 * The default constructor for an ATM router
	 * 
	 * @param address
	 *            the address of the router
	 * @since 1.0
	 */
	public LSR(int address) {
		this.address = address;
	}

	/**
	 * The return the router's address
	 * 
	 * @since 1.0
	 */
	public int getAddress() {
		return this.address;
	}

	/**
	 * Adds a nic to this router
	 * 
	 * @param nic
	 *            the nic to be added
	 * @since 1.0
	 */
	public void addNIC(LSRNIC nic) {
		this.nics.add(nic);
	}

	private LSRNIC getNICForAddress(Integer destAddress) {

		for (LSRNIC nic : this.nics) {
			OtoOLink nicLink = nic.getOtoOLink();
			LSRNIC linkNIC2 = nicLink.getNIC2();
			if (nic == nicLink.getNIC1()) {
				return nicLink.getNIC2();
			} else {
				return nicLink.getNIC1();
			}
		}
		return null;
	}

	public void setRoutingTable() {
		this.routingTable.clear();
		for (Integer nodeAddress : Topology.nodeList.keySet()) {
			if (nodeAddress != this.address) {
				LSRNIC nicForNexthop = this.getNICForAddress(Topology
						.nextHopFromTo(this.address, nodeAddress));
				this.routingTable.put(nodeAddress, nicForNexthop);
			}
		}
	}

	public void receivedPathPacket(Packet currentPacket, LSRNIC nic) {
		PathPacket currentPathPacket = (PathPacket) currentPacket;
		ArrayList<Integer> pathPackets = currentPathPacket.getPathHops();
		Integer newLabel = 0;
		this.setRoutingTable();
		this.printRoutingTable();

		if (currentPacket.getDest() == this.address) {

			if (this.labelTable.pathExists(currentPacket.source,
					currentPacket.dest)) {
				newLabel = this.labelTable.labelInForPath(currentPacket.source,
						currentPacket.dest);
				System.out
						.println("Path already exists. Using the same Label : "
								+ newLabel);

			} else {
				for (int i = 1; i < Integer.MAX_VALUE; i++) {
					if (!labelTable.labelExists(i)) {
						newLabel = i;
					}
				}
				this.labelTable.addNewPathInfo(newLabel, null,
						currentPacket.source, currentPacket.dest);
				System.out
						.println("New Path added to Label Table. Using the Label : "
								+ newLabel.toString());
			}

			ArrayList<Integer> pathhopsforresv = new ArrayList<Integer>(
					currentPathPacket.getPathHops());
			RESVPacket resvPacket = new RESVPacket(this.address,
					currentPathPacket.source, currentPathPacket.getDSCP(),
					currentPathPacket.getPHB(),
					currentPathPacket.getBwidthToBeAllocated(),
					currentPathPacket.getAFClass(), pathhopsforresv);
			resvPacket.setLabel(newLabel);
			resvPacket.setOAM();
			LSRNIC NICToForwardTo = this.routingTable.get(resvPacket
					.getNextHop());
			NICToForwardTo.sendPacket(resvPacket, this);
			System.out.println("Router " + this.address
					+ " sent a PATH to Router " + resvPacket.getDest());
		} else {
			System.out.println("Router " + this.address
					+ " received a PATH from Router "
					+ currentPathPacket.source);
			PathPacket newPathPacket = new PathPacket(currentPathPacket);
			newPathPacket.addPathHop(this.address);
			LSRNIC NICToForwardTo = this.routingTable.get(newPathPacket
					.getDest());
			NICToForwardTo.sendPacket(newPathPacket, this);
			System.out.println("Router " + this.address
					+ " sent a PATH to Router " + newPathPacket.dest);

		}

	}

	public void receivedRESVPacket(Packet currentPacket, LSRNIC nic) {
		
		RESVPacket currentRESVPacket = (RESVPacket) currentPacket;
		ArrayList<Integer> pathPackets = currentRESVPacket.getPathHops();
		
		if(currentRESVPacket.getDest() == this.address ) {
			if(nic.attemptToReserveBandwidth(currentRESVPacket.getPHB(), currentRESVPacket.getAFClass(), currentRESVPacket.getBwidthToBeAllocated()) == true) {
				this.generateLabelMapping(currentPacket, nic);
				RESVPacket resvConfPacket = new RESVPacket(this.address, currentRESVPacket.getSource());
				resvConfPacket.setOAM();
				resvConfPacket.setAsResvConfirmation();
				LSRNIC NICToForwardTo = this.labelTable.getNICLabelPairForDest(currentRESVPacket.getSource()).getNIC();
				NICToForwardTo.sendPacket(resvConfPacket, this);
				System.out.println("Router " + this.address + " sent a RESVCONF to router " +currentRESVPacket.getDest());
			}
			
		} else {
			System.out.println("Router " + this.address
					+ " received a RESV from Router "
					+ pathPackets.get(pathPackets.size() - 1));
			RESVPacket newRESVPacket = new RESVPacket(currentRESVPacket);
			newRESVPacket.setOAM();
			LSRNIC NICToForwardTo = this.routingTable.get(newRESVPacket.getNextHop());
			if(nic.attemptToReserveBandwidth(currentRESVPacket.getPHB(), currentRESVPacket.getAFClass(), currentRESVPacket.getBwidthToBeAllocated()) == true) {
				this.generateLabelMapping(newRESVPacket, nic);
				NICToForwardTo.sendPacket(newRESVPacket, this);
				System.out.println("Router " + this.address + " sent a RESV to router "+ newRESVPacket.getDest());
			} else {
				System.out.println("not enough Resources");
			}	
		}
	}

	public void receivedRESVCONFPacket(Packet currentPacket, LSRNIC nic) {
		RESVPacket currentRESVCONFPacket = (RESVPacket) currentPacket;
		System.out.println("Router " + this.address + " received a RESVCONF");
		LSRNIC NICToForwardTo = this.labelTable.getNICLabelPairForDest(
				currentRESVCONFPacket.getDest()).getNIC();
		NICToForwardTo.sendPacket(currentPacket, this);
	}

	public void setupPathForPacket(Packet packet) {
		packet.setDSCP(ConstStrings.DSCP_BE);
		allocateBandwidth(packet.getDest(), ConstStrings.PHB_BE, 0, 0);
		NICLabelPair newNICLabelPair = new NICLabelPair(
				this.routingTable.get(packet.getDest()), -1);
		newNICLabelPair.waitingForPath = true;

		this.labelTable.addNewPathInfo(-1, newNICLabelPair, packet.source,
				packet.dest);
		this.waitQueue.add(packet);
	}

	public void generateLabelMapping(Packet packet, LSRNIC nic) {
		if (!this.labelTable.pathExists(packet.getSource(), packet.getDest())) {
			Integer newLabel = 0;
			for (int i = 1; i < Integer.MAX_VALUE; i++) {
				if (!labelTable.labelExists(i)) {
					newLabel = i;
				}
			}
			Integer labelOut = ((RESVPacket) packet).getLabel();
			NICLabelPair newPair = new NICLabelPair(nic, newLabel);
			this.labelTable.addNewPathInfo(newLabel, newPair,
					packet.getSource(), packet.getDest());
			System.out.println("New Label Mapping generated");
			((RESVPacket) packet).setLabel(newLabel);
		} else {
			NICLabelPair nlPair = this.labelTable
					.getNICLabelPairForDest(packet.source);
			if (nlPair.waitingForPath) {
				nlPair.setLabel(((RESVPacket) packet).getLabel());
				this.sendFromWaitingQueue((RESVPacket) packet);
			} else {
				Integer LabelIn = this.labelTable.labelInForPath(
						packet.getSource(), packet.getDest());
				((RESVPacket) packet).setLabel(LabelIn);
				System.out.println("Packet mapped to Label:"
						+ LabelIn.toString());
			}
		}
	}

	public void sendFromWaitingQueue(RESVPacket packet) {
		for (Packet p : this.waitQueue) {
			if (p.getDest() == packet.getSource()) {
				Integer labelOut = this.labelTable.getNICLabelPairForDest(
						packet.getDest()).getLabel();
				MPLS mplsHeader = new MPLS(labelOut, packet.getDSCP(),1);
				packet.addMPLSheader(mplsHeader);
				LSRNIC NICToForwardTo = this.labelTable.getNICLabelPairForDest(
						packet.getDest()).getNIC();
				NICToForwardTo.sendPacket(packet, this);
				break;
			}
		}
	}
	
	/**
	 * This method processes data and OAM cells that arrive from any nic with
	 * this router as a destination
	 * 
	 * @param currentPacket
	 *            the packet that arrived at this router
	 * @param nic
	 *            the nic that the cell arrived on
	 * @since 1.0
	 */
	public void receivePacket(Packet currentPacket, LSRNIC nic) {

		if (!currentPacket.isOAMPacket() && currentPacket.dest == this.address) {
			System.out.println("Router " + this.address
					+ " received a packet sent from router "
					+ currentPacket.getSource());
		} else if (currentPacket.isOAMPacket()) {
			if (currentPacket.data.equals("PATH")) {
				this.receivedPathPacket(currentPacket, nic);
			} else if (currentPacket.data.equals("RESV")) {
				this.receivedRESVPacket(currentPacket, nic);
			} else if (currentPacket.data.equals("RESVCONF")) {
				this.receivedRESVCONFPacket(currentPacket, nic);
			}
		} else {
			this.sendPacket(currentPacket);
		}

	}

	/**
	 * This method creates a packet with the specified type of service field and
	 * sends it to a destination
	 * 
	 * @param destination
	 *            the distination router
	 * @param DSCP
	 *            the differentiated services code point field
	 * @since 1.0
	 */
	public void createPacket(int destination, int DSCP) {
		Packet newPacket = new Packet(this.getAddress(), destination, DSCP);
		this.sendPacket(newPacket);
	}

	/**
	 * This method allocates bandwidth for a specific traffic class from the
	 * current router to the destination router
	 * 
	 * @param dest
	 *            destination router id
	 * @param PHB
	 *            0=EF, 1=AF, 2=BE
	 * @param Class
	 *            AF classes 1,2,3,4. (0 if EF or BE)
	 * @param Bandwidth
	 *            number of packets per time unit for this PHB/Class
	 * @since 1.0
	 */
	public void allocateBandwidth(int destination, int PHB, int afClass, int bandwidth) {
		PathPacket pathPacket = new PathPacket(this.address, destination, PHB, bandwidth, afClass);
		LSRNIC NICToForwardTo = this.routingTable.get(destination);
		pathPacket.addPathHop(this.address);
		pathPacket.setOAM();
		if(NICToForwardTo!=null) {
			System.out.println("NICToForwardTo : "+NICToForwardTo.getOtoOLink().getNIC2().getParent().getAddress());
			NICToForwardTo.sendPacket(pathPacket, this);
		}
	}

	/**
	 * This method forwards a packet to the correct nic or drops if at
	 * destination router
	 * 
	 * @param newPacket
	 *            The packet that has just arrived at the router.
	 * @since 1.0
	 */
	public void sendPacket(Packet newPacket) {

		// This method should send the packet to the correct NIC.
		
		if (this.labelTable.pathExists(newPacket.getSource(), newPacket.getDest())) {
			if(this.labelTable.getNICLabelPairForDest(newPacket.getDest()).waitingForPath) {
				this.waitQueue.add(newPacket);
			} else {
				Integer labelOut = this.labelTable.getNICLabelPairForDest(newPacket.getDest()).getLabel();
				newPacket.addMPLSheader(new MPLS(labelOut, newPacket.getDSCP(), 1));
				LSRNIC NICToForwardTo = this.labelTable.getNICLabelPairForDest(newPacket.getDest()).getNIC();
				NICToForwardTo.sendPacket(newPacket, this);
			} 
		} else {
			this.setupPathForPacket(newPacket);
			System.out.println("Need to setup packet path");
		}
	}

	/**
	 * Makes each nic move its cells from the output buffer across the link to
	 * the next router's nic
	 * 
	 * @since 1.0
	 */
	public void sendPackets() {
		for (int i = 0; i < this.nics.size(); i++)
			this.nics.get(i).sendPackets();
	}

	/**
	 * Makes each nic move all of its cells from the input buffer to the output
	 * buffer
	 * 
	 * @since 1.0
	 */
	public void recievePackets() {
		for (int i = 0; i < this.nics.size(); i++)
			this.nics.get(i).recievePackets();
	}
	
	public void printRoutingTable(){
		System.out.println("Routing table for Router " + this.address + "\n===");
		for(int i:this.routingTable.keySet()){
			System.out.println(i + ": " + routingTable.get(i));
		}
	}
}
