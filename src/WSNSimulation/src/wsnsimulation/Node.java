package wsnsimulation;

import java.util.Random;

/**
 * A generic node in the network
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public abstract class Node {
    protected final WSNLogger LOGGER;
    protected final Random RAND = new Random();
    protected final double HARDW_SKEW = 1.0/*1.0 + RAND.nextGaussian() * 0.33*/;
    protected final double HARDW_OFFSET = 0.0/*RAND.nextGaussian() * 10*/;
    protected double correctionSkew = 1.0; // must be changed by the algo
    protected double correctionOffset = 0.0; // must be changed by the algo
    protected final int id;
    protected final Network network;
    protected Node[] neighbors;
    
    public Node(int id, Network network, Node[] neighbors) {
        this.id = id;
        this.network = network;
        this.neighbors = neighbors;
        
        this.LOGGER = new WSNLogger(this.toString(), network);
    }
    
    /**
     * Receives messages from another node
     * @param msg The message to receive
     * @param sender The Node that sent the message
     */
    abstract public void receiveMessage(Message msg, Node sender);
    /** Regularly called by the network when Node should simulate a tick */
    abstract public void simulate();
    
    /** Gets the Nodes unique ID */
    public int getID() { return this.id; }
    
    /**
     * Sets our list of neighbors
     * @param neighbors The list of neighbors that we can send messages to
     */
    public void setNeighbors(Node[] neighbors) { this.neighbors = neighbors; }
    
    /**
     * Gets the current time, as reported by the "hardware"
     * @return The current time
     */
    protected double getHardwareTime() {
        return HARDW_SKEW * network.getTime() + HARDW_OFFSET;
    }
    
    /**
     * Gets the current time, as reported by the logical clock
     * The algorithm should change correctionSkew and correctionOffset
     * @return The current logical time
     */
    protected double getTime() {
        return correctionSkew * this.getHardwareTime() + correctionOffset;
    }
}
