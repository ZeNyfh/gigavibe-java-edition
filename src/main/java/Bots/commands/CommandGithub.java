package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;

import static Bots.Main.createQuickEmbed;

public class CommandGithub extends BaseCommand {
    @Override
    public void execute(CommandEvent event) {
        event.replyEmbeds(createQuickEmbed(" ", "❕ Use this for bug reports and feature requests ONLY.\n\n❕ When making an issue, make sure to specify what the bug is and how to recreate it.\n\nhttps://github.com/ZeNyfh/gigavibe-java-edition"));
    }

    @Override
    public Category getCategory() {
        return Category.General;
    }

    @Override
    public String[] getNames() {
        return new String[]{"github"};
    }

    @Override
    public String getDescription() {
        return "Sends github URL and some info.";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
