package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static Bots.Main.*;

public class CommandInsert extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        if (!IsDJ(event.getGuild(), event.getChannel().asTextChannel(), event.getMember())) {
            return;
        }
        if (IsChannelBlocked(event.getGuild(), event.getChannel().asTextChannel())) {
            return;
        }
        String[] args = event.getMessage().getContentRaw().split(" ", 3);
        if (!args[1].matches("^\\d+$")) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Invalid arguments, integers only\nUsage: `<Integer> <URL/SearchTerm>`")).queue();
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        if (args[2].contains("https://") || args[2].contains("http://")) {
            if (args[2].contains("youtube.com/shorts/")) {
                args[2] = args[2].replace("youtube.com/shorts/", "youtube.com/watch?v=");
            }
            if (args[2].contains("youtu.be/")) {
                args[2] = args[2].replace("youtu.be/", "www.youtube.com/watch?v=");
            }
        } else {
            args[2] = "ytsearch: " + args[2];
        }
        if (Integer.parseInt(args[1]) >= queue.size() || queue.isEmpty()) {
            try {
                PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), args[2], true);
            } catch (FriendlyException ignored) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Something went wrong when decoding the track.\n\nError from decoder 16388")).queue();
            }
        } else {
            try {
                musicManager.scheduler.queue.clear();
                List<AudioTrack> newQueue = new ArrayList<>();
                for (int i = 0; i < Integer.parseInt(args[1]); i++) {
                    newQueue.add((AudioTrack) queue.toArray()[i]);
                    musicManager.scheduler.queue.add((AudioTrack) queue.toArray()[i]);
                }
                for (int i = 0; i < newQueue.size() - 1; i++) {
                    queue.remove(newQueue.get(i));
                }
                PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), args[2], true);
                for (int i = 0; i < Integer.parseInt(args[1]); i++) {
                    musicManager.scheduler.queue.add((AudioTrack) queue.toArray()[i]);
                }
            } catch (Exception e) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError(e.getMessage())).queue();
            }
        }
    }

    @Override
    public String getParams() {
        return "<Integer> <URL/SearchTerm>";
    }

    @Override
    public String getCategory() {
        return "dev"; //DJ
    }

    @Override
    public String[] getNames() {
        return new String[]{"insert"};
    }

    @Override
    public String getDescription() {
        return "Inserts a track into the queue.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}