package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.executor;

public class CommandTOS extends BaseCommand implements Runnable {
    private static MessageEvent event;

    @Override
    public void run() {
        event.replyEmbeds(createQuickEmbed("Terms of Service", "https://github.com/ZeNyfh/gigavibe-java-edition/blob/main/TOS.md"));
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

    @Override
    public void execute(MessageEvent e) throws InterruptedException {
        event = e;
        executor.submit(new CommandTOS());

    }
}
