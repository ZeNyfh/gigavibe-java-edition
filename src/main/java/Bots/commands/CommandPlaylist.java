package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.createQuickEmbed;

public class CommandPlaylist extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        JSONObject User;
        JSONObject jsonFileContents = null;
        List<String> args = event.getArgs();
        String userID = event.getAuthor().getId();
        String finalPlaylist1 = "";
        String finalPlaylist2 = "";
        String finalPlaylist3 = "";

        JSONParser jsonParserCheck = new JSONParser();

        try (FileReader reader = new FileReader("Users.json")) {
            jsonFileContents = (JSONObject) jsonParserCheck.parse(reader);
        } catch (ParseException e) {e.printStackTrace();
        }
        JSONObject UserCheck = new JSONObject();
        UserCheck.putIfAbsent("Playlist1", "");
        UserCheck.putIfAbsent("Playlist2", "");
        UserCheck.putIfAbsent("Playlist3", "");
        assert jsonFileContents != null;
        jsonFileContents.put(userID, UserCheck);
        FileWriter file = new FileWriter("Users.json");
        file.write(jsonFileContents.toJSONString());try {
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject json;
        JSONParser jsonParser = new JSONParser();
        try (FileReader reader = new FileReader("Users.json")) {
            jsonFileContents = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert jsonFileContents != null;
        JSONObject userStuff = (JSONObject) jsonFileContents.get(userID);
        String stringPlaylist1 = userStuff.get("Playlist1").toString();
        String stringPlaylist2 = userStuff.get("Playlist2").toString();
        String stringPlaylist3 = userStuff.get("Playlist3").toString();
        if (args.size() >= 2) {
            AudioTrack curTrack = PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack();
            finalPlaylist1 = stringPlaylist1;
            finalPlaylist2 = stringPlaylist2;
            finalPlaylist3 = stringPlaylist3;
            if (args.get(1).equalsIgnoreCase("add") && args.get(2).equals("1")) {
                if (args.size() >= 4) {
                    finalPlaylist1 = finalPlaylist1 + args.get(3);
                } else if (curTrack.getInfo().uri != null) {
                    finalPlaylist1 = finalPlaylist1 + curTrack.getInfo().uri;
                } else if (curTrack.getInfo().title != null) {
                    finalPlaylist1 = finalPlaylist1 + curTrack.getInfo().title;
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Could not add the track to the playlist.")).queue();
                    return;
                }
                json = jsonFileContents;
                User = new JSONObject();
                User.put("Playlist1", finalPlaylist1 + "\n");
                User.put("Playlist2", finalPlaylist2);
                User.put("Playlist3", finalPlaylist3);
                json.put(userID, User);
                file = new FileWriter("Users.json");
                file.write(json.toJSONString());
                try {
                    file.flush();
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (args.get(1).equalsIgnoreCase("add") && args.get(2).equals("2")) {
                if (args.size() >= 4) {
                    finalPlaylist2 = finalPlaylist2 + args.get(3);
                } else if (curTrack.getInfo().uri != null) {
                    finalPlaylist2 = finalPlaylist2 + curTrack.getInfo().uri;
                } else if (curTrack.getInfo().title != null) {
                    finalPlaylist2 = finalPlaylist2 + curTrack.getInfo().title;
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Could not add the track to the playlist.")).queue();
                    return;
                }
                json = jsonFileContents;
                User = new JSONObject();
                User.put("Playlist1", finalPlaylist2);
                User.put("Playlist2", finalPlaylist2 + "\n");
                User.put("Playlist3", finalPlaylist3);
                json.put(userID, User);
                file = new FileWriter("Users.json");
                file.write(json.toJSONString());
                try {
                    file.flush();
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (args.get(1).equalsIgnoreCase("add") && args.get(2).equals("3")) {
                if (args.size() >= 4) {
                    finalPlaylist3 = finalPlaylist3 + args.get(3);
                } else if (curTrack.getInfo().uri != null) {
                    finalPlaylist3 = finalPlaylist3 + curTrack.getInfo().uri;
                } else if (curTrack.getInfo().title != null) {
                    finalPlaylist3 = finalPlaylist3 + curTrack.getInfo().title;
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Could not add the track to the playlist.")).queue();
                    return;
                }
                json = jsonFileContents;
                User = new JSONObject();
                User.put("Playlist1", finalPlaylist1);
                User.put("Playlist2", finalPlaylist2);
                User.put("Playlist3", finalPlaylist3 + "\n");
                json.put(userID, User);
                file = new FileWriter("Users.json");
                file.write(json.toJSONString());
                try {
                    file.flush();
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            else if (args.get(1).equalsIgnoreCase("play")) {
                if (args.size() == 3) {
                    event.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel());
                    if (Objects.equals(args.get(2), "1")) {
                        String[] playlist = stringPlaylist1.split("(\\r\\n|\\r|\\n)");
                        for (int i = 0; i < playlist.length;) {
                            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), playlist[i].replace("\\", "").replace("\n", ""), false);
                            i++;
                        }
                    }
                    else if (Objects.equals(args.get(2), "2")) {
                        String[] playlist = stringPlaylist2.split("(\\r\\n|\\r|\\n)");
                        for (int i = 0; i < playlist.length;) {
                            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), playlist[i].replace("\\", "").replace("\n", ""), false);
                            i++;
                        }
                    }
                    else if (Objects.equals(args.get(2), "3")) {
                        String[] playlist = stringPlaylist3.split("(\\r\\n|\\r|\\n)");
                        for (int i = 0; i < playlist.length;) {
                            PlayerManager.getInstance().loadAndPlay(event.getTextChannel(), playlist[i].replace("\\", "").replace("\n", ""), false);
                            i++;
                        }
                    } else {
                        event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Invalid argument 2: `" + args.get(2) + "` \n\n Valid args: **1-3**.")).queue();
                    }
                }
            }
        }
    }

    @Override
    public String getParams() {
        return "<play/add> <1-3> [string/url]";
    }

    @Override
    public ArrayList<String> getAlias() {
        ArrayList list = new ArrayList();
        list.add("pl");
        return list;
    }

    @Override
    public String getName() {
        return "playlist";
    }

    @Override
    public String getCategory() {
        return "music";
    }

    @Override
    public String getDescription() {
        return "Plays a playlist or saves tracks to a playlist, you can have up to 3 playlists with 500 tracks each.";
    }
}
