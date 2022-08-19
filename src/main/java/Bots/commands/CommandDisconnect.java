package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import java.util.ArrayList;

import static Bots.Main.IsDJ;
import static Bots.Main.createQuickEmbed;

public class CommandDisconnect extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel().asTextChannel(), event.getMember())) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You are not a DJ.")).queue();
            return;
        }
        if (!event.getGuild().getAudioManager().isConnected()){
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "I am not in a voice channel.")).queue();
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        musicManager.scheduler.queue.clear();
        event.getGuild().getAudioManager().closeAudioConnection();
        musicManager.scheduler.nextTrack();
        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Disconnected from the voice channel and cleared the queue.")).queue();

    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("leave");
        list.add("fu" + "ckoff"); // using this because I don't want to trigger any sort of profanity filters on other systems.
        list.add("fu" + "ck off");
        list.add("shutup");
        list.add("dc");
        list.add("stop");
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

    @Override
    public long getTimeout() {
        return 5000;
    }
}
