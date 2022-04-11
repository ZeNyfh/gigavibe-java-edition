package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static Bots.commands.CommandLoop.loop;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;

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
        if (loop && endReason.toString().equals("FINISHED")) {
            this.player.startTrack(track, false); // doesn't work, uses the last track, literally
            PlayerManager.getInstance().loadAndPlay(null, track.getInfo().uri); // doesn't work, uses the url of the last track
            return;
        }
        if (track.getInfo().identifier.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
            try {
                Files.delete(Paths.get(track.getInfo().identifier));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (endReason.mayStartNext) {
            nextTrack();
        }
    }
}


