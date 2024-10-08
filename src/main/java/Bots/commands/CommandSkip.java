package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.CommandStateChecker.Check;
import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.LastFMManager;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;
import static Bots.lavaplayer.LastFMManager.encode;

public class CommandSkip extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_IN_SAME_VC, Check.IS_PLAYING};
    }

    @Override
    public void execute(CommandEvent event) {
        final Member self = event.getGuild().getSelfMember();
        final GuildVoiceState selfVoiceState = Objects.requireNonNull(self.getVoiceState());
        final GuildMusicManager musicManager = PlayerManager.getInstance().getMusicManager(event.getGuild());
        final AudioPlayer audioPlayer = musicManager.audioPlayer;

        skipCountGuilds.putIfAbsent(event.getGuild().getIdLong(), new ArrayList<>()); // required, otherwise there is a nullPointerException

        List<Member> votes = skipCountGuilds.get(event.getGuild().getIdLong());
        if (votes.contains(event.getMember())) {
            event.replyEmbeds(createQuickError(event.getLocaleString("CommandSkip.alreadyVoted")));
            return;
        } else {
            votes.add(event.getMember());
            skipCountGuilds.put(event.getGuild().getIdLong(), votes);
        }

        int effectiveMemberCount = 0;
        int votedMemberCount = 0;
        for (Member member : Objects.requireNonNull(selfVoiceState.getChannel()).getMembers()) {
            if (!member.getUser().isBot()) {
                effectiveMemberCount++;
            }
            if (votes.contains(member)) {
                votedMemberCount++;
            }
        }

        if (votedMemberCount >= effectiveMemberCount / 2) {
            StringBuilder messageBuilder = new StringBuilder();
            if (AutoplayGuilds.contains(event.getGuild().getIdLong())) {
                String searchTerm = LastFMManager.getSimilarSongs(audioPlayer.getPlayingTrack(), event.getGuild().getIdLong());
                boolean canPlay = true;
                if (searchTerm.equals("notfound")) {
                    messageBuilder.append("❌ **" + event.getLocaleString("Main.error") + ":**\n"+ event.getLocaleString("CommandSkip.autoplay.failedToFind") + " ").append(audioPlayer.getPlayingTrack().getInfo().title).append("\n");
                    canPlay = false;
                }
                if (searchTerm.equals("none")) {
                    messageBuilder.append("❌ **" + event.getLocaleString("Main.error") + ":**\n" + event.getLocaleString("CommandSkip.couldNotFind") + "\n");
                    canPlay = false;
                }
                if (searchTerm.isEmpty()) {
                    messageBuilder.append("❌ **" + event.getLocaleString("Main.error") + ":**\n" + event.getLocaleString("CommandSkip.noSearchTerm") + "\n");
                    canPlay = false;
                }
                if (canPlay) {
                    AudioTrack track = audioPlayer.getPlayingTrack();
                    // TODO: should be replaced with actual logic checking if last.fm has either the author or the artist name in the title.
                    String artistName = (track.getInfo().author.isEmpty() || track.getInfo().author == null)
                            ? encode((track.getInfo().title).toLowerCase(), false, true)
                            : encode(track.getInfo().author.toLowerCase(), false, true);
                    String title = encode(track.getInfo().title, true, false);
                    PlayerManager.getInstance().loadAndPlay(event, "ytsearch:" + artistName + " - " + title, false);
                    messageBuilder.append("♾️ " + event.getLocaleString("CommandSkip.autoplayQueued") + " ").append(artistName).append(" - ").append(title).append("\n");
                }
            }
            musicManager.scheduler.nextTrack();
            if (musicManager.audioPlayer.getPlayingTrack() == null) { // if there is nothing playing after the skip command
                event.replyEmbeds(createQuickEmbed(" ", "⏩ " + event.getLocaleString("CommandSkip.skippedTheTrack")));
            } else { // if there is something playing after the skip command
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(botColour);
                if (musicManager.audioPlayer.getPlayingTrack().getInfo().title != null) {
                    eb.setTitle(String.format("⏩ "+event.getLocaleString("CommandSkip.skippedTo"), "__**" + musicManager.audioPlayer.getPlayingTrack().getInfo().title + "**__"), musicManager.audioPlayer.getPlayingTrack().getInfo().uri);
                } else {
                    eb.setTitle("⏩ " + String.format(event.getLocaleString("CommandSkip.skippedTo.unknown"), "__**", "**__"));
                    eb.appendDescription(String.format(event.getLocaleString("CommandSkip.nowPlaying"), musicManager.audioPlayer.getPlayingTrack().getInfo().uri + "\n\n"));
                }
                if (musicManager.audioPlayer.getPlayingTrack().getInfo().author != null) {
                    eb.appendDescription("**" + event.getLocaleString("CommandSkip.channel") + "**\n" + musicManager.audioPlayer.getPlayingTrack().getInfo().author + "\n");
                }
                eb.appendDescription("**" + event.getLocaleString("CommandSkip.duration") + "**\n" + toSimpleTimestamp(musicManager.audioPlayer.getPlayingTrack().getInfo().length));
                eb.appendDescription(messageBuilder);
                event.replyEmbeds(eb.build());
            }
        } else {
            event.replyEmbeds(createQuickEmbed(event.getLocaleString("CommandSkip.voted.title"), String.format(event.getLocaleString("CommandSkip.voted.description"), votedMemberCount, effectiveMemberCount/2)));
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"skip", "s", "voteskip", "vs"};
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String getDescription() {
        return "Casts a vote or skips the current song.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}