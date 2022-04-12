package Bots.commands;

import Bots.BaseCommand;
import Bots.Main;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.toTimestamp;
import static java.lang.System.currentTimeMillis;

public class CommandUptime implements BaseCommand {
    public void execute(MessageReceivedEvent event) {
        long finalUptime = currentTimeMillis() - Main.Uptime;
        String finalTime = toTimestamp(finalUptime);
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "⏰ uptime: " + finalTime)).queue();
    }

    public String getCategory() {
        return "General";
    }

    public String getName() {
        return "uptime";
    }

    public String getDescription() {
        return "Returns the bot's uptime.";
    }
}
