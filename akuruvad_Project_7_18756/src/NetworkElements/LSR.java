package NetworkElements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import DataTypes.MPLS;
import DataTypes.OpticalLabel;
import DataTypes.PATHPacket;
import DataTypes.Packet;
import DataTypes.RESVPacket;

public class LSR {
	protected int address; // The AS address of this router
	protected ArrayList<LSRNIC> nics = new ArrayList<LSRNIC>(); // all of the
																// nics in this
																// router

	protected HashMap<Integer, Integer> destinationLabelMap = new HashMap<Integer, Integer>();
	protected HashMap<Integer, Integer> labelTable = new HashMap<Integer, Integer>();
	protected HashMap<Integer, LSRNIC> labelMap = new HashMap<Integer, LSRNIC>();
	protected ArrayList<Packet> waitingPackets = new ArrayList<Packet>();

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
	 * The default constructor for an ATM router
	 * 
	 * @param address
	 *            the address of the router
	 * @since 1.0
	 */
	public LSR(int address, boolean psc, boolean lsc) {
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
		System.out.println("packet: " + currentPacket.getSource() + ", "
				+ currentPacket.getDest());
		System.out.println("\tOAM: " + currentPacket.isOAM());
		if (currentPacket.isOAM()) {
			System.out.println("\tOAM: " + currentPacket.getOAMMsg() + ", "
					+ currentPacket.getOpticalLabel().toString());
		}

		if (currentPacket.isOAM()) {
			if (currentPacket.getOAMMsg().equals("PATH")
					&& currentPacket.getDest() != this.address) {
				PATHPacket receivedPathPacket = (PATHPacket) currentPacket;
				Integer prevHop = receivedPathPacket.pathHops
						.get(receivedPathPacket.pathHops.size() - 1);
				LSRNIC prevHopNIC = this.nicConnectedToDestination(prevHop);
				this.labelMap.put(receivedPathPacket.upstreamLabel, prevHopNIC);
				Integer upstreamLabel = this.generateNewLabel();
				while (this.labelTable.keySet().contains(upstreamLabel)) {
					upstreamLabel = this.generateNewLabel();
				}
				this.destinationLabelMap.put(currentPacket.getSource(), upstreamLabel);
				this.labelTable.put(upstreamLabel,
						receivedPathPacket.upstreamLabel);
				receivedPathPacket.pathHops.add(this.address);
				receivedPathPacket.setUpstreamLabel(upstreamLabel);
//				System.out.println("this.destinationLabelMap :"
//						+ this.destinationLabelMap.toString());
//				System.out.println("Current Label Table :"
//						+ this.labelTable.toString());
//				System.out
//						.println("this.labelMap :" + this.labelMap.toString());

				Integer nextPSCHop = Topology.nextHopFromTo(this.address,
						receivedPathPacket.getDest());
				if (nextPSCHop == null) {
					System.out.println("nextPSCHop is NULL!");
				} else {
					LSRNIC nicForNextHop = this
							.nicConnectedToDestination(nextPSCHop);
					if (nicForNextHop != null) {
						nicForNextHop.sendPacket(receivedPathPacket, this);
					}
				}
			} else if (currentPacket.getOAMMsg().equals("PATH")
					&& currentPacket.getDest() == this.address) {
				System.out.println("PATH from " + currentPacket.getSource()
						+ " reached!");
				PATHPacket receivedPathPacket = (PATHPacket) currentPacket;
				ArrayList<Integer> allPathHops = receivedPathPacket
						.getPathHops();
				RESVPacket resvPacket = new RESVPacket(
						receivedPathPacket.getDest(),
						receivedPathPacket.getSource());
				resvPacket.setPathHops(allPathHops);
				resvPacket.getResvHops().add(this.address);
				Integer downstreamLabel = this.generateNewLabel();

				while (this.labelTable.keySet().contains(downstreamLabel)) {
					downstreamLabel = this.generateNewLabel();
				}
				Integer newLabel = this.generateNewLabel();

				Integer prevHopAddress = allPathHops
						.remove(allPathHops.size() - 1);
				LSRNIC nicForPrevHop = this
						.nicConnectedToDestination(prevHopAddress);
				
				this.destinationLabelMap.put(currentPacket.getSource(), newLabel);
				this.labelTable.put(newLabel, downstreamLabel);// Last stop
				this.labelMap.put(downstreamLabel, nicForPrevHop);

				nicForPrevHop.sendPacket(resvPacket, this);
			}
			if (currentPacket.getOAMMsg().equals("RESV")
					&& currentPacket.getDest() != this.address) {
				RESVPacket receivedResvPacket = (RESVPacket) currentPacket;
				Integer prevResvHop = receivedResvPacket.getResvHops().get(
						receivedResvPacket.getResvHops().size() - 1);
				Integer newLabel = this.generateNewLabel();
				Integer upstreamLabelReceived = receivedResvPacket.getUpstreamLabel();
				LSRNIC nicForPrevResvHop = this.nicConnectedToDestination(prevResvHop);
				this.destinationLabelMap.put(currentPacket.getSource(), newLabel);
				this.labelTable.put(newLabel, upstreamLabelReceived);
				this.labelMap.put(upstreamLabelReceived, nicForPrevResvHop);
				System.out.println("this.destinationLabelMap :"
						+ this.destinationLabelMap.toString());
				System.out.println("Current Label Table :"
						+ this.labelTable.toString());
				System.out
						.println("this.labelMap :" + this.labelMap.toString());
				System.out.println("receivedResvPacket.getPathHops() "+ receivedResvPacket.getPathHops());
				receivedResvPacket.setUpstreamLabel(newLabel);
				receivedResvPacket.getResvHops().add(this.address);
				
				Integer nextResvHop = receivedResvPacket.getPathHops().remove(receivedResvPacket.getPathHops().size()-1);
				System.out.println("receivedResvPacket.getPathHops() "+ receivedResvPacket.getPathHops());

				LSRNIC nicForNextResvHop =  this.nicConnectedToDestination(nextResvHop);
				System.out.println("nextResvHop "+ nextResvHop);

				nicForNextResvHop.sendPacket(receivedResvPacket, this);
				
			} else if (currentPacket.getOAMMsg().equals("RESV")
			&& currentPacket.getDest() == this.address) {
				System.out.println("RESV came back successfully");
				RESVPacket receivedResvPacket = (RESVPacket) currentPacket;
				Integer upstreamLabelReceived = receivedResvPacket.getUpstreamLabel();
				Integer newLabel = this.generateNewLabel();
				Integer prevResvHop = receivedResvPacket.getResvHops().get(
						receivedResvPacket.getResvHops().size() - 1);
				LSRNIC nicForPrevResvHop = this.nicConnectedToDestination(prevResvHop);
				this.destinationLabelMap.put(currentPacket.getSource(), newLabel);
				this.labelTable.put(newLabel, upstreamLabelReceived);
				this.labelMap.put(upstreamLabelReceived, nicForPrevResvHop);
			}

		} else {
			//Data Packet
			if(currentPacket.getDest()== this.address) {
				System.out.println("Data reached!");
			}
			else if(this.destinationLabelMap.containsKey(currentPacket.getDest())) {
				Integer label = this.destinationLabelMap.get(currentPacket.getDest());
				Integer outLabel = this.labelTable.get(label);
				LSRNIC nicToForwardTo = this.labelMap.get(outLabel);
				nicToForwardTo.sendPacket(currentPacket, this);
			}
			
		}

		// if(currentPacket.getDest() == this.address) {
		// System.out.println("Reached destination!");
		// } else {
		// System.out.println("current address "+this.address);
		//
		//
		// Integer nextPSCHop = Topology.nextHopFromTo(this.address,
		// currentPacket.getDest());
		// // System.out.println("PATH from here: "+rpath.toString());
		//
		// if (nextPSCHop != null) {
		// System.out.println("Best route next hop " + nextPSCHop);
		// } else {
		// System.out.println("next PSC hop is null! for source "
		// + currentPacket.getSource());
		// }
		// System.out.println("Address of current NIC " + this.address);
		// LSRNIC nicForNextHop = this.nicConnectedToDestination(nextPSCHop);
		// if (nicForNextHop != null) {
		// nicForNextHop.sendPacket(currentPacket, this);
		// }
		// }
	}

	/**
	 * This method creates a packet with the specified type of service field and
	 * sends it to a destination
	 * 
	 * @param destination
	 *            the destination router
	 * @since 1.0
	 */
	public void createPacket(int destination) {
		Packet newPacket = new Packet(this.getAddress(), destination);
		this.sendPacket(newPacket);
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

		// This method should send the packet to the correct NIC (and wavelength
		// if LSC router).
		System.out.println("Recieved Packet to address " + newPacket.getDest());
		System.out.println("this.destinationLabelMap " + this.destinationLabelMap.toString());

		MPLS header = newPacket.popMPLSheader();
		Integer nextPSCHop = Topology.nextHopFromTo(this.address,
				newPacket.getDest());
		LSRNIC nextHOPNIC = this.nicConnectedToDestination(nextPSCHop);

		if (!this.destinationLabelMap.containsKey(newPacket.getDest())) {
			// No MPLS header, must be new packet
			System.out.println("Need to start PATH!");
			this.waitingPackets.add(newPacket);
			PATHPacket pathPacket = new PATHPacket(newPacket.getSource(),
					newPacket.getDest());
			Integer upstreamLabel = this.generateNewLabel();

			while (this.labelTable.keySet().contains(upstreamLabel)) {
				upstreamLabel = this.generateNewLabel();
			}

			pathPacket.setUpstreamLabel(upstreamLabel);
			pathPacket.addHopToPath(this.address);
			nextHOPNIC.sendPacket(pathPacket, this);
			this.labelTable.put(upstreamLabel, null);// Last stop for the
														// message.
		} else {
			Integer label = this.destinationLabelMap.get(newPacket.getDest());
			Integer outLabel = this.labelTable.get(label);
			LSRNIC outNIC = this.labelMap.get(outLabel);
			outNIC.sendPacket(newPacket, this);
		}

		// ArrayList <Integer> path = Topology.pathFromTo(this.address,
		// newPacket.getDest());
		// System.out.println("Path from this address "+path.toString());

		// Integer nextPSCHop = Topology.nextHopFromTo(this.address,
		// newPacket.getDest());
		// System.out.println("nextPSCHop "+nextPSCHop);
		//
		// if(nextPSCHop!=null) {
		// System.out.println("Best route next hop "+nextPSCHop);
		// } else {
		// System.out.println("next PSC hop is null! for source "+newPacket.getSource());
		// }
		// System.out.println("Address of current NIC "+this.address);
		//
		//
		// LSRNIC nicForNextHop = this.nicConnectedToDestination(nextPSCHop);
		// if(nicForNextHop!=null) {
		// System.out.println("Address of NIC sending to "+nicForNextHop.getParent().address);
		// nicForNextHop.sendPacket(newPacket, this);
		// }
	}

	public LSRNIC nicConnectedToDestination(int dest) {
		for (LSRNIC nic : this.nics) {
			if (nic.connectedToNICWithAddress() == dest) {
				// System.out.println("Checking neighbor nic address "+
				// nic.connectedNIC().getParent().address);
				return nic;
			}
		}
		return null;
	}

	/**
	 * This method forwards a packet to the correct nic or drops if at
	 * destination router
	 * 
	 * @param newPacket
	 *            The packet that has just arrived at the router.
	 * @since 1.0
	 */
	public void sendKeepAlivePackets() {

		// This method should send the keep alive packets for routes for each
		// the router is an inbound router

	}

	/**
	 * Makes each nic move its cells from the output buffer across the link to
	 * the next router's nic
	 * 
	 * @since 1.0
	 */
	public void sendPackets() {
		sendKeepAlivePackets();
		for (int i = 0; i < this.nics.size(); i++)
			this.nics.get(i).sendPackets();
	}

	/**
	 * Makes each nic move all of its cells from the input buffer to the output
	 * buffer
	 * 
	 * @since 1.0
	 */
	public void receivePackets() {
		for (int i = 0; i < this.nics.size(); i++)
			this.nics.get(i).receivePackets();
	}

	public void sendKeepAlive(int dest, OpticalLabel label) {
		Packet p = new Packet(this.getAddress(), dest, label);
		p.setOAM(true, "KeepAlive");
		this.sendPacket(p);
	}

	public int generateNewLabel() {
		Random rand = new Random();
		int randomNum = rand.nextInt(899) + 100;
		return randomNum;
	}
}
