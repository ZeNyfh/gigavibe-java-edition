package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.Main;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;

import java.util.Objects;

import static Bots.CommandEvent.localise;
import static Bots.Main.*;
import static java.lang.System.currentTimeMillis;

public class CommandInfo extends BaseCommand {
    final int CommandCount = commandNames.size();

    @Override
    public void execute(CommandEvent event) {
        int vcCount = 0;
        int memberCount = 0;
        int playingCount = 0;
        for (Guild guild : event.getJDA().getGuilds()) {
            if (Objects.requireNonNull(guild.getSelfMember().getVoiceState()).inAudioChannel()) {
                vcCount++;
            }
            if (PlayerManager.getInstance().getMusicManager(guild).audioPlayer.getPlayingTrack() != null) {
                playingCount++;
            }
            memberCount += guild.getMemberCount();
        }

        long memoryUsed = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(event.getJDA().getSelfUser().getName() + " " + localise("CommandInfo.info"), null);
        eb.setColor(botColour);
        eb.appendDescription("\uD83D\uDD27  **" + String.format(localise("CommandInfo.ramUsage"), "** " + String.format("%,d", memoryUsed / 1024 / 1024)) + "MB\n\n");
        long finalUptime = currentTimeMillis() - Main.startupTime;
        String finalTime = toTimestamp(finalUptime, event.getGuild().getIdLong());
        eb.appendDescription("⏰ **" + String.format(localise("CommandInfo.upTime"), "**" + finalTime) + "\n\n");
        eb.appendDescription("\uD83D\uDCE1 **" + String.format(localise("CommandInfo.discordServers"), "**" + String.format("%,d", event.getJDA().getGuilds().size())) + "\n\n");
        eb.appendDescription("\uD83D\uDC64 **" + String.format(localise("CommandInfo.discordMembers"), "**" + String.format("%,d", memberCount)) + "\n\n");
        eb.appendDescription("\uD83D\uDCD1 **" + String.format(localise("CommandInfo.registeredCommands"), "**" + CommandCount) + "\n\n");
        eb.appendDescription("\uD83C\uDFB5 **" + String.format(localise("CommandInfo.voiceChannels"), "** " + vcCount) + "\n\n");
        eb.appendDescription("\uD83D\uDD0A **" + String.format(localise("CommandInfo.playingCount"), "** " + playingCount) + "\n\n");
        eb.appendDescription("⏱️ **" + String.format(localise("CommandInfo.gatewayPing"), "**" + event.getJDA().getGatewayPing()) + "\n\n");
        eb.setFooter(String.format(localise("CommandInfo.version"), botVersion));
        long time = currentTimeMillis();
        event.replyEmbeds(response -> {
            eb.appendDescription("⏱️  **" + String.format(localise("CommandInfo.ping"), "**" + (currentTimeMillis() - time)));
            response.editMessageEmbeds(eb.build());
        }, eb.build());
    }


    @Override
    public String[] getNames() {
        return new String[]{"info"};
    }

    @Override
    public Category getCategory() {
        return Category.General;
    }

    @Override
    public String getDescription() {
        return "Tells you bot info.";
    }
}
