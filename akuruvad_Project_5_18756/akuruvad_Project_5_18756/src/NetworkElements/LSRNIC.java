package NetworkElements;

import java.util.ArrayList;
import java.util.Random;

import DataTypes.ConstStrings;
import DataTypes.FIFOQueue;
import DataTypes.Packet;

public class LSRNIC {
	private LSR parent; // The router or computer that this nic is in
	private OtoOLink link; // The link connected to this nic
	private boolean trace = false; // should we print out debug statements?
	private ArrayList<Packet> inputBuffer = new ArrayList<Packet>(); // Where packets are put between the parent and nic
	private ArrayList<Packet> outputBuffer = new ArrayList<Packet>(); // Where packets are put to be sent
	private int maximumBuffer = 100; // the maximum number of packets in the output buffer
	private int startDropAt = 20; // the minimum number of packets in the output buffer before we start dropping packets
	private int linerate = 50;  //number of packets we can send during each time interval
	private int bandwidthLeft = linerate;
	
	private ArrayList<FIFOQueue> PHBQueues = new ArrayList<FIFOQueue>();
	
	/**
	 * Default constructor for an ATM NIC
	 * @param parent
	 * @since 1.0
	 */
	public LSRNIC(LSR parent){
		this.parent = parent;
		this.parent.addNIC(this);
		
		//For the EF, AF1, AF2, AF3, AF4 classes
		for(int i = 0; i <5 ; i++) {
			PHBQueues.add(i, new FIFOQueue(0));
		}
		PHBQueues.add(ConstStrings.BestEffort,new FIFOQueue(linerate));
	}
	
	/**
	 * This method is called when a packet is passed to this nic to be sent. The packet is placed
	 * in an output buffer until a time unit passes
	 * @param currentPacket the packet to be sent (placed in the buffer)
	 * @param parent the router the packet came from
	 * @since 1.0
	 */
	public void sendPacket(Packet currentPacket, LSR parent){
		
		if(this.trace){
			System.out.println("Trace (LSR NIC): Received packet");
			if(this.link==null)
				System.out.println("Error (LSR NIC): You are trying to send a packet through a nic not connected to anything");
			if(this.parent!=parent)
				System.out.println("Error (LSR NIC): You are sending data through a nic that this router is not connected to");
			if(currentPacket==null)
				System.out.println("Warning (LSR NIC): You are sending a null packet");
		}
		
		FIFOQueue PHBQueue = PHBQueues.get(currentPacket.DSCP_AF_Map.get(currentPacket.getDSCP()));
		this.runRED(currentPacket,PHBQueue);
		
		
		parent.sendPacket(currentPacket);
	}
	
	
	/**
	 * Runs Random early detection on the packet
	 * @param currentPacket the packet to be added/dropped from the queue
	 * @since 1.0
	 */

	private void runRED(Packet currentPacket, FIFOQueue queue) {
			
		boolean cellDropped = false;
		double dropProbability = 0.0;
		boolean shouldDropCell = false;
		Random randomNumber = new Random();
		
		int currentOutputBufferSize = queue.getNumPackets();
		System.out.println("currentOutputBufferSize :"+currentOutputBufferSize);
		dropProbability = (double)(currentOutputBufferSize-this.startDropAt)/(double)(this.maximumBuffer-this.startDropAt);

		//Simulating the probability by generating a random number every time and checking if it falls  below the current buffer size
		//So, when the the buffer size is 11, probability is 0.1
		//when the the buffer size is 20, probability is 1
		
		if ( currentOutputBufferSize > this.startDropAt ) {
			System.out.println("Random number is:"+randomNumber.nextInt(Integer.MAX_VALUE));
			if ((randomNumber.nextInt(Integer.MAX_VALUE)%this.maximumBuffer <= currentOutputBufferSize)) {
				shouldDropCell = true;
			}
		}
		
		if (!shouldDropCell) {
			outputBuffer.add(currentPacket);
			cellDropped = false;
		} else {
			cellDropped = true;
		}
		
		/*insert the packet in it's queue if it passed*/
		if(cellDropped == false){
			queue.insert(currentPacket);
			return;
		}
	}
	
	
	/**
	 * This method connects a link to this nic
	 * @param link the link to connect to this nic
	 * @since 1.0
	 */
	public void connectOtoOLink(OtoOLink link){
		this.link = link;
	}
	
	public OtoOLink getOtoOLink() {
		return this.link;
	}
	
	/**
	 * This method is called when a packet is received over the link that this nic is connected to
	 * @param currentPacket the packet that was received
	 * @since 1.0
	 */
	public void receivePacket(Packet currentPacket){
		this.inputBuffer.add(currentPacket);

	}
	
	/**
	 * Moves the packets from the output buffer to the line (then they get moved to the next nic's input buffer)
	 * @since 1.0
	 */
	public void sendPackets(){
				
		int i = 0;
		int totalSent = 0;
		FIFOQueue EFQueue = this.PHBQueues.get(ConstStrings.EF);
		for(i=0;i<Math.min(EFQueue.getWeight(), EFQueue.getNumPackets()); i++) {
			this.outputBuffer.add(EFQueue.remove());
			totalSent++;
		}
		FIFOQueue AF1Queue = this.PHBQueues.get(ConstStrings.AF1);
		for(i=0;i<Math.min(EFQueue.getWeight(), AF1Queue.getNumPackets()); i++) {
			this.outputBuffer.add(AF1Queue.remove());
			totalSent++;
		}
		FIFOQueue AF2Queue = this.PHBQueues.get(ConstStrings.AF2);
		for(i=0;i<Math.min(EFQueue.getWeight(), AF2Queue.getNumPackets()); i++) {
			this.outputBuffer.add(AF2Queue.remove());
			totalSent++;
		}
		FIFOQueue AF3Queue = this.PHBQueues.get(ConstStrings.AF3);
		for(i=0;i<Math.min(EFQueue.getWeight(), AF3Queue.getNumPackets()); i++) {
			this.outputBuffer.add(AF3Queue.remove());
			totalSent++;
		}
		FIFOQueue AF4Queue = this.PHBQueues.get(ConstStrings.AF4);
		for(i=0;i<Math.min(EFQueue.getWeight(), AF4Queue.getNumPackets()); i++) {
			this.outputBuffer.add(AF4Queue.remove());
			totalSent++;
		}
		
		
		for(i=0; i<Math.min(linerate,this.outputBuffer.size()); i++)
			this.link.sendPacket(this.outputBuffer.get(i), this);
		ArrayList<Packet> temp = new ArrayList<Packet>();
		for(i=Math.min(linerate,this.outputBuffer.size()); i<this.outputBuffer.size(); i++)
			temp.add((Packet)this.outputBuffer.get(i));
		this.outputBuffer.clear();
		this.outputBuffer=temp;
	}

	
	/**
	 * Moves packets from this nics input buffer to its output buffer
	 * @since 1.0
	 */
	public void recievePackets(){
		for(int i=0; i<this.inputBuffer.size(); i++)
			this.parent.receivePacket(this.inputBuffer.get(i), this);
		this.inputBuffer.clear();
	}

	public LSR getParent() {
		return parent;
	}

	public void setParent(LSR parent) {
		this.parent = parent;
	}
	
	public Boolean attemptToReserveBandwidth(int PHB,int AFClass,int bwidthToBeAllocated) {
		if(PHB == ConstStrings.PHB_EF) {
			PHBQueues.get(PHB).increaseWeight(bwidthToBeAllocated);
			PHBQueues.get(ConstStrings.BestEffort).decreaseWeight(bwidthToBeAllocated);
			bandwidthLeft -= bwidthToBeAllocated;
		} else if(PHB == ConstStrings.PHB_AF && AFClass == ConstStrings.AF1) {
			PHBQueues.get( ConstStrings.AF1).increaseWeight(bwidthToBeAllocated);
			PHBQueues.get(ConstStrings.BestEffort).decreaseWeight(bwidthToBeAllocated);
			bandwidthLeft -= bwidthToBeAllocated;
		} else if(PHB == ConstStrings.PHB_AF && AFClass == ConstStrings.AF2) {
			PHBQueues.get( ConstStrings.AF2).increaseWeight(bwidthToBeAllocated);
			PHBQueues.get(ConstStrings.BestEffort).decreaseWeight(bwidthToBeAllocated);
			bandwidthLeft -= bwidthToBeAllocated;
		} else if(PHB == ConstStrings.PHB_AF && AFClass == ConstStrings.AF3) {
			PHBQueues.get( ConstStrings.AF3).increaseWeight(bwidthToBeAllocated);
			PHBQueues.get(ConstStrings.BestEffort).decreaseWeight(bwidthToBeAllocated);
			bandwidthLeft -= bwidthToBeAllocated;
		} else if(PHB == ConstStrings.PHB_AF && AFClass == ConstStrings.AF4) {
			PHBQueues.get( ConstStrings.AF4).increaseWeight(bwidthToBeAllocated);
			PHBQueues.get(ConstStrings.BestEffort).decreaseWeight(bwidthToBeAllocated);
			bandwidthLeft -= bwidthToBeAllocated;
		} 
		return true;
	}
}
