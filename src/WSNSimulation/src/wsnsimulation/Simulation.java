package wsnsimulation;

import wsnsimulation.MMTS.MMTSNode;

/**
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class Simulation {    
    public static final boolean DEBUG = true;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // create our network
        Network network = new Network();
        
        // create a 3x3 grid of nodes
        // nodes can only send messages up/to the side
        Node[][] nodes = new MMTSNode[5][5];
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
        
        // create simple interconnected 2 nodes
        /*Node[] nodes = new Node[]{
            new MMTSNode(0, network, new Node[0]),
            new MMTSNode(1, network, new Node[0])
        };
        nodes[0].setNeighbors(new Node[]{ nodes[1] });
        nodes[1].setNeighbors(new Node[]{ nodes[0] });
        network.addNode(nodes[0]);
        network.addNode(nodes[1]);*/
        
        for (int i = 0; i < 1000; ++i) {
            network.simulate();
        }
    }
    
}
