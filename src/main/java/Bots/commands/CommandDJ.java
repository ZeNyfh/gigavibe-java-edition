package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static Bots.Main.botColour;
import static Bots.Main.createQuickEmbed;

public class CommandDJ extends BaseCommand {
    public static JSONArray DJList = new JSONArray(); // i could not get this to work

    public void execute(MessageEvent event) throws IOException {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            return;
        }
        if (!event.getArgs().get(1).equalsIgnoreCase("list")) {
            if (event.getArgs().size() != 3) {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Invalid arguments.**", "The valid usage is: `blockchannel <remove/add> <ChannelID>` or `blockchannel list`")).queue();
                return;
            }
        }

        JSONObject obj = new JSONObject();
        JSONObject DJ = new JSONObject();
        JSONParser jsonParser = new JSONParser();

        JSONObject jsonFileContents = null;
        try (FileReader reader = new FileReader("Guilds.json")) {
            jsonFileContents = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
        JSONObject json = jsonFileContents;
        DJ.put("DJRoles", new JSONArray());
        json.putIfAbsent(event.getGuild().getId(), DJ);
        FileWriter file = new FileWriter("Guilds.json");
        try {
            file.write(json.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JSONObject guildObj = (JSONObject) json.get(event.getGuild().getId());
        JSONArray DJRoles = new JSONArray();
        try {
            DJRoles = (JSONArray) guildObj.get("DJRoles"); // getting the values from the file
        } catch (Exception ignored) {
        }

        for (int i = 0; i < event.getMember().getRoles().size(); ) {
            Role role = event.getMember().getRoles().get(i);
            if (event.getArgs().get(1).equalsIgnoreCase("add")) {
                if (role.getId().equals(event.getArgs().get(2))) {
                    DJRoles.add(event.getArgs().get(2));
                    DJ.put("BlockedChannels", DJRoles);
                    obj.put(event.getGuild().getId(), DJ);

                    try {
                        file = new FileWriter("Guilds.json");
                        file.write(obj.toJSONString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            file.flush();
                            file.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Added " + event.getJDA().getRoleById(event.getArgs().get(2)) + Objects.requireNonNull(event.getJDA().getRoleById(event.getArgs().get(2))).getName() + " to the list.")).queue();
                    return;
                } else {
                    i++;
                }
            } else if (event.getArgs().get(1).equalsIgnoreCase("remove")) {
                if (role.getId().equals(event.getArgs().get(2))) {
                    DJRoles.remove(event.getArgs().get(2));
                    DJ.put("BlockedChannels", DJRoles);
                    obj.put(event.getGuild().getId(), DJ);
                    try {
                        file = new FileWriter("Guilds.json");
                        file.write(obj.toJSONString());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            file.flush();
                            file.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Removed " + event.getJDA().getRoleById(event.getArgs().get(2)) + Objects.requireNonNull(event.getJDA().getRoleById(event.getArgs().get(2))).getName() + " from the list.")).queue();
                    return;
                } else {
                    i++;
                }
            } else if (event.getArgs().get(1).equalsIgnoreCase("list")) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(botColour);
                eb.setTitle("Blocked channels for " + event.getGuild().getName() + ":");
                if (DJRoles.size() == 0) {
                    eb.appendDescription("**None.**");
                    event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
                    return;
                }
                for (int j = 0; j < DJRoles.size(); ) {
                    eb.appendDescription("**" + DJRoles.get(j) + "** - " + Objects.requireNonNull(event.getJDA().getRoleById((String) DJRoles.get(j))).getName() + "\n");
                    j++;
                }
                event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
                return;
            } else {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Invalid arguments.\n\nThe valid usage is: `dj <remove/add> <ChannelID>`")).queue();
                return;
            }
        }
    }


    public String getCategory() {
        return "Admin";
    }

    public String getName() {
        return "dj";
    }

    public String getDescription() {
        return "Sets a role to have dj permissions or shows all current DJs.";
    }

    public String getParams() {
        return "[Role]";
    }
}