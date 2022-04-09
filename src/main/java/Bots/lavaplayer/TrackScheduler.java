package Bots.lavaplayer;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.player.event.*;
import com.sedmelluq.discord.lavaplayer.track.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.*;

import static com.sun.jmx.snmp.ThreadContext.contains;

public class TrackScheduler extends AudioEventAdapter {

    public final AudioPlayer player;
    public final BlockingQueue<AudioTrack> queue;

    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    public void queue(AudioTrack track){
        if(!this.player.startTrack(track, true)){
            this.queue.offer(track);
        }
    }

    public void nextTrack(){
        this.player.startTrack(this.queue.poll(), false);
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (track.getInfo().identifier.contains("C:\\Users\\ZeNyfh\\Desktop\\tempmusic")){
            try {
                Files.delete(Paths.get(track.getInfo().identifier));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(endReason.mayStartNext){
            nextTrack();
        }
    }
}


