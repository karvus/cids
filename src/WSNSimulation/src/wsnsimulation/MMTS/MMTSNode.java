package wsnsimulation.MMTS;

import wsnsimulation.Message;
import wsnsimulation.Network;
import wsnsimulation.Node;

/**
 * A Node that uses MMTS to synchronize time
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class MMTSNode extends Node {
    private final int BROADCAST_PERIOD = 10;
    
    private double lastMsgTime = Double.MIN_VALUE; // time we last sent out messages to neighbors
    // inherits correctionSkew (alpha hat) and correctionOffset (beta hat)
    private double mu = 0.0; // used in algorithm (mu)
    private double nu = 0.0; // used in algorithm (nu)
    
    public MMTSNode(int id, Network network, Node[] neighbors) {
        super(id, network, neighbors);
    }

    /**
     * Called when we receive a message
     * @param msg
     * @param sender 
     */
    @Override
    public void receiveMessage(Message msg, Node sender) {
        LOGGER.info("Received message from "+sender.getID()+": "+msg);
    }

    
    
    @Override
    public void simulate() {
        LOGGER.fine("Simulating: "+this.getTime());
        
        // MMTS has every node broadcast periodically
        if (this.getTime() > this.lastMsgTime + BROADCAST_PERIOD) {
            MMTSMessage msg = new MMTSMessage(
                    this.getTime(),
                    correctionSkew,
                    correctionOffset,
                    mu,
                    nu
            );
            
            // send the message to every single neighbor
            for (Node neighbor : this.neighbors) {
                network.sendMessage(msg, neighbor, this);
            }
            
            // and make sure we don't send again before we should
            this.lastMsgTime = this.getTime();
        }
    }

    @Override
    public String toString() {
        return "MMTSNode["+this.id+"]";
    }
}
