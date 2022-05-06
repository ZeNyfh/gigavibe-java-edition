package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import static java.lang.System.currentTimeMillis;

public class CommandPing extends BaseCommand {

    public void execute(MessageReceivedEvent event) {
        long time = currentTimeMillis();
        event.getTextChannel().sendMessage(".").queue(response -> response.editMessageFormat("ping: %dms", currentTimeMillis() - time).queue());
    }

    public String getCategory() {
        return "General";
    }

    public String getName() {
        return "ping";
    }

    public String getDescription() {
        return "- Shows you the bot's ping.";
    }
}
