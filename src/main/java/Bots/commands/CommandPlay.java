package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Arrays;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;

public class CommandPlay extends BaseCommand {
    public static boolean playlistCheck = false;
    public String[] audioFiles = {"mp3", "mp4", "wav", "ogg", "flac", "mov", "wmv", "m4a", "aac", "webm", "opus"};

    public void execute(MessageEvent event) {
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember()).getVoiceState();
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());
        if (!memberState.inAudioChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc.")).queue();
            return;
        }

        if (!selfState.inAudioChannel()) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) memberState.getChannel();
            audioManager.openAudioConnection(memberChannel);
        } else if (memberState.getChannel() != selfState.getChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in the same vc.")).queue();
            return;
        }

        if (!event.getMessage().getAttachments().isEmpty() && Arrays.toString(audioFiles).contains(event.getMessage().getAttachments().get(0).getFileExtension())) {
            String link = event.getMessage().getAttachments().get(0).getUrl();
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), link);
            return;
        }
        String link;
        try {
            link = event.getArgs().get(1);
        } catch (Exception ignored) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No arguments given.")).queue();
            return;
        }
        if (link.contains("https://") || link.contains("http://")) {
            link = link.replace("&play ", "");
            if (link.contains("youtu.be/")) {
                link = link.replace("youtu.be/", "www.youtube.com/watch?v=");
            }
            playlistCheck = true;
        } else {
            link = "ytsearch: " + link;
            playlistCheck = false;
        }
        try {
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), link);
        } catch (FriendlyException ignored) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Something went wrong when decoding the track.\n\nError from decoder 16388")).queue();
        }
    }

    public String getCategory() {
        return "Music";
    }

    public String getName() {
        return "play";
    }

    public String getDescription() {
        return "Plays songs or playlists from: youtube, soundcloud, bandcamp, twitch, vimeo, http urls and discord attachments.";
    }

    public String getParams() {
        return "<URL or Keywords>";
    }
}