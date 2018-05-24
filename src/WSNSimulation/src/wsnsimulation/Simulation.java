package wsnsimulation;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import wsnsimulation.ATS.ATSNode;
import wsnsimulation.MMTS.MMTSNode;

/**
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class Simulation {    
    public static final boolean DEBUG = true;
    
    private static Node[][] generateATSGrid(int n, Network network) {
        Node[][] nodes = new Node[n][n];
        for (int i = 0; i < nodes.length; ++i) {
            for (int j = 0; j < nodes[i].length; ++j) {
                nodes[i][j] = new ATSNode(
                        i*nodes.length + j,
                        network,
                        null
                );
                network.addNode(nodes[i][j]);
            }
        }
        return nodes;
    }
    
    private static Node[][] generateMMTSGrid(int n, Network network) {
        Node[][] nodes = new Node[n][n];
        for (int i = 0; i < nodes.length; ++i) {
            for (int j = 0; j < nodes[i].length; ++j) {
                nodes[i][j] = new MMTSNode(
                        i*nodes.length + j,
                        network,
                        null
                );
                network.addNode(nodes[i][j]);
            }
        }
        return nodes;
    }
    
    /**
     * Generates a network containing an NxN interconnected grid of nodes
     * @return 
     */
    private static Network generateNetwork(int n) {
        Network network = new Network();
        // create an NxN grid of nodes
        // nodes can only send messages up/to the side
        Node[][] nodes = generateMMTSGrid(n, network);
        for (int i = 0; i < nodes.length; ++i) {
            for (int j = 0; j < nodes[i].length; ++j) {
                Node[] neighbors = new Node[nodes.length + nodes[i].length - 2];
                int index = 0;
                for (int x = 0; x < nodes.length; ++x) {
                    if (x == i) { continue; }
                    neighbors[index++] = nodes[x][j];
                }
                for (int y = 0; y < nodes[i].length; ++y) {
                    if (y == j) { continue; }
                    neighbors[index++] = nodes[i][y];
                }
                nodes[i][j].setNeighbors(neighbors);
            }
        }
        
        return network;
    }
    
    /**
     * Outputs delay=0,period=100, delay=10,period=100, delay=50,period=100
     *         delay=10,period=20, delay=10,period=500 files
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        Network.MSG_DELAY = 0;
        Node.BROADCAST_PERIOD = 100;
        measureAndOutput();
        
        Network.MSG_DELAY = 10;
        Node.BROADCAST_PERIOD = 100;
        measureAndOutput();
        
        Network.MSG_DELAY = 50;
        Node.BROADCAST_PERIOD = 100;
        measureAndOutput();
        
        Network.MSG_DELAY = 10;
        Node.BROADCAST_PERIOD = 20;
        measureAndOutput();
        
        Network.MSG_DELAY = 10;
        Node.BROADCAST_PERIOD = 500;
        measureAndOutput();
    }
    
    /**
     * Creates networks, measures errors, outputs to file
     * @throws IOException 
     */
    private static void measureAndOutput() throws IOException {
        // create our network
        Network[] networks = new Network[5];
        for (int i = 0; i < networks.length; ++i) {
            networks[i] = generateNetwork(5);
        }
        
        // simulate them and print average errors every 100ms
        WSNLogger.LOG_FILE = new BufferedWriter(new FileWriter("./mmts-"+Network.MSG_DELAY+"-"+Node.BROADCAST_PERIOD+".csv"));
        WSNLogger logger = new WSNLogger("Simulation", networks[0]);
        logger.write("MMTS ("+Network.MSG_DELAY+"ms message delay, "+Node.BROADCAST_PERIOD+"ms broadcast interval)");
        logger.write("Tick,Average error");
        for (int i = 0; i < 10000; ++i) {
            for (int j = 0; j < networks.length; ++j) {
                networks[j].simulate();
            }
            
            if (i % 100 == 0) {
                double totalError = 0.0;
                for (int j = 0; j < networks.length; ++j) {
                    totalError += networks[j].averageTimeError();
                }
                double averageError = totalError / (double) networks.length;
                logger.write(networks[0].getTime()+","+averageError);
            }
        }
        WSNLogger.LOG_FILE.close();
    }
    
}
