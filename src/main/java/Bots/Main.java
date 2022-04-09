package Bots;

import Bots.commands.*;
import ca.tristan.jdacommands.JDACommands;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import javax.security.auth.login.LoginException;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import static Bots.token.botToken;
import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    public static final long Uptime = currentTimeMillis();
    public final static GatewayIntent[] INTENTS = {GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_PRESENCES};
    String botPrefix = "&";

    public static void main(String[] args) throws InterruptedException, LoginException {

        JDACommands jdaCommands = new JDACommands("&");
        jdaCommands.registerCommand(new CommandPing());
        jdaCommands.registerCommand(new CommandPlay());
        jdaCommands.registerCommand(new CommandDebug());
        jdaCommands.registerCommand(new CommandSkip());
        jdaCommands.registerCommand(new CommandLocalPlay());
        jdaCommands.registerCommand(new CommandPlayAttachment());
        jdaCommands.registerCommand(new CommandNowPlaying());
        jdaCommands.registerCommand(new CommandUptime());

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
            {
                if (days == 1) {
                    totalSet.add(days + " day");
                } else if (days != 0) {
                    totalSet.add(days + " days");
                }
                if (hours == 1) {
                    totalSet.add(hours + " hour");
                } else if (hours != 0) {
                    totalSet.add(days + " hours");
                }
                if (minutes == 1) {
                    totalSet.add(minutes + " minute");
                } else if (minutes != 0) {
                    totalSet.add(minutes + " minutes");
                }
                if (seconds == 1) {
                    totalSet.add(seconds + " second");
                } else if (seconds != 0) {
                    totalSet.add(seconds + " seconds"); // im sorry but it was annoying me, I needed to remove the brackets
                }
                return String.valueOf(totalSet).replace("[", "").replace("]", "");
            }
        }
    }
}

