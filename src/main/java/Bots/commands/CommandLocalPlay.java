package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.nio.file.Path;
import java.nio.file.Paths;

import static Bots.Main.createQuickEmbed;

public class CommandLocalPlay extends BaseCommand {
    public void execute(MessageEvent event) {

        if (!event.getMember().getVoiceState().inAudioChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc cunt")).queue();
            return;
        }

        if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }
        String path = event.getMessage().getContentRaw();
        path = path.replace("&playfile ", "");
        Path finalpath = Paths.get(path);
        PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), String.valueOf(finalpath));
    }

    public String getCategory() {
        return "dev";
    }

    public String getName() {
        return "playfile";
    }

    public String getDescription() {
        return "Plays songs from a directory.";
    }

    public String getParams() {
        return "<Path>";
    }
}