package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.TrackEndEvent;
import com.sedmelluq.discord.lavaplayer.player.event.TrackStartEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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
        Guild userData = (Guild) track.getUserData(); // UserData is being used here to return the guild, it is stored every time a new track is played.
        if (LoopGuilds.contains(userData.getId())){
            if (endReason.mayStartNext) {
                AudioTrack loopTrack = track.makeClone();
                this.player.startTrack(loopTrack, false);
                return;
            }
        }
        if (LoopQueueGuilds.contains(userData.getId())){
            if (endReason.mayStartNext) {
                AudioTrack loopTrack = track.makeClone();
                nextTrack();
                queue(loopTrack);
                return;
            }
        }
        if (endReason.mayStartNext) {
            nextTrack();
            EmbedBuilder eb = new EmbedBuilder();
            AudioTrack nextTrack = getTrackFromQueue(userData, 0);
            if (nextTrack == null) {
                return;
            } else if (!nextTrack.getInfo().title.isEmpty()){
                eb.setTitle("[Now playing: " + nextTrack.getInfo().title + "](" + nextTrack.getInfo().uri + ")");
            } else {
                eb.setTitle("Now playing: " + nextTrack.getInfo().uri);
            }
            if (nextTrack.getInfo().length >= 432000000) {
                eb.setDescription("**Channel:**\n" + nextTrack.getInfo().author + "\n\n**Duration:**\n" + toSimpleTimestamp(nextTrack.getInfo().length));
            } else {
                eb.setDescription("**Channel:**\n" + nextTrack.getInfo().author + "\n\n**Duration:**\nUnknown");
            }
            eb.setThumbnail("https://img.youtube.com/vi/" + nextTrack.getIdentifier() + "/0.jpg");
            eb.setColor(botColour);
            System.out.println(nextTrack.getInfo().title);
        }
    }
}