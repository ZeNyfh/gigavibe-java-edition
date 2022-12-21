package Bots;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

import java.io.IOException;

/**
 * Custom base command class used by all commands
 */
public class BaseCommand {

    public void execute(MessageEvent event) throws IOException { //Execution code ran for users
        System.out.println("Default command..?");
    }

    public void executeSlash(SlashCommandInteractionEvent slashEvent) throws IOException {
        System.out.println("Default slash command..?");
    }
    public boolean registerSlash() {
        return false;
    }

    public void Init() { //Optional initialisation stuff if something is required on start but doesn't have to be in main
    }

    public String[] getNames() { //The first name in the list is treated as the primary name by cmds
        return new String[]{"default"};
    }

    public String getCategory() { //The category, used by cmds
        return "default";
    }

    public String getDescription() { //The description, used by cmds
        return "default";
    }

    public long getRatelimit() { //Ratelimit in milliseconds
        return 0;
    }

    public String getParams() { //E.g. "<url> [format]". Used by cmds
        return "";
    }
}
