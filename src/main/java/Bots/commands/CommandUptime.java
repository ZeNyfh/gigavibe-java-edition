package Bots.commands;

import Bots.Main;
import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.toTimestamp;
import static java.lang.System.currentTimeMillis;

public class CommandUptime implements ICommand {
    @Override
    public void execute(ExecuteArgs event) {
        long finalUptime = currentTimeMillis() - Main.Uptime;
        String finalTime = toTimestamp(finalUptime);
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "⏰ uptime: " + finalTime)).queue();
    }

    public String getCategory() {
        return "General";
    }

    @Override
    public String getName() {
        return "uptime";
    }

    @Override
    public String helpMessage() {
        return "Returns the bot's uptime.";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
