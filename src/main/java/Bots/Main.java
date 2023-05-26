package Bots;

import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
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

import javax.security.auth.login.LoginException;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static Bots.ConfigManager.GetConfig;
import static java.lang.Math.round;
import static java.lang.System.currentTimeMillis;

public class Main extends ListenerAdapter {
    public static final long Uptime = currentTimeMillis();
    public final static GatewayIntent[] INTENTS = {GatewayIntent.GUILD_EMOJIS_AND_STICKERS, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_MESSAGE_REACTIONS, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT};
    private static final HashMap<BaseCommand, HashMap<Long, Long>> ratelimitTracker = new HashMap<>();
    public static final JSONObject commandUsageTracker = GetConfig("usage-stats");
    public static Color botColour = new Color(0, 0, 0);
    public static String botPrefix = "";
    public static String botToken = "";
    public static HashMap<Long, List<Member>> skips = new HashMap<>();
    public static HashMap<Long, Integer> queuePages = new HashMap<>();
    public static HashMap<Long, Integer> guildTimeouts = new HashMap<>();
    public static String botVersion = ""; // YY.MM.DD
    public static List<String> LoopGuilds = new ArrayList<>();
    public static List<String> LoopQueueGuilds = new ArrayList<>();
    public static List<BaseCommand> commands = new ArrayList<>();
    public static List<String> commandNames = new ArrayList<>(); //Purely for conflict detection
    public static HashMap<Long, Integer> trackLoops = new HashMap<>();

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

    public static void main(String[] args) throws InterruptedException, LoginException, IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException, URISyntaxException {
        botVersion = new SimpleDateFormat("yy.MM.dd").format(new Date(new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI()).lastModified()));
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
            e.printStackTrace();
            return;
        }
        // cleanup of old downloaded stuff
        deleteFiles(new File("auddl" + File.separator).getAbsolutePath());
        deleteFiles(new File("viddl" + File.separator).getAbsolutePath());
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
                registerCommand((BaseCommand) commandClass.getDeclaredConstructor().newInstance());
                printlnTime("loaded command: " + commandClass.getSimpleName().substring(7));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        ConfigManager.Init();
        PlayerManager.getInstance();

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
            trackLoops.put(guild.getIdLong(), 0);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(ConfigManager::SaveConfigs));

        Timer timer = new Timer();
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                // timeouts
                for (Guild guild : bot.getGuilds()) {
                    if (guild.getAudioManager().isConnected()) {
                        int i = 0;
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

                // reminders
                JSONObject reminders = GetConfig("reminders");
                Iterator iterator = reminders.keySet().iterator();
                while (iterator.hasNext()) {
                    Object key = iterator.next();
                    JSONArray reminderData = (JSONArray) reminders.get(key);
                    if (currentTimeMillis() < Long.parseLong((String) reminderData.get(0))) {
                        continue;
                    }
                    iterator.remove();
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("**Reminder!**");
                    if (reminderData.size() == 4) {
                        builder.appendDescription("\n" + reminderData.get(3)); // adding the optional reason
                    } else {
                        builder.appendDescription(" "); // required because it complains about the description being nothing
                    }
                    String initialMessage = (Objects.requireNonNull(bot.getUserById((String) reminderData.get(2)))).getAsMention();
                    // sending the message, yes this is compatible with slash commands
                    try {
                        Objects.requireNonNull(
                                bot.getTextChannelById((String) reminderData.get(1))
                        ).sendMessage(initialMessage).queue(message -> message.editMessageEmbeds(builder.build()).queue());
                    } catch (NullPointerException ignored) {
                        Objects.requireNonNull(
                                bot.getThreadChannelById((String) reminderData.get(1))
                        ).sendMessage(initialMessage).queue(message -> message.editMessageEmbeds(builder.build()).queue());
                    }
                }
            }
        };
        timer.scheduleAtFixedRate(task, 0, 1000);
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
        for (int i = 0; i < blockedChannels.size(); ) {
            if (commandChannel.getId().equals(blockedChannels.get(i))) {
                commandChannel.sendMessageEmbeds(createQuickEmbed("❌ **Blocked channel**", "you cannot use this command in this channel.")).queue();
                return true;
            }
            i++;
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
            if (people == 1) {
                return true;
            }
        }
        JSONObject config = ConfigManager.GetGuildConfig(guild.getIdLong());
        JSONArray DJRoles = (JSONArray) config.get("DJRoles");
        JSONArray DJUsers = (JSONArray) config.get("DJUsers");
        boolean check = false;
        for (int i = 0; i < DJRoles.size(); ) {
            if (member.getRoles().contains(guild.getJDA().getRoleById((Long) DJRoles.get(i)))) {
                check = true;
            }
            i++;
        }
        if (!check) {
            for (int i = 0; i < DJUsers.size(); ) {
                if (DJUsers.get(i).equals(member.getIdLong())) {
                    check = true;
                }
                i++;
            }
        }
        if (check) {
            return true;
        } else {
            commandChannel.sendMessageEmbeds(createQuickEmbed("❌ **Insufficient permissions**", "you do not have a DJ role.")).queue();
            return false;
        }
    }

    public static void deleteFiles(String filePrefix) { // ONLY USE THIS IF YOU KNOW WHAT YOU ARE DOING
        try {
            String[] command;
            if (System.getProperty("os.name").startsWith("Windows")) {
                // Windows command
                command = new String[]{"cmd", "/c", "del", filePrefix + "*"};
            } else {
                // Linux command
                command = new String[]{"sh", "-c", "rm " + filePrefix + "*"};
            }
            Process process = Runtime.getRuntime().exec(command);
            int exitCode = process.waitFor();
            if (exitCode != 0) {
                printlnTime("Error deleting file.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void printlnTime(Object... message) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        StringBuilder finalMessage = new StringBuilder(dtf.format(LocalDateTime.now()) + " |");
        for (Object segment : message) {
            finalMessage.append(" ").append(segment);
        }
        System.out.println(finalMessage);
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        queuePages.put(event.getGuild().getIdLong(), 0);
        guildTimeouts.put(event.getGuild().getIdLong(), 0);
        event.getJDA().getPresence().setActivity(Activity.playing("music for " + event.getJDA().getGuilds().size() + " servers! | ?help"));
        Objects.requireNonNull(event.getGuild().getDefaultChannel()).asStandardGuildMessageChannel().sendMessageEmbeds(createQuickEmbed("**Important!**", "This is a music bot which needs some setting up done first for the best experience. You can use `" + botPrefix + "help` for a general overview of the commands.\n\nAdd dj roles/users with the `" + botPrefix + "dj` command. This will allow some users or roles to have more control over the bots functions with commands like forceskip, disconnect and shuffle.\nIf you wish to give boosters this permission, just add the booster role to the dj roles.\n\nYou can also add optional blocked channels, which will disallow some commands from being used in the blocked channels. This can be done with the `" + botPrefix + "blockchannel` command.\n\nIf you encounter any bugs, issues, or have any feature requests, use `" + botPrefix + "bug <Message>` to report it to the developer")).queue();
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
        eb.setFooter("Syntax: \"<>\" is a required argument, \"[]\" is an optional argument. \"()\" is an alternate word for the command.");
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
                        StringBuilder builder = new StringBuilder();
                        if (Command.getNames().length == 2) {
                            builder.append("\n`Alias:` ");
                        } else if (Command.getNames().length > 2) {
                            builder.append("\n`Aliases:` ");
                        }
                        int j = 0;
                        for (String name : Command.getNames()) {
                            j++;
                            if (Command.getNames().length == 1 || j == 1) {
                                continue;
                            }
                            if (j != Command.getNames().length) {
                                builder.append("**(").append(name).append(")**, ");
                            } else {
                                builder.append("**(").append(name).append(")**");
                            }
                        }
                        eb.appendDescription("`" + i + ")` **" + Command.getNames()[0] + " " + Command.getOptions() + "** - " + Command.getDescription() + builder + "\n\n");
                    }
                }
                break;
            case "forward":
            case "backward":
        }
        if (Objects.equals(event.getButton().getId(), "forward")) {
            queuePages.put(event.getGuild().getIdLong(), queuePages.get(event.getGuild().getIdLong()) + 1);
            if (queuePages.get(event.getGuild().getIdLong()) >= round((Queue.size() / 5F) + 1)) {
                queuePages.put(event.getGuild().getIdLong(), 1);
            }
            long queueTimeLength = 0;
            for (AudioTrack track : Queue) {
                if (track.getInfo().length < 432000000) {
                    queueTimeLength = queueTimeLength + track.getInfo().length;
                }
            }
            for (int j = 5 * queuePages.get(event.getGuild().getIdLong()) - 5; j < 5 * queuePages.get(event.getGuild().getIdLong()) && j < Queue.size(); ) {
                AudioTrackInfo trackInfo = Objects.requireNonNull(getTrackFromQueue(event.getGuild(), j)).getInfo();
                eb.appendDescription(j + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
                j++;
            }
            eb.setTitle("__**Now playing:**__\n" + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title, PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri);
            eb.setFooter(Queue.size() + " songs queued. | " + round((Queue.size() / 5F) + 1) + " pages. | Length: " + toTimestamp(queueTimeLength));
            eb.setColor(botColour);
            eb.setThumbnail("https://img.youtube.com/vi/" + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
            event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
        }
        if (Objects.equals(event.getButton().getId(), "backward")) {
            queuePages.put(event.getGuild().getIdLong(), queuePages.get(event.getGuild().getIdLong()) - 1);
            if (queuePages.get(event.getGuild().getIdLong()) <= 0) {
                queuePages.put(event.getGuild().getIdLong(), round((Queue.size() / 5F) + 1));
            }
            long queueTimeLength = 0;
            for (AudioTrack track : Queue) {
                if (track.getInfo().length < 432000000) {
                    queueTimeLength = queueTimeLength + track.getInfo().length;
                }
            }
            for (int j = 5 * queuePages.get(event.getGuild().getIdLong()) - 5; j < 5 * queuePages.get(event.getGuild().getIdLong()) && j < Queue.size(); ) {
                AudioTrackInfo trackInfo = Objects.requireNonNull(getTrackFromQueue(event.getGuild(), j)).getInfo();
                eb.appendDescription(j + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
                j++;
            }
            eb.setTitle("__**Now playing:**__\n" + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title, PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri);
            eb.setFooter(Queue.size() + " songs queued. | " + round((Queue.size() / 5F) + 1) + " pages. | Length: " + toTimestamp(queueTimeLength));
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
    public void onGuildVoiceUpdate(@NotNull GuildVoiceUpdateEvent event) {
        if (event.getChannelLeft() == null) {
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
                GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(event.getGuild());
                LoopGuilds.remove(event.getGuild().getId());
                LoopQueueGuilds.remove(event.getGuild().getId());
                manager.audioPlayer.setVolume(100);
                manager.scheduler.queue.clear();
                manager.audioPlayer.destroy();
                manager.audioPlayer.setPaused(false);
                manager.audioPlayer.checkCleanup(0);
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
                clearVotes(event.getGuild().getIdLong());
            }
        } else {
            // GuildVoiceMoveEvent
            if (event.getMember().getUser() == event.getJDA().getSelfUser()) {
                if (event.getNewValue().getMembers().size() == 1) { // assuming the bot is alone there.
                    PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.destroy();
                    clearVotes(event.getGuild().getIdLong());
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
            if (members == 0) { //If alone
                try {
                    GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
                    musicManager.scheduler.queue.clear();
                    musicManager.audioPlayer.destroy();
                    musicManager.audioPlayer.setPaused(false);
                    clearVotes(event.getGuild().getIdLong());
                    event.getGuild().getAudioManager().closeAudioConnection();
                } catch (Exception ignored) {
                }
            }
        }
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
                event.replyEmbeds(createQuickError("You cannot use this command for another " + ratelimitTime + " seconds.")).queue(message -> {
                    try {
                        Thread.sleep((long) ratelimitTime * 1000);
                        message.deleteOriginal().queue();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                return false;
            }
            //run command
            String primaryName = Command.getNames()[0];
            commandUsageTracker.put(primaryName, Long.parseLong(String.valueOf(commandUsageTracker.get(primaryName))) + 1); //Nightmarish type conversion but I'm not seeing better
            try {
                Command.execute(new MessageEvent(event));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return false;
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
            //ratelimit code. ratelimit is per-user across all guilds
            float ratelimitTime = handleRateLimit(Command, Objects.requireNonNull(event.getMember()));
            if (ratelimitTime > 0) {
                event.getMessage().replyEmbeds(createQuickError("You cannot use this command for another " + ratelimitTime + " seconds.")).queue(message -> {
                    try {
                        Thread.sleep((long) ratelimitTime * 1000);
                        message.delete().queue();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                });
                return false;
            }
            //run command
            String primaryName = Command.getNames()[0];
            commandUsageTracker.put(primaryName, Long.parseLong(String.valueOf(commandUsageTracker.get(primaryName))) + 1); //Nightmarish type conversion but I'm not seeing better
            try {
                Command.execute(new MessageEvent(event));
            } catch (Exception e) {
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
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> data = new ArrayList<>();
        for (BaseCommand Command : commands) {
            if (Command.slashCommand != null) {
                data.add(Command.slashCommand);
            } else {
                SlashCommandData slashCommand = Commands.slash(Command.getNames()[0], Command.getDescription());
                Command.ProvideOptions(slashCommand);
                Command.slashCommand = slashCommand;
                data.add(slashCommand);
            }
        }
        event.getGuild().updateCommands().addCommands(data).queue();
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.isFromGuild()) {
            event.replyEmbeds(createQuickError("Commands are currently unsupported in DMs")).queue();
            return;
        }
        for (BaseCommand Command : commands) {
            if (processSlashCommand(Command, event)) {
                return;
            }
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.isFromGuild() && event.getAuthor() != event.getJDA().getSelfUser()) {
            event.getMessage().replyEmbeds(createQuickError("Commands are currently unsupported in DMs")).queue();
            return;
        }
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