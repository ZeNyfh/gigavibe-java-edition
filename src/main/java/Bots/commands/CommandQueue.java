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
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import static Bots.Main.*;
import static java.lang.Math.round;

public class CommandQueue extends BaseCommand {
    private void HandleButtonEvent(ButtonInteractionEvent event) {
        BlockingQueue<AudioTrack> Queue = PlayerManager.getInstance().getMusicManager(Objects.requireNonNull(event.getGuild())).scheduler.queue;
        if (Objects.equals(event.getButton().getId(), "forward")) {
            queuePages.put(event.getGuild().getIdLong(), queuePages.get(event.getGuild().getIdLong()) + 1);
            if (queuePages.get(event.getGuild().getIdLong()) >= round((Queue.size() / 5F) + 1)) {
                queuePages.put(event.getGuild().getIdLong(), 1);
            }
        } else if (Objects.equals(event.getButton().getId(), "backward")) {
            queuePages.put(event.getGuild().getIdLong(), queuePages.get(event.getGuild().getIdLong()) - 1);
            if (queuePages.get(event.getGuild().getIdLong()) <= 0) {
                queuePages.put(event.getGuild().getIdLong(), round((Queue.size() / 5F) + 1));
            }
        }
        long queueTimeLength = 0;
        for (AudioTrack track : Queue) {
            if (track.getInfo().length < 432000000) {
                queueTimeLength = queueTimeLength + track.getInfo().length;
            }
        }
        EmbedBuilder eb = new EmbedBuilder();
        for (int j = 5 * queuePages.get(event.getGuild().getIdLong()) - 5; j < 5 * queuePages.get(event.getGuild().getIdLong()) && j < Queue.size(); j++) {
            AudioTrackInfo trackInfo = Objects.requireNonNull(getTrackFromQueue(event.getGuild(), j)).getInfo();
            eb.appendDescription(j + 1 + ". [" + trackInfo.title + "](" + trackInfo.uri + ")\n");
        }
        eb.setTitle("__**Now playing:**__\n" + PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().title, PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack().getInfo().uri);
        eb.setFooter(Queue.size() + " songs queued. | " + round((Queue.size() / 5F) + 1) + " pages. | Length: " + toTimestamp(queueTimeLength));
        eb.setColor(botColour);
        eb.setThumbnail(PlayerManager.getInstance().getThumbURL(PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack()));
        event.getInteraction().editMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void Init() {
        registerButtonInteraction(new String[]{"forward", "backward"}, this::HandleButtonEvent);
    }

    @Override
    public void execute(MessageEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("Im not in a vc."));
            return;
        }
        EmbedBuilder embed = new EmbedBuilder();
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;
        List<AudioTrack> queue = new ArrayList<>(musicManager.scheduler.queue);
        if (queue.isEmpty()) {
            event.replyEmbeds(createQuickError("The queue is empty."));
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
        if (args.length == 2) {
            if (!args[1].matches("^\\d+$")) {
                event.replyEmbeds(createQuickError("Invalid arguments, integers only\nUsage: `<Integer> <URL/SearchTerm>`"));
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
        embed.setThumbnail(PlayerManager.getInstance().getThumbURL(audioPlayer.getPlayingTrack()));
        event.getChannel().sendMessageEmbeds(embed.build()).queue(
                message -> message.editMessageComponents().setActionRow(Button.secondary("backward", "◀"), Button.secondary("forward", "▶")).queue()
        );
    }

    @Override
    public String[] getNames() {
        return new String[]{"queue", "q"};
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getOptions() {
        return "[Page]";
    }

    @Override
    public String getDescription() {
        return "Shows you the current queue.";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.INTEGER, "page-number", "Page number of the queue");
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
