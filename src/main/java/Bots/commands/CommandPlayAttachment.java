package Bots.commands;

import Bots.lavaplayer.PlayerManager;
import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static Bots.Main.createQuickEmbed;


public class CommandPlayAttachment implements ICommand {

    public String[] audioFiles = {"mp3", "mp4", "wav", "ogg", "flac", "m4a", "mov", "wmv", "m4a", "aac", "webm", "opus"};

    @Override
    public void execute(ExecuteArgs event) {

        if(!event.getMemberVoiceState().inAudioChannel()){
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc cunt")).queue();
            return;
        }

        List<Message.Attachment> attachment = event.getMessage().getAttachments();
        String url = String.join(" ", event.getArgs());
        url = url.replace("&playattachment ", "");

        if (attachment.isEmpty()){
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No attachment was found.")).queue();
            return;
        }

        if(!event.getSelfVoiceState().inAudioChannel()){
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMemberVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }

        if (url.contains("discordapp")) {
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), (url));
            return;
        }

        if (Arrays.toString(audioFiles).contains(Objects.requireNonNull(attachment.get(0).getFileExtension().toLowerCase()))) {
            attachment.get(0).downloadToFile("C:\\Users\\ZeNyfh\\Desktop\\tempmusic\\" + attachment.get(0).getFileName());
            String path = "C:\\Users\\ZeNyfh\\Desktop\\tempmusic\\" + attachment.get(0).getFileName();
            String finalPath = String.valueOf(Paths.get(path));
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), (finalPath));
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), (finalPath)); // failsafe, causes 0 issues
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "This isn't a file that I can play")).queue();
        }

    }

    @Override
    public String getName() {
        return "song";
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