package DataTypes;
/**
 * A weighted FIFO queue.  Since it has a weight attached to it, this can be used
 * very easily in a weighted round robin scheduling scheme.
 * @author Brady Tello
 */

import java.util.ArrayList;

public class FIFOQueue{
	private int weight;
	private ArrayList<Packet> packets = new ArrayList<Packet>();
	
	/**
	 * Default constructor for a FIFO Queue
	 */
	public FIFOQueue(int weight){
		this.weight = weight;
	}
	
	/**
	 * Sets the weight of this queue
	 * @param weight the weight of this queue
	 */
	public void setWeight(int weight){
		this.weight = weight;
	}
	
	/**
	 * Gets the weight of this queue
	 * @return the weight of this queue
	 */
	public int getWeight(){
		return this.weight;
	}
	
	/**
	 * Returns the number of packets in this queue.
	 * @return the number of packets currently in the queue
	 */
	public int getNumPackets(){
		return packets.size();
	}
	
	/**
	 * insert a new packet at the end of the queue.
	 * @param p
	 */
	public void insert(Packet p){
		this.packets.add(p);
	}
	
	/**
	 * removes the packet at the head of the queue (if there is one)
	 * @return the packet from the head of the queue if there is one.
	 */
	public Packet remove(){
		if(this.packets.size()>0){
			Packet ret = this.packets.get(0);
			this.packets.remove(0);
			return ret;
		}
		else return null;
	}
	

	public void increaseWeight(int inc){
		weight += inc;
	}
	

	public void decreaseWeight(int dec){
		weight -= dec;
	}
}
