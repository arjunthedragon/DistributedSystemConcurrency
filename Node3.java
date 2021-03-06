import java.lang.*;
import java.util.*;
import java.io.*;

public class Node3 extends BaseNode {

	private static int myServerPort = 5002;
	private	static Node3 clientNodes[] = new Node3[5];

	public Node3(String clientAddress, int clientPort, String nodeName) {
		super(clientAddress, clientPort, nodeName);
	}

	public Node3(int serverPort, String nodeName) {
		super(serverPort, nodeName);
	}

	public static Node3 getClientNode(int clientPort) {
		// We try to obtain the client Port to communicate to this node whose server port value matches with this client port ...

		for(int i = 0; i < BaseNode.MAX_NUMBER_OF_NODES_ALLOWED; i++) {
			if(clientNodes[i] != null ) {
				if(clientNodes[i].getClientPort() == clientPort ) {
					return clientNodes[i];
				}
			}
		}

		return null;
	}

	public static void broadCastMessage(int priority) {
			for(int i = 0; i < BaseNode.MAX_NUMBER_OF_NODES_ALLOWED; i++) {
				if(clientNodes[i] != null) {
					clientNodes[i].requestingCriticalSection(clientNodes[i].getNodeName(), priority);
				}
			}

	}
	
	public static void main(String datas[]) throws Exception {
		
		Node3 serverNode = new Node3(myServerPort, "Node3");
		serverNode.baseNodeServerInterface = new BaseNodeServerInterface(){
			public void onReceiveCriticalSectionRequest(boolean isConditionSatisfied, int replyBackPort) {
				
				if(isConditionSatisfied == true) {
					// Now I will retrieve the clientNode which supposed to be running on this replyBackPort ...
					// i.e clientPort which is equivalent to this replyBack Port ...

					Node3 clientNode = Node3.getClientNode(replyBackPort);
					System.out.print("\n\n Now I am going to reply to port "+replyBackPort);
					clientNode.sendReply(clientNode.getNodeName());
				}
			}

			public void onLeavingCriticalSection(boolean isConditionSatisfied, ArrayList<String> deferredNodes) {
		
				if(isConditionSatisfied == true) {
					for(String deferredNode : deferredNodes) {
						int replyBackPort = getPortNumber(deferredNode);
				
						Node3 clientNode = Node3.getClientNode(replyBackPort);
						clientNode.sendReply(clientNode.getNodeName());

					}
				}
			}
		};

		Thread t1 = new Thread(serverNode);
		t1.start();

		BaseNode.registerNodeName("Node3", myServerPort);

		System.out.print("\n\n Type something if all servers are ready ");
		String continueMessage = readString();

		for(int i = 0; i < BaseNode.MAX_NUMBER_OF_NODES_ALLOWED; i++) {
			int clientPort = 5000 + i;
			
			if(clientPort != myServerPort) {
				clientNodes[i] = new Node3("127.0.0.1", clientPort, "Node3");
				clientNodes[i].setReceiveReplyPortNo(myServerPort);
			}
		}

		while(true) {
			String message = readString();

			if(message == null) {
				System.out.print("\n\n Message read is null \n\n\n");
			}

			System.out.print("\n\n Message to be send: "+message+"\n\n");
		
			if(message.equals(BaseNode.MESSAGE_TYPE_REQUEST_CRITICAL_SECTION)) {
				int priority = readNumber();
				broadCastMessage(priority);
			}
		}

	}
}
