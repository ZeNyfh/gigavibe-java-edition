package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandSkip extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("Im not in a vc."));
            return;
        }
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("You need to be in a voice channel to use this command."));
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.replyEmbeds(createQuickError("You need to be in the same voice channel to use this command."));
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        if (audioPlayer.getPlayingTrack() == null) {
            event.replyEmbeds(createQuickError("No tracks are playing right now."));
            return;
        }

        List<Member> VCMembers = new ArrayList<>(); //Filter to remove bots
        List<Member> UnfilteredMembers = Objects.requireNonNull(selfVoiceState.getChannel()).getMembers();
        for (Member member : UnfilteredMembers) {
            if (!member.getUser().isBot()) {
                VCMembers.add(member);
            }
        }
        List<Member> currentVotes = getVotes(event.getGuild().getIdLong());
        if (currentVotes.contains(event.getMember())) {
            event.replyEmbeds(createQuickError("You have already voted to skip."));
            return;
        }
        currentVotes.add(event.getMember());
        if (currentVotes.size() >= VCMembers.size() / 2) {
            clearVotes(event.getGuild().getIdLong());
            if (AutoplayGuilds.contains(event.getGuild().getIdLong())) {
                if (!audioPlayer.getPlayingTrack().getInfo().uri.toLowerCase().contains("youtube")) {
                    event.replyEmbeds(createQuickError("Autoplay is on, but the track is not supported by autoplay!\n\nUse **" + botPrefix + " autoplay** to stop autoplay."));
                }
                String trackId = audioPlayer.getPlayingTrack().getInfo().identifier;
                String radioUrl = "https://www.youtube.com/watch?v=" + trackId + "&list=" + "RD" + trackId;
                PlayerManager.getInstance().loadAndPlay(event.getChannel(), radioUrl, true);
            }
            musicManager.scheduler.nextTrack();
            if (musicManager.audioPlayer.getPlayingTrack() == null) { // if there is nothing playing after the skip command
                event.replyEmbeds(createQuickEmbed(" ", "⏩ Skipped the track."));
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
                event.replyEmbeds(eb.build());
            }
        } else {
            event.replyEmbeds(createQuickEmbed("✅ Voted to skip the track", currentVotes.size() + " of " + VCMembers.size() / 2 + " needed to skip."));
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"skip", "s", "voteskip", "vs"};
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String getDescription() {
        return "Casts a vote or skips the current song.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}