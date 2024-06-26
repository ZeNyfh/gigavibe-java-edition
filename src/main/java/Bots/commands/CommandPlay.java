package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

public class CommandPlay extends BaseCommand {
    final public Set<String> audioFiles = Set.of(
            "mp3", "mp4", "wav", "ogg", "flac", "mov", "wmv", "m4a", "aac", "webm", "opus", "m3u", "txt"
    );
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_CHANNEL_BLOCKED, Check.TRY_JOIN_VC};
    }

    @Override
    public void execute(MessageEvent event) throws IOException {
        event.deferReply(); //expect to take a while
        String string = event.getContentRaw();
        String[] args = string.split(" ", 2);

        // figure out if there are actually attachments that can be played
        List<Message.Attachment> attachments = event.getAttachments();
        boolean playAttachments = false;
        for (Message.Attachment attachment : attachments) {
            if (attachment.getFileExtension() == null || !audioFiles.contains(attachment.getFileExtension().toLowerCase())) {
                attachments.remove(attachment); // invalid file extension, remove the attachment from the list.
            } else {
                playAttachments = true;
            }
        }

        // play attachments
        if (playAttachments) {
            Message.Attachment att = attachments.get(0);
            String fileExtension = att.getFileExtension() != null ? att.getFileExtension().toLowerCase() : "";

            if (fileExtension.equals("txt")) {
                URL url = new URL(att.getUrl());
                URLConnection connection = url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                try {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        PlayerManager.getInstance().loadAndPlay(event, line.split(" ", 2)[0], false);
                    }
                    event.replyEmbeds(createQuickEmbed("✅ **Success**", "Queued **" + att.getFileName() + "**"));
                } catch (Exception e) {
                    event.replyEmbeds(createQuickError("Something went wrong when loading the tracks from the file.\n```\n" + e.getMessage() + "\n```")); // tell the user what happened.
                }
            } else {
                try {
                    for (Message.Attachment attachment : attachments) {
                        PlayerManager.getInstance().loadAndPlay(event, attachment.getUrl(), false);
                    }
                    event.reply("Queued " + attachments.size() + " tracks from attachments.");
                } catch (Exception e) {
                    event.replyEmbeds(createQuickError("Something went wrong when loading the tracks from attachments.\n```\n" + e.getMessage() + "\n```"));
                }
            }
        } else { // no valid attachments to play, check for args.
            if (args.length < 2) {
                event.replyEmbeds(createQuickError("No arguments given."));
                return;
            }
            String link = String.valueOf(args[1]);
            String[] links = link.split("http");
            List<String> linksList = new ArrayList<>();
            for (String str : links) {
                linksList.add(("http"+str)
                        .replace("youtube.com/shorts/", "youtube.com/watch?v=")
                        .replace("youtu.be/", "www.youtube.com/watch?v=").trim()
                );
            }
            int actualListSize = 0;
            if (linksList.size() > 2) {
                boolean antiSpam = false;
                for (String url : linksList) {
                    if (url.equals("http")) continue;
                    actualListSize++;
                    try {
                        PlayerManager.getInstance().loadAndPlay(event, url, false);
                    } catch (FriendlyException e) {
                        if (!antiSpam) {
                            antiSpam = true;
                            event.replyEmbeds(createQuickError("Something went wrong when decoding one of the tracks.\n```\n" + e.getMessage() + "\n```"));
                        }
                    }
                }
                event.replyEmbeds(createQuickEmbed("✅ **Success**", "Queued " + actualListSize + " songs!"));
                return;
            }
            if (link.contains("https://") || link.contains("http://")) {
                link = link.replace("youtube.com/shorts/", "youtube.com/watch?v=");
                link = link.replace("youtu.be/", "www.youtube.com/watch?v=");
            } else {
                link = "ytsearch: " + link;
            }
            try {
                PlayerManager.getInstance().loadAndPlay(event, link, true);
            } catch (FriendlyException e) {
                event.replyEmbeds(createQuickError("Something went wrong when decoding the track.\n```\n" + e.getMessage() + "\n```"));
            }
        }
    }
    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String[] getNames() {
        return new String[]{"play", "p"};
    }

    @Override
    public String getDescription() {
        return "Plays songs and playlists from sources such as yt, soundcloud, spotify, and discord/http urls.";
    }

    @Override
    public String getOptions() {
        return "<file OR track>";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOptions(
                new OptionData(OptionType.STRING, "track", "The track to play", false),
                new OptionData(OptionType.ATTACHMENT, "file", "The file to play", false)
        );
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}