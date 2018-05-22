package wsnsimulation.ATS;

import java.util.HashMap;
import java.util.Map;
import wsnsimulation.Message;
import wsnsimulation.Network;
import wsnsimulation.Node;

/**
 * A Node that uses MMTS to synchronize time
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class ATSNode extends Node {
    private double lastMsgTime = this.getTime() - BROADCAST_PERIOD - 1.0; // time we last sent out messages to neighbors
    // inherits correctionSkew (alpha hat) and correctionOffset (beta hat)
    private double tuningRelativeSkew = 0.1; // rho_n
    private double tuningSkew = 0.1; // rho_v
    private double tuningOffset = 0.1; // rho_o
    
    /** Represents data related to the pair (us,neighbor[i]) for a given i */
    private class NeighborData {
        public Node neighbor;
        public double relativeSkew = 1.0; // averaged relative skew (alpha_ij)
        public int k = 0; // number of times we've received messages
        public double prevMsgSent; // time at which they sent the previous message
        public double prevMsgReceived; // time at which we received the previous message
    }
    private final Map<Node,NeighborData> neighborDatas = new HashMap<>();
    
    public ATSNode(int id, Network network, Node[] neighbors) {
        super(id, network, neighbors);
    }

    @Override
    public void receiveMessage(Message msg, Node sender) {
        ATSMessage.Parameters theirData = ((ATSMessage) msg).getData();
        //LOGGER.err("Got message: "+theirData);
        
        // calculate a new averaged relative skew to the neighbor
        if (this.neighborDatas.containsKey(sender)) {
            // if we already have data, that means we can start calculating
            NeighborData prevData = this.neighborDatas.get(sender);
            ++prevData.k;
            
            // relative drift estimation
            //LOGGER.warn("Relative skew was: "+prevData.relativeSkew);
            prevData.relativeSkew = tuningRelativeSkew * prevData.relativeSkew
                    + (1 - tuningRelativeSkew) *
                        ( (theirData.time - prevData.prevMsgSent)
                        / (this.getHardwareTime() - prevData.prevMsgReceived));
            //LOGGER.warn("Relative skew became: "+prevData.relativeSkew);
            
            // drift compensation
            //LOGGER.warn("Skew was: "+this.correctionSkew);
            this.correctionSkew = tuningSkew * this.correctionSkew
                    + (1 - tuningSkew) * prevData.relativeSkew * theirData.correctionSkew;
            //LOGGER.warn("Skew became: "+this.correctionSkew);
            
            // offset compensation
            //LOGGER.warn("Offset was: "+this.correctionOffset);
            this.correctionOffset = this.correctionOffset // TODO: shouldn't this have tuning?
                    + (1 - tuningOffset) * (theirData.logicalTime - this.getTime());
            //LOGGER.warn("Offset became: "+this.correctionOffset);
            
            // store data for next iteration
            prevData.prevMsgSent = theirData.time;
            prevData.prevMsgReceived = this.getHardwareTime();
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
        
        // ATS has every node broadcast periodically
        if (this.getHardwareTime() > this.lastMsgTime + BROADCAST_PERIOD) {
            ATSMessage msg = new ATSMessage(
                    this.getHardwareTime(),
                    this.getTime(),
                    this.correctionSkew,
                    this.correctionOffset
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
        return "ATSNode["+this.id+"]";
    }
}
