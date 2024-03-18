package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.Main.*;

public class CommandRadio extends BaseCommand {
    private static final Pattern pattern = Pattern.compile("ga\\('send', 'event', 'tunein', 'playm3u', '([^']+)'\\);");

    public static String getRadio(String search) throws IOException {
        URL url;
        try {
            url = new URL("https://www.internet-radio.com/search/?radio=" + search);
        } catch (Exception e) {
            e.printStackTrace();
            return "None";
        }
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        StringBuilder builder = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            for (String line; (line = reader.readLine()) != null; ) {
                builder.append(line);
            }
        } catch (Exception ignored) {
            return "None";
        }
        Matcher matcher = pattern.matcher(builder.toString());
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "None";
        }
    }


    private static final HashMap<String, Pattern> patterns = new HashMap<>() {{
        put("Spotify", Pattern.compile("<img src=\"([^\"]+)\" width=\""));
        put("SoundCloud", Pattern.compile("\"thumbnail_url\":\"([^\"]+)\",\""));
    }};

    HashMap<String, String> radioLists = new HashMap<>() {{
        put("Heart", "https://media-ssl.musicradio.com/HeartLondon");
        put("1Mix Trance", "http://fr3.1mix.co.uk:8060/320");
        put("1Mix EDM", "http://fr1.1mix.co.uk:8060/320h");
        put("Beats n Breaks", "http://83.137.145.141:14280/;");
        put("Hardcore", "http://cc5.beheerstream.com:8022/stream");
        put("USA Country", "https://ais-sa2.cdnstream1.com/1976_128.mp3");
        put("USA Classic Rock", "https://hdradioclassicrock-rfritschka.radioca.st/stream");
        put("Nova DK", "https://live-bauerdk.sharp-stream.com/nova_dk_mp3");
        put("Pro FM", "https://player.profm.nl/proxy/profm?mp=/stream");
        put("Radio Comercial", "https://media3.mcr.iol.pt/livefm/comercial.mp3/icecast.audio");
        put("RMF FM", "https://rs6-krk2-cyfronet.rmfstream.pl/RMFFM48");
        put("M1 Plius", "https://radio.m-1.fm/m1plius/aacp64");
        put("NRK Jazz", "http://lyd.nrk.no:80/nrk_radio_jazz_aac_h");
    }};
    public HashMap<String, String> getRadios() {
        return radioLists;
    }

    @Override
    public void execute(MessageEvent event) throws IOException {
        if (IsChannelBlocked(event.getGuild(), event.getChannel())) {
            return;
        }

        if (event.getArgs().length == 1 || event.getArgs()[1].equalsIgnoreCase("list")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(botColour);
            eb.appendDescription("\uD83D\uDCFB **Radio list:**\n\n");
            getRadios().forEach((key, value) -> eb.appendDescription("**[" + key + "](" + value + ")**\n"));
            eb.appendDescription("\n*Or use `" + botPrefix + "radio search <String>`*");
            eb.setFooter("Use \"" + readableBotPrefix + "radio <Radio Name>\" to play a radio station.");
            if (event.getArgs().length == 1) {
                event.replyEmbeds(createQuickError("No arguments given, heres some radio stations to choose from:"), eb.build());
            } else {
                event.replyEmbeds(eb.build());
            }
            return;
        }

        final AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember().getVoiceState());
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());
        if (!memberState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("you arent in a vc."));
            return;
        }
        if (selfState.getChannel() != null && selfState.getChannel() != memberState.getChannel()) {
            event.replyEmbeds(createQuickError("The bot is already busy in another vc"));
            return;
        }
        try {
            audioManager.openAudioConnection(memberState.getChannel());
        } catch (InsufficientPermissionException e) {
            event.replyEmbeds(createQuickError("The bot can't access your channel"));
            return;
        }
        event.deferReply(); //Give us time to think

        String radioURL = null;
        StringBuilder radioSearchTerm = new StringBuilder();
        if (event.getArgs()[1].equalsIgnoreCase("search")) {
            if (event.getArgs().length == 2) {
                event.replyEmbeds(createQuickError("No search term given."));
                return;
            }
            List<String> otherArgs = new ArrayList<>(List.of(event.getArgs()));
            otherArgs.remove(0);
            otherArgs.remove(0);
            int i = 0;
            for (String string : otherArgs) {
                i++;
                if (otherArgs.size() > i) {
                    radioSearchTerm.append(string).append("+");
                } else {
                    radioSearchTerm.append(string);
                }
            }
            radioURL = getRadio(radioSearchTerm.toString());
        }
        if (radioURL != null) {
            if (radioURL.equals("None")) {
                event.replyEmbeds(createQuickError("Couldn't find a radio station with the given name"));
            } else {
                //TODO: Consider somehow getting the real name of the station rather than using the search term that found it
                PlayerManager.getInstance().loadAndPlay(event, radioURL, false);
                event.replyEmbeds(createQuickEmbed("Queued Radio station:", "**[" + radioSearchTerm + "](" + radioURL + ")**"));
            }
        } else {
            String wantedRadio = event.getContentRaw().split(" ", 2)[1].toLowerCase();
            for (Map.Entry<String, String> tempMap : getRadios().entrySet()) {
                if (tempMap.getKey().equalsIgnoreCase(wantedRadio)) {
                    PlayerManager.getInstance().loadAndPlay(event, tempMap.getValue(), false);
                    event.replyEmbeds(createQuickEmbed("Queued Radio station:", "**[" + tempMap.getKey() + "](" + tempMap.getValue() + ")**"));
                    return;
                }
            }
            event.replyEmbeds(createQuickError("Not a valid radio station."));
        }
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "source", "Play a radio station. Prefix with \"search\" to search for a custom station", false);
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String getOptions() {
        return "<list OR name> OR <search> <Radio_Name>";
    }

    @Override
    public String[] getNames() {
        return new String[]{"radio", "radios"};
    }

    @Override
    public String getDescription() {
        return "Plays a radio station. Specify nothing for a list of some available radio stations";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
