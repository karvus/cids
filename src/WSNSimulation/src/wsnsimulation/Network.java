package wsnsimulation;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * The overall network of nodes
 * Handles message sending (and delaying), as well as global time keeping
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class Network {
    private static final boolean DEBUG = Simulation.DEBUG;
    
    /**
     * Representation of a message while it is being delayed
     * Used in the PriorityQueue of messages
     */
    private class WaitingMessage implements Comparable<WaitingMessage> {
        private final int arrivalTime;
        private final Message msg;
        private final Node recipient;
        private final Node sender;
        
        public WaitingMessage(Message msg, int arrivalTime,
                Node recipient, Node sender) {
            this.arrivalTime = arrivalTime;
            this.msg = msg;
            this.recipient = recipient;
            this.sender = sender;
        }
        public int getArrivalTime() { return arrivalTime; }
        public Message getMessage() { return msg; }
        public Node getRecipient() { return recipient; }
        public Node getSender() { return sender; }
        
        /**
         * Sends the message to the recipient
         */
        public void arrive() {
            this.recipient.receiveMessage(this.msg, this.sender);
        }
        
        @Override
        public int compareTo(WaitingMessage them) { // used by PriorityQueue
            return this.arrivalTime - them.getArrivalTime();
        }
        @Override
        public String toString() {
            return "WaitingMessage<"+this.arrivalTime+":"+this.msg+">";
        }
    }
    
    private int time = -1; // -1 so we start at 0
    private final PriorityQueue<WaitingMessage> msgQueue = new PriorityQueue<>();
    private final List<Node> nodeList = new ArrayList<>();
    
    private final WSNLogger LOGGER = new WSNLogger("Network", this);
    public static int MSG_DELAY = 50;
    
    /**
     * Adds a Node to our network
     * @param node The node to add
     */
    public void addNode(Node node) {
        this.nodeList.add(node);
    }
    
    /**
     * Calculates the average error of each node
     * Where error is defined as the distance between the node's logical time
     *   and the time that is the average of every node's logical time
     */
    public double averageTimeError() {
        double[] nodeTimes = new double[this.nodeList.size()];
        double sumNodeTimes = 0.0;
        for (int i = 0; i < this.nodeList.size(); ++i) {
            nodeTimes[i] = this.nodeList.get(i).getTime();
            sumNodeTimes += nodeTimes[i];
        }
        double averageTime = sumNodeTimes / (double) nodeTimes.length;
        
        double error = 0.0;
        for (double nodeTime : nodeTimes) {
            error += Math.abs(nodeTime - averageTime);
        }
        return error / (double) nodeTimes.length;
    }
    
    /**
     * Simulates a single millisecond of time passing
     */
    public void simulate() {
        ++this.time;
        LOGGER.finer("Simulating tick "+this.time);
        LOGGER.info("Error is "+this.averageTimeError());
        
        // send off all messages that are ready to be sent
        while (msgQueue.size() > 0
                && msgQueue.peek().getArrivalTime() <= this.time) {
            WaitingMessage nextMsg = msgQueue.poll();
            nextMsg.arrive();
        }
        // then ask every node to simulate
        for (Node node : nodeList) {
            node.simulate();
        }
    }
    
    /**
     * Gets the current global time - no cheating ;)
     * @return The current real time
     */
    public int getTime() {
        return time;
    } 
    
    /**
     * Sends a message to a specific node
     * @param msg The message to be sent
     * @param recipient The node to send the message to
     * @param sender The node that sent the message
     */
    public void sendMessage(Message msg, Node recipient, Node sender) {
        if (DEBUG) {
            LOGGER.fine(
                    "Sending message {0} ({1} -> {2})",
                    new Object[]{msg, sender, recipient}
            );
        }
        
        // we delay sending the message to simulate real life uncertainties
        WaitingMessage delayedMsg = new WaitingMessage(
                msg,
                this.time + this.MSG_DELAY,
                recipient,
                sender
        );
        this.msgQueue.add(delayedMsg);
    }
}
