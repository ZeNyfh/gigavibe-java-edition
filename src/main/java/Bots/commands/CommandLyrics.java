package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.LRCLIBManager;
import Bots.lavaplayer.PlayerManager;
import Bots.lavaplayer.RadioDataFetcher;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.FileUpload;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static Bots.Main.botColour;
import static Bots.Main.createQuickError;

public class CommandLyrics extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_BOT_IN_ANY_VC, Check.IS_PLAYING};
    }

    @Override
    public void execute(MessageEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        String lyrics = LRCLIBManager.getLyrics(audioPlayer.getPlayingTrack()).trim();
        if (lyrics.isEmpty()) {
            event.replyEmbeds(createQuickError("No results found or the song title was unknown."));
            return;
        }
        EmbedBuilder builder = new EmbedBuilder().setColor(botColour).setFooter("Lyrics sourced from lrclib.net");
        String title = audioPlayer.getPlayingTrack().getInfo().title;
        if (audioPlayer.getPlayingTrack().getInfo().isStream && Objects.equals(audioPlayer.getPlayingTrack().getSourceManager().getSourceName(), "http")) {
            title = RadioDataFetcher.getStreamSongNow(audioPlayer.getPlayingTrack().getInfo().uri);
        }

        title = "Lyrics for: " + title;
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
            event.getChannel().sendFiles(FileUpload.fromData(lyrics.getBytes(StandardCharsets.UTF_8), title + ".txt")).queue();
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