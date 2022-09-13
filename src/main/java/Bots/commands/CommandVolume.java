package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.*;

public class CommandVolume extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        if (!IsDJ(event.getGuild(), event.getChannel().asTextChannel(), event.getMember())) {
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("I am not in a vc.")).queue();
            return;
        }
        if (event.getGuild().getAudioManager().getConnectedChannel() != Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("You are not in the same vc as me.")).queue();
            return;
        }
        String[] args = event.getArgs();
        if (args.length == 1) {
            musicManager.audioPlayer.setVolume(100);
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Set volume to the default of **100**.")).queue();
        } else {
            if (args[1].matches("[^\\d.]")) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The volume must be an integer.")).queue();
                return;
            }
            if (args[1].matches("^\\d+$")) {
                //If entire thing is a number
                int volume = Integer.parseInt(args[1]);
                if (volume > 500) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The volume can not be higher than 500.")).queue();
                    return;
                }
                if (volume < 0) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The volume can not be lower than 0.")).queue();
                    return;
                }
                musicManager.audioPlayer.setVolume(volume);
                if (volume > 201) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Changed the volume to **" + volume + "**.\n\nNote: I do not recommend using anything above 200")).queue();
                } else {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Changed the volume to **" + volume + "**.")).queue();
                }
            } else {
                //More specific error if they don't get the point from the [^\\d.] error above
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Invalid value.")).queue();
            }
        }
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
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
        return "<0-500>";
    }

    @Override
    public long getTimeout() {
        return 5000;
    }
}
