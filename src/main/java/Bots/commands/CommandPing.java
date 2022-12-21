package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.entities.Message;

import static java.lang.System.currentTimeMillis;

public class CommandPing implements BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        long time = currentTimeMillis();
        event.reply(response -> ((Message) response).editMessageFormat("ping: %dms", currentTimeMillis() - time).queue(), ".");
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

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
