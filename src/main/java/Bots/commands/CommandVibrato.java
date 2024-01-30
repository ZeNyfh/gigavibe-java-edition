package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import com.github.natanbc.lavadsp.vibrato.VibratoPcmAudioFilter;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static Bots.Main.*;

public class CommandVibrato extends BaseCommand {

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

        if (event.getArgs().length == 1) {
            event.replyEmbeds(createQuickEmbed("✅ **Success**", "Set the parameters to their default values."));
            VibratoPcmAudioFilter vibrato = (VibratoPcmAudioFilter) guildFilters.get(event.getGuild().getIdLong()).get(filters.Vibrato); // this needs to be redefined many times due to how it works.
            vibrato.setFrequency(1);
            vibrato.setDepth(1);
            vibrato.flush();
            return;
        }

        if (event.getArgs().length != 3) {
            event.replyEmbeds(createQuickEmbed("❌ **Invalid arguments.**", "The valid usage is: `vibrato <Frequency> <Depth>`"));
            return;
        }

        float value = Float.parseFloat(String.format("%.3f %n", Float.parseFloat(event.getArgs()[1])));
        float power = Float.parseFloat(String.format("%.3f %n", Float.parseFloat(event.getArgs()[1])));
        event.replyEmbeds(createQuickEmbed("✅ **Success**", "Set the vibrato frequency to " + value + "Hz.\nSet the vibrato depth to " + power + ".\n\n*1 is the default value.*"));
        VibratoPcmAudioFilter vibrato = (VibratoPcmAudioFilter) guildFilters.get(event.getGuild().getIdLong()).get(filters.Vibrato);
        vibrato.setFrequency(value);
        vibrato.setDepth(power);
        vibrato.flush();
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.NUMBER, "Frequency", "The frequency at which the track \"bounces\", supports decimals.", false);
        slashCommand.addOption(OptionType.NUMBER, "Depth", "How powerful the \"bounce\" will be, supports decimals.", false);
    }

    @Override
    public String getOptions() {
        return "[Number] [Number]";
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String[] getNames() {
        return new String[]{"vibrato", "vib"};
    }

    @Override
    public String getDescription() {
        return "Changes the vibrato of the audio. If no arguments are provided, resets the parameters.";
    }

    @Override
    public long getRatelimit() {
        return 2000;
    }
}
