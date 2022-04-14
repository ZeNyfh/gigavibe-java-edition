package Bots.commands;

import Bots.BaseCommand;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static Bots.Main.createQuickEmbed;

public class CommandPlayAttachment implements BaseCommand {
    public String[] audioFiles = {"mp3", "mp4", "wav", "ogg", "flac", "m4a", "mov", "wmv", "m4a", "aac", "webm", "opus"};

    public void execute(MessageReceivedEvent event) {

        if (!event.getMember().getVoiceState().inAudioChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc cunt")).queue();
            return;
        }

        List<Message.Attachment> attachment = event.getMessage().getAttachments();
        String url = event.getMessage().getContentRaw();
        url = url.replace("&playattachment ", ""); //Whats the point? If theres no attachments, it just says no anyways, so when is this in use? -9382

        if (attachment.isEmpty()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No attachment was found.")).queue();
            return;
        }

        if (!event.getGuild().getSelfMember().getVoiceState().inAudioChannel()) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }

        if (url.contains("discordapp")) {
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), (url));
            return;
        }

        if (Arrays.toString(audioFiles).contains(Objects.requireNonNull(attachment.get(0).getFileExtension().toLowerCase()))) {
            final Path musicFolder = Paths.get(System.getProperty("user.dir") + "\\temp\\music\\");
            if (!Files.exists(musicFolder)) {
                musicFolder.toFile().mkdirs();
            }
            final String musicPath = musicFolder + "\\"; //For some reason the \ gets omitted when converting using .toString() -9382
            String unix = String.valueOf(System.currentTimeMillis());
            attachment.get(0).downloadToFile(musicPath + unix + attachment.get(0).getFileName());
            long size = attachment.get(0).getSize();
            File file = new File(musicPath + unix + attachment.get(0).getFileName());
            int num = 0;
            while (size > file.length()){
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ignored) {}
                System.out.println(num++);
            }
            System.out.println("done looping");
            String finalPath = String.valueOf(Paths.get(musicPath + unix + attachment.get(0).getFileName()));
            System.out.println(finalPath);
            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), finalPath);
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "This isn't a file that I can play")).queue();
        }
    }

    public String getCategory() {
        return "Music";
    }

    public String getName() {
        return "song";
    }

    public String getDescription() {
        return "Plays songs from a directory.";
    }
}