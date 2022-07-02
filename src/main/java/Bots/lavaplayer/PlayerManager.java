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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static Bots.Main.*;

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

    public void loadAndPlay(TextChannel textChannel, String trackUrl, Boolean sendEmbed) {
        final GuildMusicManager musicManager = this.getMusicManager(textChannel.getGuild());

        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                audioTrack.setUserData(textChannel);
                if (!sendEmbed) {
                    musicManager.scheduler.queue(audioTrack);
                    return;
                }
                String length;
                musicManager.scheduler.queue(audioTrack);
                EmbedBuilder embed = new EmbedBuilder();
                if (audioTrack.getInfo().length > 432000000) { // 5 days
                    length = "Unknown";
                } else {
                    length = toTimestamp((audioTrack.getInfo().length));
                }
                embed.setColor(new Color(0, 0, 255));
                if (audioTrack.getInfo().title.isEmpty()) {
                    String[] trackNameArray = audioTrack.getInfo().identifier.split("/");
                    String trackName = trackNameArray[trackNameArray.length - 1];
                    embed.setTitle((trackName), (audioTrack.getInfo().uri));
                } else {
                    embed.setTitle(audioTrack.getInfo().title, (audioTrack.getInfo().uri));
                }
                embed.setDescription("Duration: `" + length + "`\n" + "Channel: `" + audioTrack.getInfo().author + "`");
                textChannel.sendMessageEmbeds(embed.build()).queue();
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                String length = "Unknown";
                EmbedBuilder embed = new EmbedBuilder();
                embed.setColor(new Color(0, 0, 255));
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (!tracks.isEmpty()) {
                    String author = (tracks.get(0).getInfo().author);
                    if (tracks.get(0).getInfo().length < 432000000) { // 5 days
                        length = toTimestamp((tracks.get(0).getInfo().length));
                    }
                    if (tracks.size() == 1 || audioPlaylist.getName().contains("Search results for:")) {
                        if (!sendEmbed) {
                            musicManager.scheduler.queue(tracks.get(0));
                            return;
                        }
                        musicManager.scheduler.queue(tracks.get(0));
                        embed.setThumbnail("https://img.youtube.com/vi/" + tracks.get(0).getIdentifier() + "/0.jpg");
                        embed.setTitle((tracks.get(0).getInfo().title), (tracks.get(0).getInfo().uri));
                        embed.setDescription("Duration: `" + length + "`\n" + "Channel: `" + author + "`");
                        textChannel.sendMessageEmbeds(embed.build()).queue();
                    } else {
                        long lengthSeconds = 0;
                        for (int i = 0; i < tracks.size(); ) {
                            lengthSeconds = (lengthSeconds + tracks.get(i).getInfo().length);
                            musicManager.scheduler.queue(tracks.get(i));
                            i++;
                        }
                        embed.setTitle(audioPlaylist.getName().replaceAll("&amp;", "&").replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("\\\\", "\\\\\\\\"));
                        embed.appendDescription("Size: **" + tracks.size() + "** tracks.\nLength: **" + toTimestamp(lengthSeconds) + "**\n\n");

                        for (int i = 0; i < tracks.size(); ) {
                            if (i > 4 || tracks.get(i) == null) {
                                break;
                            }
                            if (tracks.get(i).getInfo().title == null) {
                                embed.appendDescription(i + 1 + ". [" + tracks.get(i).getInfo().identifier + "](" + tracks.get(i).getInfo().uri + ")\n");
                            } else {
                                embed.appendDescription(i + 1 + ". [" + tracks.get(i).getInfo().title + "](" + tracks.get(i).getInfo().uri + ")\n");
                            }
                            i++;
                        }
                        if (tracks.size() > 5) {
                            embed.appendDescription("...");
                        }
                        embed.setThumbnail("https://img.youtube.com/vi/" + tracks.get(0).getIdentifier() + "/0.jpg");
                        textChannel.sendMessageEmbeds(embed.build()).queue();
                    }
                    for (int i = 0; i < tracks.size(); ) {
                        tracks.get(i).setUserData(textChannel);
                        i++;
                    }
                }
            }

            @Override
            public void noMatches() {
                textChannel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No matches found for the track.")).queue();
                printlnTime("No match found for the track.");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                textChannel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The track failed to load.\n\n```\n" + e.getMessage() + "\n```")).queue();
                printlnTime("track loading failed, stacktrace: ");
                e.printStackTrace();
            }
        });
    }
}
