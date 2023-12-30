package Bots.commands;

import Bots.BaseCommand;
import Bots.Main;
import Bots.MessageEvent;

import static Bots.Main.createQuickEmbed;

public class CommandTOS extends BaseCommand {

    @Override
    public void execute(MessageEvent event) throws Exception {
        event.replyEmbeds(createQuickEmbed("Terms of Service", "https://github.com/ZeNyfh/gigavibe-java-edition/blob/main/TOS.md"));
    }

    @Override
    public String[] getNames() {
        return new String[]{"tos", "terms of service", "termsofservice"};
    }

    @Override
    public String getCategory() {
        return "General";
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
