package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.CommandEvent;
import Bots.lavaplayer.LastFMManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.List;

import static Bots.Main.*;
import static Bots.lavaplayer.LastFMManager.encode;

public class CommandAutoplay extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_IN_SAME_VC, Check.IS_PLAYING};
    }

    @Override
    public void execute(CommandEvent event) {
        if (!LastFMManager.hasAPI) {
            event.replyEmbeds(createQuickError("The bot has not been given an API key for LastFM, this command does not work without it."));
            return;
        }
        if (AutoplayGuilds.contains(event.getGuild().getIdLong())) {
            event.replyEmbeds(createQuickEmbed("❌ ♾\uFE0F", "No longer autoplaying."));
            AutoplayGuilds.remove(event.getGuild().getIdLong());
        } else {
            event.replyEmbeds(createQuickEmbed("✅ ♾\uFE0F", "Now autoplaying."));
            AutoplayGuilds.add(event.getGuild().getIdLong());
            AudioTrack track = PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack();
            if (track != null) {
                List<String> list = autoPlayedTracks.get(event.getGuild().getIdLong());
                // TODO: should be replaced with actual logic checking if last.fm has either the author or the artist name in the title.
                String artistName = (track.getInfo().author.isEmpty() || track.getInfo().author == null)
                        ? encode((track.getInfo().title).toLowerCase(), false, true)
                        : encode(track.getInfo().author.toLowerCase(), false, true);
                list.add(artistName + " - " + encode(track.getInfo().title, true, false));
                autoPlayedTracks.put(event.getGuild().getIdLong(), list);
            }
        }

    }

    @Override
    public String[] getNames() {
        return new String[]{"autoplay", "ap"};
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String getDescription() {
        return "Toggles autoplay.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}