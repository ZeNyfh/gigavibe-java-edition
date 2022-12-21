package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandForceSkip implements BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel().asTextChannel(), event.getMember())) {
            return;
        }
        final TextChannel channel = event.getChannel().asTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickError("Im not in a vc.")).queue();
            return;
        }

        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickError("You need to be in a voice channel to use this command.")).queue();
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            channel.sendMessageEmbeds(createQuickError("You need to be in the same voice channel to use this command.")).queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(createQuickError("No tracks are playing right now.")).queue();
            return;
        }
        if (event.getArgs().length == 1) {
            if (musicManager.scheduler.queue.size() >= 1) {
                musicManager.scheduler.nextTrack();
                channel.sendMessageEmbeds(createQuickEmbed(" ", "⏩ Skipped the current track to __**[" + musicManager.audioPlayer.getPlayingTrack().getInfo().title + "](" + musicManager.audioPlayer.getPlayingTrack().getInfo().uri + ")**__")).queue();
            } else {
                musicManager.scheduler.nextTrack();
                channel.sendMessageEmbeds(createQuickEmbed(" ", "⏩ Skipped the current track")).queue();
            }
        } else if (event.getArgs()[1].matches("^\\d+$")) {
            if (Integer.parseInt(event.getArgs()[1]) - 1 >= musicManager.scheduler.queue.size()) {
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.nextTrack();
                channel.sendMessageEmbeds(createQuickEmbed(" ", "⏩ Skipped the entire queue")).queue();
                clearVotes(event.getGuild().getIdLong());
                return;
            } else {
                List<AudioTrack> list = new ArrayList<>(musicManager.scheduler.queue);
                musicManager.scheduler.queue.clear();
                musicManager.scheduler.queue.addAll(list.subList(Math.max(0, Math.min(Integer.parseInt(event.getArgs()[1]), list.size()) - 1), list.size()));
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "⏩ Skipped " + event.getArgs()[1] + " tracks to __**[" + musicManager.audioPlayer.getPlayingTrack().getInfo().title + "](" + musicManager.audioPlayer.getPlayingTrack().getInfo().uri + ")**__")).queue();
            }
        } else {
            if (musicManager.scheduler.queue.size() >= 1) {
                musicManager.scheduler.nextTrack();
                channel.sendMessageEmbeds(createQuickEmbed(" ", "⏩ Skipped the current track to __**[" + musicManager.audioPlayer.getPlayingTrack().getInfo().title + "](" + musicManager.audioPlayer.getPlayingTrack().getInfo().uri + ")**__")).queue();
            } else {
                musicManager.scheduler.nextTrack();
                channel.sendMessageEmbeds(createQuickEmbed(" ", "⏩ Skipped the current track")).queue();
            }
        }
        clearVotes(event.getGuild().getIdLong());
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String[] getNames() {
        return new String[]{"forceskip", "fs"};
    }

    @Override
    public String getDescription() {
        return "Casts a vote or skips the current song.";
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}