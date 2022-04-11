package Bots.commands;

import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.toSimpleTimestamp;

public class CommandNowPlaying implements ICommand {

    @Override
    public void execute(ExecuteArgs event) {
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Im not in a vc.")).queue();
            return;
        }

        final Member member = event.getMember();
        final GuildVoiceState memberVoiceState = event.getMemberVoiceState();

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

        long totalTime = audioPlayer.getPlayingTrack().getDuration();
        long trackPos = audioPlayer.getPlayingTrack().getPosition();
        int trackLocation = Math.toIntExact(Math.round(((double) totalTime - trackPos) / totalTime * 20d)); //WHY DOES (double) MATTER -9382
        String barText = new String(new char[20 - trackLocation]).replace("\0", "━") + "\uD83D\uDD18" + new String(new char[trackLocation]).replace("\0", "━");
        EmbedBuilder embed = new EmbedBuilder();
        if (audioPlayer.getPlayingTrack().getInfo().uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
            embed.setTitle((audioPlayer.getPlayingTrack().getInfo().uri).replace(System.getProperty("user.dir") + "\\temp\\music\\", ""));
            embed.setDescription("```" + barText + " " + toSimpleTimestamp(trackPos) + " / " + toSimpleTimestamp(totalTime) + "```");
        } else {
            embed.setTitle((audioPlayer.getPlayingTrack().getInfo().title), (audioPlayer.getPlayingTrack().getInfo().uri));
            embed.setDescription("```" + barText + " " + toSimpleTimestamp(trackPos) + " / " + toSimpleTimestamp(totalTime) + "```\n" + "**Channel:** \n" + (audioPlayer.getPlayingTrack().getInfo().author));
        }
        embed.setColor(new Color(0, 0, 255));
        channel.sendMessageEmbeds(embed.build()).queue();
    }

    @Override
    public String getName() {
        return "np";
    }

    @Override
    public String helpMessage() {
        return "Shows you the track that is currently playing";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
