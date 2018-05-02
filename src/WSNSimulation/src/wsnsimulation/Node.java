package wsnsimulation;

/**
 * A generic node in the network
 * @author Johan Fagerberg
 */
public interface Node {
    /**
     * Gets the unique ID of the node
     * @return A unique int that identifies this node
     */
    public int getID();
    /**
     * Gets the current logical time of the node
     * @return The current time, as reported by the Node's logical clock
     */
    public double getTime();
    /**
     * Receives messages from another node
     * @param msg The message to receive
     * @param sender The Node that sent the message
     */
    public void receiveMessage(Message msg, Node sender);
    /** Regularly called by the network when Node should simulate a tick */
    public void simulate();
}
