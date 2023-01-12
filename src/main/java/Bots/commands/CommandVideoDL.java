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
        if (System.getProperty("os.name").toLowerCase().contains("linux")) {
            ytdlp = "yt-dlp";
        } else {
            ytdlp = "modules/yt-dlp.exe";
        }
        File dir = new File("viddl");
        new Thread(() -> {
            final MessageEvent.Response[] message = new MessageEvent.Response[1];
            event.replyEmbeds(x -> message[0] = x, createQuickEmbed("Thinking...", ""));

            String filename = dir.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp4";
            Process p;
            String filteredUrl = event.getArgs()[1].replaceAll("\n", "");
            try {
                p = Runtime.getRuntime().exec(new String[]{
                        ytdlp, "--merge-output-format", "mp4", "--audio-format", "opus", "-o", filename, "--match-filter", "duration < 3600", "--no-playlist", filteredUrl
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
                    if (line.contains("ETA") && i >= 10) {
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
                    return;
                }
                try {
                    finalFile.getAbsoluteFile().delete();
                    return;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            String desiredFileSize = "<8M";
            if (event.getGuild().getBoostCount() > 7) {
                desiredFileSize = "<50M";
            }
            finalFile.delete();
            try {
                p = Runtime.getRuntime().exec(String.format(
                        "%s -f \"[filesize%s]\" -o \"%s\" \"%s\" --match-filter \"duration < 3600\" --noplaylist",
                        ytdlp, desiredFileSize, filename, filteredUrl
                ));
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
            input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            i = 0;
            try {
                while ((line = input.readLine()) != null) {
                    i++;
                    if (i >= 10 && line.contains("ETA")) {
                        message[0].editMessageEmbeds(createQuickEmbed(" ", "Previous download was too large, retrying...\n\n**" + line.replaceAll("(.*?)ETA", "Approximate ETA:**")));
                        break;
                    }
                }
                if (input.readLine() == null) {
                    input.close();
                    message[0].editMessageEmbeds(createQuickError("The file could not be downloaded at all."));
                    try {
                        finalFile.getAbsoluteFile().delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                p.waitFor();
                input.close();
            } catch (Exception ignored) {
            }
            finalFile = new File(dir.getAbsolutePath() + "/" + filename + ".mp4");
            File finalFile1 = finalFile;
            message[0].editMessageFiles(FileUpload.fromData(finalFile.getAbsoluteFile()));
            message[0].editMessageEmbeds(); //Remove embeds
            try {
                finalFile1.getAbsoluteFile().delete();
            } catch (Exception e) {
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
        return new String[]{"videodl", "vdl", "video", "viddl"};
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getOptions() {
        return "url";
    }

    @Override
    public String getDescription() {
        return "Downloads a video from a compatible URL.";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "url", "URL of the video to download.", true);
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
