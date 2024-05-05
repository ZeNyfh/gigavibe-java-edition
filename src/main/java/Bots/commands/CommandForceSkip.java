package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.AutoplayHelper;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.LastFMManager;
import Bots.lavaplayer.PlayerManager;
import Bots.lavaplayer.RadioDataFetcher;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

import static Bots.Main.*;
import static Bots.lavaplayer.LastFMManager.encode;

public class CommandForceSkip extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_IN_SAME_VC, Check.IS_PLAYING};
    }

    @Override
    public void execute(MessageEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        StringBuilder messageBuilder = new StringBuilder();
        
        if (AutoplayGuilds.contains(event.getGuild().getIdLong())) 
            AutoplayHelper.doAutoplay(messageBuilder, audioPlayer, event);
        
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