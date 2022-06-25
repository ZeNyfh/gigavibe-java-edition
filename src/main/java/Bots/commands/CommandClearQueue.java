package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

import static Bots.Main.IsDJ;
import static Bots.Main.createQuickEmbed;

public class CommandClearQueue extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        if (!IsDJ(event.getGuild(), event.getTextChannel(), event.getMember())) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You are not dj.")).queue();
            return;
        }
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Im not in a vc.")).queue();
            return;
        }

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in a voice channel to use this command.")).queue();
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in the same voice channel to use this command.")).queue();
            return;
        }

        musicManager.scheduler.queue.clear();
        musicManager.scheduler.nextTrack();
        musicManager.audioPlayer.destroy();
        channel.sendMessageEmbeds(createQuickEmbed(" ", "✅ Cleared the queue successfully!")).queue(); // not an error, intended
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
        list.add("queue clear");
        list.add("clearqueue");
        list.add("queueclear");
        list.add("clearq");
        list.add("qclear");
        return list;
    }

    @Override
    public String getName() {
        return "clear queue";
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getDescription() {
        return "Clears the current queue.";
    }
}
