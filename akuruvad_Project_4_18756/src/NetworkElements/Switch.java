/**
 * @author Andrew Fox
 */
package NetworkElements;

import java.util.*;

import DataTypes.Packet;

public class Switch implements PacketConsumer {
	ArrayList<NIC> inputNICs = new ArrayList<NIC>(); // NICs from input side
	ArrayList<NIC> outputNICs = new ArrayList<NIC>(); // NICs on output side
	boolean inputQueue = false; // if the switch is input queued or output
								// queued
	boolean outputQueue = true;
	private int maxBufSize = 200;
	public int inputBufferSize = 1;
	public int outputBufferSize = maxBufSize;

	private double throughput = 0.0;

	private int speedup = 4;
	// private int lookupLevel = 6;

	public int startNIC = 0;

	private boolean trace = true;

	public Switch(int numComputers) {
		for (int i = 0; i < numComputers; i++) {
			NIC nic1 = new NIC(this, 0);
			NIC nic2 = new NIC(this, 1);
		}
	}

	/**
	 * Adds a nic to the router side - whether it is on the source or
	 * destination side
	 */
	public void addNIC(NIC nic, int side) {
		if (side == 0) {
			inputNICs.add(nic);
			nic.setBufferSize(this.inputBufferSize);
		}
		if (side == 1) {
			outputNICs.add(nic);
			nic.setBufferSize(this.outputBufferSize);
		}
	}

	// NOT CALLED
	public void addNIC(NIC nic) {
	}

	/**
	 * Returns an array of NICs on source side
	 * 
	 * @return
	 */
	public ArrayList<NIC> getInputNICs() {
		return this.inputNICs;
	}

	/**
	 * Returns an array of NICs on destination side
	 * 
	 * @return
	 */
	public ArrayList<NIC> getOutputNICs() {
		return this.outputNICs;
	}

	/**
	 * Sets the size of the buffers in the NICs on the switch
	 */
	public void setSwitchBufferSize() {
		if (inputQueue && outputQueue) {
			this.inputBufferSize = maxBufSize;
			this.outputBufferSize = maxBufSize;
		} else if (this.inputQueue) {
			this.inputBufferSize = maxBufSize;
			this.outputBufferSize = 1;
		} else if (this.outputQueue) {
			this.inputBufferSize = 1;
			this.outputBufferSize = maxBufSize;
		}
		for (NIC nic : inputNICs) {
			nic.setBufferSize(this.inputBufferSize);
		}
		for (NIC nic : outputNICs) {
			nic.setBufferSize(this.outputBufferSize);
		}
	}

	/**
	 * Sends packets from the queues on the source side of the switch to the
	 * destination side
	 */
	public void sendFromBuffer() {

		this.addDelayToPackets();

		if (inputQueue) {

			ArrayList<Integer> packetsSentToDestination = new ArrayList<Integer>();

			Integer[] speedUpForDest = new Integer[this.outputNICs.size()];
			for (int a = 0; a < this.outputNICs.size(); a++) {
				speedUpForDest[a] = this.speedup;
			}

			for (int ii = this.startNIC; ii < this.inputNICs.size()
					+ this.startNIC; ii++) {
				int i = ii % this.inputNICs.size(); //To ensure that the priority changes every run 
				NIC inputNIC = this.inputNICs.get(i);
				ArrayList<Packet> inputBuffer = inputNIC.getBuffer();
				if (inputBuffer.size() > 0) {
					Packet packet = inputNIC.getPacketAtIndex(0);

					//This will ensure that the speedUp no of packets destined to a particular output are picked up
					if ((speedUpForDest[packet.getDest()] == 0)
							&& packetsSentToDestination.contains(packet
									.getDest())) {
						int destSpeedup = speedUpForDest[packet.getDest()];
						speedUpForDest[packet.getDest()] = destSpeedup - 1;
					}
					
					//This is when the head of the line is blocked.
					if ((speedUpForDest[packet.getDest()] == 0)
							&& packetsSentToDestination.contains(packet
									.getDest())) {
						if (this.trace) {
							System.out.println("Packet Blocked! ");
						}
						packet = inputNIC.getPacketAtIndex(0);
						if (packet == null) {
							continue;
						}
						if (packetsSentToDestination.contains(packet.getDest())) {
							if (this.trace) {
								System.out.println("Packet Blocked! ");
							}
							continue;
						}
					}

					// UnComment to run the lookup code. (Might be broken).
					// Might not work as the code above has speedUp technique implemented.
					/*
					while (j++ < lookupLevel) {
						if (inputNIC.getPacketAtIndex(j - 1) != null) {
							packet = inputNIC.getPacketAtIndex(j - 1);
						}
						if (packetsSentToDestination.contains(packet.getDest())) {
							if (this.trace) {
								System.out.println("Packet Blocked! ");
							}
						} else {
							if (this.trace) {
								System.out.println("Found a packet that can be switched!");
							}
							break;
						}
					}*/

					//If the output buffer is full, we shouldn't switch the packet and thereby blocking it. 
					NIC outputNIC = this.outputNICs.get(packet.getDest());
					if (outputNIC.getBuffer().size() >= this.outputBufferSize) {
						if (this.trace) {
							System.out
									.println("Packet Blocked! Output buffer is full.");
						}
						continue;
					}

					outputNIC.receivePacket(packet);
					packetsSentToDestination.add(packet.getDest());
					inputNIC.packetSwitchedSuccessfullyAtIndex(0);

					if (this.trace) {
						System.out.println("Switching packet to output buffer");
					}
				}
			}

		} else if (outputQueue) {
			for (int ii = this.startNIC; ii < this.inputNICs.size()
					+ this.startNIC; ii++) {
				int i = ii % this.inputNICs.size();
				NIC inputNIC = this.inputNICs.get(i);
				ArrayList<Packet> inputBuffer = inputNIC.getBuffer();
				if (inputBuffer.size() > 0) {
					Packet packet = inputNIC.getPacketAtIndex(0);
					NIC outputNIC = this.outputNICs.get(packet.getDest());
					System.out.println("Packet Aimed at destination :"
							+ packet.getDest());
					int ss = outputNIC.getBuffer().size();
					
					//The only check required here is the output buffer size check.
					// I am assuming n speed up. So, every input buffer has to be visited every toc. 
					if (outputNIC.getBuffer().size() >= this.outputBufferSize) {
						if (this.trace) {
							System.out
									.println("Packet Blocked! Output buffer is full.");
						}
						continue;
					}
					outputNIC.receivePacket(packet);

					if (this.trace) {
						System.out
								.println("Switching packet to output buffer no "
										+ packet.getDest());
					}
				}

			}
		}

		//Calculating throughput every toc.
		this.throughput = this.calcThroughput();
		System.out.println("THROUGHPUT IS " + this.throughput);

		this.startNIC = (int) (++this.startNIC) % this.inputNICs.size();
		System.out.println("Starting from nic " + this.startNIC);

	}

	private double calcThroughput() {
		Integer inputBufferPacketCount = 0;
		Integer outputBufferPacketCount = 0;

		// Checking the no of packets transmitted after every toc and recalculating the throughput.
		// The no of packets count is maintained by the NIC class.
		for (NIC nic : this.inputNICs) {
			inputBufferPacketCount += nic.getPacketCount();
		}
		for (NIC nic : this.outputNICs) {
			outputBufferPacketCount += nic.getPacketCount();
		}

		if (inputBufferPacketCount == 0) {
			return 0;
		}
		System.out.println("O/p and I/p : " + outputBufferPacketCount + " and "
				+ inputBufferPacketCount);
		double throughput = ((double) outputBufferPacketCount / (double) inputBufferPacketCount);
		return throughput;
	}

	/**
	 * Sets if the switch is input queued
	 */
	public void setInputQueue() {
		this.inputQueue = true;
		this.outputQueue = false;
		this.inputBufferSize = maxBufSize;
		this.outputBufferSize = 1;
		this.speedup = 1;
	}

	/**
	 * Sets if the switch is output queued
	 */
	public void setOutputQueue() {
		this.inputQueue = false;
		this.outputQueue = true;
		this.inputBufferSize = 1;
		this.outputBufferSize = maxBufSize;
		this.speedup = 1;
	}

	/**
	 * Sets if the switch is input as well as output queued
	 */
	public void setInputAndOutputQueue() {
		this.inputQueue = true;
		this.outputQueue = true;
		this.inputBufferSize = maxBufSize;
		this.outputBufferSize = this.speedup;
	}

	public void addDelayToPackets() {
		for (NIC ipNIC : this.inputNICs) {
			for (Packet p : ipNIC.getBuffer()) {
				p.addDelay(1);
			}
		}
	}

	/**
	 * Sends packets from the destination side to their final computer
	 * destination
	 */
	public void sendFromOutputs() {
		for (NIC nic : outputNICs) {
			nic.sendFromBuffer();
		}
	}
}
