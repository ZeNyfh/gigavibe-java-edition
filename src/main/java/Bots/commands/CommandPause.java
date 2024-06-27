package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Bots.Main.createQuickEmbed;
public class CommandPause extends BaseCommand implements Runnable {
    private static MessageEvent event;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_IN_SAME_VC, Check.IS_PLAYING};
    }

    @Override
    public void run() {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.isPaused()) {
            audioPlayer.setPaused(false);
            event.replyEmbeds(createQuickEmbed("\uD83C\uDFB5 ▶", "The track is now playing."));
        } else {
            audioPlayer.setPaused(true);
            event.replyEmbeds(createQuickEmbed("\uD83C\uDFB5 ⏸", "The track is now paused."));
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

    @Override
    public void execute(MessageEvent e) throws InterruptedException {
        event = e;
        executor.submit(new CommandPause());

    }
}