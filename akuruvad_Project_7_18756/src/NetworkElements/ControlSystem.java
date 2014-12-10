package NetworkElements;

import java.util.ArrayList;
import java.util.HashMap;

public class ControlSystem {
	
	private static ArrayList<OtoOLink> links = new ArrayList<OtoOLink>();
	private static HashMap<String, Integer> opticalLinksMap = new HashMap<String, Integer>();
	
	public static void anOpticalLinkSetup(OtoOLink opticalLink) {
		
		links.add(opticalLink);
		
		LSRNIC aNIC = opticalLink.getNIC1();
		LSRNIC bNIC = opticalLink.getNIC2();
		
		LSR a = aNIC.getParent();
		LSR b = bNIC.getParent();
		
		Integer aAddress = a.getAddress();
		Integer bAddress = b.getAddress();

		String link = aAddress.toString()+"&"+bAddress.toString();
		opticalLinksMap.put(link, 1);
		link = bAddress.toString()+"&"+aAddress.toString();
		opticalLinksMap.put(link, 1);
		
		LSRNIC ctrlNICForA = new LSRNIC(a);
		LSRNIC ctrlNICForB = new LSRNIC(b);
		
		OtoOLink ctrlLink = new OtoOLink(ctrlNICForA, ctrlNICForB);
	}
	
	public static boolean doesAnOpticalLinkExistsBetween(Integer address1, Integer address2) {
		String link = address1.toString()+"&"+address2.toString();
		if(opticalLinksMap.containsKey(link)) {
			return true;
		} else {
			return false;
		}
	}
	
	public static void packetLinkSetup(OtoOLink packetLink) {
		links.add(packetLink);
	}
	
	public static void print() {
		System.out.println("Optical links : "+ links.toString());
		System.out.println("Optical links HashMap: "+ opticalLinksMap.toString());
	}
	

}
