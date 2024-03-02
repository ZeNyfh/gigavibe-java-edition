package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

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
        if (AutoplayGuilds.contains(event.getGuild().getIdLong())) {
            if (!audioPlayer.getPlayingTrack().getInfo().uri.toLowerCase().contains("youtube")) {
                event.replyEmbeds(createQuickError("Autoplay is on, but the track is not supported by autoplay!\n\nUse **" + botPrefix + " autoplay** to stop autoplay."));
            }
            //TODO: This autoplay stuff has multiple issues (#148 work)
            //(Double reply potential, the above if check doesn't prevent the below code (that's gotta be a bug)
            String trackId = audioPlayer.getPlayingTrack().getInfo().identifier;
            String radioUrl = "https://www.youtube.com/watch?v=" + trackId + "&list=RD" + trackId;
            PlayerManager.getInstance().loadAndPlay(event.getChannel(), radioUrl, true);
        }
        if (event.getArgs().length > 1 && event.getArgs()[1].matches("^\\d+$")) {
            if (Integer.parseInt(event.getArgs()[1]) - 1 >= musicManager.scheduler.queue.size()) {
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.nextTrack();
                event.replyEmbeds(createQuickEmbed(" ", "⏩ Skipped the entire queue"));
            } else {
                List<AudioTrack> list = new ArrayList<>(musicManager.scheduler.queue);
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.queue.addAll(list.subList(Math.max(0, Math.min(Integer.parseInt(event.getArgs()[1]), list.size()) - 1), list.size()));
                musicManager.scheduler.nextTrack();
                event.replyEmbeds(createQuickEmbed(" ", "⏩ Skipped " + event.getArgs()[1] + " tracks to __**[" + musicManager.audioPlayer.getPlayingTrack().getInfo().title + "](" + musicManager.audioPlayer.getPlayingTrack().getInfo().uri + ")**__"));
            }
        } else {
            if (!musicManager.scheduler.queue.isEmpty()) {
                musicManager.scheduler.nextTrack();
                event.replyEmbeds(createQuickEmbed(" ", "⏩ Skipped the current track to __**[" + musicManager.audioPlayer.getPlayingTrack().getInfo().title + "](" + musicManager.audioPlayer.getPlayingTrack().getInfo().uri + ")**__"));
            } else {
                musicManager.scheduler.nextTrack();
                event.replyEmbeds(createQuickEmbed(" ", "⏩ Skipped the current track"));
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