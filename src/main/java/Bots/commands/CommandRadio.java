package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
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
    public static String getRadio(String search) throws IOException {
        URL url = null;
        try {
            url = new URL("https://www.internet-radio.com/search/?radio=" + search);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert url != null;
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
        Pattern pattern = Pattern.compile("ga\\('send', 'event', 'tunein', 'playm3u', '([^']+)'\\);");
        Matcher matcher = pattern.matcher(builder.toString());
        if (matcher.find()) {
            return (matcher.group(1));
        } else {
            return "None";
        }
    }

    public HashMap<String, String> getRadios() {
        HashMap<String, String> radioLists = new HashMap<String, String>();
        radioLists.put("Heart", "https://media-ssl.musicradio.com/HeartLondon");
        radioLists.put("1Mix Trance", "http://fr3.1mix.co.uk:8060/320");
        radioLists.put("1Mix EDM", "http://fr1.1mix.co.uk:8060/320h");
        radioLists.put("Estilo Leblon", "https://us4.internet-radio.com/proxy/radioestiloleblon?mp=/stream;");
        radioLists.put("Beats n Breaks", "http://83.137.145.141:14280/;");
        radioLists.put("Hardcore", "http://cc5.beheerstream.com:8022/stream");
        radioLists.put("USA Country", "https://ais-sa2.cdnstream1.com/1976_128.mp3");
        radioLists.put("USA Classic Rock", "https://hdradioclassicrock-rfritschka.radioca.st/stream");
        radioLists.put("Nova DK", "https://live-bauerdk.sharp-stream.com/nova_dk_mp3");
        radioLists.put("Pro FM", "https://player.profm.nl/proxy/profm?mp=/stream");
        radioLists.put("Radio Comercial", "https://media3.mcr.iol.pt/livefm/comercial.mp3/icecast.audio");
        radioLists.put("RMF FM", "https://rs6-krk2-cyfronet.rmfstream.pl/RMFFM48");
        radioLists.put("M1 Plius", "https://radio.m-1.fm/m1plius/aacp64");
        return radioLists;
    }

    @Override
    public void execute(MessageEvent event) throws IOException {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        eb.appendDescription("\uD83D\uDCFB **Radio list:**\n\n");
        getRadios().forEach((key, value) -> eb.appendDescription("**[" + key + "](" + value + ")**\n"));
        eb.appendDescription("\n*Or use `" + botPrefix + "radio search <String>`*");
        eb.setFooter("Use \"" + botPrefix + "radio <Radio Name>\" to play a radio station.");
        if (event.getArgs().length == 1) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No arguments given, heres some radio stations to choose from:")).queue();
            event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();
            eb.clear();
            return;
        }
        StringBuilder arg = new StringBuilder("null");
        String argFinal = "";
        if (event.getArgs().length != 1) {
            for (int i = 1; i < event.getArgs().length; ) {
                argFinal = argFinal + " " + event.getArgs()[i];
                i++;
            }
            arg = new StringBuilder(event.getArgs()[1]);
        }
        String radioURL = null;
        try {
            if (event.getArgs()[1] == null) {
                arg = new StringBuilder();
            }
        } catch (Exception ignored) {
        }
        assert event.getArgs()[1] != null;
        if (event.getArgs()[1].equalsIgnoreCase("search")) {
            if (event.getArgs().length == 2) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No search term given.")).queue();
                return;
            }
            arg = new StringBuilder();
            List<String> otherArgs = new ArrayList<>(List.of(event.getArgs()));
            otherArgs.remove(0);
            otherArgs.remove(0);
            int i = 0;
            for (String string : otherArgs) {
                i++;
                if (otherArgs.size() > i) {
                    arg.append(string).append("+");
                } else {
                    arg.append(string);
                }
            }
            radioURL = getRadio(arg.toString());
        }
        if (arg.toString().equalsIgnoreCase("list") || event.getArgs().length == 1) {
            event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();
            eb.clear();
            return;
        }
        final AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember()).getVoiceState();
        assert memberState != null;
        if (!memberState.inAudioChannel()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("you arent in a vc.")).queue();
            return;
        }
        final VoiceChannel memberChannel = (VoiceChannel) memberState.getChannel();
        argFinal = argFinal.toLowerCase().substring(1);
        if (radioURL != null) {
            audioManager.openAudioConnection(memberChannel);
            PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), radioURL, true);
        } else {
            for (Map.Entry<String, String> tempMap : getRadios().entrySet()) {
                if (tempMap.getKey().equalsIgnoreCase(argFinal)) {
                    if (IsChannelBlocked(event.getGuild(), event.getChannel().asTextChannel())) {
                        return;
                    }
                    audioManager.openAudioConnection(memberChannel);
                    PlayerManager.getInstance().loadAndPlay(event.getChannel().asTextChannel(), tempMap.getValue(), false);
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("Queued Radio station:", "**[" + tempMap.getKey() + "](" + tempMap.getValue() + ")**")).queue();
                    return;
                }
            }
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Not a valid radio station.")).queue();
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("**Disclaimer!**", "Some radio stations may not be available as these results are from [here](https://www.internet-radio.com/)")).queue(a -> {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                a.delete().queue();
            });
        }
    }

    @Override
    public String getParams() {
        return "<List>/<Radio Name>/<search> <Name>";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String[] getNames() {
        return new String[]{"radio", "radios"};
    }

    @Override
    public String getDescription() {
        return "Plays a radio station.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
