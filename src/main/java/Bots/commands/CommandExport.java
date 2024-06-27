package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static Bots.Main.executor;

public class CommandExport extends BaseCommand implements Runnable {
    private static MessageEvent event;

    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_BOT_IN_ANY_VC, Check.IS_PLAYING};
    }

    @Override
    public void run() {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        String fileName = "temp/" + System.currentTimeMillis() + ".txt";
        File text = new File(fileName);
        FileWriter writer;
        try {
            writer = new FileWriter(text);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            writer.write(audioPlayer.getPlayingTrack().getInfo().uri + " | " + audioPlayer.getPlayingTrack().getInfo().title + "\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!musicManager.scheduler.queue.isEmpty()) {
            for (AudioTrack track : musicManager.scheduler.queue) {
                try {
                    writer.write(track.getInfo().uri + " | " + track.getInfo().title + "\n");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        event.replyFiles(FileUpload.fromData(text));
    }

    @Override
    public String[] getNames() {
        return new String[]{"export", "ex", "equeue", "eq"};
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String getDescription() {
        return "Exports the current queue in the format of URL | Title";
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }

    @Override
    public void execute(MessageEvent e) throws InterruptedException {
        event = e;
        executor.submit(new CommandExport());

    }
}
