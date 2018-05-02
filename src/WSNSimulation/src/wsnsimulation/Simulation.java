package wsnsimulation;

import wsnsimulation.Nodes.BasicNode;

public class Simulation {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // create our network
        Network network = new Network();
        
        // create a ring of 3 BasicNodes
        // each node can only send messages to the next node in the ring
        BasicNode[] nodes = new BasicNode[3];
        for (int i = 0; i < nodes.length; ++i) {
            nodes[i] = new BasicNode(
                    i,
                    network,
                    null
            );
            network.addNode(nodes[i]);
            // update previous node's neighbors to be us
            if (i > 0) {
                nodes[i - 1].setNeighbors(new BasicNode[]{nodes[i]});
            }
        }
        // close the ring
        nodes[nodes.length - 1].setNeighbors(new BasicNode[]{nodes[0]});
        
        for (int i = 0; i < 100; ++i) {
            network.simulate();
        }
    }
    
}
