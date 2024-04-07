package Bots.lavaplayer;

import Bots.MessageEvent;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import io.github.cdimascio.dotenv.Dotenv;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

// Last.fm wish for their API to be used sensibly, I have outlined with comments how it is being used sensibly with attention to their note found at: https://www.last.fm/api/intro
public class LastFMManager {
    public static boolean hasAPI = false;
    private static String APIKEY = "";

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
        String songName = track.getInfo().title;
        String artistName = track.getInfo().author;

        String artist = encode(songName)[1];
        if (!artist.isEmpty()) {
            artistName = artist;
        }

        try {
            if (APIKEY == null) {
                return "noapi";
            }

            String urlString = "http://ws.audioscrobbler.com/2.0/?method=track.getSimilar&limit=5&autocorrect=1&artist=" + encode(artistName)[0] + "&track=" + encode(songName)[0] + "&api_key=" + APIKEY + "&format=json";
            System.out.println(urlString);

            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("User-Agent", "Zenvibe/" + botVersion); // identifiable User-Agent header as requested by last.fm

            StringBuilder response = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
            connection.disconnect();

            if (response.toString().startsWith("{\"error\":6,\"message\":\"Track not found\"")) {
                return "notfound";
            }

            String trackToSearch = extractTracks(response.toString(), guildID);
            if (trackToSearch.isEmpty()) {
                return "none";
            } else {
                return trackToSearch;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static String[] encode(String str) {
        String encodedStr = URLEncoder.encode(str, StandardCharsets.UTF_8);
        if (encodedStr.contains("+%28")) {
            encodedStr = encodedStr.split("\\+%28")[0].trim();
        }
        if (encodedStr.contains("%5B")) {
            encodedStr = encodedStr.split("\\+%5B")[0].trim();
        }
        if (encodedStr.toLowerCase().contains("+ft.")) {
            encodedStr = encodedStr.split("ft\\.")[0].trim();
        }
        String artistName = "";
        if (encodedStr.contains("-")) {
            artistName = encodedStr.split("-", 2)[0].trim();
            encodedStr = encodedStr.split("-", 2)[1].trim();
        }
        if (encodedStr.toLowerCase().contains("vevo")) {
            encodedStr = encodedStr.toLowerCase().replaceAll("vevo", "").trim();
        }
        if (encodedStr.contains("lyrics")) {
            encodedStr = encodedStr.toLowerCase().split("lyrics", 2)[0];
        }
        artistName = artistName.toLowerCase().replaceAll("%2b", "+").replaceAll("\\+", " ").trim().replaceAll(" ", "+");
        encodedStr = encodedStr.toLowerCase().replaceAll("%2b", "+").replaceAll("\\+", " ").trim().replaceAll(" ", "+");
        return new String[]{encodedStr, artistName};
    }

    private static String extractTracks(String rawJson, long guildID) {
        Object object = JSONValue.parse(rawJson);
        JSONArray trackInfoArray = (JSONArray) ((JSONObject) (((JSONObject) object).get("similartracks"))).get("track");

        StringBuilder builder = new StringBuilder();
        for (Object obj : trackInfoArray) {
            builder.append(String.valueOf(((JSONObject) ((JSONObject) obj).get("artist")).get("name")).toLowerCase()).append(" - ");
            builder.append(String.valueOf(((JSONObject) obj).get("name")).toLowerCase());
            System.out.println(autoPlayedTracks.get(guildID));
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