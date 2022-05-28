package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static Bots.Main.*;

public class CommandPlay extends BaseCommand {
    public static boolean playlistCheck = false;
    public String[] audioFiles = {"mp3", "mp4", "wav", "ogg", "flac", "mov", "wmv", "m4a", "aac", "webm", "opus"};

    public void execute(MessageEvent event) {
        if (IsChannelBlocked(event.getGuild(), event.getTextChannel())){
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
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc.")).queue();
            return;
        }

        if (!event.getMessage().getAttachments().isEmpty() && Arrays.toString(audioFiles).contains(Objects.requireNonNull(event.getMessage().getAttachments().get(0).getFileExtension()))) {
            String link = event.getMessage().getAttachments().get(0).getUrl();
            audioManager.openAudioConnection(memberChannel);
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), link, true);
            return;
        }
        String link;
        try {
            link = String.valueOf(args[1]);
        } catch (Exception ignored) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No arguments given.")).queue();
            return;
        }
        if (link.contains("https://") || link.contains("http://")) {
            if (link.contains("youtu.be/")) {
                link = link.replace("youtu.be/", "www.youtube.com/watch?v=");
            }
            playlistCheck = true;
        } else {
            link = "ytsearch: " + link;
            playlistCheck = false;
            System.out.println(PlayerManager.getInstance().getMusicManager(event.getGuild()));
            System.out.println(audioManager);
        }
        if (!selfState.inAudioChannel()) {
            audioManager.openAudioConnection(memberChannel);
        } else if (memberState.getChannel() != selfState.getChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in the same vc.")).queue();
            return;
        }
        try {
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), link, true);
        } catch (FriendlyException ignored) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Something went wrong when decoding the track.\n\nError from decoder 16388")).queue();
        }
    }

    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
        list.add("p");
        return list;
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