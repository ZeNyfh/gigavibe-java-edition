package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.Objects;

import static Bots.Main.*;

public class CommandSeek extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel(), event.getMember())) {
            return;
        }
        long position = 0;
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("Im not in a vc."));
            return;
        }

        final GuildVoiceState memberVoiceState = Objects.requireNonNull(event.getMember()).getVoiceState();

        assert memberVoiceState != null;
        if (!memberVoiceState.inAudioChannel()) {
            event.replyEmbeds(createQuickError("You need to be in a voice channel to use this command."));
            return;
        }

        if (!Objects.equals(memberVoiceState.getChannel(), selfVoiceState.getChannel())) {
            event.replyEmbeds(createQuickError("You need to be in the same voice channel to use this command."));
            return;
        }

        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        if (audioPlayer.getPlayingTrack() == null) {
            event.replyEmbeds(createQuickError("No tracks are playing right now."));
            return;
        }
        String[] args = event.getArgs();
        if (args.length > 1) {
            if (audioPlayer.getPlayingTrack().isSeekable()) {
                String[] times = args[1].split(":", 3);
                for (int i = 0; i < times.length; ) {
                    if (!times[i].matches("^\\d+$")) {
                        event.replyEmbeds(createQuickError("Argument is invalid, use the format `[HOURS]:[MINUTES]:<SECONDS>`"));
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
                    event.replyEmbeds(createQuickError("Argument is lower than or equal to 0."));
                    return;
                }
                audioPlayer.getPlayingTrack().setPosition(position);
                event.replyEmbeds(createQuickEmbed(" ", "✅ Set the position of the track to: **" + toSimpleTimestamp(position) + ".**"));

            } else {
                event.replyEmbeds(createQuickError("You cannot seek with this track."));
            }
        } else {
            event.replyEmbeds(createQuickError("No argument given."));
        }
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "timestamp", "Timestamp to seek to, E.g: 1:54 | 12 | 1:12:34.", true);
    }

    @Override
    public String getCategory() {
        return Categories.DJ.name();
    }


    @Override
    public String getOptions() {
        return "[HH:][MM:]<SS>";
    }

    @Override
    public String[] getNames() {
        return new String[]{"seek"};
    }

    @Override
    public String getDescription() {
        return "Seeks to a certain position in the track.";
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}
