package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.*;

public class CommandForceSkip extends BaseCommand {

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
                addToVote(event.getGuild().getIdLong(), new ArrayList<>());
                return;
            } else {
                for (int i = 0; i < Integer.parseInt(event.getArgs()[1]) - 1; i++) {
                    musicManager.scheduler.queue.remove();
                }
                musicManager.scheduler.nextTrack();
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
        addToVote(event.getGuild().getIdLong(), new ArrayList<>());
    }

    public String getCategory() {
        return "Music";
    }

    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("fs");
        return list;
    }

    public String getName() {
        return "forceskip";
    }

    public String getDescription() {
        return "Casts a vote or skips the current song.";
    }

    public long getTimeout() {
        return 1000;
    }
}