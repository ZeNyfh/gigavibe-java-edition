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
import java.util.Objects;

import static Bots.Main.*;

public class CommandRadio extends BaseCommand {
    String[] radioList = {"Hardbass", "Hardcore", "Trance", "Portugal", "Heart", "Portugal pop", "Poland", "Lithuania", "USA Country", "USA Classic Rock", "EDM", "Beats n Breaks", "Danish"};
    String[] radioURls = {"https://server6.inetcast.nl:2015/stream", "http://cc5.beheerstream.com:8022/stream", "http://fr3.1mix.co.uk:8060/320?type=http&nocache=390123", "http://137.74.160.250:8000/;stream/1", "https://media-ssl.musicradio.com/HeartLondon", "https://media3.mcr.iol.pt/livefm/comercial.mp3/icecast.audio", "https://rs6-krk2-cyfronet.rmfstream.pl/RMFFM48", "https://radio.m-1.fm/m1plius/aacp64", "https://ais-sa2.cdnstream1.com/1976_128.mp3", "https://hdradioclassicrock-rfritschka.radioca.st/stream", "http://fr1.1mix.co.uk:8060/320h", "http://83.137.145.141:14280/;", "https://live-bauerdk.sharp-stream.com/nova_dk_mp3"};

    @Override
    public void execute(MessageEvent event) throws IOException {
        String arg = "null";
        String argFinal = "";
        if (event.getArgs().size() != 1) {
            for (int i = 1; i < event.getArgs().size(); ) {
                argFinal = argFinal + " " + event.getArgs().get(i);
                i++;
            }
            arg = event.getArgs().get(1);
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        eb.appendDescription("\uD83D\uDCFB **Radio list:**\n\n");
        for (int i = 0; i < radioList.length; ) {
            eb.appendDescription("**[" + radioList[i] + "](" + radioURls[i] + ")**\n");
            i++;
        }
        eb.setFooter("Use \"" + botPrefix + "radio <Radio Name>\" to play a radio station.");
        assert arg != null;
        if (arg.equals("list") || event.getArgs().size() == 1) {
            event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
            eb.clear();
            return;
        }
        final AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember()).getVoiceState();
        assert memberState != null;
        if (!memberState.inAudioChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc.")).queue();
            return;
        }
        final VoiceChannel memberChannel = (VoiceChannel) memberState.getChannel();
        argFinal = argFinal.toLowerCase().substring(1);
        System.out.println(argFinal);
        for (int i = 0; i < radioList.length; ) {
            if (radioList[i].toLowerCase().equals(argFinal)) {
                if (IsChannelBlocked(event.getGuild(), event.getTextChannel())) {
                    return;
                }
                audioManager.openAudioConnection(memberChannel);
                PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), radioURls[i], false);
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("Queued Radio station:", "**[" + radioList[i] + "](" + radioURls[i] + ")**")).queue();
                return;
            }
            i++;
        }
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Not a valid radio station, heres a list of the valid radio stations.")).queue();
        event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
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
}
