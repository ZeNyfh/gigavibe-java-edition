package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static Bots.Main.botPrefix;
import static Bots.Main.createQuickEmbed;

public class CommandVideoDL extends BaseCommand {
    public static int queue = 0;

    public void execute(MessageEvent event) {
        String message = event.getMessage().getContentRaw();
        String arg = message.replace(botPrefix + "videodl ", "").replace(" ", "");
        File dir = new File((System.getProperty("user.dir") + "\\temp\\viddl"));
        if (arg.equals("") || arg.equals(" ")){
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No argument found.")).queue();
            return;
        }
        String filesize = "-f \"[filesize<\"8m\"]\" --no-playlist"; // these parameters will be changed for file size
        if (event.getGuild().getBoostCount() <= 7) {
            filesize = "-f \"b\" -S \"filesize~50m\" --no-playlist"; // these parameters will be changed for file size
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
                Runtime.getRuntime().exec("yt-dlp -o \"" + tempfilename + ".%(ext)s\" " + arg + " " + finalFilesize, null, dir); // if you can, try and make the filesize even smaller
            } catch (IOException ignored) {
            }
            File finalDir = new File((dir + "\\" + tempfilename + ".mp4"));
            for (int i = 150; i > 0 && !finalDir.exists(); i--) {
                File part = new File("\\" + tempfilename + ".part");
                if (i <= 130 && part.exists()) { // error handler for if the file fails to download
                    queue--;
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The download failed to start, try again.")).queue();
                    try {
                        Files.delete(Paths.get(String.valueOf(part)));
                    } catch (IOException ignored) {
                    }
                    return;
                }
                try {
                    Thread.sleep(5000);  // download process
                    event.getTextChannel().sendTyping().queue();
                    if (finalDir.exists()) {
                        try {
                            event.getTextChannel().sendMessage(event.getMember().getAsMention()).addFile(finalDir).queue();
                        } catch (Exception e) {
                            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The file was too large.")).queue();
                        }
                        for (int i1 = 5; i1 > 0; i1--) { // file deletion
                            try {
                                Thread.sleep(1000);
                                Files.delete(Paths.get(String.valueOf(finalDir)));
                            } catch (Exception ignored) {
                            }
                        }
                        queue--;
                        return;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Timed out.")).queue();
        }).start();
    }

    public String getCategory() {
        return "Music";
    }

    public String getName() {
        return "videodl";
    }

    public String getDescription() {
        return "Downloads a video from a compatible URL.";
    }

    public String getParams() { return "<URL>";}
}
