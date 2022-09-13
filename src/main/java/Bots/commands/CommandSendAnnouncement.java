package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

public class CommandSendAnnouncement extends BaseCommand {

    public void execute(MessageEvent event) {
        if (Objects.requireNonNull(event.getMember()).getIdLong() != 211789389401948160L) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("You dont have the permission to run this command.")).queue();
            return;
        }
        if (event.getArgs().length < 2) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No argument given.")).queue();
            return;
        }
        for (int i = 0; i < event.getJDA().getGuilds().size(); i++) {
            Objects.requireNonNull(event.getJDA().getGuilds().get(i).getDefaultChannel()).asTextChannel().sendMessageEmbeds(createQuickEmbed("**Announcement**", event.getMessage().getContentRaw().replace(event.getArgs()[0], ""))).queue();
            try {
                Thread.sleep(10000);
            } catch (Exception ignored) {
            }
        }
    }

    public String getCategory() {
        return "dev";
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