package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static Bots.Main.botPrefix;

public class CommandVideoDL implements ICommand {
    @Override
    public void execute(ExecuteArgs event) {
        String message = event.getMessage().getContentRaw();
        String arg = message.replace(botPrefix + "videodl ", "").replace(" ", "");
        File dir = new File((System.getProperty("user.dir") + "\\temp\\viddl"));
        String filesize = "-f \"[filesize<\"8m\"]\" --no-playlist";
        if (event.getGuild().getBoostCount() <= 7) {
            filesize = "-f \"b\" -S \"filesize~50m\" --no-playlist";
        }
        try {
            Process process = Runtime.getRuntime()
                    .exec("yt-dlp -o \"track.%(ext)s\" " + arg + " " + filesize, null, dir); // if you can, try and make the filesize even smaller
        } catch (IOException ignored) {}
        File finalDir = new File((dir + "\\track.mp4")); // will be renamed to idk, probably something which allows the user to do multiple tracks at the same time
        new Thread(() -> {
            for (int i = 30; i > 0 && !finalDir.exists(); i--){
                try {
                    Thread.sleep(5000);
                    event.getTextChannel().sendTyping().queue();
                    if (finalDir.exists()){
                        event.getTextChannel().sendMessage(event.getMember().getAsMention()).addFile(finalDir).queue();
                        for (int i1 = 5; i1 > 0; i1--) { // i am very lazy
                            try {
                                Thread.sleep(1000);
                                Files.delete(Paths.get(String.valueOf(finalDir)));
                            } catch (Exception ignored) {}
                        }
                        break;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    @Override
    public String getName() {
        return "videodl";
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
