package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import static Bots.Main.*;

public class CommandVideoDL extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (event.getArgs().length < 2 || Objects.equals(event.getArgs()[1], "")) {
            event.replyEmbeds(createQuickError("No arguments given."));
            return;
        }
        final String ytdlp;

        final String ffprobeString;
        final String ffmpegString;
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            ytdlp = "yt-dlp";
            ffprobeString = "ffprobe";
            ffmpegString = "ffmpeg";
        } else {
            ytdlp = "modules/yt-dlp.exe";
            ffprobeString = "modules/ffprobe";
            ffmpegString = "modules/ffmpeg";
        }
        File dir = new File("viddl");
        float fileSize = 25600000;
        if (event.getGuild().getBoostCount() >= 7) {
            fileSize = 51200000;
        }
        if (event.getArgs().length == 3) {
            if (event.getArgs()[2].toLowerCase().contains("true")) {
                fileSize = 25600000;
            }
        }
        float finalFileSize = fileSize;
        new Thread(() -> {
            final MessageEvent.Response[] message = new MessageEvent.Response[1];
            event.replyEmbeds(x -> message[0] = x, createQuickEmbed("Thinking...", ""));
            String unix = String.valueOf(System.currentTimeMillis());
            String fileName = dir.getAbsolutePath() + File.separator + unix;
            String inputFile = dir.getAbsolutePath() + File.separator + unix + ".mp4";
            String outputFile = dir.getAbsolutePath() + File.separator + unix + "K.mp4";

            Process p;
            String filteredUrl = event.getArgs()[1].replaceAll("\n", "");
            try {
                String[] command = new String[]{
                        ytdlp, "--merge-output-format", "mp4", "-o", inputFile, "--match-filter", "\"duration < 7200\"", "--no-playlist", filteredUrl,
                };
                printlnTime(ytdlp, "--merge-output-format", "mp4", "-o", inputFile, "--match-filter", "\"duration < 7200\"", "--no-playlist", filteredUrl);
                p = Runtime.getRuntime().exec(command);
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                int i = 0;
                boolean check = false;
                try {
                    while ((line = input.readLine()) != null) {
                        printlnTime(line);
                        i++;
                        if (i >= 10 && line.contains("ETA") && !check) {
                            message[0].editMessageEmbeds(createQuickEmbed(" ", "**" + line.replaceAll("(.*?)ETA", "Approximate ETA:**")));
                            check = true;
                        }
                    }
                } catch (Exception ignored) {
                }
                p.waitFor();
                printlnTime(new File(fileName).exists());
                input.close();
                if (new File(inputFile).length() <= finalFileSize) {
                    try {
                        command = new String[]{
                                ffmpegString, "-nostdin", "-loglevel", "error", "-y", "-i", "-vcodec", "mpeg4", "-acodec", "-acodec aac"
                        };
                        p = Runtime.getRuntime().exec(command);
                        p.waitFor();
                        event.replyFiles(FileUpload.fromData(new File(inputFile)));
                        Thread.sleep(5000);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    new File(inputFile).delete();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                message[0].editMessageEmbeds(createQuickError(e.getMessage()));
                return;
            }

            int crf = 20;
            int bitrate = 1024;
            int attempt = 0;

            try {
                // ffprobe to get res
                String ffprobeCommand = ffprobeString + " -v error -show_entries stream=width,height -of default=noprint_wrappers=1:nokey=1 " + inputFile;
                Process ffprobe = Runtime.getRuntime().exec(ffprobeCommand);
                BufferedReader ffprobeInput = new BufferedReader(new InputStreamReader(ffprobe.getInputStream()));
                String videoWidth = ffprobeInput.readLine();
                String videoHeight = ffprobeInput.readLine();

                // change res to half
                int scaleWidth = Integer.parseInt(videoWidth);
                int scaleHeight = Integer.parseInt(videoHeight);
                String scale = scaleWidth + ":" + scaleHeight;
                // compression
                int numThreads = Runtime.getRuntime().availableProcessors() / 2;
                long time = System.currentTimeMillis();

                // check filesize
                File output = new File(inputFile);
                while (output.length() > finalFileSize) {
                    attempt++;
                    message[0].editMessageEmbeds(createQuickEmbed("\uD83D\uDCCF **Resizing the video**", "Resize attempt: " + attempt + " / 10\nCurrent Filesize: " + String.format("%.3f", (double) output.length() / 1000000) + "MB\nAiming for <= " + finalFileSize / 1000000 + "MB"));
                    if (attempt > 3) {
                        crf += 2;
                        bitrate -= 64;
                    }
                    if (attempt == 2) {
                        scale = scaleWidth / 2 + ":" + scaleHeight / 2;
                    }
                    if (attempt == 4) {
                        scale = scaleWidth / 4 + ":" + scaleHeight / 4;
                    }
                    if (attempt == 6) {
                        scale = scaleWidth / 6 + ":" + scaleHeight / 6;
                    }
                    if (attempt == 6) {
                        scale = scaleWidth / 8 + ":" + scaleHeight / 8;
                    }
                    if (attempt > 10) {
                        message[0].editMessageEmbeds(createQuickError("Failed to resize the video after 10 attempts."));
                        deleteFiles(fileName);
                        return;
                    }
                    crf += 4;
                    bitrate -= 128;
                    String command = ffmpegString + " -nostdin -loglevel error -y -i " + inputFile + " -c:v libx264 -crf " + crf + " -b:a 55k -c:a aac -b:v " + bitrate + "k -vf scale=" + scale + " -threads " + numThreads + " " + outputFile;
                    p = Runtime.getRuntime().exec(command);
                    p.waitFor();
                    output = new File(outputFile);
                }
                message[0].editMessageEmbeds(createQuickEmbed("✅ **Success**", "Resizing took " + (System.currentTimeMillis() - time) / 1000 + " seconds."));
                message[0].editMessageFiles(FileUpload.fromData(output));
                Thread.sleep(10000);
                deleteFiles(fileName);
            } catch (Exception e) {
                e.printStackTrace();
            }
            deleteFiles(fileName);
        }).start();
    }

    @Override
    public void Init() {
        Path folder = Paths.get("viddl");
        if (!Files.exists(folder)) {
            printlnTime(folder.getFileName() + " doesn't exist, creating now.");
            folder.toFile().mkdirs();
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"videodl", "vdl", "video", "viddl", "resize"};
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getOptions() {
        return "<Url>";
    }

    @Override
    public String getDescription() {
        return "Downloads a video from a compatible URL.";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "url", "URL of the video to download.", true);
        slashCommand.addOption(OptionType.BOOLEAN, "25mb", "Forced the Video to be <=25MB in size", false);
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
