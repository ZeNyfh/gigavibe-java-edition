package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.Objects;

import static Bots.Main.*;

public class CommandAudioDL extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        long time = System.currentTimeMillis();
        if (event.getArgs().length < 2 || Objects.equals(event.getArgs()[1], "") || event.getArgs().length > 2) {
            event.replyEmbeds(createQuickError("No arguments given."));
            return;
        }
        final String ytdlp;

        final String ffprobeString;
        final String ffmpegString;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            ytdlp = "modules" + File.separator + "yt-dlp.exe";
            ffprobeString = "modules" + File.separator + "ffprobe.exe";
            ffmpegString = "modules" + File.separator + "ffmpeg.exe";
        } else {
            ytdlp = "yt-dlp";
            ffprobeString = "ffprobe";
            ffmpegString = "ffmpeg";
        }
        File auddl = new File("auddl");
        String filteredURL = event.getArgs()[1].replaceAll("\n", "");
        new Thread(() -> {
            final MessageEvent.Response[] message = new MessageEvent.Response[1];
            event.replyEmbeds(x -> message[0] = x, createQuickEmbed("Thinking...", ""));
            int targetSize = event.getGuild().getBoostCount() >= 7 ? 50 : 25;
            Path inputPath = Paths.get(auddl.getAbsolutePath() + File.separator + time + ".mp3");
            String targetFile = auddl.getAbsolutePath() + File.separator + time + "_Zen.mp3"; // filename out
            try {
                ProcessBuilder builder = new ProcessBuilder(ytdlp, "--quiet", "-x", "--audio-format", "mp3", "-o", inputPath.toString(), "--match-filter", "duration < 7200", "--no-playlist", filteredURL);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                process.waitFor();
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
                deleteFiles("auddl" + File.separator + time);
            }
            String download = "Download took: " + (System.currentTimeMillis() - time) / 1000 + " seconds.\n\n";
            if (!inputPath.toFile().exists()) {
                message[0].editMessageEmbeds(createQuickError("The file failed to download"));
                return;
            }
            if (inputPath.toFile().length() < targetSize * 1000L * 1024L) { // does not need resizing
                message[0].editMessageEmbeds(createQuickEmbed("✅ **Success**", download + "*if the file is not here, wait a few seconds for it to upload*"));
                message[0].editMessageFiles(FileUpload.fromData(inputPath));
                try {
                    Thread.sleep(10000);
                    deleteFiles("auddl" + File.separator + time);
                } catch (Exception ignored) {
                }
                return;
            }
            message[0].editMessageEmbeds(createQuickEmbed("\uD83D\uDD3D **Download Complete!**", download + "Resizing, the file will be here shortly..."));
            long newTime = System.currentTimeMillis();
            try {
                Process process = new ProcessBuilder(ffprobeString, "-v", "error", "-show_entries", "format=duration", "-of", "csv=p=0", inputPath.toString())
                        .redirectErrorStream(true)
                        .start();
                String output = new String(process.getInputStream().readAllBytes()).trim();

                float originalDuration = Float.parseFloat(output);
                long desiredBitRate = (long) (targetSize * 1024L / originalDuration);
                if (desiredBitRate < 32) { // check for ffmpeg bitrate limit
                    message[0].editMessageEmbeds(createQuickError("File cannot be resized to 25MB or lower."));
                    deleteFiles("auddl" + File.separator + time);
                    return;
                }
                new ProcessBuilder(ffmpegString, "-nostdin", "-loglevel", "error", "-i", inputPath.toString(), "-c:a", "aac", "-b:a", desiredBitRate + "k", targetFile).redirectErrorStream(true).start().waitFor();
                Thread.sleep(2000);
                String resize = "Resizing took: " + (System.currentTimeMillis() - newTime) / 1000 + " seconds.\n";
                String downloadSpeed = "Average download speed: " + new DecimalFormat("#.##").format(new File(targetFile).length() / ((newTime - time) * 1000.0)) + "MB per second.\n\n";
                message[0].editMessageEmbeds(createQuickEmbed("✅ **Success**", download + resize + downloadSpeed + "*if the file is not here, wait a few seconds for it to upload*"));
                message[0].editMessageFiles(FileUpload.fromData(new File(targetFile)));
                Thread.sleep(10000);
                deleteFiles("auddl" + File.separator + time);
            } catch (Exception e) {
                deleteFiles("auddl" + File.separator + time);
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
    public String getOptions() {
        return "<URL>";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "url", "URL of the audio to download", true);
        //TODO: System can now handle sub-commands, so this needs to be adjusted. -9382
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
