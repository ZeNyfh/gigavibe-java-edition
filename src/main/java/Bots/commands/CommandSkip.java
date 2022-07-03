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

import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.*;

public class CommandSkip extends BaseCommand {

    public void execute(MessageEvent event) {
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **error**", "Im not in a vc.")).queue();
            return;
        }
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in a voice channel to use this command.")).queue();
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in the same voice channel to use this command.")).queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing right now.")).queue();
        } else {
            musicManager.scheduler.nextTrack();
            if (musicManager.audioPlayer.getPlayingTrack() == null) { // if there is nothing playing after the skip command
                channel.sendMessageEmbeds(createQuickEmbed(" ", "⏩ Skipped the track.")).queue();
            } else { // if there is something playing after the skip command
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(botColour);
                if (musicManager.audioPlayer.getPlayingTrack().getInfo().title != null) {
                    eb.setTitle("⏩ Skipped the track to __**" + musicManager.audioPlayer.getPlayingTrack().getInfo().title + "**__", musicManager.audioPlayer.getPlayingTrack().getInfo().uri);
                } else {
                    eb.setTitle("⏩ Skipped the track to __**Unknown Title**__", musicManager.audioPlayer.getPlayingTrack().getInfo().uri);
                    eb.appendDescription("**Now playing:**" + musicManager.audioPlayer.getPlayingTrack().getInfo().uri + "\n\n");
                }
                if (musicManager.audioPlayer.getPlayingTrack().getInfo().author != null) {
                    eb.appendDescription("**Channel**\n" + musicManager.audioPlayer.getPlayingTrack().getInfo().author + "\n");
                }
                eb.appendDescription("**Duration**\n" + toSimpleTimestamp(musicManager.audioPlayer.getPlayingTrack().getInfo().length));
                event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
            }
        }
    }

    public String getCategory() {
        return "Music";
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
        list.add("s");
        return list;
    }

    public String getName() {
        return "skip";
    }

    public String getDescription() {
        return "Casts a vote or skips the current song.";
    } // voting not yet implemented

    public long getTimeout() {
        return 5000;
    }
}