package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;

public class CommandBlockChannel implements BaseCommand {
    private static FileWriter file;

    @Override
    public void execute(MessageReceivedEvent event) {
        JSONObject obj = new JSONObject();
        obj.put("GuildID", event.getGuild().getIdLong());
        obj.put("ChannelID", event.getGuildChannel().getIdLong());
        try {
            file = new FileWriter(System.getProperty("user.dir") + "\\jsonStorage.json");
            file.write(obj.toJSONString());

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String getName() {
        return "blockchannel";
    }

    @Override
    public String getCategory() {
        return "Admin";
    }

    @Override
    public String getDescription() {
        return "<Channel>` - Disallows certain commands to be used in specified channel/s (eg: play)";
    }
}
