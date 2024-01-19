package Bots;

import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

/**
 * Custom base command class used by all commands
 *
 * @author 9382
 * @version 1.2
 */
public abstract class BaseCommand {
    public SlashCommandData slashCommand;
    public static enum Categories {
        General, Admin, DJ, Dev, Music
    }

    public abstract void execute(MessageEvent event) throws Exception; //The main event loop

    public void Init() { //Optional initialisation stuff if something is required on start for a command
    }

    public abstract String[] getNames(); //The first name in the list is treated as the primary name by cmds

    public abstract String getCategory(); //The category, used by cmds

    public abstract String getDescription(); //The description, used by cmds

    public String getOptions() {
        return "";
    } //The options text used by help / cmds. Purely decorative

    public void ProvideOptions(SlashCommandData slashCommand) { //Provides options for a slash command
    }

    public long getRatelimit() { //Ratelimit in milliseconds
        return 0;
    }
}
