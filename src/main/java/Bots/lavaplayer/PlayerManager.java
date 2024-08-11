package Bots.lavaplayer;

import Bots.CommandEvent;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter;
import com.github.topi314.lavasrc.spotify.SpotifySourceManager;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import dev.lavalink.youtube.YoutubeAudioSourceManager;
import dev.lavalink.youtube.clients.*;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.Main.*;

public class PlayerManager {
    private static final Map<String, Pattern> patterns = new HashMap<>() {{
        put("Spotify", Pattern.compile("<img src=\"([^\"]+)\" width=\""));
        put("SoundCloud", Pattern.compile("\"thumbnail_url\":\"([^\"]+)\",\""));
    }};
    private static PlayerManager INSTANCE;
    private static boolean hasSpotify;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final AudioPlayerManager audioPlayerManager;

    public PlayerManager() {
        this.musicManagers = new HashMap<>();
        this.audioPlayerManager = new DefaultAudioPlayerManager();
        this.audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager(true, new Music(), new Web(), new AndroidTestsuite(), new AndroidLite(), new AndroidMusic(), new MediaConnect(), new Ios(), new TvHtml5Embedded()));

        String spotifyClientID = Dotenv.load().get("SPOTIFYCLIENTID");
        String spotifyClientSecret = Dotenv.load().get("SPOTIFYCLIENTSECRET");

        try {
            this.audioPlayerManager.registerSourceManager(new SpotifySourceManager(null, spotifyClientID, spotifyClientSecret, "gb", audioPlayerManager));
            hasSpotify = true;
        } catch (Exception exception) {
            System.err.println("Spotify manager was unable to load due to a complication. Continuing without it...\nError: " + exception);
            hasSpotify = false;
        }

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
            guildMusicManager.audioPlayer.setFilterFactory((track, format, output) -> {
                VibratoPcmAudioFilter vibrato = new VibratoPcmAudioFilter(output, format.channelCount, format.sampleRate);
                TimescalePcmAudioFilter timescale = new TimescalePcmAudioFilter(vibrato, format.channelCount, format.sampleRate);
                guildMusicManager.filters.put(AudioFilters.Vibrato, vibrato);
                guildMusicManager.filters.put(AudioFilters.Timescale, timescale);
                //Just make sure the items are in the reverse order they were made and all will be good
                return Arrays.asList(new AudioFilter[]{timescale, vibrato});
            });
            guild.getAudioManager().setSendingHandler(guildMusicManager.getSendHandler());
            return guildMusicManager;
        });
    }

    public EmbedBuilder createTrackEmbed(AudioTrack audioTrack) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(botColour);
        embed.setThumbnail(getThumbURL(audioTrack));
        if (audioTrack.getInfo().title.isEmpty()) { // Local file
            String[] trackNameArray = audioTrack.getInfo().identifier.split("/");
            String trackName = trackNameArray[trackNameArray.length - 1];
            embed.setTitle(trackName, audioTrack.getInfo().uri);
        } else {
            embed.setTitle(audioTrack.getInfo().title, audioTrack.getInfo().uri);
        }
        String length;
        if (audioTrack.getInfo().length > 432000000 || audioTrack.getInfo().length <= 1) {
            length = "Unknown";
        } else {
            length = toTimestamp((audioTrack.getInfo().length));
        }
        embed.setDescription("Duration: `" + length + "`\n" + "Channel: `" + audioTrack.getInfo().author + "`");
        return embed;
    }

    private void replyWithEmbed(Object eventOrChannel, MessageEmbed embed, boolean forceSendChannel) {
        if (eventOrChannel instanceof CommandEvent) {
            if (forceSendChannel) {
                ((CommandEvent) eventOrChannel).getChannel().sendMessageEmbeds(embed).queue();
            } else {
                ((CommandEvent) eventOrChannel).replyEmbeds(embed);
            }
        } else {
            ((GuildMessageChannelUnion) eventOrChannel).sendMessageEmbeds(embed).queue();
        }
    }

    private void replyWithEmbed(Object eventOrChannel, MessageEmbed embed) {
        replyWithEmbed(eventOrChannel, embed, false);
    }

    public CompletableFuture<LoadResult> loadAndPlay(Object eventOrChannel, String trackUrl, boolean sendEmbed) {
        assert (eventOrChannel instanceof CommandEvent || eventOrChannel instanceof GuildMessageChannelUnion);
        CompletableFuture<LoadResult> loadResultFuture = new CompletableFuture<>();
        Guild commandGuild;
        if (eventOrChannel instanceof CommandEvent) {
            commandGuild = ((CommandEvent) eventOrChannel).getGuild();
        } else {
            commandGuild = ((GuildMessageChannelUnion) eventOrChannel).getGuild();
        }
        if (trackUrl.toLowerCase().contains("spotify")) {
            if (!hasSpotify) {
                if (sendEmbed)
                    replyWithEmbed(eventOrChannel, createQuickError("The bot is unable to play spotify tracks right now"));
                loadResultFuture.complete(LoadResult.NO_MATCHES);
                return loadResultFuture;
            }
        }
        final GuildMusicManager musicManager = this.getMusicManager(commandGuild);
        this.audioPlayerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                audioTrack.setUserData(new TrackUserData(eventOrChannel));
                musicManager.scheduler.queue(audioTrack);
                if (sendEmbed) {
                    replyWithEmbed(eventOrChannel, createTrackEmbed(audioTrack).build());
                }
                loadResultFuture.complete(LoadResult.TRACK_LOADED);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {
                boolean autoplaying = AutoplayGuilds.contains(commandGuild.getIdLong());
                final List<AudioTrack> tracks = audioPlaylist.getTracks();
                if (!tracks.isEmpty()) {
                    AudioTrack track = tracks.get(0);
                    if (autoplaying)
                        track = tracks.get(ThreadLocalRandom.current().nextInt(2, 4)); // this is to prevent looping tracks
                    if (tracks.size() == 1 || audioPlaylist.getName().contains("Search results for:") || autoplaying) {
                        musicManager.scheduler.queue(track);
                        if (sendEmbed) {
                            replyWithEmbed(eventOrChannel, createTrackEmbed(track).build(), autoplaying);
                        }
                    } else {
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setColor(botColour);
                        long lengthSeconds = 0;
                        for (AudioTrack audioTrack : tracks) {
                            lengthSeconds = (lengthSeconds + audioTrack.getInfo().length);
                            musicManager.scheduler.queue(audioTrack);
                        }
                        embed.setTitle(audioPlaylist.getName().replaceAll("&amp;", "&").replaceAll("&gt;", ">").replaceAll("&lt;", "<").replaceAll("\\\\", "\\\\\\\\"));
                        embed.appendDescription("Size: **" + tracks.size() + "** tracks.\nLength: **" + toTimestamp(lengthSeconds) + "**\n\n");

                        for (int i = 0; i < tracks.size() && i < 5; i++) {
                            if (tracks.get(i).getInfo().title == null) {
                                embed.appendDescription(i + 1 + ". [" + tracks.get(i).getInfo().identifier + "](" + tracks.get(i).getInfo().uri + ")\n");
                            } else {
                                embed.appendDescription(i + 1 + ". [" + tracks.get(i).getInfo().title + "](" + tracks.get(i).getInfo().uri + ")\n");
                            }
                        }
                        if (tracks.size() > 5) {
                            embed.appendDescription("...");
                        }
                        embed.setThumbnail(getThumbURL(tracks.get(0)));
                        if (sendEmbed) {
                            replyWithEmbed(eventOrChannel, embed.build());
                        }
                    }
                    for (AudioTrack audioTrack : tracks) {
                        audioTrack.setUserData(new TrackUserData(eventOrChannel));
                    }
                }
                loadResultFuture.complete(LoadResult.PLAYLIST_LOADED);
            }

            @Override
            public void noMatches() {
                if (sendEmbed)
                    replyWithEmbed(eventOrChannel, createQuickError("No matches found for the track."));
                System.err.println("No match found for the track.\nURL:\"" + trackUrl + "\"");
                loadResultFuture.complete(LoadResult.NO_MATCHES);
            }

            @Override
            public void loadFailed(FriendlyException e) {
                System.err.println("Track failed to load.\nURL: \"" + trackUrl + "\"\nReason: " + e.getMessage());
                skipCountGuilds.remove(commandGuild.getIdLong());

                final StringBuilder loadFailedBuilder = new StringBuilder();
                if (e.getMessage().toLowerCase().contains("search response: 400")) {
                    loadFailedBuilder.append("An error with the youtube search API has occurred. ");
                }
                loadFailedBuilder.append(e.getMessage());
                if (sendEmbed)
                    replyWithEmbed(eventOrChannel, createQuickError("The track failed to load: " + loadFailedBuilder));
                loadResultFuture.complete(LoadResult.LOAD_FAILED);
            }
        });
        return loadResultFuture;
    }

    @Nullable
    public String getThumbURL(AudioTrack track) {
        URL url = null;
        Pattern pattern = null;
        String site = "";
        try {
            if (track.getInfo().artworkUrl != null) {
                return track.getInfo().artworkUrl;
            }
            if (track.getInfo().uri.toLowerCase().contains("youtube")) {
                return "https://img.youtube.com/vi/" + track.getIdentifier() + "/0.jpg";
            } else if (track.getInfo().uri.toLowerCase().contains("spotify")) {
                site = "Spotify";
                url = new URL("https://embed.spotify.com/oembed?url=" + track.getInfo().uri);
            } else if (track.getInfo().uri.toLowerCase().contains("soundcloud")) {
                site = "SoundCloud";
                url = new URL(track.getInfo().uri);
            } else {
                return null;
            }
            pattern = patterns.get(site);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Thumb URL Fail : " + site + " |" + url);
        }

        if (url != null && pattern != null) {
            try {
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder output = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line);
                }
                reader.close();
                Matcher matcher = pattern.matcher(output.toString());
                if (matcher.find()) {
                    return matcher.group(1);
                } else {
                    return null;
                }
            } catch (Exception ignored) {
                System.err.println("Thumb Matcher Fail : " + site + " |" + url);
            } // ignore because floods console if image url invalid
            return null;
        }
        return null;
    }

    public enum LoadResult {
        TRACK_LOADED(true),
        PLAYLIST_LOADED(true),
        NO_MATCHES(false),
        LOAD_FAILED(false);

        public final boolean songWasPlayed;

        LoadResult(boolean songWasPlayed) {
            this.songWasPlayed = songWasPlayed;
        }
    }

    public static class TrackUserData {
        public final Object eventOrChannel;
        public final Long channelId;
        public final Long guildId;

        public TrackUserData(Object eventOrChannel) {
            this.eventOrChannel = eventOrChannel;
            GuildMessageChannelUnion channel;
            if (eventOrChannel instanceof CommandEvent) {
                channel = ((CommandEvent) eventOrChannel).getChannel();
            } else {
                channel = (GuildMessageChannelUnion) eventOrChannel;
            }
            this.channelId = channel.getIdLong();
            this.guildId = channel.getGuild().getIdLong();
        }
    }
}
