package Bots;

import Bots.commands.*;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
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
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    public static final long Uptime = currentTimeMillis();
    public final static GatewayIntent[] INTENTS = {GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT};
    public static Color botColour = new Color(0, 0, 0);
    public static String botPrefix = "";
    public static String botToken = "";
    public static HashMap<Long, List<Member>> skips = new HashMap<>();
    public static HashMap<Long, Integer> queuePages = new HashMap<>();
    public static HashMap<Long, Integer> guildTimeouts = new HashMap<>();
    public static String botVersion = "22.10.06"; // YY.MM.DD
    public static List<String> LoopGuilds = new ArrayList<>();
    public static List<String> LoopQueueGuilds = new ArrayList<>();
    public static List<BaseCommand> commands = new ArrayList<>();

    public static void registerCommand(BaseCommand command) {
        commands.add(command);
        //Possibly other uses idk yet
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
        File file = new File(".env");
        if (!file.exists()) {
            printlnTime(file.getName() + " doesn't exist, creating now.");
            file.createNewFile();
            FileWriter writer = new FileWriter(".env");
            writer.write("# This is the bot token, it needs to be set.\nTOKEN=\n# Feel free to change the prefix to anything else.\nPREFIX=\n# These 2 are required for spotify support with the bot.\nSPOTIFYCLIENTID=\nSPOTIFYCLIENTSECRET=\n# This is the hex value for the bot colour\nCOLOUR=");
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
        }
        if (!errorMessage.equals("")) {
            errorMessage = errorMessage + " one or more files do not exist, please download the necessary files mentioned in the Requirements section of the readme on github: https://github.com/ZeNyfh/gigavibe-java-edition/blob/main/README.md";
            printlnTime(errorMessage);
            return;
        }
        Dotenv dotenv = Dotenv.load();
        if (dotenv.get("TOKEN") == null) {
            printlnTime("TOKEN is not set in " + new File(".env").getAbsolutePath());
        }
        if (dotenv.get("PREFIX") == null) {
            printlnTime("PREFIX is not set in " + new File(".env").getAbsolutePath());
        }
        botPrefix = dotenv.get("PREFIX");
        botToken = dotenv.get("TOKEN");

        if (dotenv.get("COLOUR") == null) {
            printlnTime("Hex value COLOUR is not set in " + new File(".env" + "\n example: #FFCCEE").getAbsolutePath());
            return;
        }
        try {
            botColour = Color.decode(dotenv.get("COLOUR"));
        } catch (NumberFormatException e) {
            printlnTime("Colour was invalid.");
            return;
        }

        //General
        registerCommand(new CommandPing());
        registerCommand(new CommandHelp());
        registerCommand(new CommandGithub());
        registerCommand(new CommandBug());
        registerCommand(new CommandInvite());

        //Music
        registerCommand(new CommandPlay());
        registerCommand(new CommandLoopQueue());
        registerCommand(new CommandLoop());
        registerCommand(new CommandSkip());
        registerCommand(new CommandNowPlaying());
        registerCommand(new CommandVideoDL());
        registerCommand(new CommandAudioDL());
        registerCommand(new CommandQueue());
        registerCommand(new CommandRadio());

        //DJ
        registerCommand(new CommandDisconnect());
        registerCommand(new CommandShuffle());
        registerCommand(new CommandForceSkip());
        registerCommand(new CommandRemove());
        registerCommand(new CommandClearQueue());
        registerCommand(new CommandVolume());
        registerCommand(new CommandSeek());
        registerCommand(new CommandJoin());

        //Admin
        registerCommand(new CommandBlockChannel());
        registerCommand(new CommandBotInfo());
        registerCommand(new CommandDJ());

        //Dev
        registerCommand(new CommandLocalPlay());
        registerCommand(new CommandPlaylist());
        registerCommand(new CommandSendAnnouncement());
        registerCommand(new CommandInsert());

        ConfigManager.Init();

        JDA bot = JDABuilder.create(botToken, Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .addEventListeners(new Main())
                .build();
        bot.awaitReady();
        printlnTime("bot is now running, have fun ig");
        bot.getPresence().setActivity(Activity.playing("music for " + bot.getGuilds().size() + " servers! | ?help"));
        for (Guild guild : bot.getGuilds()) {
            queuePages.put(guild.getIdLong(), 0);
            guildTimeouts.put(guild.getIdLong(), 0);
        }
        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                int i = 0;
                for (Guild guild : bot.getGuilds()) {
                    if (guild.getAudioManager().isConnected()) {
                        try {
                            if (!((Objects.requireNonNull(guild.getAudioManager().getConnectedChannel())).getMembers().size() <= 1)) {
                                for (Member member : Objects.requireNonNull(guild.getAudioManager().getConnectedChannel()).getMembers()) {
                                    if (!member.getUser().isBot() || !member.getUser().isSystem()) {
                                        i++;
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                            guildTimeouts.put(guild.getIdLong(), 0);
                        }
                        if (i == 0) {
                            guildTimeouts.put(guild.getIdLong(), guildTimeouts.get(guild.getIdLong()) + 1);
                            if (guildTimeouts.get(guild.getIdLong()) >= 60) {
                                guild.getAudioManager().closeAudioConnection();
                                PlayerManager.getInstance().getMusicManager(guild).audioPlayer.destroy();
                                guildTimeouts.put(guild.getIdLong(), 0);
                            }
                        } else {
                            guildTimeouts.put(guild.getIdLong(), 0);
                        }
                    }
                }
            }
        };

        timer.scheduleAtFixedRate(task,0,1000);
    }

    public static MessageEmbed createQuickEmbed(String title, String description) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title, null);
        eb.setColor(botColour);
        eb.setDescription(description);
        return eb.build();
    }

    public static MessageEmbed createQuickError(String description) {
        return createQuickEmbed("❌ **Error**", description);
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
        if (members.size() == 0) {
            skips.put(guildID, new ArrayList<>());
        }
        skips.put(guildID, members);
    }

    public static List<Member> getVotes(Long guildID) {
        return skips.get(guildID);
    }

    public static boolean IsChannelBlocked(Guild guild, TextChannel textChannel) {
        JSONObject config = ConfigManager.GetConfig(guild.getIdLong());
        JSONArray blockedChannels = (JSONArray) config.get("BlockedChannels");
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
        JSONObject config = ConfigManager.GetConfig(guild.getIdLong());
        JSONArray DJRoles = (JSONArray) config.get("DJRoles");
        JSONArray DJUsers = (JSONArray) config.get("DJUsers");
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

    public static void printlnTime(String string) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        string = dtf.format(now) + " | " + string;
        System.out.println(string);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        queuePages.put(event.getGuild().getIdLong(), 0);
        guildTimeouts.put(event.getGuild().getIdLong(), 0);
        event.getJDA().getPresence().setActivity(Activity.playing("music for " + event.getJDA().getGuilds().size() + " servers! | ?help"));
        Objects.requireNonNull(event.getGuild().getDefaultChannel()).asTextChannel().sendMessageEmbeds(createQuickEmbed("**Important!**", "This is a music bot which needs some setting up done first, I recommend using `" + botPrefix + "help` to help you with the following. \n\nadd dj roles/users with the `" + botPrefix + "dj` command, this will allow some users or roles to have more control over the bots functions for example: forceskip, disconnect and shuffle.\nif you wish to give boosters this permission, just add the booster role to the dj roles.\n\nYou can also add optional blocked channels, this will disallow some commands from being used in the blocked channels, this can be done with the `" + botPrefix + "blockchannel` command.\n\n**IF YOU ENCOUNTER ANY BUGS, ISSUES OR HAVE ANY FEATURE REQUESTS, USE** `" + botPrefix + "bug <String>` **TO REPORT THE BUG TO ME!**")).queue();
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        event.getJDA().getPresence().setActivity(Activity.playing("music for " + event.getJDA().getGuilds().size() + " servers! | ?help"));
        queuePages.remove(event.getGuild().getIdLong());
        guildTimeouts.remove(event.getGuild().getIdLong());
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
                        eb.appendDescription("`" + i + ")` **" + Command.getNames()[0] + " " + Command.getParams() + "** - " + Command.getDescription() + "\n");
                    }
                }
                break;
            case "forward":
            case "backward":
        }
        if (Objects.equals(event.getButton().getId(), "forward")) {
            queuePages.put(event.getGuild().getIdLong(), queuePages.get(event.getGuild().getIdLong()) + 1);
            if (queuePages.get(event.getGuild().getIdLong()) >= round((PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.size() / 5) + 1)) {
                queuePages.put(event.getGuild().getIdLong(), 1);
            }
            long queueTimeLength = 0;
            for (AudioTrack track : PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue) {
                if (track.getInfo().length < 432000000) {
                    queueTimeLength = queueTimeLength + track.getInfo().length;
                }
            }
            for (int j = 5 * queuePages.get(event.getGuild().getIdLong()) - 5; j < 5 * queuePages.get(event.getGuild().getIdLong()) && j < PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.size(); ) {
                AudioTrackInfo trackInfo = Objects.requireNonNull(getTrackFromQueue(event.getGuild(), j)).getInfo();
                eb.appendDescription(j + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
                j++;
            }
            eb.setTitle("__**Now playing:**__\n" + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title, PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri);
            eb.setFooter(PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.size() + " songs queued. | " + round((PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.size() / 5) + 1) + " pages. | Length: " + toTimestamp(queueTimeLength));
            eb.setColor(botColour);
            eb.setThumbnail("https://img.youtube.com/vi/" + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
            event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
        }
        if (Objects.equals(event.getButton().getId(), "backward")) {
            queuePages.put(event.getGuild().getIdLong(), queuePages.get(event.getGuild().getIdLong()) - 1);
            if (queuePages.get(event.getGuild().getIdLong()) <= 0) {
                queuePages.put(event.getGuild().getIdLong(), round((PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.size() / 5) + 1));
            }
            long queueTimeLength = 0;
            for (AudioTrack track : PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue) {
                if (track.getInfo().length < 432000000) {
                    queueTimeLength = queueTimeLength + track.getInfo().length;
                }
            }
            for (int j = 5 * queuePages.get(event.getGuild().getIdLong()) - 5; j < 5 * queuePages.get(event.getGuild().getIdLong()) && j < PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.size(); ) {
                AudioTrackInfo trackInfo = getTrackFromQueue(event.getGuild(), j).getInfo();
                eb.appendDescription(j + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
                j++;
            }
            eb.setTitle("__**Now playing:**__\n" + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title, PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri);
            eb.setFooter(PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.size() + " songs queued. | " + round((PlayerManager.getInstance().getMusicManager(event.getGuild()).scheduler.queue.size() / 5) + 1) + " pages. | Length: " + toTimestamp(queueTimeLength));
            eb.setColor(botColour);
            eb.setThumbnail("https://img.youtube.com/vi/" + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
            event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
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
        //Checks if the bot is now alone
        GuildVoiceState voiceState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());
        if (voiceState.getChannel() != null) {
            int members = 0;
            List<Member> a = voiceState.getChannel().getMembers();
            if (!a.listIterator().next().getUser().isBot()) { //Bots arent members
                members++;
            }
            if (members == 0) { //If alone
                PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.destroy();
                addToVote(event.getGuild().getIdLong(), new ArrayList<>());
            }
        }
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if (event.getMember().getUser() == event.getJDA().getSelfUser()) {
            if (event.getNewValue().getMembers().size() == 1) { // assuming the bot is alone there.
                PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.destroy();
                addToVote(event.getGuild().getIdLong(), new ArrayList<>());
            }
        }
    }

    @Override
    public void onException(@NotNull ExceptionEvent event) {
        printlnTime(Arrays.toString(event.getCause().getStackTrace()));
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
        for (Member member : botChannel.getMembers()) {
            if (!member.getUser().isBot()) {
                botChannelMemberCount++;
            }
        }
        if (botChannelMemberCount == 0) {
            PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.destroy();
            event.getGuild().getAudioManager().closeAudioConnection();
            addToVote(event.getGuild().getIdLong(), new ArrayList<>());
        }
    }

    public static String getRadio(String search) throws IOException {
        URL url = null;
        try {
            url = new URL("https://www.internet-radio.com/search/?radio=" + search);
        } catch (Exception e){e.printStackTrace();}
        assert url != null;
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))){
            for (String line; (line = reader.readLine()) != null;) {
                builder.append(line);
            }
        } catch (Exception e){e.printStackTrace();}
        Pattern pattern = Pattern.compile("ga\\('send', 'event', 'tunein', 'playm3u', '([^']+)'\\);");
        Matcher matcher = pattern.matcher(builder.toString());
        if (matcher.find()) {
            return(matcher.group(1));
        } else {
            return "None";
        }
    }

    private boolean processCommand(String matchTerm, BaseCommand Command, MessageReceivedEvent event) {
        String commandLower = event.getMessage().getContentRaw().toLowerCase();
        if (commandLower.startsWith(matchTerm)) {
            if (commandLower.length() != matchTerm.length()) { //Makes sure we arent misinterpreting
                String afterChar = commandLower.substring(matchTerm.length(), matchTerm.length() + 1);
                if (!afterChar.equals(" ") && !afterChar.equals("\n")) { //Ensure theres whitespace afterwards
                    return false;
                }
            }
            // ratelimit code to go here
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
                for (String alias : Command.getNames()) {
                    if (processCommand(botPrefix + alias, Command, event)) {
                        return; //Command executed, stop checking
                    }
                }
            }
        }
    }
}