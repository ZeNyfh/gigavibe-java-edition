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
        eb.setTitle(localise("cmd.info.info", event.getJDA().getSelfUser().getName()), null);
        eb.setColor(botColour);
        eb.appendDescription(localise("cmd.info.ramUsage", String.format("%,d", memoryUsed / 1024 / 1024)));
        long finalUptime = currentTimeMillis() - Main.startupTime;
        String finalTime = toTimestamp(finalUptime, event.getGuild().getIdLong());
        eb.appendDescription(localise("cmd.info.upTime", finalTime));
        eb.appendDescription(localise("cmd.info.discordServers", String.format("%,d", event.getJDA().getGuilds().size())));
        eb.appendDescription(localise("cmd.info.discordMembers", String.format("%,d", memberCount)));
        eb.appendDescription(localise("cmd.info.registeredCommands", CommandCount));
        eb.appendDescription(localise("cmd.info.voiceChannels", vcCount));
        eb.appendDescription(localise("cmd.info.playingCount", playingCount));
        eb.appendDescription(localise("cmd.info.gatewayPing", event.getJDA().getGatewayPing()));
        eb.setFooter(localise("cmd.info.version", botVersion));
        long time = currentTimeMillis();
        event.replyEmbeds(response -> {
            eb.appendDescription("⏱️  " + localise("cmd.info.ping", currentTimeMillis() - time));
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
