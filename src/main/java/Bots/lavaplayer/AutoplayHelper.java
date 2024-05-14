package Bots.lavaplayer;

import static Bots.lavaplayer.LastFMManager.encode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import Bots.MessageEvent;

import static Bots.Main.random;
/**
 * Class written to aid during Autoplay functionality refactoring. May be
 * merged, or finalized at a later stage.
 * 
 * @author WolfNT90
 * @since 5/5/24
 * @version 0.2
 * @implNote May change at any point. All removals will be marked with deprecation time before actual removal.
 */
public class AutoplayHelper {
	
	private static final List<Long> guilds = new ArrayList<>(300);
	private static Map<Long, List<String>> tracks = new HashMap<>();

	/** The returned list is <b>read-only!</b>. */
	public static List<Long> getGuilds(){
		return Collections.unmodifiableList(guilds);
	}

	public static boolean add(long guildId) {
		return !includes(guildId) && guilds.add(guildId);
	}

	public static boolean remove(long guildId) {
		return guilds.remove(guildId);
	}
	
	public static boolean includes(long guildId) {
		return guilds.contains(guildId);
	}
	
	public static List<String> getAlreadyListenedTo(long guildId){
		return Collections.unmodifiableList(tracks.getOrDefault(guildId, Collections.emptyList()));
	}
	
	// No instances
	private AutoplayHelper() {}

	
	
	public static void doAutoplay(StringBuilder messageBuilder, AudioPlayer audioPlayer, MessageEvent event) {
		if (messageBuilder == null || audioPlayer == null || event == null)
			return;
        AudioTrack track = audioPlayer.getPlayingTrack();
		
		String trackArtist = track.getInfo().author;
		String trackTitle = track.getInfo().title;
		
		if(trackArtist.endsWith(" - Topic"))
			trackArtist = trackArtist.replace(" - Topic", "");
		
		var lAlreadyPlayed = tracks.getOrDefault(event.getGuild().getIdLong(), new ArrayList<>());

		for (String song : lAlreadyPlayed)
			System.out.printf("[AutoplayHelper] [%o] already listened to %s ...%n",event.getGuild().getIdLong(), song);
		
		// TODO Fix this better
		if(lAlreadyPlayed.size() >= 1)
			trackTitle = lAlreadyPlayed.get(lAlreadyPlayed.size()-1);
		
		
		boolean useEffective = trackTitle.contains("-");
		trackArtist = useEffective ? trackTitle.split("-",2)[0] : trackArtist;
		trackTitle = useEffective ? trackTitle.split("-",2)[1] : trackTitle;
		System.out.println("[AutoplayHelper] ("+(useEffective?"using effective":"non-effective")+") Artist - Track: ".concat(trackArtist).concat(" - ".concat(trackTitle)));

		// Encoding
		String encodedTrackArtist = (track.getInfo().author.isEmpty() || track.getInfo().author == null)
                ? encode((track.getInfo().title).toLowerCase(), false, true)
                : encode(track.getInfo().author.toLowerCase(), false, true);
        String encodedTrackTitle = encode(track.getInfo().title, true, false);
        System.out.println("[AutoplayHelper] encodedTrackArtist ".concat(encodedTrackArtist));
        System.out.println("[AutoplayHelper] encodedTrackTitle ".concat(encodedTrackTitle));
		
		var searchResults = LastFMManager.findSimilarSongs(trackArtist, trackTitle);
		for (String searchResult : searchResults) {
			System.out.println("[AutoplayHelper] [LastFM] Similar: ".concat(searchResult));
		}
		
		switch (searchResults[0]) {
		case "notfound" -> messageBuilder.append("❌ **Error:**\nAutoplay failed to find ").append(audioPlayer.getPlayingTrack().getInfo().title).append("\n");
		case "none" -> messageBuilder.append("❌ **Error:**\nAutoplay could not find similar tracks.\n");
		case "" -> messageBuilder.append("❌ **Error:**\nAn unknown error occurred when trying to autoplay.\n");
		
		default -> autoplay(event, messageBuilder, lAlreadyPlayed, searchResults[random.nextInt(0,searchResults.length)]);
		}
	}



	private static void autoplay(MessageEvent event, StringBuilder messageBuilder, List<String> already, String what) {
		PlayerManager.getInstance().loadAndPlay(event, "ytsearch:".concat(what), false);
        messageBuilder.append("♾️ Autoplay queued: ").append(what).append("\n");
		System.out.printf("[AutoplayHelper] [%o] now listening to %s ...%n",event.getGuild().getIdLong(), what);
		already.add(what);
        tracks.put(event.getGuild().getIdLong(), already);
	}
	
}
