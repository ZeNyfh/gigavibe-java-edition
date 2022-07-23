package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.botPrefix;
import static Bots.Main.createQuickEmbed;
import static java.lang.System.currentTimeMillis;

public class CommandSendAnnouncement extends BaseCommand {

    public void execute(MessageEvent event) {
        if (Objects.requireNonNull(event.getMember()).getIdLong() != 211789389401948160L) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You dont have the permission to run this command.")).queue();
            return;
        }
        if (event.getArgs().length < 2){
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No argument given.")).queue();
            return;
        }
        for (int i = 0; i < event.getJDA().getGuilds().size(); i++) {
            Objects.requireNonNull(event.getJDA().getGuilds().get(i).getDefaultChannel()).sendMessageEmbeds(createQuickEmbed("**Announcement**", event.getMessage().getContentRaw().replace(event.getArgs()[0], ""))).queue();
        }
    }

    public String getCategory() {
        return "Dev";
    }

    public String getName() {
        return "sendannouncement";
    }

    public String getParams() {
        return "<String>";
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("announcement");
        list.add("announce");
        return list;
    }

    public String getDescription() {
        return "sends an announcement globally.";
    }

    public long getTimeout() {
        return 5000;
    }
}