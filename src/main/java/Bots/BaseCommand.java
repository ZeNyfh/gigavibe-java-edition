package Bots;

import java.io.IOException;

/**
 * Custom base command class used by all commands
 */
public class BaseCommand {

    public void execute(MessageEvent event) throws IOException {
    }

    public String[] getNames() {
        return new String[]{"default"};
    } //The first name in the list is treated as the primary name

    public String getCategory() {
        return "default";
    }

    public String getDescription() {
        return "default";
    }

    public long getTimeout() {
        return 0;
    }

    public String getParams() {
        return "";
    } //E.g. "<url> [format]"

}
