package DataTypes;

import java.util.*;

public class FIFOQueue {
	private int weight = 1;
	private int bitsRoutedSinceLastPacketSend = 0;
	private ArrayList<IPPacket> packets = new ArrayList<IPPacket>();
	private ArrayList<IPPacket> outputPackets = new ArrayList<IPPacket>();
	private double lastUpdatedFinishTime = 0.0;

	/**
	 * Default constructor for a FIFO Queue
	 */
	public FIFOQueue() {
	}

	/**
	 * Sets the weight of this queue
	 * 
	 * @param weight
	 *            the weight of this queue
	 */
	public void setWeight(int weight) {
		this.weight = weight;
	}

	/**
	 * Gets the weight of this queue
	 * 
	 * @return the weight of this queue
	 */
	public int getWeight() {
		return this.weight;
	}

	/**
	 * Adds one to the number of bits that have moved from this input queue to
	 * the 'output queue'
	 */
	public void routeBit() {
		if (this.packets.size() != 0) {
			this.bitsRoutedSinceLastPacketSend += 1;
			IPPacket frontMostPacket = this.packets.get(0);
			if (frontMostPacket.getSize() == this.bitsRoutedSinceLastPacketSend) {
				IPPacket packetCollected = this.remove();
				this.outputPackets.add(packetCollected);
			}
		}
	}

	public IPPacket getAPacketToForwardIfCollected() {

		IPPacket outputPacket = null;
		if (this.outputPackets.size() > 0) {
			outputPacket = outputPackets.get(0);
			outputPackets.remove(0);
		}
		return outputPacket;
	}

	/**
	 * returns the number of bits that have been routed since a packet was last
	 * removed from the queue
	 * 
	 * @return the number of bits that have been routed since a packet was last
	 *         removed from the queue
	 */
	public int getBitsRoutedSinceLastPacketSent() {
		return this.bitsRoutedSinceLastPacketSend;
	}

	/**
	 * Returns the packet that is second to last in the queue
	 * 
	 * @return the packet that is second to last in the queue
	 */
	public IPPacket secondLastPeek() {
		if (this.packets.size() > 1)
			return this.packets.get(this.packets.size() - 2);
		else if (this.packets.size() == 1)
			return this.packets.get(0);
		else
			return null;
	}

	/**
	 * Increases the time the packet has been delayed
	 */
	public void tock() {
		for (IPPacket packet : packets)
			packet.addDelay(1);
	}

	// http://java.sun.com/j2se/1.5.0/docs/api/java/util/Queue.html
	public IPPacket element() {
		if (this.peek() != null)
			return this.peek();
		else
			throw new NoSuchElementException();
	}

	public boolean offer(IPPacket packet) {
		this.packets.add(packet);
		return true;
	}

	public IPPacket peek() {
		if (this.packets.size() > 0)
			return this.packets.get(0);
		else
			return null;
	}

	public IPPacket remove() {
		if (this.packets.size() > 0) {
			IPPacket ret = this.packets.get(0);
			this.packets.remove(0);
			this.bitsRoutedSinceLastPacketSend = 0;
			return ret;
		} else
			return null;
	}

	/**
	 * Get the finish time of the packet which is entirely in the input queue
	 * and not being transmitted
	 * 
	 * @return packet's finish time
	 */

	public double getNextFinishTime() {

		if (this.packets.size() > 1 && this.bitsRoutedSinceLastPacketSend > 0) {
			return this.packets.get(1).getFinishTime();
		} else if (this.packets.size() > 1
				&& this.bitsRoutedSinceLastPacketSend == 0) {
			return this.packets.get(0).getFinishTime();
		} else if (this.packets.size() == 1) {
			return this.packets.get(0).getFinishTime();
		}

		// So that the return value won't be considered
		return Double.MAX_VALUE;
	}

	/**
	 * Last updated finish time of the packets in the queue. This has to be
	 * maintained in the queue in case all the packets are transmitted.
	 */
	public void setLastUpdatedFinishTime(double time) {
		this.lastUpdatedFinishTime = time;
	}

	public double getLastUpdatedFinishTime() {
		return this.lastUpdatedFinishTime;
	}
}
