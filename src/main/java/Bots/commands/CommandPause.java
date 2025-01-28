package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker;
import Bots.CommandStateChecker.Check;
import Bots.CommandEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import static Bots.CommandStateChecker.PerformChecks;
import static Bots.Main.createQuickEmbed;

public class CommandPause extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_IN_SAME_VC};
    }

    @Override
    public void execute(CommandEvent event) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null && !musicManager.scheduler.queue.isEmpty()) {
            musicManager.scheduler.nextTrack(); // No embed data :( (playNextTrack is private, dunno if its worth publicising)
            event.replyEmbeds(createQuickEmbed("\uD83C\uDFB5 ▶", "Resumed playing from the queue."));
        } else {
            CommandStateChecker.CheckResult checkResult = PerformChecks(event, Check.IS_DJ, Check.IS_PLAYING);
            if (!checkResult.Succeeded()) {
                event.replyEmbeds(createQuickEmbed("❌ **Not Allowed**", checkResult.GetMessage()));
                return;
            }
            if (audioPlayer.isPaused()) {
                audioPlayer.setPaused(false);
                event.replyEmbeds(createQuickEmbed("\uD83C\uDFB5 ▶", "The track is now playing."));
            } else {
                audioPlayer.setPaused(true);
                event.replyEmbeds(createQuickEmbed("\uD83C\uDFB5 ⏸", "The track is now paused."));
            }
        }
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String[] getNames() {
        return new String[]{"pause", "resume", "res", "continue", "unpause", "stop"};
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