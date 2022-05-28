package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandSeek extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getTextChannel(), event.getMember())){
            return;
        }

        long position = 0;
        final TextChannel channel = event.getTextChannel();
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **error**", "Im not in a vc.")).queue();
            return;
        }

        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in a voice channel to use this command.")).queue();
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in the same voice channel to use this command.")).queue();
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing right now.")).queue();
            return;
        }
        List<String> args = event.getArgs();
        if (args.size() > 1) {
            if (audioPlayer.getPlayingTrack().isSeekable()) {
                String[] times = args.get(1).split(":", 3);
                for (int i = 0; i < times.length; ) {
                    if (times[i].matches("[^0-9]")) {
                        channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Argument is invalid, use the format `[HOURS]:[MINUTES]:<SECONDS>`")).queue();
                        return;
                    }
                    i++;
                }
                if (times.length == 3) {
                    position = (Long.parseLong(times[0]) * 60 * 60) + (Long.parseLong(times[1]) * 60) + (Long.parseLong(times[2]));
                }
                if (times.length == 2) {
                    position = (Long.parseLong(times[0]) * 60) + (Long.parseLong(times[1]));
                }
                if (times.length == 1) {
                    position = Long.parseLong(times[0]);
                }
                position = position * 1000;
                if (position <= 0) {
                    channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Argument is lower than or equal to 0.")).queue();
                    return;
                }
                audioPlayer.getPlayingTrack().setPosition(position);
                channel.sendMessageEmbeds(createQuickEmbed(" ", "✅ Set the position of the track to: **" + toSimpleTimestamp(position) + ".**")).queue();

            } else {
                channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You cannot seek with this track.")).queue();
            }
        } else {
            channel.sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No argument given.")).queue();
        }
    }

    @Override
    public String getParams() {
        return "[hours]:[minutes]:<seconds>";
    }

    @Override

    public String getCategory() {
        return "DJ";
    }

    @Override

    public String getName() {
        return "seek";
    }

    @Override

    public String getDescription() {
        return "Seeks to a certain position in the track.";
    }
}
