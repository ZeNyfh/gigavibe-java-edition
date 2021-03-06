package Bots;

import Bots.commands.*;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GenericGuildVoiceEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
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
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingQueue;

import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    public static final long Uptime = currentTimeMillis();
    public final static GatewayIntent[] INTENTS = {GatewayIntent.GUILD_EMOJIS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS};
    public static Color botColour = new Color(0, 0, 0);
    public static String botPrefix = "";
    public static String botToken = "";
    public static HashMap<Long, List<Member>> skips = new HashMap<Long, List<Member>>();
    public static String botVersion = "23.07.20"; // YY.MM.DD
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
            printlnTime(folder.getFileName() + " doesn't exist, creating now.");
            folder.toFile().mkdirs();
        }
        folder = Paths.get("auddl");
        if (!Files.exists(folder)) {
            printlnTime(folder.getFileName() + " doesn't exist, creating now.");
            folder.toFile().mkdirs();
        }
        File file = new File("Users.json");
        if (!file.exists()) {
            printlnTime(file.getName() + " doesn't exist, creating now.");
            file.createNewFile();
            FileWriter writer = new FileWriter("Users.json");
            writer.write("{}");
            writer.flush();
            writer.close();
        }
        file = new File("BlockedChannels.json");
        if (!file.exists()) {
            printlnTime(file.getName() + " doesn't exist, creating now.");
            file.createNewFile();
            FileWriter writer = new FileWriter("BlockedChannels.json");
            writer.write("{}");
            writer.flush();
            writer.close();
        }
        file = new File("DJs.json");
        if (!file.exists()) {
            printlnTime(file.getName() + " doesn't exist, creating now.");
            file.createNewFile();
            FileWriter writer = new FileWriter("DJs.json");
            writer.write("{}");
            writer.flush();
            writer.close();
        }
        file = new File(".env");
        if (!file.exists()) {
            printlnTime(file.getName() + " doesn't exist, creating now.");
            file.createNewFile();
            FileWriter writer = new FileWriter(".env");
            writer.write("# This is the bot token, it needs to be set.\nTOKEN=\n# Feel free to change the prefix to anything else.\nPREFIX=\n# These 2 are required for spotify support with the bot.\nSPOTIFY CLIENT=\nSPOTIFY CLIENT SECRET=\n# This is the hex value for the bot colour\nCOLOUR=");
            writer.flush();
            writer.close();
        }
        String errorMessage = "";
        String OS = System.getProperty("os.name");
        if (OS.toLowerCase().contains("windows")) {
            file = new File("modules/ffmpeg.exe");
            if (!file.exists()) {
                errorMessage = errorMessage + file.getPath() + " does not exist." + "\n";
            }
            file = new File("modules/ffprobe.exe");
            if (!file.exists()) {
                errorMessage = errorMessage + file.getPath() + " does not exist." + "\n";
            }
            file = new File("modules/yt-dlp.exe");
            if (!file.exists()) {
                errorMessage = errorMessage + file.getPath() + " does not exist." + "\n";
            }
        } else if (OS.toLowerCase().contains("linux")) {
            file = new File("modules/ffmpeg");
            if (!file.exists()) {
                errorMessage = errorMessage + file.getPath() + " does not exist." + "\n";
            }
            file = new File("modules/ffprobe");
            if (!file.exists()) {
                errorMessage = errorMessage + file.getPath() + " does not exist." + "\n";
            }
            file = new File("modules/yt-dlp");
            if (!file.exists()) {
                errorMessage = errorMessage + file.getPath() + " does not exist." + "\n";
            }
        }
        if (!errorMessage.equals("")) {
            errorMessage = errorMessage + " one or more files do not exist, please download the necessary files mentioned in the Requirements section of the readme on github: https://github.com/ZeNyfh/gigavibe-java-edition/blob/main/README.md";
            printlnTime(errorMessage);
            return;
        }
        Dotenv dotenv = Dotenv.load();
        if (dotenv.get("TOKEN").isBlank()) {
            printlnTime("TOKEN is not set in " + new File(".env").getAbsolutePath());
        }
        if (dotenv.get("PREFIX").isBlank()) {
            printlnTime("PREFIX is not set in " + new File(".env").getAbsolutePath());
        }
        botPrefix = dotenv.get("PREFIX");
        botToken = dotenv.get("TOKEN");

        if (dotenv.get("COLOUR").isBlank()){
            printlnTime("Hex value COLOUR is not set in " + new File(".env" + "\n example: #FFCCEE").getAbsolutePath());
            return;
        }
        try {
            botColour = Color.decode(dotenv.get("COLOUR"));
        } catch (NumberFormatException e){printlnTime("Colour was invalid.");return;}

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
        registerCommand(new CommandLyrics());
        registerCommand(new CommandBug());
        registerCommand(new CommandSendAnnouncement());
        registerCommand(new CommandInvite());
        registerCommand(new CommandJoin());

        JDA bot = JDABuilder.create(botToken, Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .addEventListeners(new Main())
                .build();
        bot.awaitReady();
        printlnTime("bot is now running, have fun ig");
        bot.getPresence().setActivity(Activity.playing("music for " + bot.getGuilds().size() + " servers!"));
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

    public static void addToVote(Long guildID, List<Member> members) {
        if (members.size() == 0){
            skips.put(guildID, new ArrayList<>());
        }
        skips.put(guildID, members);
    }

    public static List<Member> getVotes(Long guildID) {
        return skips.get(guildID);
    }

    public static boolean IsChannelBlocked(Guild guild, TextChannel textChannel) throws IOException {
        JSONParser jsonParser = new JSONParser();
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
                textChannel.sendMessageEmbeds(createQuickEmbed("??? **Blocked channel**", "you cannot use this command in this channel.")).queue();
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
        assert jsonFileContents != null;
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
            textChannel.sendMessageEmbeds(createQuickEmbed("??? **Insufficient permissions**", "you do not have a DJ role.")).queue();
            return false;
        }
    }

    public static void printlnTime(String string) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        string = dtf.format(now) + " | " + string;
        System.out.println(string);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        event.getJDA().getPresence().setActivity(Activity.playing("music for " + event.getJDA().getGuilds().size() + " servers!"));
        Objects.requireNonNull(event.getGuild().getDefaultChannel()).sendMessageEmbeds(createQuickEmbed("**Important!**", "This is a music bot which needs some setting up done first, I recommend using `" + botPrefix + "help` to help you with the following. \n\nadd dj roles/users with the `" + botPrefix + "dj` command, this will allow some users or roles to have more control over the bots functions for example: forceskip, disconnect and shuffle.\nif you wish to give boosters this permission, just add the booster role to the dj roles.\n\nYou can also add optional blocked channels, this will disallow some commands from being used in the blocked channels, this can be done with the `" + botPrefix + "blockchannel` command.\n\n**IF YOU ENCOUNTER ANY BUGS, ISSUES OR HAVE ANY FEATURE REQUESTS, USE** `" + botPrefix + "bug <String>` **TO REPORT THE BUG TO ME!**")).queue();
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        event.getJDA().getPresence().setActivity(Activity.playing("music for " + event.getJDA().getGuilds().size() + " servers!"));
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        int i = 0;
        String buttonID = Objects.requireNonNull(event.getInteraction().getButton().getId()).toLowerCase();
        BlockingQueue<AudioTrack> Queue = PlayerManager.getInstance().getMusicManager(Objects.requireNonNull(event.getGuild())).scheduler.queue;
        switch (buttonID) {
            case "general":
            case "music":
            case "dj":
            case "admin":
                for (BaseCommand Command : commands) {
                    if (Command.getCategory().equalsIgnoreCase(Objects.requireNonNull(event.getButton().getId()))) {
                        i++;
                        eb.appendDescription("`" + i + ")` **" + Command.getName() + " " + Command.getParams() + "** - " + Command.getDescription() + "\n");
                    }
                }
                break;
            case "forward":
                for (int j = 0; j < Queue.size(); ) {
                    j++;
                }
                return;
            case "backward":

                return;
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
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (event.getMember().getUser() == event.getJDA().getSelfUser()) {
            if (event.getNewValue().getMembers().size() == 1) { // assuming the bot is alone there.
                event.getGuild().getAudioManager().closeAudioConnection();
            }
        }
    }

    @Override
    public void onException(@NotNull ExceptionEvent event) {
        printlnTime("");
        event.getCause().printStackTrace();
    }

    @Override
    public void onGuildVoiceLeave(GuildVoiceLeaveEvent event) {
        if (event.getChannelLeft().getMembers().contains(event.getGuild().getSelfMember())) {
            List<Member> currentVotes = getVotes(event.getGuild().getIdLong());
            if (currentVotes != null) {
                currentVotes.remove(event.getMember());
                addToVote(event.getGuild().getIdLong(), currentVotes);
            }
        }
        if (event.getMember() == event.getGuild().getSelfMember()) {
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
            PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.setVolume(100);
            event.getGuild().getAudioManager().closeAudioConnection();
            addToVote(event.getGuild().getIdLong(), new ArrayList<>());
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
            // ratelimit code here
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
                boolean fullyBreak = false;
                ArrayList<String> checkSet = Command.getAlias();
                checkSet.add(Command.getName()); //Main name + all aliases
                for (String alias : checkSet) {
                    //fullyBreak is so that we can stop checking commands after an alias works (breaks only escape the top loop) -9382
                    if (processCommand(botPrefix + alias, Command, event)) {
                        fullyBreak = true;
                        break;
                    }
                }
                if (fullyBreak) { //Correct command found, exit checks
                    break;
                }
            }
        }
    }
}