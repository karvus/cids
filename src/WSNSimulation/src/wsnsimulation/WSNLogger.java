package wsnsimulation;

import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * A simple logger that displays information as simulation-time stamped lines
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class WSNLogger {
    private final String name;
    private Level level = Level.INFO;
    private Network network;
    public WSNLogger(String name, Network network) {
        this.name = name;
        this.network = network;
    }
    
    /** Get the string that should be put before a log line */
    private String getPrefix(Level level) {
        return this.network.getTime()+" ["+level.getName()+" "+this.name+"] ";
    }
    
    /** Log a message at the specified level */
    public void log(Level level, String str) {
        if (level.intValue() < this.level.intValue()) { return; }
        System.out.println(
                this.getPrefix(level)
                + str
        );
    }
    public void log(Level level, String template, Object[] params) {
        this.log(level, MessageFormat.format(template, params));
    }
    
    /** Log a message at the INFO level */
    public void info(String str) {
        this.log(
                Level.INFO,
                str
        );
    }
    public void info(String template, Object[] params) {
        this.log(
                Level.INFO,
                template,
                params
        );
    }
    
    /** Log a message at the FINE level */
    public void fine(String str) {
        this.log(
                Level.FINE,
                str
        );
    }
    public void fine(String template, Object[] params) {
        this.log(
                Level.FINE,
                template,
                params
        );
    }
}
