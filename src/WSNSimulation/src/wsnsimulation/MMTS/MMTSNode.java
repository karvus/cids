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
    private double lastMsgTime = this.getTime() - BROADCAST_PERIOD - 1.0; // time we last sent out messages to neighbors
    // inherits correctionSkew (alpha hat) and correctionOffset (beta hat)
    private double mu = 0.0; // used in algorithm (mu)
    private double nu = 0.0; // used in algorithm (nu)
    
    /** Represents data related to the pair (us,neighbor[i]) for a given i */
    private class NeighborData {
        public Node neighbor;
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
     * Calculates the new average relative skew when a new message comes in
     * Assumes that prevData.k has been incremented and thus refers to the current k
     * @param theirData The data they sent in the message
     * @param prevData The data we stored from last time they contacted us
     */
    private double calculateRelativeSkew(
            MMTSMessage.Parameters theirData, NeighborData prevData) {
        double newSkew = (
                // (t_j(t_k) - t_j(t_k-1))/(t_i(t_k) - t_i(t_k-1))
                (theirData.time - prevData.prevMsgSent)/(this.getHardwareTime() - prevData.prevMsgReceived)
                // + (k-1)*a_ij(k-1)
                + (prevData.k - 1) * prevData.relativeSkew
        ) / prevData.k;
        
        if (DEBUG) {
            String msg = "Relative skew:";
            msg += "\n  Our HARDWARE: "+HARDW_SKEW+" : "+HARDW_OFFSET;
            msg += "\n  Their HARDWARE: "+prevData.neighbor.HARDW_SKEW+" : "+prevData.neighbor.HARDW_OFFSET;
            msg += "\n  Our correction: "+this.correctionSkew+" : "+this.correctionOffset;
            msg += "\n  Their correction: "+prevData.neighbor.correctionSkew+" : "+prevData.neighbor.correctionOffset;
            msg += "\n  Prev timings: "+prevData.prevMsgSent+"->"+prevData.prevMsgReceived;
            msg += "\n  New timings:  "+theirData.time+"->"+this.getHardwareTime();
            msg += "\n  Their interval: "+(theirData.time - prevData.prevMsgSent);
            msg += "\n  Our interval: "+(this.getHardwareTime() - prevData.prevMsgReceived);
            msg += "\n  Weighted previous skew: "+((prevData.k - 1) * prevData.relativeSkew);
            msg += "\n  New skew: "+newSkew;
            LOGGER.fine(msg);
        }
        return newSkew;
    }

    /**
     * Called when we receive a message
     * @param msg
     * @param sender 
     */
    @Override
    public void receiveMessage(Message msg, Node sender) {
        if (DEBUG) {
            LOGGER.fine("Received message at "+this.getTime()+" from "+sender.getID()+": "+msg);
        }
        
        MMTSMessage.Parameters theirData = ((MMTSMessage) msg).getData();
        
        // calculate a new averaged relative skew to the neighbor
        if (this.neighborDatas.containsKey(sender)) {
            // if we already have data, that means we can start calculating
            NeighborData prevData = this.neighborDatas.get(sender);
            ++prevData.k;
            
            // calculate our new averaged relative skew
            if (DEBUG) { LOGGER.fine("Relative skew was "+prevData.relativeSkew); }
            double relativeSkew = this.calculateRelativeSkew(theirData, prevData);
            if (DEBUG) { LOGGER.fine("Relative skew became "+relativeSkew); }
            
            double skewMax = correctionSkew + mu;
            double skewMin = correctionSkew - mu;
            double offsetMax = correctionOffset + nu;
            double offsetMin = correctionOffset - nu;
            if (skewMax == 0 || skewMin == 0) {
                LOGGER.err("Skew max/min was zero: max="+skewMax+", min="+skewMin
                    +"\n  correctionSkew: "+correctionSkew
                    +"\n  mu: "+mu);
                System.exit(1);
            }
            double p = (relativeSkew * (theirData.correctionSkew + theirData.mu))
                    / skewMax;
            double q = (relativeSkew * (theirData.correctionSkew - theirData.mu))
                    / skewMin;
            
            // apply maximum consensus if applicable
            if (p > 1.0) {
                if (DEBUG) { LOGGER.fine("Applying maximum consensus: "+p); }
                skewMax = relativeSkew * (theirData.correctionSkew + theirData.mu);
                offsetMax = (theirData.correctionSkew + theirData.mu)*theirData.time
                        + theirData.correctionOffset + theirData.nu
                        - relativeSkew * (theirData.correctionSkew + theirData.mu)*this.getHardwareTime();
            }
            if (p == 1.0) {
                if (DEBUG) { LOGGER.fine("Applying strict maximum consensus: "+p); }
                double i_variant = (this.correctionSkew + this.mu)*this.getHardwareTime()
                        + this.correctionOffset + this.nu;
                double j_variant = (theirData.correctionSkew + theirData.mu)*theirData.time
                        + theirData.correctionOffset + theirData.nu;
                
                offsetMax = Math.max(i_variant, j_variant)
                        - (this.correctionSkew + this.mu) * this.getHardwareTime();
            }
            // apply minimum consensus if applicable
            if (q < 1.0) {
                if (DEBUG) { LOGGER.fine("Applying minimum consensus: "+q); }
                skewMin = relativeSkew * (theirData.correctionSkew - theirData.mu);
                offsetMin = (theirData.correctionSkew - theirData.mu)*theirData.time
                        + theirData.correctionOffset - theirData.nu
                        - relativeSkew * (theirData.correctionSkew - theirData.mu)*this.getHardwareTime();
            }
            if (q == 1.0) {
                if (DEBUG) { LOGGER.fine("Applying strict minimum consensus: "+q); }
                double i_variant = (this.correctionSkew - this.mu)*this.getHardwareTime()
                        + this.correctionOffset - this.nu;
                double j_variant = (theirData.correctionSkew - theirData.mu)*theirData.time
                        + theirData.correctionOffset - theirData.nu;
                
                offsetMax = Math.min(i_variant, j_variant)
                        - (this.correctionSkew - this.mu) * this.getHardwareTime();
            }
            
            if (DEBUG) {
                String log = "Before processing:";
                log += "\n  a,b: "+this.correctionSkew+" , "+this.correctionOffset;
                log += "\n  y,v: "+this.mu+" , "+this.nu;
                LOGGER.fine(log);
            }
            
            // change our parameters as needed
            this.correctionSkew = (skewMax + skewMin) / 2;
            this.correctionOffset = (offsetMax + offsetMin) / 2;
            this.mu = (skewMax - skewMin) / 2;
            this.nu = (offsetMax - offsetMin) / 2;
            
            if (DEBUG) {
                String log = "After processing:";
                log += "\n  aMax,aMin: "+skewMax+" , "+skewMin;
                log += "\n  bMax,bMin: "+offsetMax+" , "+offsetMin;
                log += "\n  a,b: "+this.correctionSkew+" , "+this.correctionOffset;
                log += "\n  y,v: "+this.mu+" , "+this.nu;
                LOGGER.fine(log);
            }
            
            // and store our new data
            prevData.relativeSkew = relativeSkew;
            prevData.prevMsgReceived = this.getHardwareTime();
            prevData.prevMsgSent = theirData.time;
        } else {
            // otherwise we must initialize some data for later averaging
            NeighborData data = new NeighborData();
            data.prevMsgSent = theirData.time;
            data.prevMsgReceived = this.getHardwareTime();
            data.neighbor = sender;
            
            this.neighborDatas.put(
                    sender,
                    data
            );
        }
    }

    
    
    @Override
    public void simulate() {
        LOGGER.finer("Simulating: "+this.getTime()+", "+(this.lastMsgTime + BROADCAST_PERIOD));
        
        // MMTS has every node broadcast periodically
        if (this.getHardwareTime() > this.lastMsgTime + BROADCAST_PERIOD) {
            MMTSMessage msg = new MMTSMessage(
                    this.getHardwareTime(),
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
            this.lastMsgTime = this.getHardwareTime();
        }
    }

    @Override
    public String toString() {
        return "MMTSNode["+this.id+"]";
    }
}
