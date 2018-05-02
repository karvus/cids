package wsnsimulation.Messages;

import wsnsimulation.Message;

/**
 * A simple String that can be sent from Node to Node
 * Mostly used for debugging
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class StringMessage implements Message {
    private final String data;
    public StringMessage(String data) {
        this.data = data;
    }
    
    @Override
    public Object getData() {
        return this.data;
    }

    @Override
    public String toString() {
        return "StringMessage{\""+this.data+"\"}";
    }
}
