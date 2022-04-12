package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandHelp implements BaseCommand {
    public void execute(MessageReceivedEvent event) {
        event.getTextChannel().sendMessage("literally only the help command rn").queue(); // will be worked on in the future
    }

    public String getCategory() {
        return "General";
    }

    public String getName() {
        return "help";
    }

    public String getDescription() {
        return "Shows you a list of commands.";
    }
}
