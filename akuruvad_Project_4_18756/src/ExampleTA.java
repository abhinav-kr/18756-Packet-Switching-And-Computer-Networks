/**
 * @author Andrew Fox
 */

import java.util.ArrayList;
import java.util.Random;

import NetworkElements.Computer;
import NetworkElements.NIC;
import NetworkElements.OtoOLink;
import NetworkElements.Switch;

public class ExampleTA {
	// This object will be used to move time forward on all objects
	private ArrayList<Computer> allSourceConsumers = new ArrayList<Computer>();
	private ArrayList<Computer> allDestinationConsumers = new ArrayList<Computer>();
	private int time = 0;
	Switch s;

	/**
	 * Create a network and creates connections
	 * @since 1.0
	 */
	public void go(){
		System.out.println("** SYSTEM SETUP **");

		int numComputers=128;

		// Create Source Computers
		for (int i=0;i<numComputers;i++){
			Computer c=new Computer(i);
			NIC nic = new NIC(c);
			allSourceConsumers.add(c);
		}

		// Create Destination Computers
		for (int i=0;i<numComputers;i++){
			Computer c=new Computer(i);
			NIC nic = new NIC(c);
			allDestinationConsumers.add(c);
		}

		// Create the Switch
		s = new Switch(numComputers);
		s.setInputQueue();
//		s.setOutputQueue();
//		s.setInputAndOutputQueue();

		// connect the computers to the links
		int j=0;
		for(Computer c:allSourceConsumers){
			OtoOLink l=new OtoOLink(c.getNIC(),s.getInputNICs().get(j));
			j++;
		}
		j=0;
		for(Computer c:allDestinationConsumers){
			OtoOLink l=new OtoOLink(c.getNIC(),s.getOutputNICs().get(j));
			j++;
		}

		// Send packets
//		allSourceConsumers.get(0).sendPacket(1);		
//		allSourceConsumers.get(2).sendPacket(3);

		for(int i=0; i<3000; i++)
			this.tock();

	}

	
	/**
	 * moves time forward in all of the networks objects, so that packets take some amount of time to
	 * travel from once place to another
	 */
	
	
	public void tock(){
		System.out.println("** TIME = " + time + " **");
		time++;
		
		// Randomly generatng the destination computers for the input computers.
		// Ensuring that every input computer generates a packet every toc.
		Random rand = new Random();
		for(int i=0; i<this.allSourceConsumers.size(); i++) {
			int randomDestination = rand.nextInt(this.allDestinationConsumers.size());
			allSourceConsumers.get(i).sendPacket(randomDestination);
		}


		// send packets from all input computers to the switch
		for(int i=0; i<this.allSourceConsumers.size(); i++)
			allSourceConsumers.get(i).sendFromBuffer();
		s.sendFromOutputs();		// send packets from the output of the switch to destination computer
		s.sendFromBuffer();			// send packets across the switch
		// clears the buffers of the destination routers
		for(int i=0; i<this.allDestinationConsumers.size(); i++)
			allDestinationConsumers.get(i).clearBuffer();
		
		System.out.println("Max delay :"+ Computer.maxdelay);
	}

	public static void main(String args[]){
		ExampleTA go = new ExampleTA();
		go.go();
	}
}
