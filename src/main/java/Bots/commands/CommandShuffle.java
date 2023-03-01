package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandShuffle extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel(), event.getMember())) {
            return;
        }
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds((createQuickError("Im not in a vc.")));
            return;
        }

        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds((createQuickError("You need to be in a voice channel to use this command.")));
            return;
        }

        if (queue.size() == 0) {
            event.replyEmbeds(createQuickError("There is nothing in the queue."));
            return;
        }

        Collections.shuffle(queue);
        musicManager.scheduler.queue.clear();
        for (AudioTrack audioTrack : queue) {
            musicManager.scheduler.queue(audioTrack.makeClone());
        }
        event.replyEmbeds(createQuickEmbed("✅ **Success**", "Shuffled the queue!"));
    }

    @Override
    public String[] getNames() {
        return new String[]{"shuffle"};
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Shuffles the current queue.";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
