package Bots.lavaplayer;

import static Bots.lavaplayer.LastFMManager.encode;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import Bots.MessageEvent;

/**
 * Class written to aid during Autoplay functionality refactoring. May be
 * merged, or finalized at a later stage.
 * 
 * @author WolfNT90
 * @since 5/5/24
 * @version 0.1
 * @implNote May change at any point. All removals will be marked with deprecation time before actual removal.
 */
public class AutoplayHelper {
	
	// No instances
	private AutoplayHelper() {}

	
	
	public static void doAutoplay(StringBuilder messageBuilder, AudioPlayer audioPlayer, MessageEvent event) {
		if (messageBuilder == null || audioPlayer == null || event == null)
			return;
        AudioTrack track = audioPlayer.getPlayingTrack();
		
		String trackArtist = track.getInfo().author;
		String trackTitle = track.getInfo().title;
		
		boolean useEffective = trackTitle.contains(" - ");
		trackArtist = useEffective ? trackTitle.split(" - ",2)[0] : trackArtist;
		trackTitle = useEffective ? trackTitle.split(" - ",2)[1] : trackTitle;
		System.out.println("[Debug] [pr-autoplay] ("+(useEffective?"using effective":"non-effective")+") Artist - Track: ".concat(trackArtist).concat(" - ".concat(trackTitle)));

		// Encoding
		String encodedTrackArtist = (track.getInfo().author.isEmpty() || track.getInfo().author == null)
                ? encode((track.getInfo().title).toLowerCase(), false, true)
                : encode(track.getInfo().author.toLowerCase(), false, true);
        String encodedTrackTitle = encode(track.getInfo().title, true, false);
        System.out.println("[Debug] [pr-autoplay] encodedTrackArtist ".concat(encodedTrackArtist));
        System.out.println("[Debug] [pr-autoplay] encodedTrackTitle ".concat(encodedTrackTitle));
		
		String searchTerm = LastFMManager.getSimilarSongs(track, event.getGuild().getIdLong());
		System.out.println("[Debug] [pr-autoplay] [LastFM] Similar: ".concat(searchTerm));
		
		switch (searchTerm) {
		case "notfound" -> messageBuilder.append("❌ **Error:**\nAutoplay failed to find ").append(audioPlayer.getPlayingTrack().getInfo().title).append("\n");
		case "none" -> messageBuilder.append("❌ **Error:**\nAutoplay could not find similar tracks.\n");
		case "" -> messageBuilder.append("❌ **Error:**\nAn unknown error occurred when trying to autoplay.\n");
		
		default -> autoplay(event, messageBuilder, searchTerm);
//		throw new IllegalArgumentException("Unexpected value: " + searchTerm);
		}
	}



	private static void autoplay(MessageEvent event, StringBuilder messageBuilder, String what) {
		PlayerManager.getInstance().loadAndPlay(event, "ytsearch:".concat(what), false);
        messageBuilder.append("♾️ Autoplay queued: ").append(what).append("\n");
	}
	
}
