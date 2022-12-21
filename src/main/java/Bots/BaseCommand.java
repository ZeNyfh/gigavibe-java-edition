package Bots;

import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;

/**
 * Custom base command class used by all commands
 * @author 9382
 * @version 1.1
 */
public interface BaseCommand {

    void execute(MessageEvent event) throws IOException; //The main event loop

    default void Init() { //Optional initialisation stuff if something is required on start for a command
    }

    String[] getNames(); //The first name in the list is treated as the primary name by cmds

    String getCategory(); //The category, used by cmds

    String getDescription(); //The description, used by cmds

    default long getRatelimit() { //Ratelimit in milliseconds
        return 0;
    }

    default OptionData[] getOptions() { //Options of the command
        return new OptionData[]{};
    }
}
