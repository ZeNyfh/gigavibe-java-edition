package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import Bots.lavaplayer.PlayerManager;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandPlaylist extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        String userID = event.getAuthor().getId();
        String[] args = event.getMessage().getContentRaw().split(" ", 4); // I had to fall back to this as arg 4 is a string that can contain spaces and getArgs() is not as flexible here.
        //List<String> args = event.getArgs();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonFileContents = null;
        try (FileReader reader = new FileReader("Users.json")) {
            jsonFileContents = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject json = jsonFileContents;
        JSONObject User = new JSONObject();
        JSONObject userObj = (JSONObject) json.get(userID);

        // if the values dont exist for the userid, it adds them here.
        User.put("Playlist1", new JSONArray());
        User.put("Playlist2", new JSONArray());
        User.put("Playlist3", new JSONArray());
        json.putIfAbsent(userID, User);
        FileWriter file = new FileWriter("Users.json");
        file.write(json.toJSONString());
        try {
            file.flush();
            file.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        JSONArray playlist1Array = new JSONArray();
        JSONArray playlist2Array = new JSONArray();
        JSONArray playlist3Array = new JSONArray();
        try {
            playlist1Array = (JSONArray) userObj.get("Playlist1"); // getting the values from the file
            playlist2Array = (JSONArray) userObj.get("Playlist2");
            playlist3Array = (JSONArray) userObj.get("Playlist3");
        } catch (Exception ignored) {
        }
        AudioTrack currentTrack = PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember()).getVoiceState();
        final GuildVoiceState selfVoiceState = event.getGuild().getSelfMember().getVoiceState();

        if (args[1].equalsIgnoreCase("add")) {
            // Playlist1 adding
            if (args[2].equals("1")) {
                if (args.length >= 4) {
                    playlist1Array.add(args[3]);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Added " + args[3] + " to playlist 1.")).queue();
                } else if (currentTrack != null) {
                    playlist1Array.add(currentTrack.getInfo().uri);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Added " + currentTrack.getInfo().title + " to playlist 1.")).queue();
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing, right now. \n\n You can use **" + botPrefix + "playlist add 1 [**String/url**] to add a track to the playlist.")).queue();
                    return;
                }
            }
            // Playlist2 adding
            if (args[2].equals("2")) {
                if (args.length >= 4) {
                    playlist2Array.add(args[3]);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Added " + args[3] + " to playlist 2.")).queue();
                } else if (currentTrack != null) {
                    playlist2Array.add(currentTrack.getInfo().uri);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Added " + currentTrack.getInfo().title + " to playlist 2.")).queue();
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing, right now. \n\n You can use **" + botPrefix + "playlist add 2 [**String/url**] to add a track to the playlist.")).queue();
                    return;
                }
            }
            // Playlist3 adding
            if (args[2].equals("3")) {
                if (args.length >= 4) {
                    playlist3Array.add(args[3]);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Added " + args[3] + " to playlist 3.")).queue();
                } else if (currentTrack != null) {
                    playlist3Array.add(currentTrack.getInfo().uri);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Added " + currentTrack.getInfo().title + " to playlist 3.")).queue();
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing, right now. \n\n You can use **" + botPrefix + "playlist add 3 [**String/url**] to add a track to the playlist.")).queue();
                    return;
                }
            }
            User.put("Playlist1", playlist1Array);
            User.put("Playlist2", playlist2Array);
            User.put("Playlist3", playlist3Array);
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
        if (args[1].equalsIgnoreCase("remove")) {
            // Playlist1 removing
            if (args[2].equals("1")) {
                if (args.length >= 4) {
                    playlist1Array.remove(args[3]);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Removed " + args[3] + " from playlist 1.")).queue();
                } else if (currentTrack != null) {
                    playlist1Array.remove(currentTrack.getInfo().uri);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Removed " + currentTrack.getInfo().title + " from playlist 1.")).queue();
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing, right now. \n\n You can use **" + botPrefix + "playlist remove 1 [**String/url**] to remove a track from the playlist.")).queue();
                    return;
                }
            }
            // Playlist2 removing
            if (args[2].equals("2")) {
                if (args.length >= 4) {
                    playlist2Array.remove(args[3]);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Removed " + args[3] + " from playlist 2.")).queue();
                } else if (currentTrack != null) {
                    playlist2Array.remove(currentTrack.getInfo().uri);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Removed " + currentTrack.getInfo().title + " from playlist 2.")).queue();
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing, right now. \n\n You can use **" + botPrefix + "playlist remove 2 [**String/url**] to remove a track from the playlist.")).queue();
                    return;
                }
            }
            // Playlist3 removing
            if (args[2].equals("3")) {
                if (args.length >= 4) {
                    playlist3Array.remove(args[3]);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Removed " + args[3] + " from playlist 3.")).queue();
                } else if (currentTrack != null) {
                    playlist3Array.remove(currentTrack.getInfo().uri);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "Removed " + currentTrack.getInfo().title + " from playlist 3.")).queue();
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "No tracks are playing, right now. \n\n You can use **" + botPrefix + "playlist remove 3 [**String/url**] to remove a track from the playlist.")).queue();
                    return;
                }
            }
            User.put("Playlist1", playlist1Array);
            User.put("Playlist2", playlist2Array);
            User.put("Playlist3", playlist3Array);
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
        if (args[1].equalsIgnoreCase("play") || args[1].equalsIgnoreCase("p")) {
            if (IsChannelBlocked(event.getGuild(), event.getTextChannel())) {
                return;
            }
            assert selfVoiceState != null;
            if (selfVoiceState.getChannel() != null) {
                assert memberState != null;
                if (!Objects.equals(memberState.getChannel(), selfVoiceState.getChannel())) {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "You need to be in the same voice channel to use this command with these arguments.")).queue();
                    return;
                }
            }
            if (args[2].equals("1") || args[2].equals("2") || args[2].equals("3")) {
                JSONArray playlist = (JSONArray) userObj.get("Playlist" + event.getArgs()[2]);
                if (playlist.size() >= 1) {
                    assert memberState != null;
                    TextChannel textChannel = event.getTextChannel();
                    PlayerManager playerManagerInstance = PlayerManager.getInstance();
                    event.getGuild().getAudioManager().openAudioConnection(memberState.getChannel());
                    List finalPlaylist = playlist.stream().toList();
                    for (int i = 0; i < playlist.size(); ) {
                        playerManagerInstance.loadAndPlay(textChannel, (String) finalPlaylist.get(i), false);
                        i++;
                    }
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ Successfully queued: **playlist " + args[2] + "**.", "Playlist size: **" + playlist.size() + "**")).queue();
                } else {
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "This playlist is empty, try adding some tracks to it first!")).queue();
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
        ArrayList<String> list = new ArrayList<>();
        list.add("pl");
        return list;
    }

    @Override
    public String getName() {
        return "playlist";
    }

    @Override
    public String getCategory() {
        return "Music";
    }

    @Override
    public String getDescription() {
        return "Plays a playlist or saves tracks to a playlist, you can have up to 3 playlists with 500 tracks each.";
    }

    @Override
    public long getTimeout() {
        return 5000;
    }
}
