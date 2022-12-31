package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.entities.Message;
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

public class CommandAudioDL extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (event.getArgs().length < 2 || Objects.equals(event.getArgs()[1], "")) {
            event.replyEmbeds(createQuickError("No arguments given."));
            return;
        }
        final String ytdlp;
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            ytdlp = "yt-dlp";
        } else {
            ytdlp = "modules/yt-dlp.exe";
        }
        File dir = new File("auddl");
        new Thread(() -> {
            final MessageEvent.Response[] message = new MessageEvent.Response[1];
            event.replyEmbeds(x -> message[0] = x, createQuickEmbed("Thinking...", ""));

            String filename = dir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".ogg";
            Process p;
            String filteredUrl = event.getArgs()[1].replaceAll("\n", "");
            try {
                p = Runtime.getRuntime().exec(new String[]{
                        ytdlp, "-x", "--audio-format", "vorbis", "-o", filename, "--match-filter", "duration < 3600", "--no-playlist", filteredUrl
                });
            } catch (Exception e) {
                e.printStackTrace();
                message[0].editMessageEmbeds(createQuickError("Something's gone horribly wrong..."));
                return;
            }
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            int i = 0;
            try {
                while ((line = input.readLine()) != null) {
                    i++;
                    if (i >= 10 && line.contains("ETA")) {
                        message[0].editMessageEmbeds(createQuickEmbed(" ", "**" + line.replaceAll("(.*?)ETA", "Approximate ETA:**")));
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                p.waitFor();
                input.close();
            } catch (Exception ignored) {
            }
            File finalFile = new File(filename);
            float duration;
            if (!finalFile.exists()) {
                message[0].editMessageEmbeds(createQuickError("No file was downloaded"));
                return;
            }
            //Beyond this point, we assume that activeNotice[0] just has to exist and continue on as such
            if (finalFile.length() < 8192000 || finalFile.length() < 51200000 && event.getGuild().getBoostCount() >= 7) {
                try {
                    message[0].editMessageFiles(FileUpload.fromData(finalFile.getAbsoluteFile()));
                    message[0].editMessageEmbeds(); //Remove embeds
                } catch (Exception e) {
                    e.printStackTrace();
                    message[0].editMessageEmbeds(createQuickError("The file could not be sent."));
                    finalFile.getAbsoluteFile().delete();
                    return;
                }
            } else if (finalFile.length() > 8192000) { // this is where the file resizing process happens.
                message[0].editMessageEmbeds(createQuickEmbed(" ", "File size too large for this server, lowering bitrate..."));
                String strDuration = "";
                try {
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        strDuration = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("modules\\ffprobe -i \"" + finalFile.getAbsolutePath() + "\" -show_entries format=duration -v quiet -of csv=\"p=0\"").getInputStream())).readLine();
                    } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                        strDuration = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("ffprobe -i " + finalFile.getAbsolutePath() + " -show_entries format=duration -v quiet -of csv=p=0").getInputStream())).readLine();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                try {
                    duration = Float.parseFloat(strDuration); // duration of the audio file
                } catch (Exception ignored) {
                    message[0].editMessageEmbeds(createQuickError("Failed to get duration of track, stopping the download."));
                    finalFile.getAbsoluteFile().delete();
                    return;
                }
                long desiredBitRate = (long) (65536 / duration); // 8mb
                if (event.getGuild().getBoostCount() > 7) {
                    desiredBitRate = (long) (409600 / duration); // 50mb
                }
                if (desiredBitRate < 45) { // check for ffmpeg bitrate limit
                    message[0].editMessageEmbeds(createQuickError("File cannot be resized to 8MB or lower."));
                    try {
                        finalFile.getAbsoluteFile().delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                } // if the desired bitrate is 45 or more
                File finalFinalFile = new File(finalFile.getAbsolutePath().substring(0, finalFile.getAbsolutePath().length() - 4) + "K.ogg");
                try {
                    if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                        p = Runtime.getRuntime().exec("modules/ffmpeg -nostdin -loglevel error -i \"" + finalFile.getAbsolutePath() + "\" -b:a " + desiredBitRate + "k " + finalFinalFile);
                    } else if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                        p = Runtime.getRuntime().exec("ffmpeg -nostdin -loglevel error -i " + finalFile.getAbsolutePath() + " -b:a " + desiredBitRate + "k " + finalFinalFile);
                    }
                    p.waitFor();
                    input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                message[0].editMessageFiles(FileUpload.fromData(finalFinalFile.getAbsoluteFile()));
                message[0].editMessageEmbeds(); //Remove embeds
                finalFinalFile.getAbsoluteFile().delete();
                finalFile.getAbsoluteFile().delete();
                return;
            }
            message[0].editMessageFiles(FileUpload.fromData(finalFile.getAbsoluteFile()));
            message[0].editMessageEmbeds(); //Remove embeds
            finalFile.getAbsoluteFile().delete();
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
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "url", "URL of the audio to download", true);
        //TODO: System can now handle sub-commands, so this needs to be adjusted. -9382
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
