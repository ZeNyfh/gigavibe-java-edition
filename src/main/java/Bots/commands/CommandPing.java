package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static java.lang.System.currentTimeMillis;

public class CommandPing extends BaseCommand {

    public void execute(MessageEvent event) {
        long time = currentTimeMillis();
        event.getChannel().asTextChannel().sendMessage(".").queue(response -> response.editMessageFormat("ping: %dms", currentTimeMillis() - time).queue());
    }

    public String getCategory() {
        return "General";
    }

    public String getName() {
        return "ping";
    }

    public String getDescription() {
        return "Shows you the bot's ping.";
    }

    public long getTimeout() {
        return 5000;
    }
}
