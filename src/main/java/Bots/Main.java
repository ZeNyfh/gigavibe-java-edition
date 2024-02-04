package Bots;

import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter;
import com.sedmelluq.discord.lavaplayer.filter.AudioFilter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.middleman.AudioChannel;
import net.dv8tion.jda.api.entities.channel.unions.GuildMessageChannelUnion;
import net.dv8tion.jda.api.events.ExceptionEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.session.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static Bots.ConfigManager.GetConfig;
import static Bots.ConfigManager.SaveConfigs;
import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    public static final long Uptime = currentTimeMillis();
    public final static GatewayIntent[] INTENTS = {GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES};
    public static JSONObject commandUsageTracker;
    private static final HashMap<BaseCommand, HashMap<Long, Long>> ratelimitTracker = new HashMap<>();
    private static final HashMap<String, Consumer<ButtonInteractionEvent>> ButtonInteractionMappings = new HashMap<>();
    public static Color botColour = new Color(0, 0, 0);
    public static String botPrefix = "";
    public static String readableBotPrefix = "";
    public static HashMap<Long, List<Member>> skips = new HashMap<>();
    public static HashMap<Long, Integer> queuePages = new HashMap<>();
    public static String botVersion = ""; // YY.MM.DD
    public static List<String> LoopGuilds = new ArrayList<>();
    public static List<Long> AutoplayGuilds = new ArrayList<>();
    public static List<String> LoopQueueGuilds = new ArrayList<>();
    public static List<BaseCommand> commands = new ArrayList<>();
    public static boolean ignoreFiles = false;
    public static FileWriter logger;
    public static List<String> commandNames = new ArrayList<>(); //Purely for conflict detection
    public static HashMap<Long, Integer> trackLoops = new HashMap<>();
    public enum filters {
        Vibrato, Timescale
    }
    public static HashMap<Long, HashMap<filters, AudioFilter>> guildFilters = new HashMap<>();

    public static TimerTask task;

    public static Timer timer;

    public static void registerCommand(BaseCommand command) {
        command.Init();
        ratelimitTracker.put(command, new HashMap<>());
        commandUsageTracker.putIfAbsent(command.getNames()[0], 0L);
        commands.add(command);
        for (String name : command.getNames()) {
            if (commandNames.contains(name)) {
                printlnTime("Command conflict - 2 commands are attempting to use the name " + name);
            } else {
                commandNames.add(name);
            }
        }
    }

    public static void main(String[] args) throws Exception {
        ignoreFiles = new File("modules/").mkdir();
        if (!new File("modules/ffprobe*").exists()) {
            printlnTime("WARNING: ffprobe does not exist, be sure to download it from \"https://ffmpeg.org/download.html\" and place it into \"" + new File("modules\"").getAbsolutePath());
        }
        ignoreFiles = new File("config/").mkdir();
        ignoreFiles = new File("update/").mkdir();
        commandUsageTracker = GetConfig("usage-stats");
        ignoreFiles = new File("logs/").mkdir();
        File logFile = new File("logs/log.txt");
        if (logFile.length() != 0) {
            ignoreFiles = logFile.renameTo(new File("logs/log_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt"));
        }
        ignoreFiles = logFile.createNewFile();
        logger = new FileWriter("logs/log.txt");
        Message.suppressContentIntentWarning();
        botVersion = new SimpleDateFormat("yy.MM.dd").format(new Date(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).lastModified()));
        File env = new File(".env");
        if (!env.exists()) {
            printlnTime(env.getName() + " doesn't exist, creating now.");
            ignoreFiles = env.createNewFile();
            FileWriter writer = new FileWriter(".env");
            writer.write("# This is the bot token, it needs to be set.\nTOKEN=\n# Feel free to change the prefix to anything else.\nPREFIX=\n# These 2 are required for spotify support with the bot.\nSPOTIFYCLIENTID=\nSPOTIFYCLIENTSECRET=\n# This is the hex value for the bot colour\nCOLOUR=");
            writer.flush();
            writer.close();
        }
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            File startsh = new File("start.sh");
            if (!startsh.exists()) {
                ignoreFiles = startsh.createNewFile();
                FileWriter writer = new FileWriter("start.sh");
                writer.write("#!/bin/bash\nwhile [ True ]; do\n\tjava -jar bot.jar\n\tsleep 2\ndone");
                writer.flush();
                writer.close();
            }
        }
        Dotenv dotenv = Dotenv.load();
        if (dotenv.get("TOKEN") == null) {
            printlnTime("TOKEN is not set in " + new File(".env").getAbsolutePath());
        }
        String botToken = dotenv.get("TOKEN");

        if (dotenv.get("COLOUR") == null) {
            printlnTime("Hex value COLOUR is not set in " + new File(".env" + "\n example: #FFCCEE").getAbsolutePath());
            return;
        }
        try {
            botColour = Color.decode(dotenv.get("COLOUR"));
        } catch (NumberFormatException e) {
            printlnTime("Colour was invalid.");
            e.fillInStackTrace();
            return;
        }

        try {
            List<Class<?>> classes = new ArrayList<>();
            String tempJarPath = String.valueOf(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            printlnTime(tempJarPath);
            JarFile jarFile = null;
            boolean jarFileCheck = false;
            try {
                jarFile = new JarFile(tempJarPath.substring(5));
            } catch (FileNotFoundException ignored) {
                printlnTime("detected process in IDE, registering commands in a different way...");
                Enumeration<URL> resources = ClassLoader.getSystemClassLoader().getResources("");
                while (resources.hasMoreElements()) {
                    URL url = resources.nextElement();
                    if (url.getPath().contains("classes")) {
                        url = new URL("file:" + url.getPath() + "Bots/commands/");
                    }
                    try {
                        for (File classFile : Objects.requireNonNull(new File(url.getFile()).listFiles())) {
                            if (classFile.getName().endsWith(".class")) {
                                classes.add(ClassLoader.getSystemClassLoader().loadClass("Bots.commands." + classFile.getName().substring(0, classFile.getName().length() - 6)));
                            }
                        }
                        break;
                    } catch (Exception ignored1) {
                    }
                }
                jarFileCheck = true;
            }
            if (!jarFileCheck) {
                Enumeration<JarEntry> resources = jarFile.entries();
                while (resources.hasMoreElements()) {
                    JarEntry url = resources.nextElement();
                    if (url.toString().endsWith(".class") && url.toString().startsWith("Bots/commands/Command")) {
                        classes.add(ClassLoader.getSystemClassLoader().loadClass(url.getName().substring(0, url.getName().length() - 6).replaceAll("/", ".")));
                    }
                }
                jarFile.close();
            }

            // registering all the commands
            for (Class<?> commandClass : classes) {
                try {
                    registerCommand((BaseCommand) commandClass.getDeclaredConstructor().newInstance());
                    printlnTime("loaded command: " + commandClass.getSimpleName().substring(7));
                } catch (Exception e) {
                    printlnTime("Unable to load command " + commandClass.getSimpleName().substring(7));
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConfigManager.Init();
        PlayerManager.getInstance();

        JDA bot = JDABuilder.create(botToken, Arrays.asList(INTENTS))
                .enableCache(CacheFlag.MEMBER_OVERRIDES, CacheFlag.VOICE_STATE)
                .disableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.SCHEDULED_EVENTS)
                .addEventListeners(new Main())
                .build();
        bot.awaitReady();
        printlnTime("bot is now running, have fun ig");
        botPrefix = "<@" + bot.getSelfUser().getId() + ">";
        readableBotPrefix = "@" + bot.getSelfUser().getName();
        bot.getPresence().setActivity(Activity.playing("Use \"" + readableBotPrefix + " help\" | The bot is in " + bot.getGuilds().size() + " Servers!"));
        for (Guild guild : bot.getGuilds()) {
            queuePages.put(guild.getIdLong(), 0);
            trackLoops.put(guild.getIdLong(), 0);

            // audio filter stuff
            PlayerManager.getInstance().getMusicManager(guild).audioPlayer.setFilterFactory((track, format, output) -> {
                HashMap<filters, AudioFilter> filterMap = new HashMap<>();
                filterMap.put(filters.Vibrato, new VibratoPcmAudioFilter(output, format.channelCount, format.sampleRate));
                filterMap.put(filters.Timescale, new TimescalePcmAudioFilter(output, format.channelCount, format.sampleRate));
                guildFilters.put(guild.getIdLong(), filterMap);
                return new ArrayList<>(filterMap.values());
            });
        }

        try {
            Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::SaveConfigs));
            // auto updater code
            timer = new Timer();
            task = new TimerTask() {
                final File updateFile = new File("update/bot.jar");
                int time = 0;

                @Override
                public void run() {
                    boolean isInAnyVc = false;
                    for (Guild guild : bot.getGuilds()) {
                        if (guild.getAudioManager().isConnected()) {
                            isInAnyVc = true;
                        }
                    }

                    if (!isInAnyVc) { // not in vc
                        time++;
                        if (time >= 300 && updateFile.exists() && !System.getProperty("os.name").toLowerCase().contains("windows")) { // auto-updater only works on linux
                            // leeway for upload past the time limit
                            if (System.currentTimeMillis() - updateFile.lastModified() >= 10000) {
                                printlnTime("It's update time!");
                                File botJar = new File("bot.jar");
                                ignoreFiles = botJar.delete();
                                ignoreFiles = updateFile.renameTo(botJar);
                                killMain();
                            }
                        }
                    } else { // in a vc
                        time = 0;
                    }
                }
            };
            timer.scheduleAtFixedRate(task, 0, 1000);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public static MessageEmbed createQuickEmbed(String title, String description) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setTitle(title);
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
        seconds /= 1000;
        if (seconds <= 0) {
            return "0 seconds";
        } else {
            long days = seconds / 86400;
            seconds %= 86400;
            long hours = seconds / 3600;
            seconds %= 3600;
            long minutes = seconds / 60;
            seconds %= 60;
            ArrayList<String> totalSet = new ArrayList<>();
            if (days != 0) totalSet.add(days + (days == 1 ? " day" : " days"));
            if (hours != 0) totalSet.add(hours + (hours == 1 ? " hour" : " hours"));
            if (minutes != 0) totalSet.add(minutes + (minutes == 1 ? " minute" : " minutes"));
            if (seconds != 0) totalSet.add(seconds + (seconds == 1 ? " second" : " seconds"));
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

    public static void clearVotes(Long guildID) {
        skips.put(guildID, new ArrayList<>());
    }

    public static List<Member> getVotes(Long guildID) {
        skips.putIfAbsent(guildID, new ArrayList<>());
        return skips.get(guildID);
    }

    public static boolean IsChannelBlocked(Guild guild, GuildMessageChannelUnion commandChannel) {
        JSONObject config = ConfigManager.GetGuildConfig(guild.getIdLong());
        JSONArray blockedChannels = (JSONArray) config.get("BlockedChannels");
        for (Object blockedChannel : blockedChannels) {
            if (commandChannel.getId().equals(blockedChannel)) {
                commandChannel.sendMessageEmbeds(createQuickEmbed("❌ **Blocked channel**", "you cannot use this command in this channel.")).queue();
                return true;
            }
        }
        return false;
    }

    public static boolean IsDJ(Guild guild, GuildMessageChannelUnion commandChannel, Member member) {
        int people = 0;
        if (member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
            for (Member vcMember : Objects.requireNonNull(Objects.requireNonNull(member.getVoiceState()).getChannel()).getMembers()) {
                if (!vcMember.getUser().isBot()) {
                    people++;
                }
            }
            if (people == 1) { //People alone in a VC are allowed to use VC DJ commands
                return true;
            }
        }
        JSONObject config = ConfigManager.GetGuildConfig(guild.getIdLong());
        JSONArray DJRoles = (JSONArray) config.get("DJRoles");
        JSONArray DJUsers = (JSONArray) config.get("DJUsers");
        boolean check = false;
        for (Object DJRole : DJRoles) {
            if (member.getRoles().contains(guild.getJDA().getRoleById((Long) DJRole))) {
                check = true;
            }
        }
        if (!check) {
            for (Object DJUser : DJUsers) {
                if (DJUser.equals(member.getIdLong())) {
                    check = true;
                }
            }
        }
        if (check) {
            return true;
        } else {
            commandChannel.sendMessageEmbeds(createQuickEmbed("❌ **Insufficient permissions**", "You do not have a DJ role.")).queue();
            return false;
        }
    }

    public static void deleteFiles(String filePrefix) { // ONLY USE THIS IF YOU KNOW WHAT YOU ARE DOING
        try {
            String[] command = System.getProperty("os.name").toLowerCase().contains("windows") ? new String[]{"cmd", "/c", "del", filePrefix + "*"} : new String[]{"sh", "-c", "rm " + filePrefix + "*"};
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                printlnTime("Error deleting file.");
            }
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    public static void printlnTime(Object... message) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        StringBuilder finalMessage = new StringBuilder(dtf.format(LocalDateTime.now()) + " |");
        for (Object segment : message) {
            finalMessage.append(" ").append(segment);
        }
        System.out.println(finalMessage);
        try {
            logger.write(finalMessage + "\n");
            logger.flush();
        } catch (Exception ignored) {
        }
    }

    public static void registerButtonInteraction(String[] names, Consumer<ButtonInteractionEvent> func) {
        for (String name : names) {
            registerButtonInteraction(name, func);
        }
    }

    public static void registerButtonInteraction(String name, Consumer<ButtonInteractionEvent> func) {
        if (ButtonInteractionMappings.containsKey(name)) {
            printlnTime("Attempting to override the button manager for id " + name);
        }
        ButtonInteractionMappings.put(name.toLowerCase(), func);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        queuePages.put(event.getGuild().getIdLong(), 0);
        event.getJDA().getPresence().setActivity(Activity.playing("music for " + event.getJDA().getGuilds().size() + " servers! | " + readableBotPrefix + " help"));
        Objects.requireNonNull(event.getGuild().getDefaultChannel()).asStandardGuildMessageChannel().sendMessageEmbeds(createQuickEmbed("**Important!**", "This is a music bot which needs some setting up done first for the best experience. You can use `" + botPrefix + "help` or simply reply to this message with `help` for a general overview of the commands.\n\nAdd dj roles/users with the `" + botPrefix + "dj` command. This will allow some users or roles to have more control over the bots functions with commands like forceskip, disconnect and shuffle.\nIf you wish to give boosters this permission, just add the booster role to the dj roles.\n\nYou can also add optional blocked channels, which will disallow some commands from being used in the blocked channels. This can be done with the `" + botPrefix + "blockchannel` command.\n\nIf you encounter any bugs, issues, or have any feature requests, use `" + botPrefix + "bug <Message>` to report it to the developer")).queue();
    }

    @Override
    public void onGuildLeave(@NotNull GuildLeaveEvent event) {
        event.getJDA().getPresence().setActivity(Activity.playing("music for " + event.getJDA().getGuilds().size() + " servers! | " + readableBotPrefix + " help"));
        queuePages.remove(event.getGuild().getIdLong());
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String buttonID = Objects.requireNonNull(event.getInteraction().getButton().getId()).toLowerCase();
        for (String name : ButtonInteractionMappings.keySet()) {
            if (Objects.equals(name, buttonID)) {
                ButtonInteractionMappings.get(name).accept(event);
                return;
            }
        }
        printlnTime("Button of ID " + buttonID + " has gone ignored - missing listener?");
    }

    @Override
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null) {
            return;
            // GuildVoiceJoinEvent
        } else if (event.getChannelJoined() == null) {
            // GuildVoiceLeaveEvent
            if (event.getChannelLeft().getMembers().contains(event.getGuild().getSelfMember())) {
                List<Member> currentVotes = getVotes(event.getGuild().getIdLong());
                if (currentVotes != null) {
                    currentVotes.remove(event.getMember());
                    clearVotes(event.getGuild().getIdLong());
                }
            }
            if (event.getMember() == event.getGuild().getSelfMember()) {
                cleanUpAudioPlayer(event.getGuild());
                return;
            }
            AudioChannel botChannel = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel();
            if (!Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).inAudioChannel() || botChannel == null) { // if not in vc, clear for safe measure
                cleanUpAudioPlayer(event.getGuild());
                return;
            }
            int botChannelMemberCount = 0;
            for (Member member : botChannel.getMembers()) {
                if (!member.getUser().isBot()) {
                    botChannelMemberCount++;
                }
            }
            if (botChannelMemberCount == 0) {
                cleanUpAudioPlayer(event.getGuild());
            }
        } else {
            // GuildVoiceMoveEvent
            if (event.getMember().getUser() == event.getJDA().getSelfUser()) {
                if (Objects.requireNonNull(event.getNewValue()).getMembers().size() == 1) { // assuming the bot is alone there.
                    cleanUpAudioPlayer(event.getGuild());
                }
            }
        }
        GuildVoiceState voiceState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());
        if (voiceState.getChannel() != null) {
            int members = 0;
            for (Member member : voiceState.getChannel().getMembers()) {
                if (!member.getUser().isBot()) {
                    members++;
                }
            }
            if (members == 0) { // If alone
                try {
                    cleanUpAudioPlayer(event.getGuild());
                } catch (Exception ignored) {
                }
            }
        }
    }

    public static void cleanUpAudioPlayer(Guild guild) {
        Long id = guild.getIdLong();
        GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(guild);
        String stringID = id.toString();
        LoopGuilds.remove(stringID);
        LoopQueueGuilds.remove(stringID);
        manager.audioPlayer.setVolume(100);
        manager.scheduler.queue.clear();
        manager.audioPlayer.destroy();
        manager.audioPlayer.setPaused(false);
        manager.audioPlayer.checkCleanup(0);
        guild.getAudioManager().closeAudioConnection();
        clearVotes(id);
    }
    @Override
    public void onException(@NotNull ExceptionEvent event) {
        printlnTime(Arrays.toString(event.getCause().getStackTrace()));
    }

    private float handleRateLimit(BaseCommand Command, Member member) {
        long ratelimit = Command.getRatelimit();
        long lastRatelimit = ratelimitTracker.get(Command).getOrDefault(member.getIdLong(), 0L);
        long curTime = System.currentTimeMillis();
        float timeLeft = (ratelimit - (curTime - lastRatelimit)) / 1000F;
        if (timeLeft > 0f) {
            return timeLeft;
        } else {
            ratelimitTracker.get(Command).put(member.getIdLong(), curTime);
            return -1F;
        }
    }

    private boolean processSlashCommand(BaseCommand Command, SlashCommandInteractionEvent event) {
        if (event.getInteraction().getName().equalsIgnoreCase(Command.getNames()[0])) {
            float ratelimitTime = handleRateLimit(Command, Objects.requireNonNull(event.getInteraction().getMember()));
            if (ratelimitTime > 0) {
                event.replyEmbeds(createQuickError("You cannot use this command for another " + ratelimitTime + " seconds.")).queue(message -> message.deleteOriginal().queueAfter((long) ratelimitTime, TimeUnit.SECONDS));
                return false;
            }
            //run command
            String primaryName = Command.getNames()[0];
            commandUsageTracker.put(primaryName, Long.parseLong(String.valueOf(commandUsageTracker.get(primaryName))) + 1); //Nightmarish type conversion but I'm not seeing better
            try {
                Command.execute(new MessageEvent(event));
            } catch (Exception e) {
                e.fillInStackTrace();
            }
            return true;
        }
        return false;
    }

    private boolean processCommand(String matchTerm, BaseCommand Command, MessageReceivedEvent event) {
        String commandLower = event.getMessage().getContentRaw().toLowerCase();
        commandLower = commandLower.replaceFirst(botPrefix, "").trim();
        if (commandLower.startsWith(matchTerm)) {
            if (commandLower.length() != matchTerm.length()) { //Makes sure we arent misinterpreting
                String afterChar = commandLower.substring(matchTerm.length(), matchTerm.length() + 1);
                if (!afterChar.equals(" ") && !afterChar.equals("\n")) { //Ensure theres whitespace afterwards
                    return false;
                }
            }
            //ratelimit code. ratelimit is per-user across all guilds
            float ratelimitTime = handleRateLimit(Command, Objects.requireNonNull(event.getMember()));
            if (ratelimitTime > 0) {
                event.getMessage().replyEmbeds(createQuickError("You cannot use this command for another " + ratelimitTime + " seconds.")).queue(message -> message.delete().queueAfter((long) ratelimitTime, TimeUnit.SECONDS));
                return false;
            }
            //run command
            String primaryName = Command.getNames()[0];
            commandUsageTracker.put(primaryName, Long.parseLong(String.valueOf(commandUsageTracker.get(primaryName))) + 1); //Nightmarish type conversion but I'm not seeing better
            try {
                Command.execute(new MessageEvent(event));
            } catch (Exception e) {
                e.fillInStackTrace();
            }
            return true;
        }
        return false;
    }

    public static void killMain() {
        SaveConfigs();
        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            e.fillInStackTrace();
        }
        System.exit(0);
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event) {
        event.getJDA().getAudioManagers().clear();
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> data = new ArrayList<>();
        List<net.dv8tion.jda.api.interactions.commands.Command> eventCommands = event.getGuild().retrieveCommands().complete();
        List<String> eventCommandsStr = new ArrayList<>();
        for (BaseCommand Command : commands) {
            for (net.dv8tion.jda.api.interactions.commands.Command command : eventCommands) {
                eventCommandsStr.add(command.getName().toLowerCase());
            }
            if (Command.slashCommand != null) {
                data.add(Command.slashCommand);
            } else {
                SlashCommandData slashCommand = Commands.slash(Command.getNames()[0], Command.getDescription());
                Command.ProvideOptions(slashCommand);
                Command.slashCommand = slashCommand;
                data.add(slashCommand);
            }
        }
        if (new HashSet<>(commandNames).containsAll(eventCommandsStr)) {
            return;
        }
        event.getGuild().updateCommands().addCommands(data).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        for (BaseCommand Command : commands) {
            if (processSlashCommand(Command, event)) {
                return;
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw();
        if (messageContent.isEmpty()) {
            return;
        }

        if (event.getMessage().getMentions().getUsers().contains(event.getJDA().getSelfUser())) {
            for (BaseCommand Command : commands) {
                for (String alias : Command.getNames()) {
                    if (processCommand(alias, Command, event)) {
                        return; //Command executed, stop checking
                    }
                }
            }
        }
    }
}