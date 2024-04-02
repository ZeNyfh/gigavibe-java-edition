package Bots.lavaplayer;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class LRCLIBManager {
    public static String getLyrics(AudioTrack track) {
        if (track.getInfo().title == null || track.getInfo().title.equalsIgnoreCase("unknown title")) {
            return "";
        }
        String url = createURL(track);
        if (url.isEmpty()) {
            return "";
        }

        try {
            URL requestURL = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) requestURL.openConnection();
            connection.setRequestMethod("GET");

            StringBuilder responseBuilder = new StringBuilder();
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            String response = responseBuilder.toString();
            if (response.equals("[]")) {
                return "";
            }

            String lyrics = parseLyrics(response);
            if (lyrics == null || lyrics.equalsIgnoreCase("null")) {
                return "";
            }
            return lyrics;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private static String createURL(AudioTrack track) {
        StringBuilder urlBuilder = new StringBuilder();
        String title = track.getInfo().title;
        String duration = "";
        urlBuilder.append("https://lrclib.net/api/search?q=");

        if (!(track.getInfo().length > 432000000) || !(track.getInfo().length <= 1)) {
            duration = "&duration=" + (int) (track.getInfo().length / 1000);
        }

        urlBuilder.append(java.net.URLEncoder.encode(title, StandardCharsets.UTF_8));
        String url = urlBuilder.toString();
        if (url.contains("+%28")) {
            url = url.split("\\+%28")[0].trim();
        }
        if (url.toLowerCase().contains("+ft.")) {
            url = url.split("\\+ft\\.")[0].trim();
        }
        url = url + duration;
        return url;
    }

    private static String parseLyrics(String rawJson) {
        Object object = JSONValue.parse(rawJson);
        JSONObject trackDetailsObject = (JSONObject) ((JSONArray) object).get(1);
        return (String) trackDetailsObject.get("plainLyrics");
    }
}
