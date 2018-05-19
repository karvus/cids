package wsnsimulation.MMTS;

import java.util.HashMap;
import java.util.Map;
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
    
    /** Represents data related to the pair (us,neighbor[i]) for a given i */
    private class NeighborData {
        public double relativeSkew = 1.0; // averaged relative skew (alpha_ij)
        public int k = 0; // number of times we've received messages
        public double prevMsgSent; // time at which they sent the previous message
        public double prevMsgReceived; // time at which we received the previous message
    }
    private final Map<Node,NeighborData> neighborDatas = new HashMap<>();
    
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
        
        MMTSMessage.Parameters theirData = ((MMTSMessage) msg).getData();
        
        // calculate a new averaged relative skew to the neighbor
        if (this.neighborDatas.containsKey(sender)) {
            // if we already have data, that means we can start calculating
            NeighborData prevData = this.neighborDatas.get(sender);
            ++prevData.k;
            LOGGER.fine("Relative skew was "+prevData.relativeSkew);
            // calculate our new averaged relative skew
            double relativeSkew = (
                    // (t_j(t_k) - t_j(t_k-1))/(t_i(t_k) - t_i(t_k-1))
                    (theirData.time - prevData.prevMsgSent)/(this.getTime() - prevData.prevMsgReceived)
                    // + (k-1)*a_ij(k-1)
                    + (prevData.k - 1) * prevData.relativeSkew
            ) / prevData.k;
            LOGGER.fine("Relative skew became "+relativeSkew);
            
            double skewMax = correctionSkew + mu;
            double skewMin = correctionSkew - mu;
            double offsetMax = correctionOffset + nu;
            double offsetMin = correctionOffset - nu;
            double p = (relativeSkew * (theirData.correctionSkew + theirData.mu))
                    / skewMax;
            double q = (relativeSkew * (theirData.correctionSkew - theirData.mu))
                    / skewMin;
            
            // apply maximum consensus or minimum consensus
            if (p > 1.0) {
                skewMax = relativeSkew * (theirData.correctionSkew + theirData.mu);
                offsetMax = (theirData.correctionSkew + theirData.mu)*theirData.time
                        + theirData.correctionOffset + theirData.nu
                        - relativeSkew * (theirData.correctionSkew + theirData.mu)*this.getTime();
            } // TODO: implement p == 1.0
            if (q < 1.0) {
                skewMin = relativeSkew * (theirData.correctionSkew - theirData.mu);
                offsetMin = (theirData.correctionSkew - theirData.mu)*theirData.time
                        + theirData.correctionOffset - theirData.nu
                        - relativeSkew * (theirData.correctionSkew - theirData.mu)*this.getTime();
            } // TODO: implement q == 1.0
            
            // change our parameters as needed
            this.correctionSkew = (skewMax + skewMin) / 2;
            this.correctionOffset = (offsetMax + offsetMin) / 2;
            this.mu = (skewMax - skewMin) / 2;
            this.nu = (offsetMax - offsetMin) / 2;
            
            // and store our new data
            prevData.relativeSkew = relativeSkew;
            prevData.prevMsgReceived = this.getTime();
            prevData.prevMsgSent = theirData.time;
        } else {
            // otherwise we must initialize some data for later averaging
            NeighborData data = new NeighborData();
            data.prevMsgSent = theirData.time;
            data.prevMsgReceived = this.getTime();
            
            this.neighborDatas.put(
                    sender,
                    data
            );
        }
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
