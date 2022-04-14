package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import static Bots.Main.createQuickEmbed;

public class CommandDisconnect implements BaseCommand {
    @Override
    public void execute(MessageReceivedEvent event) {
        event.getGuild().getAudioManager().closeAudioConnection();
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Disconnected from the voice channel and cleared the queue.")).queue();

    } // this will also need to be checked with DJ permissions in the future

    @Override
    public String getName() {
        return "disconnect";
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getDescription() {
        return "Makes the bot forcefully leave the vc.";
    }
}
