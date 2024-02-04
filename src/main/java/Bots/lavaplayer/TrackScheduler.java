package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static Bots.Main.*;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public TrackEndEvent event;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track) {
        if (!this.player.startTrack(track, true)) {
            this.queue.offer(track);
        }
    }

    public void nextTrack() {
        this.player.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        GuildMessageChannelUnion userData = (GuildMessageChannelUnion) track.getUserData();
        clearVotes(userData.getGuild().getIdLong());
        if (LoopGuilds.contains(userData.getGuild().getId())) {
            if (endReason.mayStartNext) {
                AudioTrack loopTrack = track.makeClone();
                this.player.startTrack(loopTrack, false);
                int value = trackLoops.get(userData.getGuild().getIdLong()) + 1; // it had to be a variable, it hated me just adding 1 in the put
                trackLoops.put(userData.getGuild().getIdLong(), value);
                return;
            }
        }
        if (LoopQueueGuilds.contains(userData.getGuild().getId())) {
            if (endReason.mayStartNext) {
                AudioTrack loopTrack = track.makeClone();
                nextTrack();
                queue(loopTrack);
                return;
            }
        }
        if (AutoplayGuilds.contains(userData.getGuild().getIdLong())) {
            if (endReason.mayStartNext) {
                if (!track.getInfo().uri.toLowerCase().contains("youtube")) {
                    userData.sendMessageEmbeds(createQuickError("Autoplay is on, but the track is not supported by autoplay!\n\nUse **" + botPrefix + " autoplay** to stop autoplay.")).queue();
                    return;
                }
                String trackId = track.getInfo().identifier;
                String radioUrl = "https://www.youtube.com/watch?v=" + trackId + "&list=" + "RD" + trackId;
                PlayerManager.getInstance().loadAndPlay((GuildMessageChannelUnion) userData, radioUrl, true);
                return;
            }
        }
        trackLoops.put(userData.getGuild().getIdLong(), 0);
        AudioTrack nextTrack = null;
        if (endReason.mayStartNext) {
            nextTrack();
            nextTrack = player.getPlayingTrack();
            EmbedBuilder eb = new EmbedBuilder();
            if (!nextTrack.getInfo().title.isEmpty()) {
                eb.setTitle("Now playing: " + nextTrack.getInfo().title, nextTrack.getInfo().uri);
            } else {
                eb.setTitle("Now playing: " + nextTrack.getInfo().uri);
            }
            if (nextTrack.getInfo().length <= 432000000) {
                eb.setDescription("**Channel**\n" + nextTrack.getInfo().author);
                eb.addField("**Duration**\n", toSimpleTimestamp(nextTrack.getInfo().length), true);
            } else {
                eb.setDescription("**Channel**\n" + nextTrack.getInfo().author);
                eb.addField("**Duration**\n", "Unknown", true);
            }
            eb.setThumbnail(PlayerManager.getInstance().getThumbURL(nextTrack));
            eb.setColor(botColour);
            userData.sendMessageEmbeds(eb.build()).queue();
            return;
        }
        if (endReason.name().equals("REPLACED") || endReason.name().equals("FINISHED")) {
            return;
        }
        assert false;
        onTrackStuck(nextTrack);
    }

    private void onTrackStuck(AudioTrack nextTrack) {
        GuildMessageChannelUnion userData = (GuildMessageChannelUnion) nextTrack.getUserData();
        clearVotes(userData.getGuild().getIdLong());
        userData.sendMessageEmbeds(createQuickError("Track got stuck, skipping.")).queue();
        nextTrack();
    }
}
