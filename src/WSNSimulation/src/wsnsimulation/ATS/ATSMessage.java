package wsnsimulation.ATS;

import wsnsimulation.Message;

/**
 * A message used to share parameters amongst ATSNodes
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class ATSMessage implements Message {
    public class Parameters {
        // these all mirror parameters from the Node
        public double time;
        public double logicalTime;
        public double correctionSkew;
        public double correctionOffset;
        
        @Override
        public String toString() {
            return String.format(
                    "t=%.2f,L(t)=%.2f,α=%.3f,β=%.3f",
                    time,
                    logicalTime,
                    correctionSkew,
                    correctionOffset
            );
        }
    }
    
    private final Parameters data;
    public ATSMessage(double time, double logicalTime, double skew, double offset) {
        this.data = new Parameters();
        this.data.time = time;
        this.data.logicalTime = logicalTime;
        this.data.correctionSkew = skew;
        this.data.correctionOffset = offset;
    }
    
    @Override
    public Parameters getData() {
        return this.data;
    }
    
    @Override
    public String toString() {
        return "ATSMessage{"+this.data+"}";
    }
}
