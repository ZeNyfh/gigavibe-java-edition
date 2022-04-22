package Bots.commands;

import Bots.BaseCommand;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;

public class CommandPlay implements BaseCommand {

    public void execute(MessageReceivedEvent event) {
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

        String link = event.getMessage().getContentRaw();
        link = link.replace("&play ", "");
        link = link.replace("&play", ""); // just in case someone supplied 0 args
        if (link.equals("") || (link.equals(" "))){
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No arguments given.")).queue();
            return;
        }
        if (link.contains("https://") || link.contains("http://")) {
            link = link.replace("&play ", "");
            if (link.contains("youtu.be/")) {
                link = link.replace("youtu.be/", "www.youtube.com/watch?v=");
            }
        } else {
            link = "ytsearch: " + link;
        }
        PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), link);
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
}