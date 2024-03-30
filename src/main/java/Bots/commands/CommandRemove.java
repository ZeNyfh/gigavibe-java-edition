package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandRemove extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel(), event.getMember())) {
            return;
        }
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();
        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("You need to be in a voice channel to use this command."));
            return;
        }
        assert selfVoiceState != null;
        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.replyEmbeds(createQuickError("You need to be in the same voice channel to use this command."));
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        if (queue.isEmpty()) {
            event.replyEmbeds(createQuickError("There are no songs in the queue to remove."));
            return;
        }
        if (event.getArgs().length == 1 || !event.getArgs()[1].matches("^\\d+$")) {
            event.replyEmbeds(createQuickError("Invalid arguments, integers only."));
            return;
        }
        int position = Integer.parseInt(event.getArgs()[1]);
        if (queue.size() < position - 1) {
            event.replyEmbeds(createQuickError("The provided number was larger than the size of the queue."));
            return;
        }
        musicManager.scheduler.queue.clear();
        queue.remove(position - 1);
        for (AudioTrack audioTrack : queue) {
            musicManager.scheduler.queue(audioTrack.makeClone());
        }
        event.replyEmbeds(createQuickEmbed(" ", "✅ Skipped queued track **" + position + "** successfully."));
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.INTEGER, "position", "The track to remove from the position in the queue.", true);
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String getOptions() {
        return "<Queue_Position>";
    }

    @Override
    public String[] getNames() {
        return new String[]{"remove", "rem"};
    }

    @Override
    public String getDescription() {
        return "Removes a specified track from the queue.";
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}
