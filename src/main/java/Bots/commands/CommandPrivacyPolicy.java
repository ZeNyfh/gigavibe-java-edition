package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.executor;

public class CommandPrivacyPolicy extends BaseCommand implements Runnable {
    private static MessageEvent event;

    @Override
    public void run() {
        event.replyEmbeds(createQuickEmbed("Privacy Policy", "https://github.com/ZeNyfh/gigavibe-java-edition/blob/main/PRIVACY_POLICY.md"));
    }

    @Override
    public String[] getNames() {
        return new String[]{"privacypolicy", "privacy policy", "pp"};
    }

    @Override
    public Category getCategory() {
        return Category.General;
    }

    @Override
    public String getDescription() {
        return "Sends a link to the privacy policy.";
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }

    @Override
    public void execute(MessageEvent e) throws InterruptedException {
        event = e;
        executor.submit(new CommandPrivacyPolicy());

    }
}
