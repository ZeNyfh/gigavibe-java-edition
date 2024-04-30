package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

import static Bots.Main.*;

public class CommandInsert extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_CHANNEL_BLOCKED, Check.IS_USER_IN_ANY_VC, Check.IS_PLAYING};
        // QUESTION: Should we support insert when nothing is playing? shouldn't be unreasonable to support -9382
        // (If we do, make sure to change IS_USER_IN_ANY_VC and IS_PLAYING to TRY_JOIN_VC)
    }

    @Override
    public void execute(MessageEvent event) {
        String[] args = event.getContentRaw().split(" ", 3);
        // check here to ensure args[2] is never undefined.
        if (args.length != 3) {
            event.replyEmbeds(createQuickError("Not enough arguments provided\nUsage: `<Integer> <URL/SearchTerm>`"));
            return;
        }
        if (!args[1].matches("^\\d+$")) {
            event.replyEmbeds(createQuickError("Invalid arguments, integers only\nUsage: `<Integer> <URL/SearchTerm>`"));
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        List<String> tracksToPlay = new ArrayList<>();
        // prioritise attachment/s.
        if (!event.getAttachments().isEmpty()) {
            for (Message.Attachment attachment : event.getAttachments()) {
                tracksToPlay.add(attachment.getUrl());
            }
        }
        // if no attachment, try find url.
        else if (args[2].contains("https://") || args[2].contains("http://")) {
            if (args[2].contains("youtube.com/shorts/")) {
                args[2] = args[2].replace("youtube.com/shorts/", "youtube.com/watch?v=");
            }
            if (args[2].contains("youtu.be/")) {
                args[2] = args[2].replace("youtu.be/", "www.youtube.com/watch?v=");
            }
        } else {
            args[2] = "ytsearch: " + args[2];
        }
        int position = Integer.parseInt(args[1]) - 1;
        tracksToPlay.add(args[2]);
        boolean sendEmbedBool = true;
        // queue is empty or the argument was larger than or equal to the queue size, simply load and play like usual.
        if (position + 1 >= queue.size() || queue.isEmpty()) {
            for (String track : tracksToPlay) {
                try {
                    PlayerManager.getInstance().loadAndPlay(event, track, sendEmbedBool);
                    sendEmbedBool = false;
                } catch (FriendlyException ignored) {
                    event.replyEmbeds(createQuickError("Something went wrong when decoding the track."));
                }
            }
        } else {
            // insertion between songs happens here
            try {
                List<AudioTrack> TemporaryQueue = new ArrayList<>();
                musicManager.scheduler.queue.drainTo(TemporaryQueue);
                for (int i = 0; i < position; i++) {
                    musicManager.scheduler.queue(TemporaryQueue.get(i));
                }
                for (String track : tracksToPlay) {
                    PlayerManager.getInstance().loadAndPlay(event, track, false, () -> {
                        for (int i = position; i < TemporaryQueue.size(); i++) {
                            musicManager.scheduler.queue(TemporaryQueue.get(i));
                        }
                        event.replyEmbeds(createQuickEmbed(" ", "Added the track to position **" + args[1] + "**"));
                    });
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOptions(
                new OptionData(OptionType.INTEGER, "position", "The position to insert the track.", true),
                new OptionData(OptionType.STRING, "track", "The track to insert.", true)
        );
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String getOptions() {
        return "<Position> <Track>";
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