import NetworkElements.*;

import java.util.*;

public class ProjectSetup {
	// This object will be used to move time forward on all objects
	private int time = 0;
	private ArrayList<LSR> allConsumers = new ArrayList<LSR>();
	/**
	 * Create a network and creates connections
	 * @since 1.0
	 */
	public void go(){
		System.out.println("** SYSTEM SETUP **");
		
		LSR lsrA = new LSR(1);
		LSR lsrB = new LSR(2);
		LSR lsrC = new LSR(3);
		LSR lsrD = new LSR(4);
		LSR lsrE = new LSR(5);
		LSR lsrF = new LSR(6);
		LSR lsrG = new LSR(7);

		LSRNIC A1n1 = new LSRNIC(lsrA);
		LSRNIC B1n1 = new LSRNIC(lsrB);
		LSRNIC B1n2 = new LSRNIC(lsrB);
		LSRNIC C1n1 = new LSRNIC(lsrC);
		LSRNIC C1n2 = new LSRNIC(lsrC);
		LSRNIC D1n1 = new LSRNIC(lsrD);
		LSRNIC D1n2 = new LSRNIC(lsrD);
		LSRNIC E1n1 = new LSRNIC(lsrE);
		LSRNIC E1n2 = new LSRNIC(lsrE);
		LSRNIC F1n1 = new LSRNIC(lsrF);
		LSRNIC F1n2 = new LSRNIC(lsrF);
		LSRNIC G1n1 = new LSRNIC(lsrG);
		LSRNIC G1n2 = new LSRNIC(lsrG);
		
		// physically connect the router's nics
		OtoOLink l1 = new OtoOLink(A1n1, B1n1);
		OtoOLink l2opt = new OtoOLink(B1n2, C1n1);
		OtoOLink l3opt = new OtoOLink(C1n2, D1n1); // optical link
		OtoOLink l4opt = new OtoOLink(D1n2, E1n1);
		OtoOLink l5opt = new OtoOLink(E1n2, F1n1); // optical link
		OtoOLink l6 = new OtoOLink(F1n2, G1n1);
		
		// Add the objects that need to move in time to an array
		this.allConsumers.add(lsrA);
		this.allConsumers.add(lsrB);
		this.allConsumers.add(lsrC);
		this.allConsumers.add(lsrD);
		this.allConsumers.add(lsrE);
		this.allConsumers.add(lsrF);
		this.allConsumers.add(lsrG);

		ControlSystem.print();
		
		//send packets from router 1 to the other routers...
		lsrA.createPacket(7);
		
		for(int i = 0; i< 20; i++) {
			tock();
		}
		lsrG.createPacket(1);
		for(int i = 0; i< 20; i++) {
			tock();
		}	
	}
	
	public void tock(){
		System.out.println("** TIME = " + time + " **");
		time++;		
		
		// Send packets between routers
		for(int i=0; i<this.allConsumers.size(); i++)
			allConsumers.get(i).sendPackets();

		// Move packets from input buffers to output buffers
		for(int i=0; i<this.allConsumers.size(); i++)
			allConsumers.get(i).receivePackets();
		
	}
	public static void main(String args[]){
		ProjectSetup go = new ProjectSetup();
		go.go();
	}
}