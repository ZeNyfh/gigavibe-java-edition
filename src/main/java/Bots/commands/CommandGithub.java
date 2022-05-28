package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static Bots.Main.createQuickEmbed;


public class CommandGithub extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "❕ Use this for bug reports and feature requests ONLY.\n\n❕ When making an issue, make sure to specify what the bug is and how to recreate it.\n\nhttps://github.com/ZeNyfh/gigavibe-java-edition")).queue();
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String getName() {
        return "github";
    }

    @Override
    public String getDescription() {
        return "Sends github URL and some info.";
    }
}
