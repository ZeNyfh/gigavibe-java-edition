package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;

import static Bots.CommandEvent.localise;
import static Bots.Main.createQuickEmbed;

public class CommandTOS extends BaseCommand {

    @Override
    public void execute(CommandEvent event) {
        event.replyEmbeds(createQuickEmbed(localise("CommandTOS.TermsOfService"), "https://github.com/ZeNyfh/gigavibe-java-edition/blob/main/TOS.md"));
    }

    @Override
    public String[] getNames() {
        return new String[]{"tos", "terms of service", "termsofservice"};
    }

    @Override
    public Category getCategory() {
        return Category.General;
    }

    @Override
    public String getDescription() {
        return "Sends a link to the terms of service.";
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }
}
