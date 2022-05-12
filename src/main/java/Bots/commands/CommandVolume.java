package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;
import static java.lang.Math.round;

public class CommandVolume extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "I am not in a vc.")).queue();
            return;
        }
        if (event.getGuild().getAudioManager().getConnectedChannel() != Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You are not in the same vc as me.")).queue();
            return;
        }
        String string = event.getMessage().getContentRaw();
        int volume = 0;
        if (string.contains(" ")) {
            String[] args = string.split(" ", 2);
            string = args[1];
            string = string.replaceAll("[^0-9]", "");
            volume = Integer.parseInt(string);
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Defaulted back to 100 volume.")).queue();
            musicManager.audioPlayer.setVolume(100);
            return;
        }
        if (volume > 200) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Volume too high.")).queue();
            return;
        }
        if (volume < 0) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Volume too low.")).queue();
            return;
        }
        musicManager.audioPlayer.setVolume(round(volume));
        event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Changed the volume to: **" + volume + ".**")).queue();

    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
        list.add("vol");
        list.add("v");
        return list;
    }

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getDescription() {
        return "Changes the volume of the current track (100 default)";
    }

    @Override
    public String getParams() {
        return "<0-200>";
    }
}
