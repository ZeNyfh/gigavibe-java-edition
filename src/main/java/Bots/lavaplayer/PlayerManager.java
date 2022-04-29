package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.awt.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Bots.Main.toTimestamp;
import static Bots.commands.CommandPlay.playlistCheck;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();

        AudioSourceManagers.registerRemoteSources(this.audioPlayerManager);
        AudioSourceManagers.registerLocalSource(this.audioPlayerManager);
    }

    public static PlayerManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getMusicManager(Guild guild) {
        return this.musicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            final GuildMusicManager guildMusicManager = new GuildMusicManager(this.audioPlayerManager);
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public void loadAndPlay(TextChannel textChannel, String trackUrl) {
        final GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());
        String[] midiAudioFiles = {"mid", "midi", "mod"};

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                String length;
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                musicManager.scheduler.queue(audioTrack);

                EmbedBuilder embed = new EmbedBuilder();
                if (audioTrack.getInfo().length > 432000000) { // 5 days
                    length = "Unknown";
                } else {
                    length = toTimestamp((audioTrack.getInfo().length));
                }
                embed.setColor(new Color(0, 0, 255));
                if (audioTrack.getInfo().uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
                    embed.setTitle((audioTrack.getInfo().uri).replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13));
                    embed.setDescription("Duration: `" + length + "`");
                } else {
                    embed.setTitle((audioTrack.getInfo().title), (audioTrack.getInfo().uri));
                    embed.setDescription("Duration: `" + length + "`\n" + "Channel: `" + audioTrack.getInfo().author + "`");
                }
                textChannel.sendMessageEmbeds(embed.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                String length;
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(new Color(0, 0, 255));
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (!tracks.isEmpty()) {
                    if (tracks.size() > 1 && !playlistCheck){
                        System.out.println(tracks.get(0).getIdentifier());
                        if (tracks.get(0).getInfo().length > 432000000) { // 5 days
                            length = "Unknown";
                        } else {
                            length = toTimestamp((tracks.get(0).getInfo().length));
                        }
                        musicManager.scheduler.queue(tracks.get(0));
                        if (tracks.get(0).getInfo().uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
                            embed.setTitle((tracks.get(0).getInfo().uri).replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13));
                        } else {
                            embed.setThumbnail("https://img.youtube.com/vi/" + tracks.get(0).getIdentifier() + "/0.jpg");
                            embed.setTitle((tracks.get(0).getInfo().title), (tracks.get(0).getInfo().uri));
                        }
                        if (tracks.get(0).getInfo().uri.contains("cdn.discordapp.com") || tracks.get(0).getInfo().uri.contains("media.discordapp.net")) {
                            embed.setTitle((tracks.get(0).getInfo().uri).replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13));
                            embed.setThumbnail(tracks.get(0).getInfo().uri + "?format=jpeg");
                        }
                        String author = (tracks.get(0).getInfo().author);
                        embed.setDescription("Duration: `" + length + "`\n" + "Channel: `" + author + "`");
                        textChannel.sendMessageEmbeds(embed.build()).queue();
                    } else {
                        long lengthSeconds = 0;
                        for (int i = 0; i < tracks.size();){
                            lengthSeconds = (lengthSeconds + tracks.get(i).getInfo().length);
                            musicManager.scheduler.queue(tracks.get(i));
                            i++;
                        }
                        length = String.valueOf(lengthSeconds);
                    }
                }
            }

            @Override
            public void noMatches() {
                System.out.println("No track found.");
            }

            @Override
            public void loadFailed(FriendlyException e) {
            }
        });
    }

}
