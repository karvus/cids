package wsnsimulation.Nodes;

import java.util.Random;
import wsnsimulation.Message;
import wsnsimulation.Messages.StringMessage;
import wsnsimulation.Network;
import wsnsimulation.Node;
import wsnsimulation.WSNLogger;

/**
 * A basic Node, that doesn't implement anything but a hardware clock
 * Is supposed to be extended for use by algorithm-specific Node implementations
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class BasicNode implements Node {
    private final WSNLogger LOGGER;
    private final Random RAND = new Random();
    private final double HARDW_SKEW = 1.0 + RAND.nextGaussian() * 0.33;
    private final double HARDW_OFFSET = RAND.nextGaussian() * 10;
    
    private boolean msgInFlight = false;
    private double correctionSkew = 1.0;
    private double correctionOffset = 0.0;
    
    private final int id;
    private final Network network;
    private Node[] neighbors;
    public BasicNode(int id, Network network, Node[] neighbors) {
        this.id = id;
        this.network = network;
        this.neighbors = neighbors;
        
        this.LOGGER = new WSNLogger(this.toString(), network);
    }
    
    /** Gets the Nodes unique ID */
    @Override
    public int getID() {
        return this.id;
    }
    
    /**
     * Sets our list of neighbors
     * @param neighbors The list of neighbors that we can send messages to
     */
    public void setNeighbors(Node[] neighbors) {
        this.neighbors = neighbors;
    }
    
    /**
     * Gets the current time, as reported by the "hardware"
     * @return The current time
     */
    private double getHardwareTime() {
        return HARDW_SKEW * network.getTime() + HARDW_OFFSET;
    }
    
    /**
     * Gets the current time, as reported by the logical clock
     * BasicNode doesn't implement a logical clock, so this is just hardware
     * @return The current logical time
     */
    private double getTime() {
        return correctionSkew * this.getHardwareTime() + correctionOffset;
    }

    /**
     * Called when we receive a message
     * @param msg
     * @param sender 
     */
    @Override
    public void receiveMessage(Message msg, Node sender) {
        LOGGER.info("Received message from "+sender.getID()+": "+msg);
        
        // if it's an ACK message, ready us for sending again
        if (msg instanceof StringMessage
                && msg.getData().equals("Received")) {
            msgInFlight = false;
        // otherwise, send an ACK message
        } else {
            this.network.sendMessage(
                    new StringMessage("Received"),
                    sender,
                    this
            );
        }
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    
    
    @Override
    public void simulate() {
        LOGGER.fine("Simulating: "+this.getTime());
        
        if (!msgInFlight) {
            // send a message to a random neighbor
            Node targetNeighbor = this.neighbors[
                    RAND.nextInt(this.neighbors.length)
            ];
            this.network.sendMessage(
                    new StringMessage("Hello, "+targetNeighbor.getID()+"!"),
                    targetNeighbor,
                    this
            );
            
            msgInFlight = true;
        }
        
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String toString() {
        return "BasicNode["+this.id+"]";
    }
}
