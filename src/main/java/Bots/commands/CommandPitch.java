package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import com.github.natanbc.lavadsp.timescale.TimescalePcmAudioFilter;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static Bots.Main.*;

public class CommandPitch extends BaseCommand {

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
            event.replyEmbeds(createQuickEmbed("✅ **Success**", "Set the pitch back to 1."));
            TimescalePcmAudioFilter timescale = (TimescalePcmAudioFilter) guildFilters.get(event.getGuild().getIdLong()).get(filters.Timescale); // this needs to be redefined many times due to how it works.
            timescale.setPitch(1);
            timescale.flush();
            return;
        }

        float value = Float.parseFloat(String.format("%.3f %n", Float.parseFloat(event.getArgs()[1])));
        event.replyEmbeds(createQuickEmbed("✅ **Success**", "Set the pitch of the track to " + value + "x."));
        TimescalePcmAudioFilter timescale = (TimescalePcmAudioFilter) guildFilters.get(event.getGuild().getIdLong()).get(filters.Timescale);
        timescale.setPitch(value);
        timescale.flush();
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.NUMBER, "pitch", "The pitch of the song, supports decimals.", false);
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
        return new String[]{"pitch"};
    }

    @Override
    public String getDescription() {
        return "Changes the pitch of the song.";
    }

    @Override
    public long getRatelimit() {
        return 2000;
    }
}
