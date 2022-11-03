package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static Bots.Main.*;
import static java.lang.String.valueOf;

public class CommandAudioDL extends BaseCommand {
    public static int queue = 0;
    Message[] messageVar = new Message[1];

    @Override
    public void execute(MessageEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No arguments given.")).queue();
            return;
        }
        File dir = new File("auddl");
        if (event.getArgs().length < 2 || Objects.equals(event.getArgs()[1], "")) {
            return;
        }
        new Thread(() -> {
            try {
                String filename = valueOf(System.currentTimeMillis());
                Process p = null;
                String filteredUrl = event.getArgs()[1].replace("\"", "\\\"");
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    try {
                        p = Runtime.getRuntime().exec("yt-dlp -x --audio-format vorbis -o " + dir.getPath() + "/" + filename + ".%(ext)s " + filteredUrl + "--match-filter \\\"duration < 3600\\\" --no-playlist");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    p = Runtime.getRuntime().exec("modules/yt-dlp -x --audio-format vorbis -o " + dir.getAbsolutePath() + "/" + filename + ".%(ext)s " + filteredUrl + " --match-filter \"duration < 3600\" --no-playlist");
                }
                if (p == null) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The file could not be downloaded because the bot is running on an unsupported operating system.")).queue();
                    return;
                }
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                int i = 0;
                try {
                    while ((line = input.readLine()) != null) {
                        i++;
                        if (i >= 10 && line.contains("ETA")) {
                            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "**" + line.replaceAll("(.*?)ETA", "Approximate ETA:**"))).queue(messageETA -> messageVar[0] = messageETA);
                            break;
                        }
                    }
                } catch (Exception ignored) {
                }
                p.waitFor();
                input.close();
                File finalFile = new File(dir.getAbsolutePath() + "/" + filename + ".ogg");
                float duration;
                if (finalFile.length() < 8192000 || finalFile.length() < 51200000 && event.getGuild().getBoostCount() >= 7) {
                    assert messageVar[0] != null;
                    messageVar[0].delete().queue();
                    try {
                        event.getMessage().replyFiles(FileUpload.fromData(finalFile.getAbsoluteFile())).queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The file could not be sent.")).queue();
                    }
                    try {
                        finalFile.getAbsoluteFile().delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (finalFile.length() > 8192000) { // if the file is 8mb or over and the boost count of the guild is less than 7
                    assert messageVar[0] != null;
                    messageVar[0].editMessageEmbeds(createQuickEmbed(" ", "File size too large, lowering bitrate...\n\nThis server hasnt unlocked the 50MB upload limit through boosts, sound quality may be suboptimal.")).queue();
                    String strDuration = "";
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        strDuration = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("modules\\ffprobe -i \"" + finalFile.getAbsolutePath() + "\" -show_entries format=duration -v quiet -of csv=\"p=0\"").getInputStream())).readLine();
                    } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                        strDuration = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ffprobe -i " + finalFile.getAbsolutePath() + " -show_entries format=duration -v quiet -of csv=p=0").getInputStream())).readLine();
                    }
                    try {
                        duration = Float.parseFloat(strDuration); // duration of the audio file
                    } catch (Exception ignored) {
                        messageVar[0].editMessageEmbeds(createQuickError("Failed to get duration of track, stopping the download.")).queue();
                        finalFile.getAbsoluteFile().delete();
                        return;
                    }
                    long desiredBitRate = 0;
                    if (event.getGuild().getBoostCount() < 7) {
                        desiredBitRate = (long) (Math.round(8 * 8192) / duration); // 8mb
                    } else {
                        desiredBitRate = (long) (Math.round(50 * 8192) / duration); // 50mb
                    }
                    if (desiredBitRate < 45) { // check for ffmpeg bitrate limit
                        try {
                            new File("auddl/" + filename + ".ogg").getAbsoluteFile().delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        assert messageVar[0] != null;
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("File cannot be resized to 8MB or lower.")).queue(a -> messageVar[0].delete().queue());
                        finalFile.delete();
                        return;
                    } // if the desired bitrate is 45 or more
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        p = Runtime.getRuntime().exec("modules/ffmpeg -nostdin -loglevel error -i \"" + finalFile.getAbsolutePath() + "\" -b:a " + desiredBitRate + "k " + dir + "\\" + filename + "K.ogg");
                    } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                        p = Runtime.getRuntime().exec("ffmpeg -nostdin -loglevel error -i " + finalFile.getAbsolutePath() + " -b:a " + desiredBitRate + "k " + dir.getAbsolutePath() + "/" + filename + "K.ogg");
                    }
                    p.waitFor();
                    try {
                        finalFile = new File("auddl/" + filename + "K.ogg");
                        assert messageVar[0] != null;
                        File finalFile1 = finalFile;
                        event.getMessage().replyFiles(FileUpload.fromData(finalFile.getAbsoluteFile())).queue(a -> messageVar[0].delete().queue(b -> finalFile1.getAbsoluteFile().delete()));
                    } catch (Exception e) {
                        messageVar[0].editMessageEmbeds(createQuickError("Could not send the file.")).queue();
                        finalFile.delete();
                        e.printStackTrace();
                    }
                    try {
                        new File("auddl/" + filename + ".ogg").getAbsoluteFile().delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void Init() {
        Path folder = Paths.get("auddl");
        if (!Files.exists(folder)) {
            printlnTime(folder.getFileName() + " doesn't exist, creating now.");
            folder.toFile().mkdirs();
        }
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String[] getNames() {
        return new String[]{"audiodl", "adl", "dl", "audio"};
    }

    @Override
    public String getDescription() {
        return "Downloads a video from a compatible URL.";
    }

    @Override
    public String getParams() {
        return "<URL>";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
