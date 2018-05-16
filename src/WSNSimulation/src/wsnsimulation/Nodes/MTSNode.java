package wsnsimulation.Nodes;

import java.util.Random;
import java.util.logging.Level;
import wsnsimulation.Message;
import wsnsimulation.Messages.StringMessage;
import wsnsimulation.Network;
import wsnsimulation.Node;
import wsnsimulation.WSNLogger;

/**
 *
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class MTSNode implements Node {

    private final WSNLogger LOGGER;
    private final Random RAND = new Random();
    private final double HARDW_SKEW = 1.0 + RAND.nextGaussian() * 0.33;
    private final double HARDW_OFFSET = RAND.nextGaussian() * 10;
    private double correctionSkew = 1.0d;
    private double correctionOffset = 0.0d;

    private int comparingIndex = 0;
    private Node comparingWith = null;
    private double lastTime, lastTimeTarget;

    private boolean msgInFlight = false;

    private final int id;
    private final Network network;
    private Node[] neighbors;

    public MTSNode( int id, Network network, Node[] neighbors )
    {
        this.id = id;
        this.network = network;
        this.neighbors = neighbors;

        this.LOGGER = new WSNLogger( this.toString(), network );
    }

    /** Gets the Nodes unique ID */
    @Override
    public int getID()
    {
        return this.id;
    }

    public void setNeighbors( Node[] neighbors )
    {
        this.neighbors = neighbors;
    }

    public void compareAndUpdateTime( Node target )
    {
        if ( comparingWith != null ) {
            LOGGER.adv( Level.WARNING, "Already doing comparison...", null );
        }
        else {
            comparingWith = target;
            lastTime = getTime();
            //Should probably get target time in a message with delay and such
            // TODO: can't read time from another node
            lastTimeTarget = target.getTime();
        }
    }
    
    /**
     * Gets the current time, as reported by the "hardware"
     * @return The current time
     */
    private double getHardwareTime() {
        return HARDW_SKEW * network.getTime() + HARDW_OFFSET;
    }

    private double getTime()
    {
        return correctionSkew * this.getHardwareTime() + correctionOffset;
    }

    @Override
    public void receiveMessage( Message msg, Node sender )
    {
        LOGGER.info( "Received message from " + sender.getID() + ": " + msg );

        // if it's an ACK message, ready us for sending again
        if ( msg instanceof StringMessage
                && msg.getData().equals( "Received" ) ) {
            msgInFlight = false;
            // otherwise, send an ACK message
        }
        else {
            this.network.sendMessage(
                    new StringMessage( "Received" ),
                    sender,
                    this
            );
        }
    }

    @Override
    public void simulate()
    {
        LOGGER.fine( "Simulating: " + this.getTime() );

        if ( comparingWith == null ) {
            comparingWith = neighbors[comparingIndex++ % neighbors.length];
            compareAndUpdateTime( comparingWith );
        }
        else {
            // TODO: can't read time from another node
            double time = getTime(), targetTime = comparingWith.getTime();
            
            
            //TODO calculate correction-constants
            
            
            comparingWith = null;
        }

        if ( !msgInFlight ) {
            // send a message to a random neighbor
            Node targetNeighbor = this.neighbors[RAND.nextInt( this.neighbors.length )];
            this.network.sendMessage(
                    new StringMessage( "Hello, " + targetNeighbor.getID() + "!" ),
                    targetNeighbor,
                    this
            );

            msgInFlight = true;
        }
    }

    @Override
    public String toString()
    {
        return "MTSNode[" + this.id + "]";
    }
}
