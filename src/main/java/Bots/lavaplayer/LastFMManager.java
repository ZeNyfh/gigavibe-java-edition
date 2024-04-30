package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static Bots.Main.autoPlayedTracks;
import static Bots.Main.botVersion;

// Last.fm wish for their API to be used sensibly, I have outlined with comments how it is being used sensibly with attention to their note found at: https://www.last.fm/api/intro
public class LastFMManager {
    public static boolean hasAPI = false;
    private static String APIKEY = null;

    public static void Init() {
        Dotenv dotenv = Dotenv.load();
        String key = dotenv.get("LASTFMTOKEN");
        if (key == null) {
            System.err.println("LASTFMTOKEN is not set in " + new File(".env").getAbsolutePath());
        } else {
            System.out.println("LastFM manager initialised");
            hasAPI = true;
        }
        APIKEY = key;
    }

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
}
