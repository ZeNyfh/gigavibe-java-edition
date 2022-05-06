package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static Bots.commands.CommandLoop.loop;
import static Bots.commands.CommandLoopQueue.loopQueue;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;
    public MessageReceivedEvent event;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
        this.event = event;
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
        if (loop && endReason.toString().equals("FINISHED")) {
            AudioTrack loopTrack = track.makeClone();
            this.player.startTrack(loopTrack, true);
            return;
        } else if (loopQueue && endReason.toString().equals("FINISHED")) {
            AudioTrack loopTrack = track.makeClone();
            nextTrack();
            queue(loopTrack);
            return;
        }
        if (track.getInfo().identifier.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
            for (int i2 = 5; i2 > 0; i2--) {  // file deletion
                try {
                    Thread.sleep(500);
                    Files.delete(Paths.get(track.getInfo().identifier));
                } catch (Exception ignored) {
                }
            }
        }
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }
}