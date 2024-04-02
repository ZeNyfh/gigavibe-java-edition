package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.LRCLIBManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.utils.FileUpload;

import java.nio.charset.StandardCharsets;

import static Bots.Main.*;

public class CommandLyrics extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("Im not in a vc."));
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            event.replyEmbeds(createQuickError("No tracks are playing right now."));
            return;
        }

        String lyrics = LRCLIBManager.getLyrics(audioPlayer.getPlayingTrack()).trim();
        if (lyrics.isEmpty()) {
            event.replyEmbeds(createQuickError("No results found or the song title was unknown."));
            return;
        }
        EmbedBuilder builder = new EmbedBuilder().setColor(botColour).setFooter("Lyrics sourced from lrclib.net");
        String title = "Lyrics for: " + audioPlayer.getPlayingTrack().getInfo().title;
        if (title.length() > 256) {
            title = title.substring(0, 253) + "...";
        }
        if (lyrics.length() <= 2000) {
            builder.setDescription(lyrics);
            builder.setTitle(title);
            event.replyEmbeds(builder.build());
        } else {
            builder.setDescription("Lyrics were too long, uploading them as a file.");
            event.replyEmbeds(builder.build());
            event.getChannel().sendFiles(FileUpload.fromData(lyrics.getBytes(StandardCharsets.UTF_8), title+".txt")).queue();
        }
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String[] getNames() {
        return new String[]{"lyrics"};
    }

    @Override
    public String getDescription() {
        return "Gets the lyrics from the current song.";
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }
}