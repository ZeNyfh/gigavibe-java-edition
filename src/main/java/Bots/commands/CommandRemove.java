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
        String string = event.getArgs()[1];
        if (string.matches("")) {
            event.replyEmbeds(createQuickError("Invalid arguments, provide an integer."));
            return;
        }
        if (!string.matches("^\\d+$")) {
            event.replyEmbeds(createQuickError("Invalid arguments, integers only."));
            return;
        }
        if (queue.isEmpty()) {
            event.replyEmbeds(createQuickError("What are you trying to remove, the queue is empty."));
            return;
        }
        if (queue.size() < Integer.parseInt(string) - 1) {
            event.replyEmbeds(createQuickError("The provided number was too large."));
            return;
        }
        musicManager.scheduler.queue.clear();
        queue.remove(Integer.parseInt(string) - 1);
        for (AudioTrack audioTrack : queue) {
            musicManager.scheduler.queue(audioTrack.makeClone());
        }
        event.replyEmbeds(createQuickEmbed(" ", "✅ Skipped queued track **" + (Integer.parseInt(string)) + "** successfully.")); // not an error, intended
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.INTEGER, "position", "The track to remove from the position in the queue.", true);
    }

    @Override
    public String getCategory() {
        return "DJ";
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
