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

public class CommandAudioDL extends BaseCommand {
    public static int queue = 0;

    public void execute(MessageEvent event) {
        File dir = new File("auddl");
        if (Objects.equals(event.getArgs()[1], "")) {
            return;
        }
        final Message[] messageVar = new Message[1];
        new Thread(() -> {
            try {
                String filename = valueOf(System.currentTimeMillis());
                Process p = Runtime.getRuntime().exec("modules/yt-dlp -x --audio-format mp3 -o " + dir.getAbsolutePath() + "/" + filename + "\".%(ext)s\" " + event.getArgs()[1]);
                BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                int i = 0;
                boolean embedCheck = false;
                while ((line = input.readLine()) != null) {
                    i++;
                    if (i >= 8 && line.contains("ETA") && !embedCheck) {
                        embedCheck = true;
                        line = line + "**";
                        event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", line.replaceAll("(.*?)ETA", "Approximate ETA:**"))).queue(message -> messageVar[0] = message);
                    }
                }
                p.waitFor();
                input.close();
                File finalFile = new File(dir + "/" + filename + ".mp3");
                float duration;
                if (finalFile.length() > 8000000) { // if the file is 8mb or over
                    messageVar[0].editMessage("File size too large, lowering bitrate...\n\nThis server hasnt unlocked the 8MB upload limit through boosts, sound quality may be suboptimal.").queue((message -> messageVar[0] = message));
                    String strDuration = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec("modules\\ffprobe -i " + finalFile + " -show_entries format=duration -v quiet -of csv=\"p=0\"").getInputStream())).readLine();
                    duration = Float.parseFloat(strDuration); // duration of the audio file
                    long desiredBitRate;
                    if (event.getGuild().getBoostCount() < 7) {
                        desiredBitRate = (long) (Math.round(7 * 8192) / duration); // 1mb lower just in case
                    } else {
                        desiredBitRate = (long) (Math.round(49 * 8192) / duration); // 1mb lower just in case
                    }
                    if (desiredBitRate < 33) { // check for ffmpeg bitrate limit
                        new File("auddl/" + filename + ".mp3").delete();
                        event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "File cannot be resized to 8MB or lower.")).queue(a -> messageVar[0].delete().queue());
                        return;
                    }
                    p = Runtime.getRuntime().exec("modules/ffmpeg -nostdin -loglevel error -i \"" + finalFile.getAbsolutePath() + "\" -b:a " + desiredBitRate + "k " + dir + "\\" + filename + "K.mp3");
                    p.waitFor();
                    try {
                        event.getTextChannel().sendFile(new File("auddl/" + filename + "K.mp3")).queue(a -> messageVar[0].delete().queue(b -> new File("auddl/" + filename + "K.mp3").delete()));
                        new File("auddl/" + filename + ".mp3").delete();
                    } catch (Exception e) {
                        Objects.requireNonNull(event.getJDA().getUserById("211789389401948160")).openPrivateChannel().queue(a -> a.sendMessage(e.getMessage()).queue());
                    }
                } else if (finalFile.length() < 8000000 || finalFile.length() < 50000000 && event.getGuild().getBoostCount() >= 7) { // if the file is not 8mb OR the file is >50MB and the server boost count greater than or equal to 7
                    try {
                        event.getTextChannel().sendFile(new File("auddl/" + filename + ".mp3")).queue(a -> messageVar[0].delete().queue(b -> new File("auddl/" + filename + ".mp3").delete()));
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
        ArrayList list = new ArrayList();
        list.add("adl");
        list.add("dl");
        list.add("audio");
        return list;
    }

    public String getCategory() {
        return "Music";
    }

    public String getName() {
        return "audiodl";
    }

    public String getDescription() {
        return "Downloads a video from a compatible URL.";
    }

    public String getParams() {
        return "<URL>";
    }
}
