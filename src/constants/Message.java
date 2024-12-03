package constants;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * this object is used to communicate between programs.
 * Any amount of serializable object can be passes to the constructor.
 * The command is the first item passed, which is to they signify the recipient
 * of the message.
 */
public class Message implements Serializable {
    ArrayList<Object> args = new ArrayList<>();

    public Message(Object... args) {
        for(Object arg : args) {
            this.args.add(arg);
        }
    }

    public String toStrings() {
        String returnStr = new String();
        for(Object arg: args) {
            returnStr += arg.toString();
            returnStr += ",";

        }
        return returnStr;
    }

    public String getCommand() {
        return this.args.get(0).toString();
    }

    public Object splitCommand(int arg) {
        return this.args.get(arg);
    }
}
