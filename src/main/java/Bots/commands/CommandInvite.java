package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static Bots.Main.createQuickEmbed;

public class CommandInvite implements BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("**Have fun!**", "http://bot.zenyfh.co.uk/")).queue();
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String[] getNames() {
        return new String[]{"invite"};
    }

    @Override
    public String getDescription() {
        return "Sends an invite to the bot.";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
