package Bots;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
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
import java.util.List;
import java.util.Objects;

import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    private static final long Uptime = currentTimeMillis();
    String botPrefix = "&";
    String[] audioFiles = {"mp3", "mp4", "wav", "ogg", "flac", "m4a", "mov", "wmv"};
    static AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

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
        eb.setColor(new Color(0, 0, 255));
        eb.setDescription(description);
        return eb.build();
    }

    static String toTimestamp(long seconds) {
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

    private void connectTo(AudioChannel channel) {
        Guild guild = channel.getGuild();
        AudioManager audioManager = guild.getAudioManager();
        audioManager.openAudioConnection(channel);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) { // this is going to get unreadable fast
        Message msg = event.getMessage();

        User user = msg.getAuthor();
        if (user.isBot()) {
            return;
        }

        String content = msg.getContentRaw();
        Guild guild = event.getGuild();
        MessageChannel channel = event.getChannel();
        Member member = event.getMember();
        JDA bot = event.getJDA();
        assert member != null;
        GuildVoiceState voiceState = member.getVoiceState();
        assert voiceState != null;
        AudioChannel vc = voiceState.getChannel();


        if (content.startsWith(botPrefix + "uptime")) {
            long finalUptime = currentTimeMillis() - Main.Uptime;
            String finalTime = toTimestamp(finalUptime);
            channel.sendMessageEmbeds(createQuickEmbed(" ", "⏰ uptime: " + finalTime)).queue();
            System.out.println(finalTime);
        }

        if (!event.isFromGuild()) {
            return;
        }

        if (content.startsWith(botPrefix + "help")) {
            channel.sendMessage("literally only the help command rn").queue(); // will be worked on in the future
            //ArrayList General = new ArrayList();
            //ArrayList Music = new ArrayList();
            //ArrayList DJ = new ArrayList();
            //ArrayList Admin = new ArrayList();
        }

        if (msg.getContentRaw().equals(botPrefix + "ping")) {
            long time = currentTimeMillis();
            channel.sendMessage(".").queue(response -> response.editMessageFormat("ping: %dms", currentTimeMillis() - time).queue());
        }

        if (msg.getContentRaw().equals(botPrefix + "song")) {
            List<Message.Attachment> messageAttachments = msg.getAttachments();
            if (messageAttachments.isEmpty()) {
                channel.sendMessageEmbeds(createQuickEmbed("Error", "No attachments found.")).queue();
            } else {
                String ext = messageAttachments.get(0).getFileExtension();
                for (String audioFile : audioFiles) {
                    if (Objects.equals(audioFile, ext)) {
                        channel.sendMessageEmbeds(createQuickEmbed(" ", "I found your attachment, it's a " + ext + ", I will attempt to play it")).queue();
                        AudioPlayer player = playerManager.createPlayer();
                        return;
                    }
                }
                channel.sendMessageEmbeds(createQuickEmbed("Error", "I found your attachment, it's a " + ext + " but is not playable by the bot.")).queue();
            }

            if (msg.getContentRaw().equals(botPrefix + "join vc")) {
                if (vc == null) {
                    channel.sendMessageEmbeds(createQuickEmbed("Error", "you arent in a vc cunt")).queue();
                } else {
                    connectTo(vc);
                }
            }
        }
    }

    public AudioPlayerManager getPlayerManager() {
        return Main.playerManager;
    }
}
