package Bots.lavaplayer;

import Bots.MessageEvent;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static Bots.Main.*;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public TrackEndEvent event;
    private final HashMap<Long, Integer> guildFailCount = new HashMap<>();

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
        MessageEvent originalEvent = (MessageEvent) track.getUserData();
        GuildMessageChannelUnion originalEventChannel = originalEvent.getChannel();
        long guildId = originalEvent.getGuild().getIdLong();
        clearVotes(guildId);
        if (endReason == AudioTrackEndReason.LOAD_FAILED) {
            guildFailCount.compute(guildId, (guild, fails) -> fails == null ? 1 : fails + 1);
            if (guildFailCount.get(guildId) == 1) {
                errorlnTime("Failed to load a track " + track.getInfo().uri + " retrying...");
                event.player.startTrack(track, false);
            }
            if (guildFailCount.get(guildId) >= 3) {
                errorlnTime("Failed to load a song 3 times in a row, killing queue");
                queue.clear();
                guildFailCount.put(guildId, 0);
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("❌ **Critical Error**");
                eb.setDescription("Tracks have now failed to load 3 times, likely due to an upstream network issue beyond our control. Clearing the queue to avoid track spam.");
                eb.setFooter("If this issue persists with specific audio sources, please file a /bug report");
                eb.setColor(botColour);
                originalEventChannel.sendMessageEmbeds(eb.build()).queue();
                return;
            } else {
                originalEventChannel.sendMessageEmbeds(createQuickError("The track failed to load due to an unknown reason. Skipping...")).queue();
                if (queue.isEmpty()) {
                    guildFailCount.put(guildId, 0);
                }
            }
        } else {
            guildFailCount.put(guildId, 0);
            if (endReason.mayStartNext) { //Not to be handled if a load failed
                if (LoopGuilds.contains(guildId)) {
                    AudioTrack loopTrack = track.makeClone();
                    this.player.startTrack(loopTrack, false);
                    trackLoops.put(guildId, trackLoops.get(guildId) + 1);
                    return;
                }
                if (LoopQueueGuilds.contains(guildId)) {
                    AudioTrack loopTrack = track.makeClone();
                    nextTrack();
                    queue(loopTrack);
                    return;
                }
                if (AutoplayGuilds.contains(guildId)) {
                    if (!track.getInfo().uri.toLowerCase().contains("youtube")) {
                        originalEventChannel.sendMessageEmbeds(createQuickError("Autoplay is on, but the track is not supported by autoplay!\n\nUse **" + botPrefix + " autoplay** to stop autoplay.")).queue();
                        return;
                    }
                    String trackId = track.getInfo().identifier;
                    String radioUrl = "https://www.youtube.com/watch?v=" + trackId + "&list=" + "RD" + trackId;
                    PlayerManager.getInstance().loadAndPlay(originalEvent, radioUrl, true);
                    return;
                }
            }
        }
        trackLoops.put(guildId, 0);
        if (endReason.mayStartNext) {
            nextTrack();
            AudioTrack nextTrack = player.getPlayingTrack();
            if (nextTrack != null) { //Otherwise, we silent-error, which isn't an issue, but nicer to be safe
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
                if (PlayerManager.getInstance().getThumbURL(nextTrack) != null) eb.setThumbnail(PlayerManager.getInstance().getThumbURL(nextTrack));
                eb.setColor(botColour);
                originalEventChannel.sendMessageEmbeds(eb.build()).queue();
            }
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Guild guild = ((MessageEvent) track.getUserData()).getGuild();
        printlnTime("AudioPlayer in", guild.getIdLong(), guild.getName(), "threw friendly exception on track", track.getInfo().uri);
        exception.printStackTrace();
    }
}
