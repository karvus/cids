package wsnsimulation.MMTS;

import wsnsimulation.Message;

/**
 * A message used to share parameters amongst MMTSNodes
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class MMTSMessage implements Message {
    public class Parameters {
        // these all mirror parameters from the Node
        public double time;
        public double correctionSkew;
        public double correctionOffset;
        public double mu;
        public double nu;
        
        @Override
        public String toString() {
            return String.format(
                    "t=%.2f,α=%.3f,β=%.3f,μ=%.3f,ν=%.3f",
                    time,
                    correctionSkew,
                    correctionOffset,
                    mu,
                    nu
            );
        }
    }
    
    private final Parameters data;
    public MMTSMessage(double time, double skew, double offset,
            double mu, double nu) {
        this.data = new Parameters();
        this.data.time = time;
        this.data.correctionSkew = skew;
        this.data.correctionOffset = offset;
        this.data.mu = mu;
        this.data.nu = nu;
    }
    
    @Override
    public Parameters getData() {
        return this.data;
    }
    
    @Override
    public String toString() {
        return "MMTSMessage{"+this.data+"}";
    }
}
