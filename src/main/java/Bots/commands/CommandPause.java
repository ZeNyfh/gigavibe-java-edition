package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import java.util.Objects;

import static Bots.Main.*;

public class CommandPause extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel(), event.getMember())) {
            return;
        }
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("Im not in a vc."));
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
            event.replyEmbeds(createQuickError("No tracks are playing right now."));
            return;
        }
        if (audioPlayer.isPaused()) {
            audioPlayer.setPaused(false);
            event.replyEmbeds(createQuickEmbed("\uD83C\uDFB5 ▶", "The track is now unpaused."));
        } else {
            audioPlayer.setPaused(true);
            event.replyEmbeds(createQuickEmbed("\uD83C\uDFB5 ⏸", "The track is now paused."));
        }
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String[] getNames() {
        return new String[]{"pause", "resume", "res", "continue", "unpause", "stop"};
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getDescription() {
        return "pauses or resumes the current track.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}