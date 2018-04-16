import java.util.*;
import java.lang.*;
import java.io.*;
import java.net.*;
import java.sql.*;

public class BaseNode implements Runnable {
	
	public BaseNodeServerInterface baseNodeServerInterface;
	private String mNodeName;

	private static NodeStatus nodeStatus = NodeStatus.NodeStatusIdle;
	private long mCurrentRequestTimeStamp;

	public static ArrayList<String> allNodes = new ArrayList<String>(Arrays.asList("Node1", "Node2", "Node3", "Node4", "Node5"));
	private ArrayList<String> mRepliedNodes = new ArrayList<String>();
	private ArrayList<String> mDeferredNodes = new ArrayList<String>();

	public static RequestPriority requestPriority = RequestPriority.RequestPriorityVeryLow;

	private static HashMap<Integer, String> portNumberNodeNameMap = new HashMap<Integer, String>(){{
		put(new Integer(5000), "Node1");
		put(new Integer(5001), "Node2");
		put(new Integer(5002), "Node3");
		put(new Integer(5003), "Node4");
		put(new Integer(5004), "Node5");
	}};

	private static HashMap<String, Integer> nodeNamePortNumberMap = new HashMap<String, Integer>(){{
		put("Node1", new Integer(5000));
		put("Node2", new Integer(5001));
		put("Node3", new Integer(5002));
		put("Node4", new Integer(5003));
		put("Node5", new Integer(5004));
	}};

	// Client parameters ...
	private String mClientAddress;
	private int mClientPort;
	private Socket mClientSocket;
	private DataOutputStream mOutputStream;
	private String nodeMessage;

	// Server parameters ...
	private int mServerPort;
	private Socket mServerSocket;
	private ServerSocket mServerServerSocket;
	private DataInputStream mInputStream;

	// same as server port ...
	// But we are implementing it separately for readability ...
	private int mReceiveReplyPortNo;

	public static final int THREAD_FUNCTIONALITY_SENDER = 0;
	public static final int THREAD_FUNCTIONALITY_RECEIVER_MASTER = 1;
	public static final int THREAD_FUNCTIONALITY_RECEIVER_SLAVE = 2;

	public static final String MESSAGE_TYPE_REQUEST_CRITICAL_SECTION = "critical";
	public static final String MESSAGE_TYPE_REPLY= "reply";

	public static final int MAX_NUMBER_OF_NODES_ALLOWED = 5;

	public Integer mThreadFunctionality;

	protected static String readString() {
		Scanner s = new Scanner(System.in);
		return s.nextLine();
	}

	protected static int readNumber() {
		String numberString = readString();
		return Integer.parseInt(numberString);
	}

	public String getClientAddress() {
		return mClientAddress;
	}

	public void setClientAddress(String clientAddress) {
		mClientAddress = clientAddress;
	}

	public int getClientPort(){
		return mClientPort;
	}
	
	public void setClientPort(int clientPort) {
		mClientPort = clientPort;
	}

	public Socket getClientSocket(){
		return mClientSocket;
	}
	
	public void setClientSocket(Socket clientSocket) {
		mClientSocket = clientSocket;
	}

	public Socket getServerSocket(){
		return mServerSocket;
	}
	
	public void setServerSocket(Socket serverSocket) {
		mServerSocket = serverSocket;
	}

	public ServerSocket getServerServerSocket(){
		return mServerServerSocket;
	}
	
	public void setServerServerSocket(ServerSocket serverServerSocket) {
		mServerServerSocket = serverServerSocket;
	}

	public int getServerPort(){
		return mServerPort;
	}
	
	public void setServerPort(int serverPort) {
		mServerPort = serverPort;
	}

	public int getReceiveReplyPortNo() {
		return mReceiveReplyPortNo;
	}

	public void setReceiveReplyPortNo(int receiveReplyPortNo) {
		mReceiveReplyPortNo = receiveReplyPortNo;
	}

	public void setMessage(String message) {
		nodeMessage = message;
	}

	public void setNodeName(String nodeName) {
		mNodeName = nodeName;
	}

	public String getNodeName() {
		return mNodeName;
	}

	public void setNodeStatus(NodeStatus nodeStatus) {
		nodeStatus = nodeStatus;
	}

	public void setRepliedNodes(ArrayList<String> repliedNodes) {
		mRepliedNodes = repliedNodes;
	}

	public void setDeferredNodes(ArrayList<String> deferredNodes) {
		mDeferredNodes = deferredNodes;
	}

	public static String getNodeName(int serverPort) {
		Integer serverPortInteger = serverPort;
		return portNumberNodeNameMap.get(serverPortInteger);
	}

	public static int getPortNumber(String nodeName) {
		return nodeNamePortNumberMap.get(nodeName).intValue();
	}

	public void setRequestPriority(RequestPriority priority) {
		requestPriority = priority;
	}

	public static void registerNodeName(String nodeName, int serverPort) {
		Integer serverPortInteger = serverPort;
		portNumberNodeNameMap.put(serverPortInteger, nodeName);
		nodeNamePortNumberMap.put(nodeName, serverPortInteger);
	}

	public boolean areAllNodesReplied() {
		ArrayList<String> allNodes = BaseNode.allNodes;
		
		if(mRepliedNodes != null) {
			return mRepliedNodes.containsAll(allNodes);
		}

		return false; 
	}

	public boolean isClientInitiated() {
		boolean status = true;

		if( mClientSocket == null || mOutputStream == null ) {
			status = false;
		}

		return status;
	}

	public boolean initClient() {
		boolean initStatus = true;

		try {
			mClientSocket = new Socket(mClientAddress, mClientPort);
			System.out.print("\n\n Connected ip "+mClientAddress+" and port "+mClientPort+"\n");

			mOutputStream = new DataOutputStream(mClientSocket.getOutputStream());
		}catch (Exception e) {
			System.out.print("\n\n Exception: "+e);
			mClientSocket = null;
			mOutputStream = null;

			initStatus = false;
		}

		return initStatus;
	}

	public void clientSendData(String content) {

		try {

				mOutputStream.writeUTF(content);
				mOutputStream.flush();

				if(content.equals("Over")) {
					deallocClient();
				}
		} catch (Exception i) {
			i.printStackTrace();
		}
	}

	public void deallocClient() {
		try {
			mOutputStream.close();
			mClientSocket.close();
		} catch(Exception e) {
			System.out.print("\n\n Dealloc Exception: "+e);
		}
	}

	public void initiateServer() {
			
			try {
				if(mServerServerSocket == null) {
					mServerServerSocket = new ServerSocket(mServerPort);
					mRepliedNodes.add(mNodeName);
				}
				System.out.print("\n\n Server listening on port "+mServerPort+" \n");
			} catch(Exception e) {
				e.printStackTrace();
			}
	}

	public void activateServer() {
		try {
			
			System.out.print("\n\n Waiting for a client \n\n");
			while(true) {
				try {
					mServerSocket = mServerServerSocket.accept();
				} catch(Exception e) {
					e.printStackTrace();
				}

				// We set those values which are to be shared by the slave nodes ...
				BaseNode slaveNode = new BaseNode(mServerServerSocket, mServerSocket, mNodeName);
				slaveNode.baseNodeServerInterface = baseNodeServerInterface;
				slaveNode.setDeferredNodes(mDeferredNodes);
				slaveNode.setRepliedNodes(mRepliedNodes);
				slaveNode.setNodeStatus(nodeStatus);
				slaveNode.setRequestPriority(requestPriority);

				System.out.print("\n\n Node status when master received the request "+nodeStatus+"\n");

				Thread t = new Thread(slaveNode);
				t.start();
			}

		} catch(Exception e) {
			System.out.print("\n\n Exception here :"+e);
		} finally {
			mServerServerSocket = null;
		}
	}

	public void readContent() {
		try {
		
			System.out.print("\n\n Accepting client \n\n");
			mInputStream = new DataInputStream(new BufferedInputStream(mServerSocket.getInputStream()));
			
			String receivedContent = "";
			while(!receivedContent.equals("Over")){
				try {

						receivedContent = mInputStream.readUTF();

						if(receivedContent == null) {
							receivedContent = "Nothing received";
						}
						System.out.print("\n\n Received Content: "+receivedContent+"\n");
						handleReceivedContent(receivedContent);
				} catch(Exception e) {
					e.printStackTrace();
				}
			}
			

			System.out.print("\n\n Closing Connection \n");
			mServerSocket.close();
			mInputStream.close();

			mServerSocket = null;
			mInputStream = null;
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	public void handleReceivedContent(String receivedContent) {
		String[] details = receivedContent.split(":");
		String command = details[0];
		String nodeName = details[1];
		
		if(command.equals(BaseNode.MESSAGE_TYPE_REQUEST_CRITICAL_SECTION)) {
			if(nodeStatus != NodeStatus.NodeStatusAccessingCriticalSection) {
				int replyBackPort = Integer.parseInt(details[2]);
				long incomingTimeStamp = Long.parseLong(details[3]);
				int incomingPriority = Integer.parseInt(details[4]);
				
				if(nodeStatus != NodeStatus.NodeStatusRequestCriticalSection) {
					System.out.print("\n\n About to reply back \n\n");
					baseNodeServerInterface.onReceiveCriticalSectionRequest(true, replyBackPort);
				} else {
					System.out.print("\n\n "+mNodeName+" Requesting Critical Section. So Check timestamp \n");
					
					if(incomingPriority > RequestPriority.priorityValue(requestPriority)) {
						baseNodeServerInterface.onReceiveCriticalSectionRequest(true, replyBackPort);
					} else if(mCurrentRequestTimeStamp > incomingTimeStamp) {
						baseNodeServerInterface.onReceiveCriticalSectionRequest(true, replyBackPort);
					} else {
						System.out.print("\n\n "+nodeName+" on timestamp evaluation with "+mNodeName+" \n");
						
						if(!mDeferredNodes.contains(nodeName)) {
							mDeferredNodes.add(nodeName);
						}
						
						System.out.print("\n\n Nodes Deferred: "+mDeferredNodes+"\n");
					}
				}

			} else {
					System.out.print("\n\n "+mNodeName+" Accessing Critical Section. So Deferred "+nodeName+"\n");
					if(!mDeferredNodes.contains(nodeName)) {
						mDeferredNodes.add(nodeName);
					}
					System.out.print("\n\n Nodes Deferred: "+mDeferredNodes+"\n");
			}
		} else if(command.equals(BaseNode.MESSAGE_TYPE_REPLY)) {
			System.out.print("\n\n Received Reply from "+nodeName+"\n");
			
			if(!mRepliedNodes.contains(nodeName)) {
				mRepliedNodes.add(nodeName);
			}

			System.out.print("\n\n Received Replies from Nodes "+mRepliedNodes+"\n");

			if(areAllNodesReplied() == true) {
				System.out.print("\n\n Received reply from everyone \n\n");
				performCriticalSection(mNodeName);
				baseNodeServerInterface.onLeavingCriticalSection(true, mDeferredNodes);
				nodeStatus = NodeStatus.NodeStatusIdle;
				mDeferredNodes.clear();
			}
		}	
		
	}

	public synchronized void performCriticalSection(String nodeName) {
		System.out.print("\n\n Enter the synchronized function \n");
		if(nodeStatus == NodeStatus.NodeStatusRequestCriticalSection) {	
			if(mRepliedNodes.size() <= 1) {
				// Sometimes, some function may have already satisfied the condition and coming inside the function. But, it is prevented here as we are clearing the repliedNodes list in the finally clause ... 

				return ;
			}
			
			nodeStatus = NodeStatus.NodeStatusAccessingCriticalSection;
			System.out.print("\n\n "+nodeName+" performing critical section \n");
			try {	
				Thread.sleep(20000);
			} catch(Exception e) {
				e.printStackTrace();
			} finally {
				mRepliedNodes.clear();
				mRepliedNodes.add(mNodeName);
			}
			System.out.print("\n\n "+nodeName+" performed critical section \n\n");
			nodeStatus = NodeStatus.NodeStatusLeavingCriticalSection;
		}
	}

	public static String getMessageRepresentation(String messageType, String nodeName, int portNo, int priority) {
		String formattedMessage = "";
						

		if(messageType == BaseNode.MESSAGE_TYPE_REQUEST_CRITICAL_SECTION) {
			formattedMessage = BaseNode.MESSAGE_TYPE_REQUEST_CRITICAL_SECTION;
		} else if(messageType == BaseNode.MESSAGE_TYPE_REPLY) {
			formattedMessage = BaseNode.MESSAGE_TYPE_REPLY;
		}
		
		formattedMessage += ":" + nodeName + ":" + String.valueOf(portNo);
		
		if(messageType == BaseNode.MESSAGE_TYPE_REQUEST_CRITICAL_SECTION) {
			formattedMessage += ":" + String.valueOf(BaseNode.getCurrentTimeStamp()) + ":" + String.valueOf(priority);
		}
	
		return formattedMessage;
	}

	public static long getCurrentTimeStamp() {
		return new Timestamp(System.currentTimeMillis()).getTime();
	}

	public void requestingCriticalSection(String nodeName, int priority) {
		nodeStatus = NodeStatus.NodeStatusRequestCriticalSection;
		requestPriority = RequestPriority.requestPriority(priority);
		clientSendMessage(BaseNode.getMessageRepresentation(BaseNode.MESSAGE_TYPE_REQUEST_CRITICAL_SECTION, nodeName, mReceiveReplyPortNo, priority));
	}

	public void sendReply(String nodeName) {
		clientSendMessage(BaseNode.getMessageRepresentation(BaseNode.MESSAGE_TYPE_REPLY, nodeName, mReceiveReplyPortNo, RequestPriority.priorityValue(RequestPriority.RequestPriorityVeryLow)));
	}

	public void clientSendMessage(String nodeMessage) {
				
		if(isClientInitiated() == false) {
			System.out.print("\n\n initializing \n\n");
			if( initClient() == false ) {
					// if client failed to initiate ...
					// Then do not perform the next line of code ...

					return;
				}
			}
						
			clientSendData(nodeMessage);
	}


	public void run() {
		try {
			if( mThreadFunctionality == BaseNode.THREAD_FUNCTIONALITY_RECEIVER_MASTER ) {
				initiateServer();
				activateServer();
			} else if ( mThreadFunctionality == BaseNode.THREAD_FUNCTIONALITY_RECEIVER_SLAVE ) {
				readContent();
			} else if ( mThreadFunctionality == BaseNode.THREAD_FUNCTIONALITY_SENDER) {
			
			}

		} catch(Exception e) {
			System.out.print("\n\n Exception "+e);
			e.printStackTrace();
		}
		
	}

	public BaseNode(String clientAddress, int clientPort, String nodeName) {
		mClientAddress = clientAddress;
		mClientPort = clientPort;
		mThreadFunctionality = BaseNode.THREAD_FUNCTIONALITY_SENDER;
		mNodeName = nodeName;
	}

	public BaseNode(int serverPort, String nodeName) {
		mServerPort = serverPort;
		mThreadFunctionality = BaseNode.THREAD_FUNCTIONALITY_RECEIVER_MASTER;
		mNodeName = nodeName;
	}

	public BaseNode(ServerSocket serverServerSocket, Socket serverSocket, String nodeName) {
		mServerServerSocket = serverServerSocket;
		mServerSocket = serverSocket;
    mThreadFunctionality = BaseNode.THREAD_FUNCTIONALITY_RECEIVER_SLAVE;
		mNodeName = nodeName;
	}

};
