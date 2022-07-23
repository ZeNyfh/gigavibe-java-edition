package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static Bots.Main.createQuickEmbed;


public class CommandInvite extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed("**Have fun!**", "https://discord.com/api/oauth2/authorize?client_id=920435768726532107&permissions=412689493104&scope=bot")).queue();
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String getName() {
        return "invite";
    }

    @Override
    public String getDescription() {
        return "Sends an invite for the bot.";
    }

    @Override
    public long getTimeout() {
        return 5000;
    }
}
