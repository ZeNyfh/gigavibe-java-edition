package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Bots.Main.createQuickEmbed;
public class CommandInvite extends BaseCommand implements Runnable {
    private static MessageEvent event;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    @Override
    public void run() {
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
    public String getDescription() {
        return "Sends an invite to the bot.";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }

    @Override
    public void execute(MessageEvent e) throws InterruptedException {
        event = e;
        executor.submit(new CommandInvite());

    }
}
