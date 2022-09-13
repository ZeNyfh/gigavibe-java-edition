package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;


public class CommandRemove extends BaseCommand {
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel().asTextChannel(), event.getMember())) {
            return;
        }
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();
        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("You need to be in a voice channel to use this command.")).queue();
            return;
        }
        assert selfVoiceState != null;
        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("You need to be in the same voice channel to use this command.")).queue();
            return;
        }
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        String string = event.getArgs()[1];
        if (string.matches("")) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Invalid arguments, provide an integer.")).queue();
            return;
        }
        if (!string.matches("^\\d+$")) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Invalid arguments, integers only.")).queue();
            return;
        }
        if (queue.isEmpty()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("What are you trying to remove, the queue is empty.")).queue();
            return;
        }
        if (queue.size() < Integer.parseInt(string) - 1) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("The provided number was too large.")).queue();
            return;
        }
        musicManager.scheduler.queue.clear();
        queue.remove(Integer.parseInt(string) - 1);
        for (AudioTrack audioTrack : queue) {
            musicManager.scheduler.queue(audioTrack.makeClone());
        }
        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Skipped queued track **" + (Integer.parseInt(string)) + "** successfully.")).queue(); // not an error, intended
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList<String> list = new ArrayList<>();
        list.add("rem");
        return list;
    }

    @Override
    public String getParams() {
        return "<Number>";
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getName() {
        return "remove";
    }

    @Override
    public String getDescription() {
        return "Removes a specified track from the queue.";
    }

    @Override
    public long getTimeout() {
        return 5000;
    }
}
