package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static java.lang.System.currentTimeMillis;

public class CommandPing extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        long time = currentTimeMillis();
        event.getChannel().asTextChannel().sendMessage(".").queue(response -> response.editMessageFormat("ping: %dms", currentTimeMillis() - time).queue());
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String[] getNames() {
        return new String[]{"ping"};
    }

    @Override
    public String getDescription() {
        return "Shows you the bot's ping.";
    }
}
