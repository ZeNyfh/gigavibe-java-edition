package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;
import static java.lang.String.valueOf;

public class CommandVideoDL extends BaseCommand {
    public static int queue = 0;
    Message[] messageVar = new Message[1];

    @Override
    public void execute(MessageEvent event) {
        if (event.getArgs().length < 1) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No arguments given.")).queue();
            return;
        }
        File dir = new File("viddl");
        if (Objects.equals(event.getArgs()[1], "")) {
            return;
        }
        new Thread(() -> {
            try {
                String filename = valueOf(System.currentTimeMillis());
                Process p = null;
                String filteredUrl = event.getArgs()[1].replaceAll("\n", "");
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    try {
                        p = Runtime.getRuntime().exec("yt-dlp --remux-video mp4 -o " + dir.getAbsolutePath() + "/" + filename + ".mp4 " + filteredUrl); // +  "--match-filter \\\"duration < 3600\\\" --no-playlist");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    p = Runtime.getRuntime().exec("modules/yt-dlp --merge-output-format mp4 --audio-format vorbis -o " + dir.getAbsolutePath() + "/" + filename + "\".mp4\" \"" + filteredUrl + "\" --match-filter \"duration < 3600\" --no-playlist"); // works on windows as of now
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
                        if (i < 5 && line.contains("ETA")) {
                            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "**" + line.replaceAll("(.*?)ETA", "Approximate ETA:**"))).queue(messageETA -> messageVar[0] = messageETA);
                        }
                    }
                } catch (Exception ignored) {
                }
                p.waitFor();
                input.close();
                File finalFile = new File(dir.getAbsolutePath() + "/" + filename + ".mp4");
                if (finalFile.length() < 8192000 || finalFile.length() < 51200000 && event.getGuild().getBoostCount() >= 7) {
                    try {
                        messageVar[0].delete().queue();
                        event.getMessage().replyFiles(FileUpload.fromData(finalFile.getAbsoluteFile())).queue();
                    } catch (Exception e) {
                        e.printStackTrace();
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The file could not be sent.")).queue();
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
                assert messageVar[0] != null;
                finalFile.delete();
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    try {
                        p = Runtime.getRuntime().exec("yt-dlp -f \\\"[filesize" + desiredFileSize + "]\\\" -o " + dir.getAbsolutePath() + "/" + filename + ".mp4 " + filteredUrl); // + " --match-filter \"duration < 3600\" --no-playlist");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    p = Runtime.getRuntime().exec("modules/yt-dlp -f \"[filesize" + desiredFileSize + "]\" -o " + dir.getAbsolutePath() + "/" + filename + "\".mp4\" \"" + filteredUrl + "\" --match-filter \"duration < 3600\" --no-playlist"); // works on windows as of now
                }
                input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                i = 0;
                try {
                    while ((line = input.readLine()) != null) {
                        i++;
                        if (i >= 10 && line.contains("ETA")) {
                            messageVar[0].editMessageEmbeds(createQuickEmbed(" ", "Previous download was too large, retrying...\n\n**" + line.replaceAll("(.*?)ETA", "Approximate ETA:**"))).queue(messageETA -> messageVar[0] = messageETA);
                            break;
                        }
                        if (i == 2 && line.contains("ETA")) {
                            messageVar[0].editMessageEmbeds(createQuickEmbed(" ", "Previous download was too large, retrying...\n\n**" + line.replaceAll("(.*?)ETA", "Approximate ETA:**"))).queue(messageETA -> messageVar[0] = messageETA);
                        }
                    }
                    if (input.readLine() == null) {
                        messageVar[0].delete().queue();
                        input.close();
                        event.getMessage().replyEmbeds(createQuickError("The file could not be downloaded at all.")).queue();
                        try {
                            finalFile.getAbsoluteFile().delete();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                } catch (Exception ignored) {
                }
                p.waitFor();
                finalFile = new File(dir.getAbsolutePath() + "/" + filename + ".mp4");
                input.close();
                File finalFile1 = finalFile;
                event.getMessage().replyFiles(FileUpload.fromData(finalFile.getAbsoluteFile())).queue(sent -> {
                    try {
                        finalFile1.getAbsoluteFile().delete();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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
    public String getDescription() {
        return "Downloads a video from a compatible URL.";
    }

    @Override
    public String getParams() {
        return "<URL>";
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }
}
