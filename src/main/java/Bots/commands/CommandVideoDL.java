package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.entities.Message;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;
import static java.lang.String.valueOf;

public class CommandVideoDL extends BaseCommand {
    public static int queue = 0;
    Message[] messageVar = new Message[1];

    public void execute(MessageEvent event) {
        File dir = new File("viddl");
        if (Objects.equals(event.getArgs()[1], "")) {
            return;
        }
        long id = Long.parseLong("211789389401948160");
        if (Objects.requireNonNull(event.getMember()).getIdLong() != id) {
            id = Long.parseLong("260016427900076033");
            if (Objects.requireNonNull(event.getMember()).getIdLong() != id) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You dont have the permission to run this command.")).queue();
                return;
            }
        }
        new Thread(() -> {
            try {
                String filename = valueOf(System.currentTimeMillis());
                Process p = null;
                String filteredUrl = event.getArgs()[1].replace("\"","\\\"");
                if (System.getProperty("os.name").toLowerCase().contains("linux")) {
                    try {
                        p = Runtime.getRuntime().exec("yt-dlp --merge-output-format mp4 -o " + dir.getPath() + "/" + filename + "\".mp4\" \"" + filteredUrl + "\"");
                    } catch (Exception e){e.printStackTrace();}
                }
                else if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                    p = Runtime.getRuntime().exec("modules/yt-dlp --merge-output-format mp4 -o " + dir.getAbsolutePath() + "/" + filename + "\".mp4\" \"" + filteredUrl + "\"");
                }
                if (p == null){
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The file could not be downloaded because the bot is running on an unsupported operating system.")).queue();
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
                } catch (Exception ignored){}
                p.waitFor();
                input.close();
                File finalFile = new File(dir.getAbsolutePath() + "/" + filename + ".mp4");
                float duration;
                if (finalFile.length() < 8000000 || finalFile.length() < 50000000 && event.getGuild().getBoostCount() >= 7) {
                    assert messageVar[0] != null;
                    messageVar[0].delete().queue();
                    try {
                        event.getMessage().reply(finalFile.getAbsoluteFile()).queue();
                    } catch (Exception e){e.printStackTrace();event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The file could not be sent.")).queue();}
                    try {
                        finalFile.getAbsoluteFile().delete();
                    } catch (Exception e){e.printStackTrace();}
                }
                else if (finalFile.length() > 8000000) { // if the file is 8mb or over and the boost count of the guild is less than 7
                    assert messageVar[0] != null;
                    messageVar[0].editMessageEmbeds(createQuickEmbed(" ", "File size too large, lowering bitrate...\n\nThis server hasnt unlocked the 50MB upload limit through boosts, sound quality may be suboptimal.")).queue();


                    try {
                        new File("viddl/" + filename + ".mp4").getAbsoluteFile().delete();
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
    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("vdl");
        list.add("video");
        list.add("viddl");
        return list;
    }

    public String getCategory() {
        return "Dev";
    }

    public String getName() {
        return "videodl";
    }

    public String getDescription() {
        return "Downloads a video from a compatible URL.";
    }

    public String getParams() {
        return "<URL>";
    }

    public long getTimeout() {
        return 10000;
    }
}
