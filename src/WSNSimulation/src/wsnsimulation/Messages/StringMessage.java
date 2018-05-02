package wsnsimulation.Messages;

import wsnsimulation.Message;

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
