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
        eb.setTitle(event.getJDA().getSelfUser().getName() + " " + localise("Info", "CmdInfo.info"), null);
        eb.setColor(botColour);
        eb.appendDescription(localise("\uD83D\uDD27  **Ram usage:** {ram in MB}MB", "CmdInfo.ramUsage", String.format("%,d", memoryUsed / 1024 / 1024)) + "MB\n\n");
        long finalUptime = currentTimeMillis() - Main.startupTime;
        String finalTime = toTimestamp(finalUptime, event.getGuild().getIdLong());
        eb.appendDescription(localise("⏰ **Uptime:** {time}\n\n", "CmdInfo.upTime", finalTime));
        eb.appendDescription("\uD83D\uDCE1 " + localise("**Guilds:** {guildCount}\n\n", "CmdInfo.discordServers", String.format("%,d", event.getJDA().getGuilds().size())));
        eb.appendDescription("\uD83D\uDC64 " + localise("**Users:** {userCount}\n\n", "Cmd.discordMembers", String.format("%,d", memberCount)));
        eb.appendDescription("\uD83D\uDCD1 " + localise("**Registered Commands:** {commandCount}\n\n", "CmdInfo.registeredCommands", +CommandCount));
        eb.appendDescription("\uD83C\uDFB5 " + localise("**VCs:** {VCCount}\n\n", "CmdInfo.voiceChannels", vcCount));
        eb.appendDescription("\uD83D\uDD0A " + localise("**Playing Count:** {playingCount}\n\n", "CmdInfo.playingCount", playingCount));
        eb.appendDescription("⏱️ " + localise("**Gateway Ping:** {gPingNum}ms\n\n", "CommandInfo.gatewayPing", event.getJDA().getGatewayPing()));
        eb.setFooter(localise("Version: {1}", "CommandInfo.version", botVersion));
        long time = currentTimeMillis();
        event.replyEmbeds(response -> {
            eb.appendDescription("⏱️  " + localise("**Ping:** {pingNum}ms", "CmdInfo.ping", currentTimeMillis() - time));
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
