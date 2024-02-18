package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static Bots.Main.*;

public class CommandSpeed extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel(), event.getMember())) {
            return;
        }
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("Im not in a vc."));
            return;
        }

        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("You need to be in a voice channel to use this command."));
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.replyEmbeds(createQuickError("You need to be in the same voice channel to use this command."));
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        TimescalePcmAudioFilter timescale = (TimescalePcmAudioFilter) musicManager.filters.get(audioFilters.Timescale); // this needs to be redefined many times due to how it works.

        if (event.getArgs().length == 1) {
            timescale.setSpeed(1);
            event.replyEmbeds(createQuickEmbed("✅ **Success**", "Set the speed back to 1."));
            return;
        }

        float value = Float.parseFloat(String.format("%.3f %n", Float.parseFloat(event.getArgs()[1])));
        timescale.setSpeed(value);
        event.replyEmbeds(createQuickEmbed("✅ **Success**", "Set the playback speed of the track to " + value + "x."));
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.NUMBER, "tempo", "The speed at which the track will playback, supports decimals.", false);
    }

    @Override
    public String getOptions() {
        return "<Number>";
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String[] getNames() {
        return new String[]{"speed", "tempo"};
    }

    @Override
    public String getDescription() {
        return "Changes the speed/tempo of the song.";
    }

    @Override
    public long getRatelimit() {
        return 2000;
    }
}
