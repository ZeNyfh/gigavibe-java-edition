package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

import static Bots.Main.*;

public class CommandDJ extends BaseCommand {
    public static JSONArray DJList = new JSONArray();

    public void execute(MessageEvent event) throws IOException {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Insufficient permissions**", "you do not have the permission to use this command.")).queue();
            return;
        }
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonFileContents = null;
        try (FileReader reader = new FileReader("DJs.json")) {
            jsonFileContents = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject json = jsonFileContents;
        JSONArray DJUsers = new JSONArray();
        JSONArray DJRoles = new JSONArray();
        JSONObject check = new JSONObject();
        JSONObject GuildContents = (JSONObject) json.get(event.getGuild().getId());
        if (json.get(event.getGuild().getId()) == null) {
            check.put("users", DJUsers);
            check.put("roles", DJRoles);
            json.putIfAbsent(event.getGuild().getId(), check);
            FileWriter file = new FileWriter("DJs.json");
            file.write(json.toJSONString());
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try (FileReader reader = new FileReader("DJs.json")) {
                jsonFileContents = (JSONObject) jsonParser.parse(reader);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            json = jsonFileContents;
            GuildContents = (JSONObject) json.get(event.getGuild().getId());
        } else {
            DJUsers = (JSONArray) GuildContents.get("users");
            DJRoles = (JSONArray) GuildContents.get("roles");
        }
        if (event.getArgs()[1].equalsIgnoreCase("list")) {
            if (event.getArgs()[2].equalsIgnoreCase("users") || event.getArgs()[2].equalsIgnoreCase("roles")) {
                String arg2 = event.getArgs()[2].toLowerCase();
                JSONArray arg2Array = (JSONArray) GuildContents.get(arg2);
                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("** DJ " + event.getArgs()[2] + "** for " + event.getGuild().getName());
                eb.setColor(botColour);
                if (arg2Array.size() == 0) {
                    eb.setDescription("None.");
                    event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
                    return;
                }
                if (event.getArgs()[2].equalsIgnoreCase("roles")) {
                    for (int i = 0; i < arg2Array.size(); ) {
                        eb.appendDescription(arg2Array.get(i) + " - **" + Objects.requireNonNull(event.getJDA().getRoleById((String) arg2Array.get(i))).getName() + "**\n");
                        i++;
                    }
                }
                if (event.getArgs()[2].equalsIgnoreCase("users")) {
                    for (int i = 0; i < arg2Array.size(); ) {
                        eb.appendDescription(arg2Array.get(i) + " - **" + Objects.requireNonNull(event.getJDA().getUserById((String) arg2Array.get(i))).getName() + "**\n");
                        i++;
                    }
                }
                event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
            } else {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Incorrect arguments, use `" + botPrefix + "dj list roles` or `" + botPrefix + "dj list users` to show all DJs.")).queue();
            }
        } else if (event.getArgs()[1].equalsIgnoreCase("add")) {
            if (event.getArgs()[2].equalsIgnoreCase("user")) {
                if (event.getArgs()[3].contains("<@")) {
                    String UserID = event.getArgs()[3].replace("<@", "").replace(">", "");
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Added to DJ Users list.")).queue();
                    DJUsers.add(UserID);
                }
            } else if (event.getArgs()[2].equalsIgnoreCase("role")) {
                if (event.getArgs()[3].contains("<@&")) {
                    String RoleID = event.getArgs()[3].replace("<@&", "").replace(">", "");
                    DJRoles.add(RoleID);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Added to DJ Roles list.")).queue();
                }
            } else {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Incorrect arguments, use `" + botPrefix + "dj role add <RoleID/Ping>` or `" + botPrefix + "dj user add <UserID/Ping>`")).queue();
            }
            GuildContents.put("users", DJUsers);
            GuildContents.put("roles", DJRoles);
            json.put(event.getGuild().getId(), GuildContents);
            FileWriter file = new FileWriter("DJs.json");
            file.write(json.toJSONString());
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (event.getArgs()[1].equalsIgnoreCase("remove")) {
            if (event.getArgs()[2].equalsIgnoreCase("user")) {
                if (event.getArgs()[3].contains("<@")) {
                    String UserID = event.getArgs()[3].replace("<@", "").replace(">", "");
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Removed from DJ Users list.")).queue();
                    DJUsers.remove(UserID);
                }
            } else if (event.getArgs()[2].equalsIgnoreCase("role")) {
                if (event.getArgs()[3].contains("<@&")) {
                    String RoleID = event.getArgs()[3].replace("<@&", "").replace(">", "");
                    DJRoles.remove(RoleID);
                    event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Removed from DJ Roles list.")).queue();
                }
            } else {
                event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "Incorrect arguments, use `" + botPrefix + "dj role remove <RoleID/Ping>` or `" + botPrefix + "dj user remove <UserID/Ping>`")).queue();
            }
            GuildContents.put("users", DJUsers);
            GuildContents.put("roles", DJRoles);
            json.put(event.getGuild().getId(), GuildContents);
            FileWriter file = new FileWriter("DJs.json");
            file.write(json.toJSONString());
            try {
                file.flush();
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
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
        return "Adds or removes a user or role from the DJ list. Alternatively, shows you the DJ roles/users.";
    }

    public String getParams() {
        return "<list> OR <add/remove> <user/role> <ID/Ping>";
    }
}