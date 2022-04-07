package Bots;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
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

import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    private static long uptime = currentTimeMillis();
    String botPrefix = "&";
    String embedColour = "#0000ff";

    public static void main(String[] args) throws InterruptedException, LoginException {
        JDA bot = JDABuilder.createDefault(token.botToken)
                .addEventListeners(new Main())
                .setActivity(Activity.playing("in development..."))
                .build();
        bot.awaitReady();
        System.out.println("bot is now running, have fun ig");
    }

    static MessageEmbed createQuickEmbed(String title, String description) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title, null);
        eb.setColor(new Color(0,0,255));
        eb.setDescription(description);
        MessageEmbed embed = eb.build();
        return embed;
    }

    static String toTimestamp(long number) {
        if (number <= 0) {
            return "0 seconds";
        } else {
            number = number / (1000);
            long days = number / (24 * 3600);
            number = number % (24 * 3600);
            long hours = number / 3600;
            number %= 3600;
            long minutes = number / 60;
            number %= 60;
            long seconds = number;
            ArrayList totalSet = new ArrayList();
            if (days > 0) {
                totalSet.add(days + " day(s)");
            }
            if (hours > 0) {
                totalSet.add(hours + " hour(s)");
            }
            if (minutes > 0) {
                totalSet.add(minutes + " minute(s)");
            }
            if (seconds > 0) {
                totalSet.add(seconds + " second(s)");
            }
            String time = String.valueOf(totalSet);
            String temptime = time.replace("[", "");
            String othertime = temptime.replace("]", "");
            return othertime;
        }
    }

    private void connectTo(AudioChannel channel) {
        Guild guild = channel.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(channel);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) { // this is gonna get unreadable fast
        Message msg = event.getMessage();

        User user = msg.getAuthor();
        if (user.isBot()) {
            return;
        }

        String content = msg.getContentRaw();
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Member member = event.getMember();
        GuildVoiceState voiceState = member.getVoiceState();
        AudioChannel vc = voiceState.getChannel();

        if (content.startsWith(botPrefix + "uptime")) {
            long finaluptime = currentTimeMillis() - Main.uptime;
            String finaltime = toTimestamp(finaluptime);
            channel.sendMessage("uptime: " + finaltime).queue();
            System.out.println(finaltime);
        }

        if (!event.isFromGuild()) {
            return;
        }

        if (content.startsWith(botPrefix + "help")) {
            channel.sendMessage("literally only the help command rn").queue(); // will be worked on in the future
            ArrayList General = new ArrayList();
            ArrayList Music = new ArrayList();
            ArrayList DJ = new ArrayList();
            ArrayList Admin = new ArrayList();
        }

        if (msg.getContentRaw().equals(botPrefix + "ping")) {
            long time = currentTimeMillis();
            channel.sendMessage(".").queue(response -> {
                response.editMessageFormat("ping: %dms", currentTimeMillis() - time).queue();
            });
        }

        if (msg.getContentRaw().equals(botPrefix + "join vc")) {
            if (vc != null) {
                connectTo(vc);
                }
            else {
                channel.sendMessageEmbeds(createQuickEmbed("Error", "you arent in a vc cunt")).queue();
                }
            }
        }
    }
