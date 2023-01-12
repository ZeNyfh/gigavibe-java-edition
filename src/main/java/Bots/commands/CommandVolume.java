package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static Bots.Main.*;

public class CommandVolume extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel().asTextChannel(), event.getMember())) {
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        if (!event.getGuild().getAudioManager().isConnected()) {
            event.replyEmbeds(createQuickError("I am not in a vc."));
            return;
        }
        if (event.getGuild().getAudioManager().getConnectedChannel() != Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel()) {
            event.replyEmbeds(createQuickError("You are not in the same vc as me."));
            return;
        }
        String[] args = event.getArgs();
        if (args.length == 1) {
            musicManager.audioPlayer.setVolume(100);
            event.replyEmbeds(createQuickEmbed(" ", "✅ Set volume to the default of **100**."));
        } else {
            if (args[1].matches("[^\\d.]")) {
                event.replyEmbeds(createQuickError("The volume must be an integer."));
                return;
            }
            if (args[1].matches("^\\d+$")) {
                //If entire thing is a number
                int volume = Integer.parseInt(args[1]);
                if (volume > 500) {
                    event.replyEmbeds(createQuickError("The volume can not be higher than 500."));
                    return;
                }
                if (volume < 0) {
                    event.replyEmbeds(createQuickError("The volume can not be lower than 0."));
                    return;
                }
                musicManager.audioPlayer.setVolume(volume);
                if (volume > 201) {
                    event.replyEmbeds(createQuickEmbed(" ", "✅ Changed the volume to **" + volume + "**.\n\nNote: I do not recommend using anything above 200"));
                } else {
                    event.replyEmbeds(createQuickEmbed(" ", "✅ Changed the volume to **" + volume + "**."));
                }
            } else {
                //More specific error if they don't get the point from the [^\\d.] error above
                event.replyEmbeds(createQuickError("Invalid value."));
            }
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"volume", "vol", "v"};
    }

    @Override
    public String getOptions() {
        return "<0 TO 500>";
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
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.INTEGER, "volume", "The volume to set (0-500), 100 by default.", true);
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
