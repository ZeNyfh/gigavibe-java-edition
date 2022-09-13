package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandShuffle extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        if (!IsDJ(event.getGuild(), event.getChannel().asTextChannel(), event.getMember())) {
            return;
        }
        final TextChannel channel = event.getChannel().asTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickError("Im not in a vc.")).queue();
            return;
        }

        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickError("You need to be in a voice channel to use this command.")).queue();
            return;
        }

        if (queue.size() == 0) {
            channel.sendMessageEmbeds(createQuickError("There is nothing in the queue.")).queue();
            return;
        }

        Collections.shuffle(queue);
        musicManager.scheduler.queue.clear();
        for (AudioTrack audioTrack : queue) {
            musicManager.scheduler.queue(audioTrack.makeClone());
        }
        channel.sendMessageEmbeds(createQuickEmbed(" ", "✅ Shuffled the queue successfully!")).queue();
    }

    @Override
    public String getName() {
        return "shuffle";
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getDescription() {
        return "Shuffles the current queue.";
    }

    @Override
    public long getTimeout() {
        return 5000;
    }
}
