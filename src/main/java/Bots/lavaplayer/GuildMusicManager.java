package Bots.lavaplayer;

import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.equalizer.Equalizer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class GuildMusicManager extends AudioEventAdapter {

    public final AudioPlayer audioPlayer;
    public final TrackScheduler scheduler;
    public final GuildMusicParameters parameters;
    private final AudioPlayerSendHandler sendHandler;

    public GuildMusicManager(AudioPlayerManager manager) {
        this.audioPlayer = manager.createPlayer();
        this.scheduler = new TrackScheduler(audioPlayer);
        this.sendHandler = new AudioPlayerSendHandler(audioPlayer);
        this.parameters = new GuildMusicParameters();

        audioPlayer.addListener(this);
    }

    public AudioPlayerSendHandler getSendHandler() {
        return sendHandler;
    }
    public void applyFilters() {
        audioPlayer.setFilterFactory(
                (track, format, output) -> {
                    List<AudioFilter> filters = new ArrayList<>();

                    // bass boosting stuff
//                    if (GuildMusicParameters.getBass()) {
//                        Equalizer equalizer = new Equalizer(audioDataFormat.channelCount, universalPcmAudioFilter);
//                        for (int i = 0; i < BASS_BOOST.length; i++) {
//                            equalizer.setGain(i, multiplier * BASS_BOOST[i]);
//                        }
//                        filters.add(equalizer);
//                    }

                    // speed stuff
                    TimescalePcmAudioFilter speed = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
                    speed.setSpeed(parameters.getSpeed()); //1.5x normal speed
                    filters.add(speed);

                    // pitch stuff
                    TimescalePcmAudioFilter pitch = new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate);
                    pitch.setPitch(parameters.getPitch());
                    filters.add(pitch);
                    return filters;
                }
        );
    }
}
