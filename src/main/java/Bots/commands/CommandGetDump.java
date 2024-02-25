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
            new File("temp/dump.txt").delete();
            try {
                Process p = Runtime.getRuntime().exec("jps");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));

                String line;
                String PID = "";
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
                BufferedWriter writer = new BufferedWriter(new FileWriter("temp/dump.txt"));

                while ((line = reader.readLine()) != null) {
                    writer.write(line);
                    writer.newLine();
                }

                writer.close();
                reader.close();
                p.waitFor();
            } catch (Exception e) {
                e.printStackTrace();
                event.replyEmbeds(createQuickError("Could not get dump.\n```\n" + e.getMessage() + "\n```"));
            }
            event.deferReply();
            event.replyFiles(FileUpload.fromData(new File("temp/dump.txt")));
        } else {
            event.replyEmbeds(createQuickError("You do not have the permissions for this."));
        }
    }

    @Override
    public Category getCategory() {
        return Category.Dev;
    }

    @Override
    public String[] getNames() {
        return new String[]{"getdump", "dump"};
    }

    @Override
    public String getDescription() {
        return "Returns a dump of jstack.";
    }

}
