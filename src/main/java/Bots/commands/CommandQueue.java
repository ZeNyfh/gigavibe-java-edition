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
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Im not in a vc.")).queue();
            return;
        }
        EmbedBuilder embed = new EmbedBuilder();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        if (queue.isEmpty()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "The queue is empty.")).queue();
            embed.clear();
            return;
        }
        try {
            if (audioPlayer.getPlayingTrack().getInfo().uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
                embed.setTitle("__**Now playing:**__\n" + audioPlayer.getPlayingTrack().getInfo().uri.replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13));
            } else {
                embed.setTitle("__**Now playing:**__\n" + audioPlayer.getPlayingTrack().getInfo().title, audioPlayer.getPlayingTrack().getInfo().uri);
            }
        } catch (Exception ignored) {
        }
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
        String string = event.getMessage().getContentRaw();
        int multiplier = 1;
        String[] args = string.split(" ", 2);
        if (args.length >= 2) {
            string = args[1];
            multiplier = Integer.parseInt(string.replaceAll("[^0-9]", ""));
        }
        for (int i = 5 * multiplier - 5; i < 5 * multiplier && i < queueLength; ) {
            AudioTrackInfo trackInfo = queue.get(i).getInfo();
            if (trackInfo.uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
                embed.appendDescription(i + 1 + ". " + (trackInfo.uri).replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13) + "\n");
            } else {
                embed.appendDescription(i + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
            }
            i++;
        }
        embed.setFooter(queueLength + " songs queued. | " + round((queueLength / 5) + 1) + " pages. | Length: " + toTimestamp(queueTimeLength));
        embed.setColor(botColour);
        embed.setThumbnail("https://img.youtube.com/vi/" + audioPlayer.getPlayingTrack().getIdentifier() + "/0.jpg");
        channel.sendMessageEmbeds(embed.build()).queue(a -> a.editMessageComponents().setActionRow(Button.secondary("queueBack", "◄️"), Button.secondary("queueForward", "►️")).queue());

    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
        list.add("q");
        return list;
    }

    @Override
    public String getName() {
        return "queue";
    }

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
        return "[Page]";
    }
}
