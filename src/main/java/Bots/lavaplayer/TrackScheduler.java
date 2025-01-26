package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static Bots.Main.*;
import static Bots.lavaplayer.LastFMManager.encode;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    private final Map<Long, Integer> guildFailCount = new HashMap<>();

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

        PlayerManager.TrackUserData trackUserData = (PlayerManager.TrackUserData) track.getUserData();
        GuildMessageChannelUnion originalEventChannel = (GuildMessageChannelUnion) getGuildChannelFromID(trackUserData.channelId);
        long guildID = trackUserData.guildId;
        HashMap<String, String> lang = guildLocales.get(guildID);
        guildFailCount.remove(guildID);

        if (endReason == AudioTrackEndReason.LOAD_FAILED) {
            int currentFailCount = guildFailCount.getOrDefault(guildID, 0); // gets the value, if there is none, sets it to 0
            guildFailCount.put(guildID, currentFailCount + 1);
            handleTrackFailure(originalEventChannel, track);
            return;
        }

        if (endReason.mayStartNext) {
            if (LoopGuilds.contains(guildID)) { // track is looping
                AudioTrack loopTrack = track.makeClone();
                this.player.startTrack(loopTrack, false);
                trackLoops.put(guildID, trackLoops.get(guildID) + 1);
                return;
            }
            if (LoopQueueGuilds.contains(guildID)) { // queue is looping
                AudioTrack loopTrack = track.makeClone();
                nextTrack();
                queue(loopTrack);
                return;
            }

            if (AutoplayGuilds.contains(guildID)) { // is autoplaying
                StringBuilder errorBuilder = new StringBuilder();
                String searchTerm = LastFMManager.getSimilarSongs(track, guildID);
                boolean canAutoPlay = true;

                if (searchTerm.equals("notfound")) {
                    errorBuilder.append("❌ **").append(lang.get("Main.error")).append(":**\n").append(String.format(lang.get("TrackScheduler.autoplay.notFound"), track.getInfo().title)).append("\n");
                    canAutoPlay = false;
                }
                if (searchTerm.equals("none")) {
                    errorBuilder.append("❌ **").append(lang.get("Main.error")).append(":**\n").append(lang.get("TrackScheduler.autoplay.noSimilar")).append("\n");
                    canAutoPlay = false;
                }
                if (searchTerm.isEmpty()) {
                    errorBuilder.append("❌ **").append(lang.get("Main.error")).append(":**\n").append(lang.get("TrackScheduler.autoplay")).append("\n");
                    canAutoPlay = false;
                }

                if (canAutoPlay) {
                    // TODO: will be replaced by https://github.com/ZeNyfh/gigavibe-java-edition/pull/166
                    String artistName = (track.getInfo().author.isEmpty() || track.getInfo().author == null)
                            ? encode(track.getInfo().title.toLowerCase(), false, true)
                            : encode(track.getInfo().author.toLowerCase(), false, true);
                    String title = encode(track.getInfo().title, true, false);
                    PlayerManager.getInstance().loadAndPlay(trackUserData.eventOrChannel, "ytsearch:" + artistName + " " + title, true);
                    createQuickEmbed("♾️ " + lang.get("TrackScheduler.autoplay.queued"), artistName + " - " + title);
                } else { // cannot autoplay
                    originalEventChannel.sendMessageEmbeds(createQuickError(errorBuilder.toString())).queue();
                    // go to next track in the queue
                    playNextTrack(player, originalEventChannel);
                }
            } else { // is not autoplaying
                playNextTrack(player, originalEventChannel);
            }
        }
    }



    private void handleTrackFailure(GuildMessageChannelUnion originalEventChannel, AudioTrack track) {
        long guildID = originalEventChannel.getGuild().getIdLong();

        int failCount = guildFailCount.getOrDefault(guildID, 0) + 1;
        guildFailCount.put(guildID, failCount);

        if (failCount == 1) { // if fails once, retry the track.
            retryTrack(track);
        } else if (failCount < 3) { // if it is less than 3 but not 1, handle a regular failure.
            handleRegularFailure(originalEventChannel);
        } else { // if it is more than 3, worry (network issue usually).
            handleCriticalFailure(originalEventChannel);
        }
    }

    private void handleRegularFailure(GuildMessageChannelUnion originalEventChannel) {
        long guildID = originalEventChannel.getGuild().getIdLong();

        try {
            originalEventChannel.sendMessageEmbeds(createQuickError(guildLocales.get(originalEventChannel.getGuild().getIdLong()).get("TrackScheduler.regularFailure"))).queue();
        } catch (InsufficientPermissionException ignored) {
            // This should not be logged.
        }

        if (queue.isEmpty()) {
            guildFailCount.put(guildID, 0);
        }
    }

    private void handleCriticalFailure(GuildMessageChannelUnion originalEventChannel) {
        long guildID = originalEventChannel.getGuild().getIdLong();
        queue.clear();
        guildFailCount.put(guildID, 0);
        HashMap<String, String> lang = guildLocales.get(originalEventChannel.getGuild().getIdLong());

        MessageEmbed failureEmbed = createQuickEmbed(
                "❌ " + lang.get("TrackScheduler.criticalFailure.title"),
                lang.get("TrackScheduler.criticalFailure.description"),
                lang.get("TrackScheduler.criticalFailure.footer")
        );

        try {
            originalEventChannel.sendMessageEmbeds(failureEmbed).queue();
        } catch (InsufficientPermissionException ignored) {
            // This should not be logged.
        }
    }

    private void retryTrack(AudioTrack track) {
        System.err.println("Failed to load track " + track.getInfo().uri + " ; retrying...");
        this.player.startTrack(track.makeClone(), false);
    }

    private void playNextTrack(AudioPlayer player, GuildMessageChannelUnion originalEventChannel) {
        long guildID = originalEventChannel.getGuild().getIdLong();
        HashMap<String, String> lang = guildLocales.get(guildID);

        trackLoops.put(guildID, 0);
        nextTrack();
        AudioTrack nextTrack = player.getPlayingTrack();
        if (nextTrack == null) {
            return;
        }

        EmbedBuilder eb = new EmbedBuilder();

        eb.setTitle(String.format(lang.get("TrackScheduler.nextTrack.nowPlaying"), (nextTrack.getInfo().title.isEmpty() ?
            nextTrack.getInfo().uri :
            nextTrack.getInfo().title)), nextTrack.getInfo().uri
        );

        eb.setDescription("**" + String.format(lang.get("Main.channel"), "**\n" + (nextTrack.getInfo().author.isEmpty() ?
            lang.get("Main.unknown") :
            nextTrack.getInfo().author))
        );

        eb.addField("**" + String.format(lang.get("Main.duration"), "**\n"),
            nextTrack.getInfo().length > 432000000 ? lang.get("Main.unknown") :
            toSimpleTimestamp(nextTrack.getInfo().length),
            true
        );

        String name;
        name = ((PlayerManager.TrackUserData) nextTrack.getUserData()).username;
        if (!name.isEmpty()) {
            eb.setFooter("played by: " + name);
        }

        if (PlayerManager.getInstance().getThumbURL(nextTrack) != null) {
            eb.setThumbnail(PlayerManager.getInstance().getThumbURL(nextTrack));
        }
        eb.setColor(botColour);
        try {
            originalEventChannel.sendMessageEmbeds(eb.build()).queue();
        } catch (InsufficientPermissionException ignored) {
            // this should not be logged.
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        PlayerManager.TrackUserData trackUserData = (PlayerManager.TrackUserData) track.getUserData();
        Guild guild = getGuildChannelFromID(trackUserData.channelId).getGuild();

        System.err.println("AudioPlayer in " + guild.getIdLong() + " (" + guild.getName() + ") threw friendly exception on track " + track.getInfo().uri);
        System.err.println(exception.getMessage());
    }
}
