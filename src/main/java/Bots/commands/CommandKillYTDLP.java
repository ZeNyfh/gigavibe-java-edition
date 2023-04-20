package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.io.File;

import static Bots.Main.*;
import static java.lang.System.currentTimeMillis;

public class CommandKillYTDLP extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (event.getUser().getIdLong() == 211789389401948160L) {
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                try {
                    Runtime.getRuntime().exec("taskkill /F /IM yt-dlp.exe");
                    deleteFiles(new File("auddl" + File.separator).getAbsolutePath());
                    deleteFiles(new File("viddl" + File.separator).getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    event.replyEmbeds(createQuickError("Could not kill yt-dlp processes\n```\n" + e.getMessage() + "\n```"));
                }
            } else {
                try {
                    Runtime.getRuntime().exec("pkill -f yt-dlp");
                    deleteFiles(new File("auddl" + File.separator).getAbsolutePath());
                    deleteFiles(new File("viddl" + File.separator).getAbsolutePath());
                } catch (Exception e) {
                    e.printStackTrace();
                    event.replyEmbeds(createQuickError("Could not kill yt-dlp processes\n```\n" + e.getMessage() + "\n```"));
                }
            }
            event.replyEmbeds(createQuickEmbed("✅ **Success**", "Killed all processes and cleared the directories."));
        } else {
            event.replyEmbeds(createQuickError("You do not have the permissions for this."));
        }
    }

    @Override
    public String getCategory() {
        return "Dev";
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String[] getNames() {
        return new String[]{"killytdlp", "killall", "killdl"};
    }

    @Override
    public String getDescription() {
        return "Kills YT-DLP processes, should only be used if a download is hanging.";
    }

    @Override
    public long getRatelimit() {
        return 0;
    }
}
