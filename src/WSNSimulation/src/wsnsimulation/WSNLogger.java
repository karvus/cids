package wsnsimulation;

import java.text.MessageFormat;
import java.util.logging.Level;

/**
 * A simple logger that displays information as simulation-time stamped lines
 * @authors jofag17, luols17, moell17, perat17, tsten16
 */
public class WSNLogger {
    private final String COL_PRE = "\u001B[";
    private final String name;
    private Level level = Level.INFO;
    private Network network;
    public WSNLogger(String name, Network network) {
        this.name = name;
        this.network = network;
    }
    
    /** Prefixed and postfixes a terminal color code */
    private String col(int code) {
        return "\u001B["+code+"m";
    }
    
    /** Get the string that should be put before a log line */
    private String getPrefix(Level level) {
        String levelName = level.getName();
        if (level == Level.FINER) {
            levelName = "FINR";
        } else if (level == Level.WARNING) {
            levelName = "WARN";
        } else if (level == Level.SEVERE) {
            levelName = "SEVR";
        }
        
        return col(34)+this.network.getTime()
                +col(0)+col(2)+col(90)+" ["+levelName+" "+this.name+"] "
                +col(0);
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
    
    /** Log a message as an error at the SEVERE level */
    public void err(String str) {
        this.log(
                Level.SEVERE,
                col(41) + str.replaceAll("\n", "\n"+col(41)) + col(0)
        );
    }
    public void err(String template, Object[] params) {
        this.log(
                Level.SEVERE,
                col(41) + template.replaceAll("\n", "\n"+col(41)) + col(0),
                params
        );
    }
    
    /** Log a message as a warning at the WARNING level */
    public void warn(String str) {
        this.log(
                Level.WARNING,
                col(43) + str.replaceAll("\n", "\n"+col(43)) + col(0)
        );
    }
    public void warn(String template, Object[] params) {
        this.log(
                Level.WARNING,
                col(43) + template.replaceAll("\n", "\n"+col(43)) + col(0),
                params
        );
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
    
    /** Log a message at the FINE level */
    public void finer(String str) {
        this.log(
                Level.FINER,
                str
        );
    }
    public void finer(String template, Object[] params) {
        this.log(
                Level.FINER,
                template,
                params
        );
    }
    
    public void adv(Level level, String str, Object... params){
        this.log(
                level,
                str,
                params);
    }
}
