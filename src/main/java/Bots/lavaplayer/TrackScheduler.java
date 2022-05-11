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
            //    EmbedBuilder eb = new EmbedBuilder();
            //    if (!nextTrack.getInfo().title.isEmpty()){
            //        eb.setTitle("[Now playing: " + nextTrack.getInfo().title + "](" + nextTrack.getInfo().uri + ")");
            //    } else {
            //        eb.setTitle("Now playing: " + nextTrack.getInfo().uri);
            //    }
            //    if (nextTrack.getInfo().length >= 432000000) {
            //        eb.setDescription("**Channel:**\n" + nextTrack.getInfo().author + "\n\n**Duration:**\n" + toSimpleTimestamp(nextTrack.getInfo().length));
            //    } else {
            //        eb.setDescription("**Channel:**\n" + nextTrack.getInfo().author + "\n\n**Duration:**\nUnknown");
            //    }
            //    eb.setThumbnail("https://img.youtube.com/vi/" + nextTrack.getIdentifier() + "/0.jpg");
            //    eb.setColor(new Color(0, 0, 255));
            //    System.out.println(nextTrack.getInfo().title);
        }
    }
}