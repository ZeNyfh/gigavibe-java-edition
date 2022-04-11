package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import javafx.concurrent.Task;
import net.dv8tion.jda.api.entities.Guild;
import org.jetbrains.annotations.Async;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.IntStream;

import static Bots.Main.botPrefix;
import static Bots.Main.createQuickEmbed;
import static java.lang.String.valueOf;
import static java.lang.Thread.sleep;
import static jdk.nashorn.internal.runtime.ScriptingFunctions.exec;

public class CommandAudioDL implements ICommand {
    @Override
    public void execute(ExecuteArgs event) {
        Path musicFolder = Paths.get(System.getProperty("user.dir")+"\\temp\\music\\");
        if (!Files.exists(musicFolder)) {
            musicFolder.toFile().mkdirs();
        } // im lazy and tired
        musicFolder = Paths.get(System.getProperty("user.dir") + "\\temp\\auddl");
        if (!Files.exists(musicFolder)) {
            musicFolder.toFile().mkdirs();
        }
        String message = event.getMessage().getContentRaw();
        String arg = message.replace(botPrefix + "dl ", "").replace(" ", ""); // replacing all spaces just in case
        File dir = new File((System.getProperty("user.dir") + "\\temp\\auddl"));
        String filesize = "8m";
        if (event.getGuild().getBoostCount() <= 7) {
            filesize = "50m";
        }
        String tempfilename = event.getMember().getId() + System.currentTimeMillis();
        try {
            Runtime.getRuntime().exec("yt-dlp -o \"" + tempfilename + ".%(ext)s\" " + arg + " -f \"b\" -S \"filesize~" + filesize + "\" --part -x --audio-format mp3", null, dir);
        } catch (IOException ignored) {}
        File finalDir = new File((dir + "\\" + tempfilename + ".mp3"));
        new Thread(() -> {
            for (int i = 100; i > 0 && !finalDir.exists(); i--){
                try {
                    event.getTextChannel().sendTyping().queue();
                    Thread.sleep(5000);
                    if (finalDir.exists()){
                        i = 0;
                        File mp4 = new File(dir + "\\" + tempfilename + ".mp4");
                        for (int i1 = 150; i1 > 0; i1--) {
                            Thread.sleep(2000);
                            if (!mp4.exists()){
                                try {
                                    event.getTextChannel().sendMessage(event.getMember().getAsMention()).addFile(finalDir).queue();
                                } catch (Exception e) {event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The file was too large." )).queue();}
                                for (int i2 = 5; i2 > 0; i2--) {
                                    try {
                                        Thread.sleep(1000);
                                        Files.delete(Paths.get(valueOf(finalDir)));
                                    } catch (Exception ignored) {}
                                }
                                return;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Timed out." )).queue();
        }).start();
    }


    @Override
    public String getName() {
        return "dl";
    }

    @Override
    public String helpMessage() {
        return "Downloads a video from a compatible URL.";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
