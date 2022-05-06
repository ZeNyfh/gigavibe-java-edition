package Bots.commands;

import Bots.BaseCommand;
import Bots.Main;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

import static Bots.Main.*;
import static java.lang.System.currentTimeMillis;

public class CommandBotInfo extends BaseCommand {
    @Override
    public void execute(MessageReceivedEvent event) throws IOException {
        int CommandCount = 0;
        long id = Long.parseLong("211789389401948160"); // why
        if (Objects.requireNonNull(event.getMember()).getIdLong() != id) {
            id = Long.parseLong("260016427900076033");
            if (Objects.requireNonNull(event.getMember()).getIdLong() != id) {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You dont have the permission to run this command.")).queue();
                return;
            }
        }
        for (BaseCommand ignored : commands) {
            CommandCount++;
        }
        long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(event.getJDA().getSelfUser().getName() + " Info", null);
        eb.setColor(new Color(0, 0, 255));
        eb.appendDescription("\uD83D\uDD27  **Ram usage:** " + memoryUsed / 1000 / 1000 + "MB\n\n");
        //
        long finalUptime = currentTimeMillis() - Main.Uptime;
        String finalTime = toTimestamp(finalUptime);
        eb.appendDescription("⏰  **Uptime:** " + finalTime + "\n\n");
        //
        eb.appendDescription("\uD83D\uDCE1  **Guilds:** " + event.getJDA().getGuilds().size() + "\n\n");
        //
        eb.appendDescription("\uD83D\uDCD1 **Registered Commands: **" + CommandCount + "\n\n");
        //
        long time = currentTimeMillis();
        event.getTextChannel().sendMessageEmbeds(eb.build()).queue(response -> {
            eb.appendDescription("⏱️  **Ping:** " + (currentTimeMillis() - time) + "ms");
            response.editMessageEmbeds(eb.build()).queue();
        });
    }


    @Override
    public String getName() {
        return "info";
    }

    @Override
    public String getCategory() {
        return "Admin";
    }

    @Override
    public String getDescription() {
        return "- Tells you bot info.";
    }
}
