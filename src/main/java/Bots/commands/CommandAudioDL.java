package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static Bots.Main.botPrefix;
import static Bots.Main.createQuickEmbed;
import static java.lang.String.valueOf;

public class CommandAudioDL implements ICommand {
    public static int queue = 0;

    @Override
    public void execute(ExecuteArgs event) {
        Path musicFolder = Paths.get(System.getProperty("user.dir") + "\\temp\\music\\");
        if (!Files.exists(musicFolder)) {
            musicFolder.toFile().mkdirs();
        }
        musicFolder = Paths.get(System.getProperty("user.dir") + "\\temp\\auddl");
        if (!Files.exists(musicFolder)) {
            musicFolder.toFile().mkdirs();
        }
        String message = event.getMessage().getContentRaw();
        String arg = message.replace(botPrefix + "dl ", "").replace(" ", "");
        File dir = new File((System.getProperty("user.dir") + "\\temp\\auddl"));
        String filesize = "8m";
        if (event.getGuild().getBoostCount() <= 7) {
            filesize = "50m";
        }
        String finalFilesize = filesize;
        new Thread(() -> {
            for (int loop = 900; loop > 0 && queue >= 1; loop--) { // queue system
                try {
                    Thread.sleep(2000);
                } catch (Exception ignored) {
                }
                event.getTextChannel().sendTyping().queue();
                System.out.println(queue);
            }
            queue++;
            String tempfilename = event.getMember().getId() + System.currentTimeMillis();
            try {
                Runtime.getRuntime().exec("yt-dlp -o \"" + tempfilename + ".%(ext)s\" " + arg + " -f \"b\" -S \"filesize~" + finalFilesize + "\" --part -x --audio-format mp3", null, dir);
            } catch (IOException ignored) {
            }
            File finalDir = new File((dir + "\\" + tempfilename + ".mp3"));
            for (int i = 100; i > 0 && !finalDir.exists(); i--) { // download process
                try {
                    event.getTextChannel().sendTyping().queue();
                    Thread.sleep(5000);
                    if (finalDir.exists()) {
                        i = 0;
                        File mp4 = new File(dir + "\\" + tempfilename + ".mp4"); // check for if the file is fully written to
                        for (int i1 = 150; i1 > 0; i1--) {
                            Thread.sleep(2000);
                            File part = new File("\\" + tempfilename + ".part"); // error handler for if the file fails to download
                            if (i1 <= 130 && part.exists()) {
                                queue--;
                                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The download failed to start, try again.")).queue();
                                Files.delete(Paths.get(String.valueOf(part)));
                                return;
                            }
                            if (!mp4.exists()) {
                                queue--;
                                try {
                                    event.getTextChannel().sendMessage(event.getMember().getAsMention()).addFile(finalDir).queue();
                                } catch (Exception e) {
                                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The file was too large.")).queue();
                                }
                                for (int i2 = 5; i2 > 0; i2--) {  // file deletion
                                    try {
                                        Thread.sleep(1000);
                                        Files.delete(Paths.get(valueOf(finalDir)));
                                    } catch (Exception ignored) {
                                    }
                                }
                                return;
                            }
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    queue--;
                } catch (IOException ignored) {
                    queue--;
                }
            }
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Timed out.")).queue();
            queue--;
        }).start();
    }

    public String getCategory() {
        return "Music";
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
