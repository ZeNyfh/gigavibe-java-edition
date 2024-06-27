package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.lang.System.currentTimeMillis;
public class CommandPing extends BaseCommand implements Runnable {
    private static MessageEvent event;
    private static final ExecutorService executor = Executors.newCachedThreadPool();

    @Override
    public void run() {
        long time = currentTimeMillis();
        event.reply(response -> response.editMessageFormat("ping: %dms", currentTimeMillis() - time), ".");
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

    @Override
    public void execute(MessageEvent e) throws InterruptedException {
        event = e;
        executor.submit(new CommandPing());
    }
}