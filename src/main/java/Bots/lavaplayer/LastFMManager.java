package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.annotations.DeprecatedSince;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

import static Bots.Main.autoPlayedTracks;
import static Bots.Main.botVersion;

/**
 * @author ZeNyfh
 * @implNote Last.fm wish for their API to be used sensibly, I have outlined
 *           with comments how it is being used sensibly with attention to their
 *           note found at: https://www.last.fm/api/intro
 * @see https://www.last.fm/api/intro
 * 
 * @author WolfNT90 (pr-autoplay)
 */
public class LastFMManager {
	private static String APIKEY = null;
	
	@Deprecated(forRemoval = true)
	@DeprecatedSince(value = "pr-autoplay")
	/**
	 * @implNote Do NOT use this - starting with pr-autoplay, use
	 *           {@link LastFMManager#isInitialized()} instead! This boolean will be
	 *           removed soon and will ALWAYS return false until its removal!
	 */
	public static final boolean hasAPI = false;
	private static Pattern undesirableWordsInQuery;
	
	static {
		var env = Dotenv.load();
		LastFMManager.APIKEY = env.get("LASTFMTOKEN", "");

		if (LastFMManager.APIKEY.isBlank()) {
			System.out.printf("[LastFMManager] LASTFMTOKEN is not set in .env file. (%s). Most-all functions from this class won't work without it!",new File(".env").getAbsolutePath());
		} else {
			var evarFilterpattern = env.get("LASTFM_FILTERPATTERN", "(m/v|vevo|\\((?!(?:.*ft\\.|.*feat\\.|.*Remix)).*?\\)|live|music video|hd|explicit|lyric.|\\\\[(.*?)\\\\])");
			var evarFilterCaseSensitive = env.get("LASTFM_FILTERPATTERN_CASE_SENSITIVE", "false").equals("true");
			
			undesirableWordsInQuery = Pattern.compile(evarFilterpattern, evarFilterCaseSensitive ? Pattern.CASE_INSENSITIVE : 0);
	
			System.out.printf("LastFM manager initialised with filterpattern: /%s/ (sensitive? %b)", evarFilterpattern, evarFilterCaseSensitive);
		}
	}

	/** Marked for removal. LastFMManager instantiates statically */
	@Deprecated(forRemoval = true)
	@DeprecatedSince(value = "pr-autoplay")
	public static void Init() {
	}

	/**
	 * @return <b>true</b> if the {@link LastFMManager} has successfully set
	 *         <i>LASTFMTOKEN</i> from the .env file.
	 * @see {@link Dotenv}
	 */
	public static boolean isInitialized() {
		return !APIKEY.isBlank();
	}

	
	/** Marked for removal. Superceeded by {@link LastFMManager#findSimilarSongs(String, String)} */
	@Deprecated(forRemoval = true)
	@DeprecatedSince(value = "pr-autoplay")
	public static String getSimilarSongs(AudioTrack track, Long guildID) {
        if (APIKEY == null) {
            return "noapi";
        }

        String songName;
        if (track.getInfo().title.contains("-")) {
            songName = encode(track.getInfo().title.toLowerCase(), true, true);
        } else {
            songName = encode(track.getInfo().title.toLowerCase(), true, false);
        }
        // TODO: should be replaced with actual logic checking if last.fm has either the author or the artist name in the title.
        String artistName = (track.getInfo().author.isEmpty() || track.getInfo().author == null || track.getInfo().title.contains("-"))
                ? encode((track.getInfo().title).toLowerCase(), false, true)
                : encode((track.getInfo().author).toLowerCase(), false, true);


        StringBuilder urlStringBuilder = new StringBuilder();
        urlStringBuilder.append("http://ws.audioscrobbler.com/2.0/?method=track.getSimilar&limit=5&autocorrect=1&artist=").append(artistName).append("&track=").append(songName);
        System.out.println(urlStringBuilder); // debug printing but removing the API key from the print.
        urlStringBuilder.append("&api_key=").append(APIKEY).append("&format=json");
        String urlString = urlStringBuilder.toString();

        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Zenvibe/" + botVersion); // identifiable User-Agent header as requested by last.fm

            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }

        if (response.toString().startsWith("{\"error\":6,\"message\":\"Track not found\"") || response.toString().startsWith("{}")) {
            return "notfound";
        }

        String trackToSearch = extractTracks(response.toString(), guildID);
        return trackToSearch.isEmpty() ? "none" : trackToSearch;
    }


	/**
	 * Marked for removal. Superceeded by {@link LastFMManager#asEncoded(String)}
	 * and {@link LastFMManager#filter(String)}
	 */
	@Deprecated(forRemoval = true)
	@DeprecatedSince(value = "pr-autoplay")
    public static String encode(String str, boolean isTitle, boolean shouldCheck) {
        str = URLEncoder.encode(str, StandardCharsets.UTF_8).toLowerCase();
        if (str.contains("%28")) str = str.split("%28")[0];
        if (str.contains("%5b")) str = str.split("%5b")[0];
        if (str.contains("ft.")) str = str.split("ft\\.")[0];
        if (str.contains("lyric")) str = str.split("lyric", 2)[0];
        if (str.contains("official")) str = str.split("official", 2)[0];
        str = !str.startsWith("vevo") ? str.split("vevo", 2)[0] : str.replaceAll("vevo", "").trim();
        if (str.contains("+")) str = str.replaceAll("\\+", " ").trim();
        if (shouldCheck && str.contains("-")) {
            String[] split = str.split("-");
            return isTitle
                    ? split[1].replaceAll("\\+", " ").trim().replaceAll(" ", "+")
                    : split[0].replaceAll("\\+", " ").trim().replaceAll(" ", "+");
        } else {
            return str.replaceAll("\\+", " ").replaceAll(" ", "+");
        }
    }

	/** Marked for removal. Superceeded by {@link LastFMManager#performQuery(QueryType, Object...)} and {@link LastFMManager#parseJsonResponse(QueryType, String)} */
	@Deprecated(forRemoval = true)
	@DeprecatedSince(value = "pr-autoplay")
    private static String extractTracks(String rawJson, long guildID) {
        Object object = JSONValue.parse(rawJson);
        JSONArray trackInfoArray = (JSONArray) ((JSONObject) (((JSONObject) object).get("similartracks"))).get("track");
        StringBuilder builder = new StringBuilder();

        for (Object obj : trackInfoArray) {
            builder.append(((JSONObject) ((JSONObject) obj).get("artist")).get("name")).append(" - ");
            builder.append(((JSONObject) obj).get("name"));
            if (autoPlayedTracks.get(guildID).contains(builder.toString())) {
                builder.setLength(0);
            } else {
                List<String> list = autoPlayedTracks.get(guildID);
                list.add(builder.toString().toLowerCase());
                autoPlayedTracks.put(guildID, list);
                break;
            }
        }
        return builder.toString();
    }

	static enum QueryType {
		TRACK_SIMILAR
	}
	
	public static String asEncoded(String string) {
	    try {
	        return URLEncoder.encode(string, StandardCharsets.UTF_8.toString());
	    } catch (UnsupportedEncodingException e) {
	        throw new RuntimeException("Error encoding String: " + e.getMessage());
	    }
	}
	
	public static String filter(String string) {
		if(!isInitialized()) {
			System.out.printf("[LastFMManager#filter] Uninitialized LastFM instance doesn't have a pattern matcher to filter '%s'. Restart this application with at least LASTFMTOKEN present in your .env file.", string);
			return string;
		}
		return undesirableWordsInQuery.matcher(string).replaceAll("");
	}

	/**
	 * Performs a query with LastFM
	 * 
	 * @param type - One of {@link QueryType}
	 * @param args - Arguments you wish to pass in ascending order.
	 * 
	 * @implNote For 'args', example, for a 'type' of
	 *           {@link QueryType.TRACK_SIMILAR} you would first pass the artist,
	 *           then song title. Then optionally, the limit of tracks to return.
	 * 
	 * @return a {@link JSONObject} of the response, or a simple string if an error
	 *         occurs.
	 * 
	 */
	private static Object performQuery(QueryType type, Object... args) { // TODO is it better to keep it as Object... and dynamically pass data, or create submethods with special args?
		var strArtist = args.length > 0 ? args[0] : "";
		var strTitle = args.length > 1 ? args[1] : "";
		var iLimit = args.length > 2 ? args[2] : 5;

		System.err.printf("performQuery 1: %1$s 2: %2$s 3: %3$o",strArtist,strTitle,iLimit); //TODO Remove

		switch (type) { // TODO Implement more here when QueryType gets more types
		case TRACK_SIMILAR: {
			var url = String.format(
					"https://ws.audioscrobbler.com/2.0/?method=track.getSimilar&limit=%o&autocorrect=1&artist=%2$s&track=%3$s&format=json&api_key=%4$s",
					iLimit, strArtist, strTitle, APIKEY);

			// debug printing but removing the API key from the print via substring (length of APIKEY)
			try {
				System.out.println(url.substring(0, url.length() - APIKEY.length())); 
			} catch (IndexOutOfBoundsException e) {
				System.err.println("[LastFMManager#performQuery] Ehh??? IndexOutOfBoundsException on var url: ".concat(url));
				e.printStackTrace();
				return ""; // Assume we can't establish a connection if the URL couldn't even be substring'd
			}

			StringBuilder response = new StringBuilder();
			try {
				HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
				connection.setRequestMethod("GET");
				connection.setRequestProperty("User-Agent", "Zenvibe/" + botVersion); // identifiable User-Agent header as requested by last.fm

				try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
					String inputLine;

					while ((inputLine = in.readLine()) != null)
						response.append(inputLine);
					
				}
				connection.disconnect();
				System.out.println("[LastFMManager#performQuery] response: ".concat(response.toString()));
			} catch (IOException e) {
				e.printStackTrace();
				response.setLength(0);
			}
			return response;
		}
		default:
			throw new IllegalArgumentException("[LastFMManager#performQuery] type: " + type);
		}
	}

	private static String[] parseJsonResponse(QueryType type, String data) {
		System.out.printf("%n [Debug] parseJsonResponse %s %s", type.toString(), data);
		switch (type) {
		case TRACK_SIMILAR: {
			JSONArray dataArray = null;
			try {
				dataArray = (JSONArray) ((JSONObject) (((JSONObject) JSONValue.parse(data)).get("similartracks")))
						.get("track");
			} catch (NullPointerException e) {
				System.err.println("[LastFMManager#parseJsonResponse] NullPointer: type: TRACK_SIMILAR data: " + data);
				return new String[] { "" };
			}

			if (Collections.emptyList().equals(dataArray))
				return new String[] { "none" };

			final String[] returnable = new String[dataArray.size()];

			for (int i = 0; i < returnable.length; i++) {
				returnable[i] = ((JSONObject) ((JSONObject) dataArray.get(i)).get("artist")).get("name") + " - "
						+ ((JSONObject) dataArray.get(i)).get("name");
				System.out.printf("%n[LastFMManager#parseJsonResponse] foreach: %o %s", i, returnable[i]);
			}
			return returnable;
		}
		default:
			throw new IllegalArgumentException("[LastFMManager#parseJsonResponse] type: " + type);
		}
	}

	/**
	 * Finds similar songs on LastFM and returns the results
	 * 
	 * @param artist - The song artist
	 * @param title  - The song title
	 * @implNote This method automatically passes both strings through
	 *           {@link LastFMManager#asEncoded(String)}, and the title passes
	 *           through {@link LastFMManager#filter(String)}
	 */
	public static String[] findSimilarSongs(String artist, String title) {
		if (!isInitialized())
			return new String[] { "noapi" };
		
		System.out.println("[pr-autoplay] Passing %2$s through the filter (%1$s) ..."); //TODO Remove
		title = filter(title);
		
		System.out.println("[pr-autoplay] Encoding %1$s - %2$s to be URL friendly ..."); //TODO Remove
		artist = asEncoded(artist);
		title = asEncoded(title);

		var jsonResponse = performQuery(LastFMManager.QueryType.TRACK_SIMILAR, artist, title);

		if (jsonResponse != null) {
			var strArraySongs = parseJsonResponse(LastFMManager.QueryType.TRACK_SIMILAR, "" + jsonResponse);

			for (String string : strArraySongs)
				System.out.println("LastFMManager.findSimilarSong() " + string);

			return strArraySongs;
		} else
			return new String[] { "" };

	}

	/**
	 * Finds a similar song on LastFM and returns the first result Original
	 * behavior.
	 * 
	 * @param artist - The song artist
	 * @param title  - The song title
	 * 
	 * @implNote This method passes both strings through
	 *           {@link LastFMManager#asEncoded(String)}, and the title passes
	 *           through {@link LastFMManager#filter(String)}
	 */
	public static String findSimilarSong(String artist, String title) {
		/**
		 * This method is a very condensed version of findSimilarSongs, with an extra
		 * argument for song limit of 1 for performQuery as mentioned in implNote of
		 * that method
		 */	
		var returnable = parseJsonResponse(LastFMManager.QueryType.TRACK_SIMILAR, "" + performQuery(LastFMManager.QueryType.TRACK_SIMILAR, artist, title, 1))[0];
		return returnable.isBlank() ? "none" : returnable; //If blank, that means the performQuery method errored out
	}
}
