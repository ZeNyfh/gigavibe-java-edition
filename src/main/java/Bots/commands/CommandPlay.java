package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.util.Arrays;
import java.util.Objects;

import static Bots.Main.IsChannelBlocked;
import static Bots.Main.createQuickError;

public class CommandPlay extends BaseCommand {
    final public String[] audioFiles = {"mp3", "mp4", "wav", "ogg", "flac", "mov", "wmv", "m4a", "aac", "webm", "opus", "m3u"};

    @Override
    public void execute(MessageEvent event) {
        if (IsChannelBlocked(event.getGuild(), event.getChannel().asTextChannel())) {
            return;
        }

        String string = event.getContentRaw();
        String[] args = string.split(" ", 2);
        final AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember()).getVoiceState();
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());
        assert memberState != null;
        final VoiceChannel memberChannel = (VoiceChannel) memberState.getChannel();
        if (!memberState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("you arent in a vc."));
            return;
        }

        if (!event.getAttachments().isEmpty() && Arrays.toString(audioFiles).contains(Objects.requireNonNull(event.getAttachments().get(0).getFileExtension()))) {
            String link = event.getAttachments().get(0).getUrl();
            audioManager.openAudioConnection(memberChannel);
            PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), link, true);
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
            PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), link, true);
        } catch (FriendlyException ignored) {
            event.replyEmbeds(createQuickError("Something went wrong when decoding the track.\n\nError from decoder 16388"));
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
        return "Plays songs or playlists from many sources including yt, soundcloud spotify and discord/http urls.";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addSubcommands(
                new SubcommandData("file", "Plays an uploaded file").addOptions(
                        new OptionData(OptionType.ATTACHMENT, "file", "The file to play", true)
                ),
                new SubcommandData("track", "Plays the given url or track").addOptions(
                        new OptionData(OptionType.STRING, "track", "The track to play", true)
                )
        );
        //TODO: System can now handle sub-commands. This was roughly adjusted but the command itself needs to be able to handle the new options. -9382
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}