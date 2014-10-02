package NetworkElements;

import java.net.Inet4Address;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import DataTypes.FIFOQueue;
import DataTypes.IPPacket;

public class IPRouter implements IPConsumer {
	private ArrayList<IPNIC> nics = new ArrayList<IPNIC>();
	private HashMap<Inet4Address, IPNIC> forwardingTable = new HashMap<Inet4Address, IPNIC>();
	private int time = 0;
	private Boolean fifo = true, rr = false, wrr = false, wfq = false,
			routeEntirePacket = true;
	private HashMap<IPNIC, FIFOQueue> inputQueues = new HashMap<IPNIC, FIFOQueue>();
	private int lastNicServiced = -1, weightFulfilled = 1;
	// remembering the queue rather than the interface number is useful for wfq
	private FIFOQueue lastServicedQueue = null;
	private FIFOQueue mainFIFOQueue = null;
	private FIFOQueue nextMinQueueToService = null;
	private double estimatedTimeToComplete = -1.0;

	/**
	 * The default constructor of a router
	 */
	public IPRouter() {

	}

	/**
	 * adds a forwarding address in the forwarding table
	 * 
	 * @param destAddress
	 *            the address of the destination
	 * @param nic
	 *            the nic the packet should be sent on if the destination
	 *            address matches
	 */
	public void addForwardingAddress(Inet4Address destAddress, IPNIC nic) {
		forwardingTable.put(destAddress, nic);
	}

	/**
	 * receives a packet from the NIC
	 * 
	 * @param packet
	 *            the packet received
	 * @param nic
	 *            the nic the packet was received on
	 */
	public void receivePacket(IPPacket packet, IPNIC nic) {

		if (this.fifo) {
			// enqueue
			this.mainFIFOQueue.offer(packet);
		} else if (!this.wfq) {
			this.inputQueues.get(nic).offer(packet);
		}

		// If wfq set the expected finish time
		if (this.wfq) {

			FIFOQueue currQueue = this.inputQueues.get(nic);

			// For the first time
			if (this.nextMinQueueToService == null) {
				this.nextMinQueueToService = currQueue;
			}

			/* Calculating using the formula -
			 * F(i,k,t) = max(R(t), F(i-1,k,t)+T(i,k))
			 */

			// R(t)
			// Line Speed = 1 bit per second
			double timeSliceOfQueue = (double) (1 / (double) currQueue
					.getWeight());
			double totalTimeSlicesNeededForThisPacket = (double) (packet
					.getSize() * timeSliceOfQueue);

			// System.out.println("timeSliceOfQueue : "+(double)timeSliceOfQueue);

			// To calculate F(i-1,k,t) -> secondLastPacketFinishTime
			// i-1 -> packetBehindThisPacket
			IPPacket packetBehindThisPacket = currQueue.secondLastPeek();
			double secondLastPacketFinishTime = 0.0;
			if (packetBehindThisPacket == null) {
				secondLastPacketFinishTime = currQueue
						.getLastUpdatedFinishTime();
			} else {
				secondLastPacketFinishTime = packetBehindThisPacket
						.getFinishTime();
			}

			//max(R(t), F(i-1,k,t)+T(i,k))
			double packetFinishTime = 0.0;
			packetFinishTime = Math.max(this.estimatedTimeToComplete,
					secondLastPacketFinishTime)
					+ totalTimeSlicesNeededForThisPacket;

			packet.setFinishTime(packetFinishTime);
			
			//It is necessary for the queue to maintain the state of its latest packet finish time.
			this.inputQueues.get(nic)
					.setLastUpdatedFinishTime(packetFinishTime);
			this.inputQueues.get(nic).offer(packet);

			System.out.println("est fin: " + packetFinishTime);

			// Update nextMinQueueToService
			this.updateNextQueueForWFQ();
		}
	}

	public void forwardPacket(IPPacket packet) {
		forwardingTable.get(packet.getDest()).sendIPPacket(packet);
	}

	public void routeBit() {
		/*
		 * FIFO scheduler
		 */
		if (this.fifo) {
			this.fifo();
		}

		/*
		 * RR scheduler
		 */
		if (this.rr)
			this.rr();

		/*
		 * WRR scheduler
		 */
		if (this.wrr)
			this.wrr();

		/*
		 * WFQ scheduler
		 */
		if (this.wfq)
			this.wfq();
	}

	/**
	 * Perform FIFO scheduler on the queue
	 */
	private void fifo() {

		this.mainFIFOQueue.tock();
		this.mainFIFOQueue.routeBit();

		IPPacket packetToForward;
		if ((packetToForward = this.mainFIFOQueue
				.getAPacketToForwardIfCollected()) != null) {
			this.forwardPacket(packetToForward);
		}
	}

	/**
	 * Perform round robin on the queue
	 */
	private void rr() {

		if (this.routeEntirePacket) {

			/*
			 * PACKET-BY-PACKET ROUND ROBIN SCHEDULING
			 */

			// First time, begin with the nic at index 0
			if (this.lastNicServiced == -1) {
				this.lastNicServiced = 0;
			}

			// Search for the next FIFOqueue that has at-least one packet in its
			// input queue
			if (this.lastServicedQueue == null) {
				int count = 0;
				while (count++ < nics.size()) {
					IPNIC currentNIC = this.nics.get(this.lastNicServiced);
					FIFOQueue tempQ = this.inputQueues.get(currentNIC);

					if (tempQ.peek() != null) {

						/*
						 * Found the queue. This queue will be routed bit by bit
						 * until the entire packet in the front of the queue is
						 * transferred to the output queue and then forwarded to
						 * the destination.
						 */

						this.lastServicedQueue = tempQ;
						this.lastNicServiced = this.nics.indexOf(currentNIC);
						break;

					} else {
						// Move on to the next NIC.
						this.lastNicServiced++;
						// Round Robin -> Move back to the first element after
						// finishing the last
						if (this.lastNicServiced >= this.nics.size()) {
							this.lastNicServiced = 0;
						}
					}
				}
			}

			// Continue servicing the queue until the packet is forwarded
			if (this.lastServicedQueue != null) {
				this.lastServicedQueue.routeBit();
				IPPacket packetToForward;

				// Checking if the entire packet has been collected in the
				// output queue
				if ((packetToForward = this.lastServicedQueue
						.getAPacketToForwardIfCollected()) != null) {

					/*
					 * Forwarding the packet and resetting lastServicedQueue to
					 * null, which has to be identified again for the next
					 * round..
					 */
					this.forwardPacket(packetToForward);
					this.lastServicedQueue = null;
					this.lastNicServiced++;
					// Round Robin -> Move back to the first element after
					// finishing the last
					if (this.lastNicServiced >= this.nics.size()) {
						this.lastNicServiced = 0;
					}
				}

			} else {
				// There are no queues with packets to be routed.
			}

		} else {

			/*
			 * BIT-BY-BIT ROUND ROBIN SCHEDULING
			 */

			// First time, begin with the nic at index 0
			if (this.lastNicServiced == -1) {
				this.lastNicServiced = 0;
			}

			FIFOQueue nextQueueToRouteBit = null;
			int count = 0;

			while (count++ != this.nics.size()) {

				// Round Robin -> Move back to the first element after finishing
				// the last
				if (this.lastNicServiced == this.nics.size()) {
					this.lastNicServiced = 0;
				}

				IPNIC currentNIC = nics.get(this.lastNicServiced);
				FIFOQueue tempQ = this.inputQueues.get(currentNIC);

				if (tempQ.peek() != null) {
					// Found the next NIC with bits that can be forwarded.
					tempQ.routeBit();
					nextQueueToRouteBit = tempQ;
					break;
				} else {
					// Move to the next NIC. The NIC under consideration has no
					// bits to forward and lost its chance
					this.lastNicServiced++;
				}
			}

			IPPacket packetToBeForwarded;
			if (nextQueueToRouteBit != null
					&& (packetToBeForwarded = nextQueueToRouteBit
							.getAPacketToForwardIfCollected()) != null) {

				this.forwardPacket(packetToBeForwarded);
			}
			this.lastNicServiced++;
			if (this.lastNicServiced >= this.nics.size()) {
				this.lastNicServiced = 0;
			}
		}
	}

	/**
	 * Perform weighted round robin on the queue
	 */
	private void wrr() {

		if (this.routeEntirePacket) {
			/*
			 * PACKET-BY-PACKET WEIGHTED ROUND ROBIN SCHEDULING
			 */
			// First time, begin with the nic at index 0
			if (this.lastNicServiced == -1) {
				this.lastNicServiced = 0;
				this.lastServicedQueue = this.inputQueues.get(this.nics.get(0));
				this.weightFulfilled = this.lastServicedQueue.getWeight();
			}

			if (this.lastServicedQueue == null) {
				this.lastServicedQueue = this.getNextNonemptyQueue();
				this.weightFulfilled = this.lastServicedQueue.getWeight();
			}

			this.lastServicedQueue.routeBit();
			this.weightFulfilled--;

			IPPacket packetToBeForwarded;
			if (this.lastServicedQueue != null
					&& (packetToBeForwarded = this.lastServicedQueue
							.getAPacketToForwardIfCollected()) != null) {

				this.forwardPacket(packetToBeForwarded);
				this.lastServicedQueue = null;
			}

		} else {
			/*
			 * BIT-BY-BIT WEIGHTED ROUND ROBIN SCHEDULING
			 */

			// First time, begin with the nic at index 0
			if (this.lastNicServiced == -1) {
				this.lastNicServiced = 0;
				this.weightFulfilled = this.inputQueues.get(this.nics.get(0))
						.getWeight();
			}

			FIFOQueue nextQueueToRouteBit = this.inputQueues.get(nics
					.get(this.lastNicServiced));
			if (this.weightFulfilled == 0) {
				nextQueueToRouteBit = this.getNextNonemptyQueue();
				this.weightFulfilled = nextQueueToRouteBit.getWeight();
			}

			nextQueueToRouteBit.routeBit();
			this.weightFulfilled--;

			IPPacket packetToBeForwarded;
			if (nextQueueToRouteBit != null
					&& (packetToBeForwarded = nextQueueToRouteBit
							.getAPacketToForwardIfCollected()) != null) {

				this.forwardPacket(packetToBeForwarded);
			}
		}
	}
	
	/**
	 * Perform weighted fair queuing on the queue
	 */
	private void wfq() {

		//Initializing for the first time
		if (this.lastServicedQueue == null) {
			this.lastServicedQueue = this.nextMinQueueToService;
		}
		this.updateEstimatedTimeToCompleteForWFQ();

		IPPacket packetToBeForwarded;
		//Checking if an entire packet has been routed adn then forward the packet.
		if ((packetToBeForwarded = this.lastServicedQueue
				.getAPacketToForwardIfCollected()) != null) {
			
			this.forwardPacket(packetToBeForwarded);
			
			//Choose the next queue with the best score to route
			this.updateNextQueueForWFQ();
			this.lastServicedQueue = this.nextMinQueueToService;
		}

		this.lastServicedQueue.routeBit();
	}
	
	/**
	 * Update the queue that should be serviced next for wfq
	 */
	public void updateNextQueueForWFQ() {

		for (FIFOQueue queue : this.inputQueues.values()) {
			if ((queue.getNextFinishTime() < this.nextMinQueueToService
					.getNextFinishTime())) {
				this.nextMinQueueToService = queue;
			}
		}
	}
	
	/**
	 * update R(t)
	 */
	public void updateEstimatedTimeToCompleteForWFQ() {
		double sumOfWeights = 0;
		for (FIFOQueue queue : this.inputQueues.values()) {
			if (queue.peek() != null || queue.getBitsRoutedSinceLastPacketSent()>0) {
//				System.out.println(" queue and weight "+queue+"  "+queue.getWeight());
				sumOfWeights += queue.getWeight();
			}
		}
//		System.out.println("sumOfWeights : "+sumOfWeights+ " at time: "+this.time+" an - "+ (this.time / sumOfWeights));
		this.estimatedTimeToComplete = (this.time / sumOfWeights);
	}
	

	/**
	 * adds a nic to the consumer
	 * 
	 * @param nic
	 *            the nic to be added
	 */
	public void addNIC(IPNIC nic) {
		this.nics.add(nic);
	}

	/**
	 * sets the weight of queues, used when a weighted algorithm is used.
	 * Example Nic A = 1 Nic B = 4
	 * 
	 * For every 5 bits of service, A would get one, B would get 4.
	 * 
	 * @param nic
	 *            the nic queue to set the weight of
	 * @param weight
	 *            the weight of the queue
	 */
	public void setQueueWeight(IPNIC nic, int weight) {
		if (this.inputQueues.containsKey(nic))
			this.inputQueues.get(nic).setWeight(weight);

		else
			System.err
					.println("(IPRouter) Error: The given NIC does not have a queue associated with it");
	}

	/**
	 * moves time forward 1 millisecond
	 */
	public void tock() {
		this.time += 1;

		// Add 1 delay to all packets in queues
		ArrayList<FIFOQueue> delayedQueues = new ArrayList<FIFOQueue>();
		for (Iterator<FIFOQueue> queues = this.inputQueues.values().iterator(); queues
				.hasNext();) {
			FIFOQueue queue = queues.next();
			if (!delayedQueues.contains(queue)) {
				delayedQueues.add(queue);
				queue.tock();
			}
		}

		// calculate the new virtual time for the next round
		if (this.wfq) {
//			this.updateEstimatedTimeToCompleteForWFQ();
		}

		// route bit for this round
		this.routeBit();
	}

	/**
	 * get the next queue that should be serviced
	 */
	private FIFOQueue getNextNonemptyQueue() {
		FIFOQueue nextNonemptyQueue = null;
		this.lastNicServiced = (this.lastNicServiced + 1) % this.nics.size();
		int count = 0;
		while (count++ != this.nics.size()) {

			IPNIC currentNIC = nics.get(this.lastNicServiced);
			FIFOQueue tempQ = this.inputQueues.get(currentNIC);

			if (tempQ.peek() != null) {
				// Found the next NIC with bits that can be forwarded.
				break;
			} else {
				// Move to the next NIC. The NIC under consideration has no
				// bits to forward and lost its chance
				this.lastNicServiced = (this.lastNicServiced + 1)
						% this.nics.size();
				nextNonemptyQueue = this.inputQueues.get(this.nics
						.get(this.lastNicServiced));
			}
		}

		nextNonemptyQueue = this.inputQueues.get(this.nics
				.get(this.lastNicServiced));

		return nextNonemptyQueue;
	}

	/**
	 * set the router to use FIFO service
	 */
	public void setIsFIFO() {
		this.fifo = true;
		this.rr = false;
		this.wrr = false;
		this.wfq = false;

		// Setting up on FIFOQueue for all the computers to dump their packets
		// in the queue
		this.mainFIFOQueue = new FIFOQueue();
	}

	/**
	 * set the router to use Round Robin service
	 */
	public void setIsRoundRobin() {
		this.fifo = false;
		this.rr = true;
		this.wrr = false;
		this.wfq = false;

		// Setting up the queues for all the connected computers
		for (IPNIC nic : nics) {
			FIFOQueue nicQueue = new FIFOQueue();
			this.inputQueues.put(nic, nicQueue);
		}
	}

	/**
	 * sets the router to use weighted round robin service
	 */
	public void setIsWeightedRoundRobin() {
		this.fifo = false;
		this.rr = false;
		this.wrr = true;
		this.wfq = false;

		// Setting up the queues for all the connected computers
		for (IPNIC nic : nics) {
			FIFOQueue nicQueue = new FIFOQueue();
			this.inputQueues.put(nic, nicQueue);
		}
	}

	/**
	 * sets the router to use weighted fair queuing
	 */
	public void setIsWeightedFairQueuing() {
		this.fifo = false;
		this.rr = false;
		this.wrr = false;
		this.wfq = true;

		// Setting up the queues for all the connected computers
		for (IPNIC nic : nics) {
			FIFOQueue nicQueue = new FIFOQueue();
			this.inputQueues.put(nic, nicQueue);
		}

		// Allocating The next queue with packet with least transfer time.
		// this.nextMinQueueToService = new FIFOQueue();
	}

	/**
	 * sets if the router should route bit-by-bit, or entire packets at a time
	 * 
	 * @param routeEntirePacket
	 *            if the entire packet should be routed
	 */
	public void setRouteEntirePacket(Boolean routeEntirePacket) {
		this.routeEntirePacket = routeEntirePacket;
	}
}
