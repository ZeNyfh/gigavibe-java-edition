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
import net.dv8tion.jda.api.events.ShutdownEvent;
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
import java.io.FileWriter;
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

import static Bots.token.botToken;
import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    public static final long Uptime = currentTimeMillis();
    public final static GatewayIntent[] INTENTS = {GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS};
    public final static String botPrefix = "&";
    public final static Color botColour = new Color(0, 0, 255);
    public static List<String> LoopGuilds = new ArrayList<>();
    public static List<String> LoopQueueGuilds = new ArrayList<>();
    public static List<BaseCommand> commands = new ArrayList<>();

    private static void registerCommand(BaseCommand command) {
        commands.add(command);
        //Possibly other uses idk yet -9382
    }

    public static void main(String[] args) throws InterruptedException, LoginException, IOException {

        Path folder = Paths.get("viddl");
        if (!Files.exists(folder)) {
            System.out.println(folder.getFileName() + " doesn't exist, creating now.");
            folder.toFile().mkdirs();
        }
        folder = Paths.get("auddl");
        if (!Files.exists(folder)) {
            System.out.println(folder.getFileName() + " doesn't exist, creating now.");
            folder.toFile().mkdirs();
        }
        File file = new File("Users.json");
        if (!file.exists()) {
            System.out.println(file.getName() + " doesn't exist, creating now.");
            file.createNewFile();
            FileWriter writer = new FileWriter("Users.json");
            writer.write("{}");
            writer.flush();
            writer.close();
        }
        file = new File("BlockedChannels.json");
        if (!file.exists()) {
            System.out.println(file.getName() + " doesn't exist, creating now.");
            file.createNewFile();
            FileWriter writer = new FileWriter("BlockedChannels.json");
            writer.write("{}");
            writer.flush();
            writer.close();
        }
        file = new File("DJs.json");
        if (!file.exists()) {
            System.out.println(file.getName() + " doesn't exist, creating now.");
            file.createNewFile();
            FileWriter writer = new FileWriter("DJs.json");
            writer.write("{}");
            writer.flush();
            writer.close();
        }

        registerCommand(new CommandPing());
        registerCommand(new CommandPlay());
        registerCommand(new CommandLoopQueue());
        registerCommand(new CommandLoop());
        registerCommand(new CommandSkip());
        registerCommand(new CommandLocalPlay());
        registerCommand(new CommandNowPlaying());
        registerCommand(new CommandHelp());
        registerCommand(new CommandVideoDL());
        registerCommand(new CommandAudioDL());
        registerCommand(new CommandDisconnect());
        registerCommand(new CommandBlockChannel());
        registerCommand(new CommandQueue());
        registerCommand(new CommandBotInfo());
        registerCommand(new CommandShuffle());
        registerCommand(new CommandGithub());
        registerCommand(new CommandDJ());
        registerCommand(new CommandExec());
        registerCommand(new CommandRemove());
        registerCommand(new CommandClearQueue());
        registerCommand(new CommandVolume());
        registerCommand(new CommandForceSkip());
        registerCommand(new CommandRadio());
        registerCommand(new CommandSeek());
        registerCommand(new CommandPlaylist());

        JDA bot = JDABuilder.create(botToken, Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .addEventListeners(new Main())
                .setActivity(Activity.playing("Beta test!"))
                .build();
        bot.awaitReady();

        System.out.println("bot is now running, have fun ig");
    }

    public static MessageEmbed createQuickEmbed(String title, String description) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title, null);
        eb.setColor(botColour);
        eb.setDescription(description);
        return eb.build();
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

    public static boolean IsChannelBlocked(Guild guild, TextChannel textChannel) throws IOException {
        JSONParser jsonParser = new JSONParser();
        JSONObject obj = new JSONObject();
        JSONObject blocked = new JSONObject();
        JSONObject jsonFileContents = null;
        try (FileReader reader = new FileReader("BlockedChannels.json")) {
            jsonFileContents = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        JSONObject json = jsonFileContents;
        blocked.put("BlockedChannels", new JSONArray());
        json.putIfAbsent(guild.getId(), blocked);
        FileWriter file = new FileWriter("BlockedChannels.json");
        try {
            file.write(json.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        JSONObject guildObj = (JSONObject) jsonFileContents.get(guild.getId());
        JSONArray blockedChannels = (JSONArray) guildObj.get("BlockedChannels");
        for (int i = 0; i < blockedChannels.size(); ) {
            if (textChannel.getId().equals(blockedChannels.get(i))) {
                textChannel.sendMessageEmbeds(createQuickEmbed("❌ **Blocked channel**", "you cannot use this command in this channel.")).queue();
                return true;
            }
            i++;
        }
        return false;
    }

    public static boolean IsDJ(Guild guild, TextChannel textChannel, Member member) {
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonFileContents = null;
        try (FileReader reader = new FileReader("DJs.json")) {
            jsonFileContents = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        JSONObject guildObj = (JSONObject) jsonFileContents.get(guild.getId());
        JSONArray DJRoles = (JSONArray) guildObj.get("roles");
        JSONArray DJUsers = (JSONArray) guildObj.get("users");
        boolean check = false;
        for (int i = 0; i < DJRoles.size(); ) {
            if (member.getRoles().contains(guild.getJDA().getRoleById((String) DJRoles.get(i)))) {
                check = true;
            }
            i++;
        }
        if (!check) {
            for (int i = 0; i < DJUsers.size(); ) {
                if (DJUsers.get(i).equals(member.getId())) {
                    check = true;
                }
                i++;
            }
        }
        if (check) {
            return true;
        } else {
            textChannel.sendMessageEmbeds(createQuickEmbed("❌ **Insufficient permissions**", "you do not have a DJ role.")).queue();
            return false;
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        int i = 0;
        String buttonID = Objects.requireNonNull(event.getInteraction().getButton().getId()).toLowerCase();
        if (buttonID.equals("general") || (buttonID.equals("music") || (buttonID.equals("dj") || (buttonID.equals("admin"))))) {
            for (BaseCommand Command : commands) {
                if (Command.getCategory().equalsIgnoreCase(Objects.requireNonNull(event.getButton().getId()))) {
                    i++;
                    eb.appendDescription("`" + i + ")` **" + Command.getName() + " " + Command.getParams() + "** - " + Command.getDescription() + "\n");
                }
            }
        }
        if (Objects.equals(event.getButton().getId(), "general")) {
            eb.setTitle("\uD83D\uDCD6 **General**");
            event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
        }
        if (Objects.equals(event.getButton().getId(), "music")) {
            eb.setTitle("\uD83D\uDD0A **Music**");
            event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
        }
        if (Objects.equals(event.getButton().getId().toLowerCase(), "dj")) {
            eb.setTitle("\uD83C\uDFA7 **DJ**");
            event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
        }
        if (Objects.equals(event.getButton().getId(), "admin")) {
            eb.setTitle("\uD83D\uDCD1 **Admin**");
            event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
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
            LoopGuilds.remove(event.getGuild().getId());
            LoopQueueGuilds.remove(event.getGuild().getId());
            return;
        }
        AudioChannel botChannel = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel();
        if (botChannel == null) {
            return;
        }
        int botChannelMemberCount = 0;
        for (int i = 0; i < botChannel.getMembers().size(); ) {
            if (!botChannel.getMembers().get(i).getUser().isBot()) {
                botChannelMemberCount = botChannelMemberCount + 1;
            }
            i++;
        }
        if (botChannelMemberCount == 0) {
            PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.clear();
            PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.nextTrack();
            event.getGuild().getAudioManager().closeAudioConnection();
            PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.setVolume(100);
        }
    }

    private boolean processCommand(String matchTerm, BaseCommand Command, MessageReceivedEvent event) {
        String commandLower = event.getMessage().getContentRaw().toLowerCase();
        if (commandLower.startsWith(matchTerm)) {
            if (commandLower.length() != matchTerm.length()) { //Makes sure we arent misinterpreting -9382
                String afterChar = commandLower.substring(matchTerm.length(), matchTerm.length() + 1);
                if (!afterChar.equals(" ") && !afterChar.equals("\n")) {
                    return false;
                }
            }
            System.out.println("your command is: " + Command);
            try {
                Command.execute(new MessageEvent(event));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }


    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        event.getJDA().getAudioManagers().clear();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getMessage().getContentRaw().startsWith(botPrefix)) {
            for (BaseCommand Command : commands) {
                if (processCommand(botPrefix + Command.getName(), Command, event)) {
                    break;
                }
                boolean fullyBreak = false;
                for (String alias : Command.getAlias()) {
                    //fullyBreak is so that we can stop checking commands after an alias works (breaks only escape the top loop) -9382
                    if (processCommand(botPrefix + alias, Command, event)) {
                        fullyBreak = true;
                        break;
                    }
                }
                if (fullyBreak) {
                    break;
                }
            }
        }
    }
}