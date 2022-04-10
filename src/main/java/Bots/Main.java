package Bots;

import Bots.commands.*;
import ca.tristan.jdacommands.JDACommands;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;

import static Bots.commands.CommandLoop.loop;
import static Bots.token.botToken;
import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    public static final long Uptime = currentTimeMillis();
    public final static GatewayIntent[] INTENTS = {GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS};
    String botPrefix = "&";

    public static void main(String[] args) throws InterruptedException, LoginException {

        JDACommands jdaCommands = new JDACommands("&");
        jdaCommands.registerCommand(new CommandPing());
        jdaCommands.registerCommand(new CommandPlay());
        jdaCommands.registerCommand(new CommandLoop());
        jdaCommands.registerCommand(new CommandSkip());
        jdaCommands.registerCommand(new CommandLocalPlay());
        jdaCommands.registerCommand(new CommandPlayAttachment());
        jdaCommands.registerCommand(new CommandNowPlaying());
        jdaCommands.registerCommand(new CommandUptime());
        jdaCommands.registerCommand(new CommandHelp());

        JDA bot = JDABuilder.create(botToken, Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .addEventListeners(new Main())
                .addEventListeners(jdaCommands)
                .setActivity(Activity.playing("in development..."))
                .build();
        bot.awaitReady();
        System.out.println("bot is now running, have fun ig");
    }

    public static MessageEmbed createQuickEmbed(String title, String description) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title, null);
        eb.setColor(new Color(0, 0, 255));
        eb.setDescription(description);
        return eb.build();
    }

    public static String toTimestamp(long seconds) {
        if (seconds <= 0) {
            return "0 seconds";
        } else {
            seconds /= 1000;
            long days = seconds / 86400;
            seconds %= 86400;
            long hours = seconds / 3600;
            seconds %= 3600;
            long minutes = seconds / 60;
            seconds %= 60;
            ArrayList<String> totalSet = new ArrayList<>();
            if (days == 1) {
                totalSet.add(days + " day");
            } else if (days != 0) {
                totalSet.add(days + " days");
            }
            if (hours == 1) {
                totalSet.add(hours + " hour");
            } else if (hours != 0) {
                totalSet.add(hours + " hours");
            }
            if (minutes == 1) {
                totalSet.add(minutes + " minute");
            } else if (minutes != 0) {
                totalSet.add(minutes + " minutes");
            }
            if (seconds == 1) {
                totalSet.add(seconds + " second");
            } else if (seconds != 0) {
                totalSet.add(seconds + " seconds");
            }
            return String.valueOf(totalSet).replace("[", "").replace("]", "");
        }
    }

    public static String toSimpleTimestamp(long seconds) {
        ArrayList<String> totalSet = new ArrayList<>();
        seconds /= 1000;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        if (minutes < 10) {
            String finalMinutes = ("0" + minutes);
            totalSet.add(finalMinutes + ":");
        } else {
            String finalMinutes = String.valueOf(minutes);
            totalSet.add(finalMinutes + ":");
        }
        if (seconds < 10) {
            String finalSeconds = ("0" + seconds);
            totalSet.add(finalSeconds);
        } else {
            String finalSeconds = String.valueOf(seconds);
            totalSet.add(finalSeconds);
        }
        return String.valueOf(totalSet).replace("[", "").replace("]", "").replace(", ", "");
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if (event.getMember().getId().equals(event.getJDA().getSelfUser().getId())) {
            loop = false;
        }
    }
}