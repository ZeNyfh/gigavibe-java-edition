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
        final Message[] messageVar = new Message[1];
        new Thread(() -> {
            try {
                String filename = valueOf(System.currentTimeMillis());
                Process p = Runtime.getRuntime().exec("modules/yt-dlp --merge-output-format mp4 -o " + dir.getAbsolutePath() + "/" + filename + "\".mp4\" " + event.getArgs()[1]);
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                int i = 0;
                boolean embedCheck = false;
                while ((line = input.readLine()) != null) {
                    i++;
                    if (i >= 8 && line.contains("ETA") && !embedCheck) {
                        embedCheck = true;
                        line = line + "**";
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", line.replaceAll("(.*?)ETA", "Approximate ETA:**"))).queue(message -> messageVar[0] = message);
                    }
                }
                p.waitFor();
                input.close();
                File finalFile = new File(dir + "/" + filename + ".mp4");
                if (finalFile.length() > 8000000) { // if the file is 8mb or over
                    messageVar[0].editMessage("File size too large, lowering bitrate...\n\nThis server hasnt unlocked the 8MB upload limit through boosts, sound quality may be suboptimal.").queue((message -> messageVar[0] = message));
                    long desiredBitRate;
                    if (event.getGuild().getBoostCount() < 7) {
                        desiredBitRate = (Math.round(7 * 8192) / (finalFile.length() / 8192)); // 1mb lower just in case
                    } else {
                        desiredBitRate = (Math.round(49 * 8192) / (finalFile.length() / 8192)); // 1mb lower just in case
                    }
                    if (desiredBitRate < 33) { // check for ffmpeg bitrate limit
                        String strWidth = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("modules\\ffprobe -v error -show_entries stream=width,height -of default=noprint_wrappers=1:nokey=1 " + finalFile).getInputStream())).readLine();
                        long desiredWidth = Long.parseLong(strWidth) / 2;
                        messageVar[0].editMessage("File size still too large, lowering bitrate and resolution...\n\nThis server hasnt unlocked the 8MB upload limit through boosts, sound quality may be suboptimal.").queue((message -> messageVar[0] = message));
                        p = Runtime.getRuntime().exec("modules/ffmpeg -nostdin -loglevel error -i \"" + finalFile.getAbsolutePath() + "\" -vf \"scale= " + desiredWidth + ":-2\" " + dir + "/" + filename + "R.mp4");
                        p.waitFor();
                        File finalFinalFile = new File(dir + "/" + filename + "R.mp4");
                        if (finalFinalFile.length() > 8000000) { // if the Finalfile is 8mb or over
                            if (event.getGuild().getBoostCount() < 7) {
                                desiredBitRate = (Math.round(7 * 8192) / (finalFinalFile.length() / 8192)); // 1mb lower just in case
                            } else {
                                desiredBitRate = (Math.round(49 * 8192) / (finalFinalFile.length() / 8192)); // 1mb lower just in case
                            }
                            p = Runtime.getRuntime().exec("modules/ffmpeg -nostdin -loglevel error -i \"" + finalFinalFile.getAbsolutePath() + "\" -b:v " + desiredBitRate + "k " + dir + "\\" + filename + "KR.mp4");
                            p.waitFor();
                            File KRFile = new File(dir + "\\" + filename + "KR.mp4");
                            p = Runtime.getRuntime().exec("modules/ffmpeg -nostdin -loglevel error -i \"" + KRFile.getAbsolutePath() + "\" -b:a 33k " + dir + "\\" + filename + "KRF.mp4"); // they dont need good audio quality, they want video (im lazy)
                            p.waitFor();
                            try {
                                event.getChannel().asTextChannel().sendFile(new File("viddl/" + filename + "KRF.mp4")).queue(a -> messageVar[0].delete().queue(b -> new File("viddl/" + filename + "KRF.mp4").delete()));
                                new File("viddl/" + filename + "KR.mp4").delete();
                                new File("viddl/" + filename + "K.mp4").delete();
                                new File("viddl/" + filename + ".mp4").delete();
                                return;
                            } catch (Exception e) {
                                event.getJDA().getUserById("211789389401948160");
                                Objects.requireNonNull(event.getJDA().getUserById("211789389401948160")).openPrivateChannel().queue(a -> a.sendMessage(e.getMessage()).queue());
                                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "File could not be lowered to under 8mb")).queue();
                            }
                        } else {
                            try {
                                event.getChannel().asTextChannel().sendFile(new File("viddl/" + filename + "KR.mp4")).queue(a -> messageVar[0].delete().queue(b -> new File("viddl/" + filename + "R.mp4").delete()));
                                new File("viddl/" + filename + "K.mp4").delete();
                                new File("viddl/" + filename + ".mp4").delete();
                            } catch (Exception ignored) {
                            }
                            return;
                        }
                        new File("viddl/" + filename + ".mp4").delete();
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "File cannot be resized to 8MB or lower.")).queue(a -> messageVar[0].delete().queue());
                        return;
                    }
                    p = Runtime.getRuntime().exec("modules/ffmpeg -nostdin -loglevel error -i \"" + finalFile.getAbsolutePath() + "\" -b:v " + desiredBitRate + "k " + dir + "\\" + filename + "K.mp4");
                    p.waitFor();
                    try {
                        event.getChannel().asTextChannel().sendFile(new File("viddl/" + filename + "K.mp4")).queue(a -> messageVar[0].delete().queue(b -> new File("viddl/" + filename + "K.mp4").delete()));
                        new File("viddl/" + filename + ".mp4").delete();
                    } catch (Exception e) {
                        Objects.requireNonNull(event.getJDA().getUserById("211789389401948160")).openPrivateChannel().queue(a -> a.sendMessage(e.getMessage()).queue());
                    }
                } else if (finalFile.length() < 8000000 || finalFile.length() < 50000000 && event.getGuild().getBoostCount() >= 7) { // if the file is not 8mb OR the file is >50MB and the server boost count greater than or equal to 7
                    try {
                        event.getChannel().asTextChannel().sendFile(new File("viddl/" + filename + ".mp4")).queue(a -> messageVar[0].delete().queue(b -> new File("viddl/" + filename + ".mp4").delete()));
                    } catch (Exception e) {
                        Objects.requireNonNull(event.getJDA().getUserById("211789389401948160")).openPrivateChannel().queue(a -> a.sendMessage(e.getMessage()).queue());
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
