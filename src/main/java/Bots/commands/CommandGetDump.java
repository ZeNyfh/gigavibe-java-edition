package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.utils.FileUpload;

import java.io.*;
import java.util.Objects;

import static Bots.Main.createQuickError;

public class CommandGetDump extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (event.getUser().getIdLong() == 211789389401948160L || event.getUser().getIdLong() == 260016427900076033L) {
            if (new File("log.txt").exists()) {
                new File("log.txt").delete();
            }
            String PID = "";
            try {
                Process p = Runtime.getRuntime().exec("jps");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    String[] jpsOutputLine = line.split(" ");
                    if (jpsOutputLine.length >= 2 && (jpsOutputLine[1].equals("bot") || jpsOutputLine[1].equals("bot.jar") || jpsOutputLine[1].equals("Main"))) {
                        PID = jpsOutputLine[0];
                    }
                }
                reader.close();
                if (Objects.equals(PID, "")) {
                    event.replyEmbeds(createQuickError("Could not get dump as the process ID was not found."));
                    return;
                }
                p = Runtime.getRuntime().exec("jstack " + PID);
                reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                BufferedWriter writer = new BufferedWriter(new FileWriter("log.txt"));

                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }

                writer.close();
                reader.close();
                p.waitFor();
            } catch (Exception e) {
                e.fillInStackTrace();
                event.replyEmbeds(createQuickError("Could not get dump.\n```\n" + e.getMessage() + "\n```"));
            }
            event.replyFiles(FileUpload.fromData(new File("log.txt")));
            try {
                Thread.sleep(10000);
                new File("log.txt").delete();
            } catch (Exception ignored) {
            }
        } else {
            event.replyEmbeds(createQuickError("You do not have the permissions for this."));
        }
    }

    @Override
    public Category getCategory() {
        return Category.Dev;
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String[] getNames() {
        return new String[]{"getdump", "dump"};
    }

    @Override
    public String getDescription() {
        return "Returns a dump of jstack.";
    }

    @Override
    public long getRatelimit() {
        return 0;
    }
}
