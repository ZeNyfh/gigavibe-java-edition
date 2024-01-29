package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandPlay extends BaseCommand {
    final public String[] audioFiles = {"mp3", "mp4", "wav", "ogg", "flac", "mov", "wmv", "m4a", "aac", "webm", "opus", "m3u", "txt"};

    @Override
    public void execute(MessageEvent event) throws IOException {
        if (IsChannelBlocked(event.getGuild(), event.getChannel())) {
            return;
        }

        if (event.isSlash()) {
            event.deferReply(); //expect to take a while
        }
        PlayerManager.message = event;
        String string = event.getContentRaw();
        String[] args = string.split(" ", 2);
        final AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember()).getVoiceState();
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());
        assert memberState != null;
        final VoiceChannel memberChannel = (VoiceChannel) memberState.getChannel();
        if (!memberState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("You aren't in a vc."));
            return;
        }

        if (!event.getAttachments().isEmpty() && Arrays.toString(audioFiles).contains(Objects.requireNonNull(event.getAttachments().get(0).getFileExtension()).toLowerCase())) {
            // txt file custom playlists
            if (Objects.requireNonNull(event.getAttachments().get(0).getFileExtension()).equalsIgnoreCase("txt")) {
                audioManager.openAudioConnection(memberChannel);
                URL url = new URL(event.getAttachments().get(0).getUrl());
                URLConnection connection = url.openConnection();
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String line;
                try {
                    while ((line = reader.readLine()) != null) {
                        PlayerManager.getInstance().loadAndPlay(event.getChannel(), line.split(" ", 2)[0], false);
                    }
                    event.replyEmbeds(createQuickEmbed("✅ **Success**", "Queued **" + event.getAttachments().get(0).getFileName() + "**"));
                    return;
                } catch (Exception ignored) {
                    event.replyEmbeds(createQuickError("Something went wrong when loading the track."));
                }
            }

            // audio/video attachments
            List<Message.Attachment> links = event.getAttachments();
            audioManager.openAudioConnection(memberChannel);
            boolean sendEmbedBool = true;
            if (links.size() > 1) {
                event.reply("Queued " + links.size() + " tracks from attachments.");
            }
            for (Message.Attachment attachment : links) {
                try {
                    PlayerManager.getInstance().loadAndPlay(event.getChannel(), attachment.getUrl(), sendEmbedBool);
                    sendEmbedBool = false;
                } catch (Exception ignored) {
                    event.replyEmbeds(createQuickError("Something went wrong when loading the track."));
                }
            }
            return;
        }
        String link;
        try {
            link = String.valueOf(args[1]);
        } catch (Exception ignored) {
            event.replyEmbeds(createQuickError("No arguments given."));
            return;
        }
        if (link.contains("https://") || link.contains("http://")) {
            if (link.contains("youtube.com/shorts/")) {
                link = link.replace("youtube.com/shorts/", "youtube.com/watch?v=");
            }
            if (link.contains("youtu.be/")) {
                link = link.replace("youtu.be/", "www.youtube.com/watch?v=");
            }
        } else {
            link = "ytsearch: " + link;
        }
        if (!selfState.inAudioChannel()) {
            audioManager.openAudioConnection(memberChannel);
        } else if (memberState.getChannel() != selfState.getChannel()) {
            event.replyEmbeds(createQuickError("you arent in the same vc."));
            return;
        }
        try {
            PlayerManager.getInstance().loadAndPlay(event.getChannel(), link, true);
        } catch (FriendlyException ignored) {
            event.replyEmbeds(createQuickError("Something went wrong when decoding the track."));
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
        return "Plays songs or playlists from many sources including yt, soundcloud spotify and discord/http urls.";
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