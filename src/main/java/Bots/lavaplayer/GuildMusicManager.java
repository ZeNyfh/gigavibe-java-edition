package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.*;

public class GuildMusicManager{
    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(this.audioPlayer);
        this.audioPlayer.addListener(this.scheduler);
        this.sendHandler = new AudioPlayerSendHandler(this.audioPlayer);
    }
    public AudioPlayerSendHandler getSendHandler() {
            return this.sendHandler;
    }
}
