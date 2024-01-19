package Bots.commands;

import Bots.BaseCommand;
import Bots.Main;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.io.IOException;
import java.util.Objects;

import static Bots.Main.*;
import static java.lang.System.currentTimeMillis;

public class CommandInfo extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        int CommandCount = 0;
        int vcCount = 0;
        int memberCount = 0;
        for (BaseCommand ignored : commands) {
            CommandCount++;
        }
        for (Guild guild : event.getJDA().getGuilds()) {
            if (Objects.requireNonNull(guild.getSelfMember().getVoiceState()).inAudioChannel()) {
                vcCount++;
            }
            memberCount += guild.getMemberCount();
        }

        long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(event.getJDA().getSelfUser().getName() + " Info", null);
        eb.setColor(botColour);
        eb.appendDescription("\uD83D\uDD27  **Ram usage:** " + memoryUsed / 1024 / 1024 + "MB\n\n");
        long finalUptime = currentTimeMillis() - Main.Uptime;
        String finalTime = toTimestamp(finalUptime);
        eb.appendDescription("⏰  **Uptime:** " + finalTime + "\n\n");
        eb.appendDescription("\uD83D\uDCE1  **Guilds:** " + event.getJDA().getGuilds().size() + "\n\n");
        eb.appendDescription("\uD83D\uDC64 **Users:** " + memberCount + "\n\n");
        eb.appendDescription("\uD83D\uDCD1 **Registered Commands: **" + CommandCount + "\n\n");
        eb.appendDescription("\uD83C\uDFB5  **VCs: ** " + vcCount + "\n\n");
        eb.setFooter("Version: " + botVersion);
        long time = currentTimeMillis();
        event.replyEmbeds(response -> {
            eb.appendDescription("⏱️  **Ping:** " + (currentTimeMillis() - time) + "ms");
            response.editMessageEmbeds(eb.build());
        }, eb.build());
    }


    @Override
    public String[] getNames() {
        return new String[]{"info"};
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getCategory() {
        return Categories.General.name();
    }

    @Override
    public String getDescription() {
        return "Tells you bot info.";
    }
}
