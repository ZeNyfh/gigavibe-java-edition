package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;

import static Bots.Main.createQuickEmbed;

public class CommandGithub extends BaseCommand {
    @Override
    public void execute(CommandEvent event) {
        event.replyEmbeds(createQuickEmbed(" ", String.format(event.getLocaleString("CommandGithub.message"), "❕ ", "\n\n❕ ", "\n\n")));
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
