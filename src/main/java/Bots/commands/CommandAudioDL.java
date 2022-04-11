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
        try {
            Process process = Runtime.getRuntime()
                    .exec("yt-dlp -o \"track.%(ext)s\" " + arg + " -f \"[filesize<" + filesize + "]\" -x --audio-format mp3", null, dir);
        } catch (IOException ignored) {} // spat out an error but made the file anyways, ignored
        File finalDir = new File((dir + "\\track.mp3")); // will be renamed to idk, probably something which allows the user to do multiple tracks at the same time
        new Thread(() -> {
            for (int i = 10; i > 0 && !finalDir.exists(); i--){
                try {
                    event.getTextChannel().sendTyping().queue();
                    Thread.sleep(5000);
                    if (finalDir.exists()){
                        i = 0;
                        File mp4 = new File(dir + "\\track.mp4"); // will be renamed to idk, probably something which allows the user to do multiple tracks at the same time
                        for (int i1 = 10; i1 > 0; i1--) {
                            Thread.sleep(2000);
                            if (!mp4.exists()){
                                event.getTextChannel().sendMessage(event.getMember().getAsMention()).addFile(finalDir).queue();
                                for (int i2 = 5; i2 > 0; i2--) {
                                    try {
                                        Thread.sleep(1000);
                                        Files.delete(Paths.get(String.valueOf(finalDir)));
                                    } catch (Exception ignored) {}
                                }
                                break;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
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
