package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static java.lang.System.currentTimeMillis;

public class CommandPing extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        long time = currentTimeMillis();
        event.reply(response -> response.editMessageFormat("ping: %dms", currentTimeMillis() - time), ".");
    }

    @Override
    public Category getCategory() {
        return Category.General;
    }

    @Override
    public String[] getNames() {
        return new String[]{"ping"};
    }

    @Override
    public String getDescription() {
        return "Shows you the bot's ping.";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
