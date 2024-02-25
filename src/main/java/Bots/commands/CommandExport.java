package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static Bots.Main.createQuickError;

public class CommandExport extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.replyEmbeds(createQuickError("The bot is not in a VC."));
            return;
        }
        if (audioPlayer.getPlayingTrack() == null) {
            event.replyEmbeds(createQuickError("The bot is not playing anything."));
            return;
        }
        String fileName = "temp/" + System.currentTimeMillis() + ".txt";
        File text = new File(fileName);
        FileWriter writer = new FileWriter(text);
        writer.write(audioPlayer.getPlayingTrack().getInfo().uri + " | " + audioPlayer.getPlayingTrack().getInfo().title + "\n");
        if (!musicManager.scheduler.queue.isEmpty()) {
            for (AudioTrack track : musicManager.scheduler.queue) {
                writer.write(track.getInfo().uri + " | " + track.getInfo().title + "\n");
            }
        }
        writer.flush();
        writer.close();
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
}
