package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.LastFMManager;
import Bots.lavaplayer.PlayerManager;
import Bots.lavaplayer.RadioDataFetcher;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;
import static Bots.lavaplayer.LastFMManager.encode;

public class CommandForceSkip extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel(), event.getMember())) {
            return;
        }
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("I'm not in a vc."));
            return;
        }

        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("You need to be in a voice channel to use this command."));
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.replyEmbeds(createQuickError("You need to be in the same voice channel to use this command."));
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            event.replyEmbeds(createQuickError("Nothing is playing right now."));
            return;
        }
        StringBuilder messageBuilder = new StringBuilder();
        if (AutoplayGuilds.contains(event.getGuild().getIdLong())) {
            String searchTerm = LastFMManager.getSimilarSongs(audioPlayer.getPlayingTrack(), event.getGuild().getIdLong());
            boolean canPlay = true;
            if (searchTerm.equals("notfound")) {
                messageBuilder.append("❌ **Error:**\nAutoplay failed to find ").append(audioPlayer.getPlayingTrack().getInfo().title).append("\n");
                canPlay = false;
            }
            if (searchTerm.equals("none")) {
                messageBuilder.append("❌ **Error:**\nAutoplay could not find similar tracks.\n");
                canPlay = false;
            }
            if (searchTerm.isEmpty()) {
                messageBuilder.append("❌ **Error:**\nAn unknown error occurred when trying to autoplay.\n");
                canPlay = false;
            }
            if (canPlay) {
                AudioTrack track = audioPlayer.getPlayingTrack();
                // TODO: should be replaced with actual logic checking if last.fm has either the author or the artist name in the title.
                String artistName = (track.getInfo().author.isEmpty() || track.getInfo().author == null)
                        ? encode((track.getInfo().title).toLowerCase(), false, true)
                        : encode(track.getInfo().author.toLowerCase(), false, true);
                String title = encode(track.getInfo().title, true, false);
                PlayerManager.getInstance().loadAndPlay(event, "ytsearch:" + artistName + " - " + title, false);
                messageBuilder.append("♾️ Autoplay queued: ").append(artistName).append(" - ").append(title).append("\n");
            }
        }
        if (event.getArgs().length > 1 && event.getArgs()[1].matches("^\\d+$")) { // autoplay logic shouldn't exist here
            if (Integer.parseInt(event.getArgs()[1]) - 1 >= musicManager.scheduler.queue.size()) {
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.nextTrack();
                event.replyEmbeds(createQuickEmbed(" ", "⏩ Skipped the entire queue"));
            } else {
                List<AudioTrack> list = new ArrayList<>(musicManager.scheduler.queue);
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.queue.addAll(list.subList(Math.max(0, Math.min(Integer.parseInt(event.getArgs()[1]), list.size()) - 1), list.size()));
                musicManager.scheduler.nextTrack();
                AudioTrackInfo trackInfo = musicManager.audioPlayer.getPlayingTrack().getInfo();
                String title = trackInfo.title;
                if (trackInfo.isStream) {
                    String streamTitle = RadioDataFetcher.getStreamTitle(trackInfo.uri);
                    if (streamTitle != null) {
                        title = streamTitle;
                    }
                }
                event.replyEmbeds(createQuickEmbed(" ", "⏩ Skipped " + event.getArgs()[1] + " tracks to __**[" + title + "](" + trackInfo.uri + ")**__"));
            }
        } else {
            if (!musicManager.scheduler.queue.isEmpty()) {
                musicManager.scheduler.nextTrack();
                AudioTrackInfo trackInfo = musicManager.audioPlayer.getPlayingTrack().getInfo();
                String title = trackInfo.title;
                if (trackInfo.isStream) {
                    String streamTitle = RadioDataFetcher.getStreamTitle(trackInfo.uri);
                    if (streamTitle != null) {
                        title = streamTitle;
                    }
                }
                event.replyEmbeds(createQuickEmbed(" ", ("⏩ Skipped the current track to __**[" + title + "](" + trackInfo.uri + ")**__\n\n" + messageBuilder).trim()));
            } else {
                musicManager.scheduler.nextTrack();
                event.replyEmbeds(createQuickEmbed(" ", ("⏩ Skipped the current track\n\n" + messageBuilder).trim()));
            }
        }
        clearVotes(event.getGuild().getIdLong());
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String[] getNames() {
        return new String[]{"forceskip", "fs"};
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.INTEGER, "amount", "Amount of tracks to skip from the queue.", false);
    }

    @Override
    public String getOptions() {
        return "[Number]";
    }

    @Override
    public String getDescription() {
        return "Skips the song forcefully.";
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}