package Bots.commands;

import Bots.lavaplayer.PlayerManager;
import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.nio.file.Path;
import java.nio.file.Paths;

import static Bots.Main.createQuickEmbed;

public class CommandLocalPlay implements ICommand {

    @Override
    public void execute(ExecuteArgs event) {

        if (!event.getMemberVoiceState().inAudioChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc cunt")).queue();
            return;
        }

        if (!event.getSelfVoiceState().inAudioChannel()) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMemberVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }
        String path = String.join(" ", event.getArgs());
        path = path.replace("&playfile ", "");
        Path finalpath = Paths.get(path);
        PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), String.valueOf(finalpath));
    }

    public String getCategory() {
        return "Music";
    }

    @Override
    public String getName() {
        return "playfile";
    }

    @Override
    public String helpMessage() {
        return "Plays songs from a directory.";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}