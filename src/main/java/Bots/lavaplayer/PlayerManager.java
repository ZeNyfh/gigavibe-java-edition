package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.source.*;
import com.sedmelluq.discord.lavaplayer.tools.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.util.*;
import java.util.List;

import static Bots.Main.toTimestamp;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager(){
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public GuildMusicManager getMusicManager(Guild guild){
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel textChannel, String trackUrl){
        final GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                musicManager.scheduler.queue(audioTrack);

                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle((audioTrack.getInfo().title), (audioTrack.getInfo().uri));
                embed.setColor(new Color(0, 0, 255));
                String author = (audioTrack.getInfo().author);
                String length = toTimestamp((audioTrack.getInfo().length));
                embed.setDescription("Duration: `" + length + "`" + System.lineSeparator() + "Channel: `" + author + "`");
                textChannel.sendMessageEmbeds(embed.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if(!tracks.isEmpty()){
                    musicManager.scheduler.queue(tracks.get(0));
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle((tracks.get(0).getInfo().title), (tracks.get(0).getInfo().uri));
                    embed.setColor(new Color(0, 0, 255));
                    String author = (tracks.get(0).getInfo().author);
                    String length = toTimestamp((tracks.get(0).getInfo().length));
                    embed.setDescription("Duration: `" + length + "`" + System.lineSeparator() + "Channel: `" + author + "`");
                    textChannel.sendMessageEmbeds(embed.build()).queue();
                }
            }

            @Override
            public void noMatches() {

            }

            @Override
            public void loadFailed(FriendlyException e) {

            }
        });
    }

    public static PlayerManager getInstance(){
        if(INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

}
