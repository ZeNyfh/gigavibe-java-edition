package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static Bots.Main.createQuickEmbed;

public class CommandInvite extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        event.replyEmbeds(createQuickEmbed("**Have fun!**", "http://zenvibe.ddns.net/"));
    }

    @Override
    public Category getCategory() {
        return Category.General;
    }

    @Override
    public String[] getNames() {
        return new String[]{"invite"};
    }

    @Override
    public String getOptions() {
        return "";
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
