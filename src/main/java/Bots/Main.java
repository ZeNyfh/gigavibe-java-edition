package Bots;

import Bots.commands.*;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static Bots.commands.CommandLoop.loop;
import static Bots.token.botToken;
import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {

    public static final long Uptime = currentTimeMillis();
    public final static GatewayIntent[] INTENTS = {GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS};
    public final static String botPrefix = "&";
    public static List<BaseCommand> commands = new ArrayList<>();

    private static void registerCommand(BaseCommand command) {
        commands.add(command);
        //Possibly other uses idk yet -9382
    }

    public static void main(String[] args) throws InterruptedException, LoginException, IOException {

        Path folder = Paths.get(System.getProperty("user.dir") + "\\temp\\music\\");
        if (!Files.exists(folder)) {
            folder.toFile().mkdirs();
        }
        folder = Paths.get(System.getProperty("user.dir") + "\\temp\\viddl");
        if (!Files.exists(folder)) {
            folder.toFile().mkdirs();
        }
        folder = Paths.get(System.getProperty("user.dir") + "\\temp\\auddl");
        if (!Files.exists(folder)) {
            folder.toFile().mkdirs();
        }
        File file = new File(System.getProperty("user.dir") + "\\jsonStorage.json");
        if (!file.exists()) {
            file.createNewFile();
        }

        registerCommand(new CommandPing());
        registerCommand(new CommandPlay());
        registerCommand(new CommandLoopQueue());
        registerCommand(new CommandLoop());
        registerCommand(new CommandSkip());
        registerCommand(new CommandLocalPlay());
        registerCommand(new CommandPlayAttachment());
        registerCommand(new CommandNowPlaying());
        registerCommand(new CommandHelp());
        registerCommand(new CommandVideoDL());
        registerCommand(new CommandAudioDL());
        registerCommand(new CommandDisconnect());
        registerCommand(new CommandBlockChannel());
        registerCommand(new CommandQueue());
        registerCommand(new CommandBotInfo());
        registerCommand(new CommandShuffle());

        JDA bot = JDABuilder.create(botToken, Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .addEventListeners(new Main())
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

    public static Boolean isBlocked(JSONObject json, String guild, String channel) throws IOException, ParseException {
        JSONParser parser = new org.json.simple.parser.JSONParser();
        try {
            Object obj = parser.parse(new FileReader(System.getProperty("user.dir") + "\\jsonStorage.txt"));
            json = (JSONObject) obj;
            JSONArray array = (JSONArray) json.get(null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static AudioTrack getTrackFromQueue(Guild guild, int queuePos) {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(guild);
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        if (queue.isEmpty()) {
            return null;
        } else {
            return queue.get(queuePos);
        }
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
            return String.join(", ", totalSet);
        }
    }

    public static String toSimpleTimestamp(long seconds) {
        ArrayList<String> totalSet = new ArrayList<>();
        seconds /= 1000;
        long hours = seconds / 3600;
        seconds %= 3600;
        long minutes = seconds / 60;
        seconds %= 60;
        String finalMinutes;
        String finalHours;

        if (hours != 0) {
            if (hours < 10) {
                finalHours = ("0" + hours);
                totalSet.add(finalHours + ":");
            } else {
                totalSet.add(hours + ":");
            }
        }
        if (minutes < 10) {
            finalMinutes = ("0" + minutes);
        } else {
            finalMinutes = String.valueOf(minutes);
        }
        totalSet.add(finalMinutes + ":");
        String finalSeconds;
        if (seconds < 10) {
            finalSeconds = ("0" + seconds);
        } else {
            finalSeconds = String.valueOf(seconds);
        }
        totalSet.add(finalSeconds);
        return String.join("", totalSet);
    }

    //public static boolean isDJ(Member member) {
    // the json array
    //if (member.isBoosting() && boosterDJ) {
    //return true;
    //} else return DJList.contains(member.getIdLong());
    //}

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        if (Objects.equals(event.getButton().getId(), "general")) {
            event.editMessageEmbeds().setEmbeds();
            event.deferEdit();
        }
        if (Objects.equals(event.getButton().getId(), "music")) {
            event.editMessageEmbeds();
            event.deferEdit();
        }
        if (Objects.equals(event.getButton().getId(), "dj")) {
            event.editMessageEmbeds();
            event.deferEdit();
        }
        if (Objects.equals(event.getButton().getId(), "admin")) {
            event.editMessageEmbeds();
            event.deferEdit();
        }
        if (Objects.equals(event.getButton().getId(), "queueBack")) {
            event.editMessageEmbeds();
            event.deferEdit();
        }
        if (Objects.equals(event.getButton().getId(), "queueForward")) {
            event.editMessageEmbeds();
            event.deferEdit();
        }
    }

    @Override
    public void onGenericGuildVoice(@NotNull GenericGuildVoiceEvent event) {
        if (Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel() != null) {
            int i = 1;
            List<Member> a = event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers();
            if (a.listIterator().next().getUser().isBot()) {
                i++;
            }
            if (event.getGuild().getSelfMember().getVoiceState().getChannel().getMembers().size() - i == 0) {
                event.getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

    @Override
    public void onException(@NotNull ExceptionEvent event) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        System.out.println(dtf.format(now));
        event.getCause().printStackTrace();
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getMember().getId().equals(event.getJDA().getSelfUser().getId())) {
            loop = false;
        }
        AudioChannel botChannel = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel();
        if (Objects.requireNonNull(event.getMember().getVoiceState()) == Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState())) {
            assert botChannel != null;
            List<Member> vcMembers = botChannel.getMembers();
            int botChannelMemberCount = (int) vcMembers.stream()
                    .filter(user -> !user.getUser().isBot()).count();
            if (botChannelMemberCount == 0) {
                try {
                    Thread.sleep(150000); // 2m 30s minute
                } catch (InterruptedException ignored) {
                }
                PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.clear();
                event.getGuild().getAudioManager().closeAudioConnection();
                PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.nextTrack();
            }
        }
    }

    private void processCommand(BaseCommand Command, MessageReceivedEvent event) {
        //NOTE: Consider using custom event that extends ontop of MessageRecievedEvent -9382
        System.out.println("your command is: " + Command);
        try {
            Command.execute(event);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(botPrefix)) {
            for (BaseCommand Command : commands) {
                String commandLower = event.getMessage().getContentRaw().toLowerCase();
                if (commandLower.startsWith(botPrefix + Command.getName())) {
                    processCommand(Command,event);
                    break;
                }
                for (String alias : Command.getAlias()) {
                    if (commandLower.startsWith(botPrefix + alias)) {
                        processCommand(Command,event);
                        break;
                    }
                }
            }
        }
    }
}