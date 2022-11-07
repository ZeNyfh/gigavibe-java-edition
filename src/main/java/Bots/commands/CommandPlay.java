package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;

import static Bots.Main.IsChannelBlocked;
import static Bots.Main.createQuickError;

public class CommandPlay extends BaseCommand {
    public String[] audioFiles = {"mp3", "mp4", "wav", "ogg", "flac", "mov", "wmv", "m4a", "aac", "webm", "opus", "m3u"};

    @Override
    public void execute(MessageEvent event) throws IOException {
        if (IsChannelBlocked(event.getGuild(), event.getChannel().asTextChannel())) {
            return;
        }

        String string = event.getMessage().getContentRaw();
        String[] args = string.split(" ", 2);
        final AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember()).getVoiceState();
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());
        assert memberState != null;
        final VoiceChannel memberChannel = (VoiceChannel) memberState.getChannel();
        if (!memberState.inAudioChannel()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("you arent in a vc.")).queue();
            return;
        }

        if (!event.getMessage().getAttachments().isEmpty() && Arrays.toString(audioFiles).contains(Objects.requireNonNull(event.getMessage().getAttachments().get(0).getFileExtension()))) {
            String link = event.getMessage().getAttachments().get(0).getUrl();
            audioManager.openAudioConnection(memberChannel);
            PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), link, true);
            return;
        }
        String link;
        try {
            link = String.valueOf(args[1]);
        } catch (Exception ignored) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No arguments given.")).queue();
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
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("you arent in the same vc.")).queue();
            return;
        }
        try {
            PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), link, true);
        } catch (FriendlyException ignored) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Something went wrong when decoding the track.\n\nError from decoder 16388")).queue();
        }
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String[] getNames() {
        return new String[]{"play", "p"};
    }

    @Override
    public String getDescription() {
        return "Plays songs or playlists from: youtube, soundcloud, bandcamp, twitch, vimeo, spotify, apple music, http urls and discord attachments.";
    }

    @Override
    public String getParams() {
        return "<URL or Keywords>";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}