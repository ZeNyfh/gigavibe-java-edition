package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;

import static Bots.Main.*;
import static java.lang.Math.round;

public class CommandQueue extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
        final TextChannel channel = event.getChannel().asTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickError("Im not in a vc.")).queue();
            return;
        }
        EmbedBuilder embed = new EmbedBuilder();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        if (queue.isEmpty()) {
            channel.sendMessageEmbeds(createQuickError("The queue is empty.")).queue();
            embed.clear();
            return;
        }
        embed.setTitle("__**Now playing:**__\n" + audioPlayer.getPlayingTrack().getInfo().title, audioPlayer.getPlayingTrack().getInfo().uri);
        int queueLength = queue.size();
        long queueTimeLength = 0;
        for (int x = 0; x < queueLength; ) {
            if (queue.get(x).getInfo().length > 432000000) {
                x++;
                continue; // will be slightly inaccurate due to tracks with unknown duration
            }
            queueTimeLength = queueTimeLength + queue.get(x).getInfo().length;
            x++;
        }
        queuePages.put(event.getGuild().getIdLong(), 1);
        String[] args = event.getArgs();
        if (event.getArgs().length == 2) {
            if (!args[1].matches("^\\d+$")) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Invalid arguments, integers only\nUsage: `<Integer> <URL/SearchTerm>`")).queue();
                return;
            }
            queuePages.put(event.getGuild().getIdLong(), Integer.parseInt(args[1]));
        }
        int multiplier = queuePages.get(event.getGuild().getIdLong());
        for (int i = 5 * multiplier - 5; i < 5 * multiplier && i < queueLength; ) {
            AudioTrackInfo trackInfo = queue.get(i).getInfo();
            embed.appendDescription(i + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
            i++;
        }
        embed.setFooter(queueLength + " songs queued. | " + round((queueLength / 5F) + 1) + " pages. | Length: " + toTimestamp(queueTimeLength));
        embed.setColor(botColour);
        embed.setThumbnail("https://img.youtube.com/vi/" + audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
        channel.sendMessageEmbeds(embed.build()).queue(message -> message.editMessageComponents().setActionRow(Button.secondary("backward", "◀"), Button.secondary("forward", "▶")).queue());
    }

    @Override
    public String[] getNames() {
        return new String[]{"queue","q"};}

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "Shows you the current queue.";
    }

    @Override
    public String getParams() {
        return "<Page>";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }
}
