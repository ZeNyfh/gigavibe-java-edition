package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

import java.io.IOException;
import java.util.Objects;

import static Bots.Main.*;

public class CommandPitch implements BaseCommand {
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
            musicManager.parameters.setPitch(1);
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Set pitch to the default of **1**.")).queue();
        } else {
            if (args[1].matches("^\\d*(?:\\.\\d+)?$")) {
                //If entire thing is a valid double or
                double pitch = Double.parseDouble(args[1]);
                if (pitch > 5) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The pitch can not be higher than 5.")).queue();
                    return;
                }
                if (pitch < 0) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The pitch can not be lower than 0.")).queue();
                    return;
                }
                musicManager.parameters.setPitch(pitch);
                musicManager.applyFilters();
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Changed the pitch to **" + pitch + "**.")).queue();
            } else {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The pitch must be a valid integer or decimal.")).queue();
            }
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"pitch"};
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getDescription() {
        return "Changes the pitch of the current track (1 default)";
    }

    @Override
    public OptionData[] getOptions() {
        return new OptionData[]{
                new OptionData(OptionType.NUMBER, "pitch", "The pitch to set (0-5)", true)
        };
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}