package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static Bots.Main.*;

//import static Bots.Main.LoopGuilds;
//import static Bots.commands.CommandLoop.loop;
//import static Bots.commands.CommandLoopQueue.loopQueue;

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
        TextChannel userData = (TextChannel) track.getUserData();
        AudioTrack nextTrack = getTrackFromQueue(userData.getGuild(), 0);
        if (LoopGuilds.contains(userData.getGuild().getId())) {
            if (endReason.mayStartNext) {
                AudioTrack loopTrack = track.makeClone();
                this.player.startTrack(loopTrack, false);
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
        EmbedBuilder eb = new EmbedBuilder();
        if (nextTrack == null) {
            return;
        } else if (!nextTrack.getInfo().title.isEmpty()) {
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
        eb.setThumbnail("https://img.youtube.com/vi/" + nextTrack.getIdentifier() + "/0.jpg");
        eb.setColor(botColour);
        System.out.println(nextTrack.getInfo().title);
        userData.sendMessageEmbeds(eb.build()).queue();
        if (endReason.mayStartNext) {
            nextTrack();
        }
        if (endReason.name().equals("REPLACED")){
            return;
        }
        onTrackStuck(nextTrack);
    }

    private void onTrackStuck(AudioTrack nextTrack) {
        TextChannel userData = (TextChannel) nextTrack.getUserData();
        userData.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Track got stuck, skipping.")).queue();
        nextTrack();
    }
}