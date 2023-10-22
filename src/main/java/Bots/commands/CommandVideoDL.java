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
import java.text.DecimalFormat;
import java.util.Objects;

import static Bots.Main.*;

public class CommandVideoDL extends BaseCommand {

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
        File viddl = new File("viddl");
        String filteredURL = event.getArgs()[1].replaceAll("\n", "");
        new Thread(() -> {
            final MessageEvent.Response[] message = new MessageEvent.Response[1];
            event.replyEmbeds(x -> message[0] = x, createQuickEmbed("(This command will be removed from zenvibe soon) Thinking...", ""));
            int targetSize = event.getGuild().getBoostCount() >= 7 ? 50 : 25;
            Path inputPath = Paths.get(viddl.getAbsolutePath() + File.separator + time + ".mp4");
            String targetFile = viddl.getAbsolutePath() + File.separator + time + "_Zen.mp4"; // filename out
            String Null = System.getProperty("os.name").toLowerCase().contains("windows") ? "NUL" : "/dev/null";
            try {
                ProcessBuilder builder = new ProcessBuilder(ytdlp, "--quiet", "--merge-output-format", "mp4", "-o", inputPath.toString(), "--match-filter", "duration < 7200", "--no-playlist", filteredURL);
                builder.redirectErrorStream(true);
                Process process = builder.start();
                process.waitFor();
                Thread.sleep(1000);

                builder = new ProcessBuilder(ffprobeString, "-v", "error", "-show_entries", "stream=codec_name", "-of", "default=noprint_wrappers=1:nokey=1", inputPath.toString());

                try {
                    process = builder.start();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));

                    String line;
                    StringBuilder output = new StringBuilder();
                    while ((line = reader.readLine()) != null) {
                        output.append(line);
                    }
                    reader.close();
                    if (!output.toString().toLowerCase().contains("mpeg4") || !output.toString().toLowerCase().contains("aac")) {
                        printlnTime("File does not contain correct codec, re-encoding");
                        Path newFile = Paths.get(viddl.getAbsolutePath() + File.separator + time + "K.mp4");
                        ProcessBuilder processBuilder = new ProcessBuilder(
                                ffmpegString, "-nostdin", "-loglevel", "error", "-y", "-i", inputPath.toString(), "-c:v", "libx264", "-c:a", "aac", newFile.toString()
                        );
                        processBuilder.redirectErrorStream(true);
                        processBuilder.start().waitFor();
                        Thread.sleep(1000);
                        inputPath.toFile().delete();
                        inputPath = newFile;
                    }
                    process.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
                deleteFiles("viddl" + File.separator + time);
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
                    deleteFiles("viddl" + File.separator + time);
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
                process = new ProcessBuilder(ffprobeString, "-v", "error", "-select_streams", "a:0", "-show_entries", "stream=bit_rate", "-of", "csv=p=0", inputPath.toString())
                        .redirectErrorStream(true)
                        .start();
                output = new String(process.getInputStream().readAllBytes()).trim();

                int originalAudioRate = Integer.parseInt(output) / 1024;
                double targetVideoRate = ((targetSize * 8192.0) / (1.1 * originalDuration)) - originalAudioRate; // used to be 1.048576, but I got an edge case.
                double targetMinSize = (originalAudioRate * originalDuration) / 8192;
                if (targetMinSize > targetSize) {
                    message[0].editMessageEmbeds(createQuickError("Could not resize the video down to " + targetSize + "."));
                    deleteFiles("viddl" + File.separator + time);
                    return;
                }
                File passLogFile = File.createTempFile(String.valueOf(time), ".log");
                // 1st pass
                ProcessBuilder processBuilder = new ProcessBuilder(
                        ffmpegString, "-nostdin", "-loglevel", "error", "-y", "-i", inputPath.toString(), "-c:v", "libx264", "-b:v", (int) targetVideoRate + "k",
                        "-pass", "1", "-an", "-f", "mp4", "-passlogfile", passLogFile.getAbsolutePath(), "-map", "0", "-map_metadata", "-1", Null
                );
                processBuilder.redirectErrorStream(true);
                processBuilder.start().waitFor();
                // 2nd pass
                processBuilder = new ProcessBuilder(
                        ffmpegString, "-nostdin", "-loglevel", "error", "-y", "-i", inputPath.toString(), "-c:v", "libx264", "-b:v", (int) targetVideoRate + "k",
                        "-pass", "2", "-c:a", "aac", "-b:a", originalAudioRate + "k", "-passlogfile", passLogFile.getAbsolutePath(), "-map", "0", "-map_metadata", "-1", targetFile
                );
                processBuilder.redirectErrorStream(true);
                processBuilder.start().waitFor();
                Files.deleteIfExists(passLogFile.toPath());
                String resize = "Resizing took: " + (System.currentTimeMillis() - newTime) / 1000 + " seconds.\n";
                String downloadSpeed = "Average download speed: " + new DecimalFormat("#.##").format(new File(targetFile).length() / ((newTime - time) * 1000.0)) + "MB per second.\n\n";
                message[0].editMessageEmbeds(createQuickEmbed("✅ **Success**", download + resize + downloadSpeed + "*if the file is not here, wait a few seconds for it to upload*"));
                message[0].editMessageFiles(FileUpload.fromData(new File(targetFile)));
                Thread.sleep(10000);
                deleteFiles("viddl" + File.separator + time);
            } catch (Exception e) {
                deleteFiles("viddl" + File.separator + time);
                e.printStackTrace();
            }
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
