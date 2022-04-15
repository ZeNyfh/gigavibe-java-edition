package Bots.commands;

import Bots.BaseCommand;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.getTrackFromQueue;

public class CommandQueue implements BaseCommand {
    @Override
    public void execute(MessageReceivedEvent event) {
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
        if (audioPlayer.getPlayingTrack().getInfo().uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
            embed.setTitle("__**Now playing:**__\n" + audioPlayer.getPlayingTrack().getInfo().uri.replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13));
        } else {
            embed.setTitle("__**Now playing:**__\n" + audioPlayer.getPlayingTrack().getInfo().title, audioPlayer.getPlayingTrack().getInfo().uri);
        }
        if (queue.isEmpty()){
            channel.sendMessageEmbeds(createQuickEmbed(" ", "❌ The queue is empty.")).queue();
            embed.clear();
            return;
        }
        int queueLength = queue.size();
        for (int i = 0; i < 5 && i < queueLength;) {
            AudioTrackInfo trackInfo = queue.get(i).getInfo();
            if (trackInfo.uri.contains(System.getProperty("user.dir") + "\\temp\\music\\")) {
                embed.appendDescription(i + 1 + ". " + (trackInfo.uri).replace(System.getProperty("user.dir") + "\\temp\\music\\", "").substring(13) + "\n");
            } else {
                embed.appendDescription(i + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
            }
            i++;
        }
        embed.setColor(new Color(0, 0, 255));
        embed.build();
        channel.sendMessageEmbeds(embed.build()).queue();
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
}
