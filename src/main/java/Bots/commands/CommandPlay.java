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

        if (!isUrl(link)) {
            link = "ytsearch:" + link;
            link = link.replace("&play", "");
            if (link.contains("youtu.be/")) {
                link = link.replace("youtu.be/", "www.youtube.com/watch?v=");
            }
        }

        PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), link);

    }

    private boolean isUrl(String url) {
        try {
            new URI(url);
            return true;
        } catch (URISyntaxException e) {
            return false;
        }
    }

    public String getCategory() {
        return "Music";
    }

    public String getName() {
        return "play";
    }

    public String getDescription() {
        return "Plays songs or playlists from youtube.";
    }
}