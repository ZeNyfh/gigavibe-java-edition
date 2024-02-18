package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
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

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        VibratoPcmAudioFilter vibrato = (VibratoPcmAudioFilter) musicManager.filters.get(audioFilters.Vibrato); // this needs to be redefined many times due to how it works.

        if (event.getArgs().length == 1) {
            vibrato.setFrequency(2);
            vibrato.setDepth(0.5f);
            event.replyEmbeds(createQuickEmbed("✅ **Success**", "Set the parameters to their default values."));
            return;
        }

        if (event.getArgs().length != 3) {
            event.replyEmbeds(createQuickEmbed("❌ **Invalid arguments.**", "The valid usage is: `vibrato <Frequency> <Depth>`"));
            return;
        }

        float value = Float.parseFloat(String.format("%.3f %n", Float.parseFloat(event.getArgs()[1])));
        float power = Float.parseFloat(String.format("%.3f %n", Float.parseFloat(event.getArgs()[2])));
        vibrato.setFrequency(value);
        vibrato.setDepth(power);
        event.replyEmbeds(createQuickEmbed("✅ **Success**", "Set the vibrato frequency to " + value + "Hz.\nSet the vibrato depth to " + power + ".\n\n*0.5 is the default depth value.*"));
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.NUMBER, "frequency", "The frequency at which the track \"bounces\", supports decimals.", false);
        slashCommand.addOption(OptionType.NUMBER, "depth", "How powerful the \"bounce\" will be, supports decimals.", false);
    }

    @Override
    public String getOptions() {
        return "[frequency] [depth]";
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
