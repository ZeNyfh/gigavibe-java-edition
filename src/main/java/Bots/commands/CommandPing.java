package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;

import static java.lang.System.currentTimeMillis;

public class CommandPing implements ICommand {

    @Override
    public void execute(ExecuteArgs event) {
        long time = currentTimeMillis();
        event.getTextChannel().sendMessage(".").queue(response -> response.editMessageFormat("ping: %dms", currentTimeMillis() - time).queue());
    }

    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public String helpMessage() {
        return "Shows you the bot's ping.";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
