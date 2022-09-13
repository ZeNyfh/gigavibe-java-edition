package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static Bots.Main.*;

public class CommandRadio extends BaseCommand {
    public HashMap<String, String> getRadios() {
        HashMap<String, String> radioLists = new HashMap<String, String>();
        radioLists.put("Hardbass", "https://server6.inetcast.nl:2015/stream");
        radioLists.put("Hardcore", "http://cc5.beheerstream.com:8022/stream");
        radioLists.put("Trance", "http://fr3.1mix.co.uk:8060/320?type=http&nocache=390123");
        radioLists.put("Portugal", "http://137.74.160.250:8000/;stream/1");
        radioLists.put("Heart", "https://media-ssl.musicradio.com/HeartLondon");
        radioLists.put("Portugal pop", "https://media3.mcr.iol.pt/livefm/comercial.mp3/icecast.audio");
        radioLists.put("Poland", "https://rs6-krk2-cyfronet.rmfstream.pl/RMFFM48");
        radioLists.put("Lithuania", "https://radio.m-1.fm/m1plius/aacp64");
        radioLists.put("USA Country", "https://ais-sa2.cdnstream1.com/1976_128.mp3");
        radioLists.put("USA Classic Rock", "https://hdradioclassicrock-rfritschka.radioca.st/stream");
        radioLists.put("EDM", "http://fr1.1mix.co.uk:8060/320h");
        radioLists.put("Beats n Breaks", "http://83.137.145.141:14280/;");
        radioLists.put("Danish", "https://live-bauerdk.sharp-stream.com/nova_dk_mp3");
        return radioLists;
    }


    @Override
    public void execute(MessageEvent event) throws IOException {
        String arg = "null";
        String argFinal = "";
        if (event.getArgs().length != 1) {
            for (int i = 1; i < event.getArgs().length; ) {
                argFinal = argFinal + " " + event.getArgs()[i];
                i++;
            }
            arg = event.getArgs()[1];
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        eb.appendDescription("\uD83D\uDCFB **Radio list:**\n\n");
        getRadios().forEach((key, value) -> eb.appendDescription("**[" + key + "](" + value + ")**\n"));
        eb.setFooter("Use \"" + botPrefix + "radio <Radio Name>\" to play a radio station.");
        assert arg != null;
        if (arg.equals("list") || event.getArgs().length == 1) {
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
        printlnTime(argFinal);
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
        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Not a valid radio station, heres a list of the valid radio stations.")).queue();
        event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("radios");
        return list;
    }

    @Override
    public String getParams() {
        return "<List/Radio Name>";
    }

    public String getCategory() {
        return "Music";
    }

    public String getName() {
        return "radio";
    }

    public String getDescription() {
        return "Plays a radio station.";
    }

    public long getTimeout() {
        return 5000;
    }
}
