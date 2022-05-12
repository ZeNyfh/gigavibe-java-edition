package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import java.util.ArrayList;

import static Bots.Main.createQuickEmbed;

public class CommandDisconnect extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        musicManager.scheduler.queue.clear();
        event.getGuild().getAudioManager().closeAudioConnection();
        musicManager.scheduler.nextTrack();
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Disconnected from the voice channel and cleared the queue.")).queue();

    } // this will also need to be checked with DJ permissions in the future

    @Override
    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
        list.add("leave");
        return list;
    }

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
