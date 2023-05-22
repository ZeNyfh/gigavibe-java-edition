package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;

import static Bots.Main.*;

public class CommandNowPlaying extends BaseCommand {

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
        EmbedBuilder embed = new EmbedBuilder();
        long trackPos = audioPlayer.getPlayingTrack().getPosition();
        long totalTime = audioPlayer.getPlayingTrack().getDuration();
        String totalTimeText;
        if (totalTime > 432000000) { // 5 days
            totalTimeText = "Unknown"; //Assume malformed
        } else {
            totalTimeText = toSimpleTimestamp(totalTime);
        }
        int trackLocation = Math.toIntExact(Math.round(((double) totalTime - trackPos) / totalTime * 20d)); //WHY DOES (double) MATTER -9382
        String barText = "";
        try {
            barText = new String(new char[20 - trackLocation]).replace("\0", "━") + "\uD83D\uDD18" + new String(new char[trackLocation]).replace("\0", "━");
        } catch (Exception ignored) {
        }
        embed.setThumbnail("https://img.youtube.com/vi/" + audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
        try {
            embed.setTitle((audioPlayer.getPlayingTrack().getInfo().title), (audioPlayer.getPlayingTrack().getInfo().uri));
            if (audioPlayer.getPlayingTrack().getInfo().isStream) {
                if (getStreamTitle(audioPlayer.getPlayingTrack().getInfo().uri) != null) {
                    embed.setTitle(getStreamTitle(audioPlayer.getPlayingTrack().getInfo().uri), (audioPlayer.getPlayingTrack().getInfo().uri));
                }
            }
        } catch (Exception ignored) {
            embed.setTitle("Unknown");
        }
        embed.setDescription("```" + barText + " " + toSimpleTimestamp(trackPos) + " / " + totalTimeText + "```");
        embed.addField("\uD83D\uDC64 Channel:", audioPlayer.getPlayingTrack().getInfo().author, true);
        if (getTrackFromQueue(event.getGuild(), 0) != null) {
            embed.setThumbnail("https://img.youtube.com/vi/" + audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
            embed.addField("▶️ Up next:", "[" + Objects.requireNonNull(getTrackFromQueue(event.getGuild(), 0)).getInfo().title + "](" + Objects.requireNonNull(getTrackFromQueue(event.getGuild(), 0)).getInfo().uri + ")", true);
        } else {
            embed.addField(" ", " ", true);
        }
        embed.addField(" ", " ", true);
        if (LoopGuilds.contains(event.getGuild().getId())) {
            embed.addField("\uD83D\uDD02 Track looping:", "✅ **True**", true);
            embed.setFooter("Loop Count: " + trackLoops.get(event.getGuild().getIdLong()));
        } else {
            embed.addField("\uD83D\uDD02 Track looping:", "❌ **False**", true);
        }
        if (LoopQueueGuilds.contains(event.getGuild().getId())) {
            embed.addField("\uD83D\uDD01 Queue looping:", "✅ **True**", true);
        } else {
            embed.addField("\uD83D\uDD01 Queue looping:", "❌ **False**", true);
        }
        embed.addField(" ", " ", true);
        embed.setColor(botColour);
        event.replyEmbeds(embed.build());
    }

    private static String getStreamTitle(String streamUrl) {
        String[] cmd = {"ffprobe", "-v", "quiet", "-show_entries", "format_tags=StreamTitle", "-of", "default=noprint_wrappers=1:nokey=1", streamUrl};
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            cmd = new String[]{"modules/ffprobe.exe", "-v", "quiet", "-show_entries", "format_tags=StreamTitle", "-of", "default=noprint_wrappers=1:nokey=1", streamUrl};
        }
        String streamTitle = null;
        try {
            Process process = new ProcessBuilder(cmd).start();
            BufferedReader ffprobeInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
            streamTitle = ffprobeInput.readLine();
        } catch (Exception e) {
            e.printStackTrace();
            streamTitle = "Unknown";
        }
        if (Objects.equals(streamTitle, "")) {
            streamTitle = "Unknown";
        }
        if (streamTitle != null) {
            if (streamTitle.length() > 70) {
                streamTitle = streamTitle.substring(0, 70) + "...";
            }
        }
        return streamTitle;
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String[] getNames() {
        return new String[]{"np", "nowplaying"};
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Shows you the track that is currently playing";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
