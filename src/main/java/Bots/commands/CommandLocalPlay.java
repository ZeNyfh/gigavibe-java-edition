package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.IsChannelBlocked;
import static Bots.Main.createQuickEmbed;

public class CommandLocalPlay extends BaseCommand {
    public void execute(MessageEvent event) {
        if (IsChannelBlocked(event.getGuild(), event.getTextChannel())){
            return;
        }

        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inAudioChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "you arent in a vc cunt")).queue();
            return;
        }

        if (!Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).inAudioChannel()) {
            final AudioManager audioManager = event.getGuild().getAudioManager();
            final VoiceChannel memberChannel = (VoiceChannel) event.getMember().getVoiceState().getChannel();

            audioManager.openAudioConnection(memberChannel);
        }
        String string = event.getMessage().getContentRaw();
        String[] args = string.split(" ", 2);
        Path finalPath = Paths.get(args[1]);
        PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), String.valueOf(finalPath), true);
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
        list.add("pf");
        return list;
    }

    public String getCategory() {
        return "dev";
    }

    public String getName() {
        return "playfile";
    }

    public String getDescription() {
        return "Plays songs from a directory.";
    }

    public String getParams() {
        return "<Path>";
    }
}