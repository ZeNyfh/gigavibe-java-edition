package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;

import static Bots.CommandEvent.localise;
import static java.lang.System.currentTimeMillis;

public class CommandPing extends BaseCommand {

    @Override
    public void execute(CommandEvent event) {
        long time = currentTimeMillis();
        event.reply(response -> response.editMessageFormat(localise("ping: {num}ms","CmdPing.ping", currentTimeMillis() - time) + "\n" + localise("gateway ping: {num}ms","CmdPing.gatewayPing", event.getJDA().getGatewayPing())), ".");
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
        return 1000;
    }
}
