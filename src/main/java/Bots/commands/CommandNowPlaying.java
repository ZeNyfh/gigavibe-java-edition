package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Objects;

import static Bots.Main.*;

public class CommandNowPlaying extends BaseCommand {

    public void execute(MessageEvent event) {
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Im not in a vc.")).queue();
            return;
        }

        final GuildVoiceState memberVoiceState = event.getMember().getVoiceState();

        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in a voice channel to use this command.")).queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(createQuickEmbed(" ", "No tracks are playing right now.")).queue(); // not an error, intended
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
        if (trackLocation > 20 || trackLocation < 0) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The track duration is broken")).queue();
            return;
        }
        String barText = new String(new char[20 - trackLocation]).replace("\0", "━") + "\uD83D\uDD18" + new String(new char[trackLocation]).replace("\0", "━");
        if (audioPlayer.getPlayingTrack().getInfo().uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
            embed.setTitle((audioPlayer.getPlayingTrack().getInfo().uri).replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13));
            embed.setDescription("```" + barText + " " + toSimpleTimestamp(trackPos) + " / " + totalTimeText + "```");
        } else {
            embed.setThumbnail("https://img.youtube.com/vi/" + audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
            try {
                embed.setTitle((audioPlayer.getPlayingTrack().getInfo().title), (audioPlayer.getPlayingTrack().getInfo().uri));
            } catch (Exception ignored) {
                embed.setTitle("Unknown");
            }
            embed.setDescription("```" + barText + " " + toSimpleTimestamp(trackPos) + " / " + totalTimeText + "```\n" + "**Channel:**\n" + audioPlayer.getPlayingTrack().getInfo().author);
        }
        if (getTrackFromQueue(event.getGuild(), 0) != null) {
            if (Objects.requireNonNull(getTrackFromQueue(event.getGuild(), 0)).getInfo().uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
                embed.addField("**Up next:**\n", Objects.requireNonNull(getTrackFromQueue(event.getGuild(), 0)).getInfo().uri.replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13), true);
            } else {
                embed.setThumbnail("https://img.youtube.com/vi/" + audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
                embed.addField("**Up next:**\n", "[" + Objects.requireNonNull(getTrackFromQueue(event.getGuild(), 0)).getInfo().title + "](" + Objects.requireNonNull(getTrackFromQueue(event.getGuild(), 0)).getInfo().uri + ")", true);
            }
        }
        embed.setColor(new Color(0, 0, 255));
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    public String getCategory() {
        return "Music";
    }

    public String getName() {
        return "np";
    }

    public String getDescription() {
        return "Shows you the track that is currently playing";
    }
}
