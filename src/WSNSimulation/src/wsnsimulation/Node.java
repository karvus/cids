package wsnsimulation;

/**
 * A generic node in the network
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public interface Node {
    /**
     * Gets the unique ID of the node
     * @return A unique int that identifies this node
     */
    public int getID();
    /**
     * Receives messages from another node
     * @param msg The message to receive
     * @param sender The Node that sent the message
     */
    public void receiveMessage(Message msg, Node sender);
    /** Regularly called by the network when Node should simulate a tick */
    public void simulate();
}
