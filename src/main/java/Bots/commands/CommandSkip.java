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
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;
import static java.lang.Math.round;

public class CommandSkip extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        final TextChannel channel = event.getChannel().asTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickError("Im not in a vc.")).queue();
            return;
        }
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickError("You need to be in a voice channel to use this command.")).queue();
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            channel.sendMessageEmbeds(createQuickError("You need to be in the same voice channel to use this command.")).queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(createQuickError("No tracks are playing right now.")).queue();
        } else {
            ArrayList<Member> VCMembers = new ArrayList<>(); // the current members of the voice channel.
            for (int i = 0; i < Objects.requireNonNull(selfVoiceState.getChannel()).getMembers().size(); ) {
                if (!selfVoiceState.getChannel().getMembers().get(i).getUser().isBot()) {
                    VCMembers.add(selfVoiceState.getChannel().getMembers().get(i));
                }
                i++;
            }
            try {
                if (getVotes(event.getGuild().getIdLong()).contains(event.getMember()) && getVotes(event.getGuild().getIdLong()).size() < VCMembers.size() / 2) {
                    channel.sendMessageEmbeds(createQuickError("You have already voted to skip.")).queue();
                    return;
                }
            } catch (Exception ignored) {
            }
            List<Member> currentVotes = getVotes(event.getGuild().getIdLong());
            if (getVotes(event.getGuild().getIdLong()) != null) {
                currentVotes.add(event.getMember());
            } else {
                currentVotes = new ArrayList<>();
                currentVotes.add(event.getMember());
            }
            addToVote(event.getGuild().getIdLong(), currentVotes); // add the member to the votes
            if (getVotes(event.getGuild().getIdLong()).size() >= VCMembers.size() / 2) {
                addToVote(event.getGuild().getIdLong(), new ArrayList<>()); // clearing the votes
                assert event.getArgs()[1] != null;
//                if (event.getArgs()[1].matches("^\\d+$")){
//                    for (int i = 0; i < Integer.parseInt(event.getArgs()[1]); i++) {
//                        musicManager.scheduler.nextTrack();
//                        //event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("","⏩ Skipped " + event.getArgs()[1] + " tracks.")).queue();
//                    }
//                } else {
                musicManager.scheduler.nextTrack();
                //}
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
                    event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();
                }
            } else {
                channel.sendMessageEmbeds(createQuickEmbed("✅ Voted to skip the track", getVotes(event.getGuild().getIdLong()).size() + " of " + round(VCMembers.size() / 2) + " needed to skip.")).queue();
            }
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"skip", "s", "voteskip", "vs"};
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getParams() {
        return "[Integer]";
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