package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import core.GLA;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;

public class CommandLyrics extends BaseCommand {

    public void execute(MessageEvent event) {
        if (Objects.requireNonNull(event.getMember()).getIdLong() != 211789389401948160L) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You dont have the permission to run this command.")).queue();
            return;
        }
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Im not in a vc.")).queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing right now.")).queue();
            return;
        }

        String title = audioPlayer.getPlayingTrack().getInfo().title;
        GLA gla = new GLA();

        try {
            String lyrics = gla.search(title.toLowerCase().replaceAll("official", "").replaceAll("music", "").replaceAll("video", "")).getHits().getFirst().fetchLyrics();
            if (lyrics.isEmpty() || lyrics.isBlank()) {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No results found.")).queue();
                return;
            }
            byte[] finalLyrics = lyrics.getBytes();
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", title)).addFile(finalLyrics, "lyrics.txt").queue();
        } catch (Exception e) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No results found.")).queue();
        }
    }

    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("lyr");
        list.add("lyric");
        return list;
    }

    public String getCategory() {
        return "Dev";
    }

    public String getName() {
        return "lyrics";
    }

    public String getDescription() {
        return "Gets the lyrics from the current song.";
    }

    public long getTimeout() {
        return 5000;
    }
}