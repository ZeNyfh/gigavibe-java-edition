package Bots;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.io.IOException;

/**
 * Custom base command class used by all commands
 *
 * @author 9382
 * @version 1.2
 */
public abstract class BaseCommand {
    SlashCommandData slashCommand;

    public void execute(MessageEvent event) throws IOException { //The main event loop
    }

    public void Init() { //Optional initialisation stuff if something is required on start for a command
    }

    public abstract String getOptions();

    public void ProvideOptions(SlashCommandData slashCommand) { //Provides options for a slash command
    }

    public String[] getNames() {
        return new String[]{"<unset>"};
    } //The first name in the list is treated as the primary name by cmds

    public String getCategory() {//The category, used by cmds
        return "<unset>";
    }

    public String getDescription() {//The description, used by cmds
        return "<unset>";
    }

    public long getRatelimit() { //Ratelimit in milliseconds
        return 0;
    }
}
