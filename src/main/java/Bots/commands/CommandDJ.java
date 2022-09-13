package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
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
        JSONParser jsonParser = new JSONParser();
        JSONObject json = new JSONObject();

        try (FileReader reader = new FileReader("DJs.json")) {
            json = (JSONObject) jsonParser.parse(reader);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        JSONObject GuildContents = (JSONObject) json.get(event.getGuild().getId());

        if (json.get(event.getGuild().getId()) == null) { // adding values if they are missing.
            GuildContents.put("roles", new JSONArray());
            GuildContents.put("users", new JSONArray());
            json.put(event.getGuild().getId(), GuildContents);
            try {
                FileWriter writer = new FileWriter("DJs.json");
                writer.write(json.toJSONString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        JSONArray DJRoles = (JSONArray) GuildContents.get("roles");
        JSONArray DJUsers = (JSONArray) GuildContents.get("users");

        if (event.getArgs()[1].equalsIgnoreCase("list")) { // list djs
            EmbedBuilder eb = new EmbedBuilder();
            StringBuilder builder = new StringBuilder();
            builder.append("**Roles:**\n"); // list DJ roles in embed
            if (DJRoles.size() == 0) {
                builder.append("None.");
            } else {
                for (int i = 0; i < DJRoles.size() - 1; i++) {
                    builder.append(event.getJDA().getRoleById((String) DJRoles.get(i)).getAsMention() + ", ");
                }
            }
            builder.append("\n\n**Users:**\n"); // list DJ users in embed
            if (DJUsers.size() == 0) {
                builder.append("None.");
            } else {
                for (int i = 0; i < DJUsers.size() - 1; i++) {
                    builder.append(event.getJDA().getUserById((String) DJUsers.get(i)).getAsMention() + ", ");
                }
            }
            eb.setColor(botColour);
            eb.setTitle("DJs for " + event.getGuild().getName());
            eb.appendDescription(builder);
            event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();

        } else if (event.getArgs()[1].equalsIgnoreCase("add")) { // adding djs
            if (event.getArgs()[2] == null) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No user or role were specified.")).queue();
                return;
            }
            String member = null;
            String role = null;
            try { // check to see if its a role or member or mention of a role or member
                member = Objects.requireNonNull(event.getJDA().getUserById(event.getArgs()[2])).getId(); // check for userid
            } catch (Exception ignored1) {
            }
            try {
                role = Objects.requireNonNull(event.getJDA().getRoleById(event.getArgs()[2])).getId(); // check for roleid
            } catch (Exception ignored2) {
            }
            try {
                if (event.getMessage().getMentions().getRoles().size() != 0) { // check if the message contains a mentioned role
                    role = event.getMessage().getMentions().getRoles().get(0).getId();
                } else if (event.getMessage().getMentions().getMembers().size() != 0) { // check if the message contains a mentioned member
                    member = event.getMessage().getMentions().getMembers().get(0).getId();
                }
            } catch (Exception ignored3) {
            }
            if (member == null && role == null || Objects.equals(member, event.getGuild().getId()) || Objects.equals(role, event.getGuild().getId())) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No valid user or role was given.")).queue();
                return;
            }
            if (role == null) {
                if (((JSONArray) GuildContents.get("users")).contains(member)) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This member is already in the DJ list!")).queue();
                    return;
                }
                DJUsers.add(member);
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Added " + event.getArgs()[2] + " to the list of DJ members.")).queue();
                ((JSONArray) GuildContents.get("users")).add(member);
            }
            if (member == null) {
                if (((JSONArray) GuildContents.get("roles")).contains(role)) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This role is already in the DJ list!")).queue();
                    return;
                }
                DJRoles.add(role);
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Added " + event.getArgs()[2] + " to the list of DJ roles.")).queue();
                ((JSONArray) GuildContents.get("roles")).add(role);
            }
            json.put(event.getGuild().getId(), GuildContents);
            try {
                FileWriter writer = new FileWriter("DJs.json");
                writer.write(json.toJSONString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if (event.getArgs()[1].equalsIgnoreCase("remove")) { // removing djs
            if (event.getArgs()[2] == null) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No user or role were specified.")).queue();
                return;
            }
            String member = null;
            String role = null;
            try { // check to see if its a role or member or mention of a role or member
                member = Objects.requireNonNull(event.getJDA().getUserById(event.getArgs()[2])).getId(); // check for userid
            } catch (Exception ignored1) {
            }
            try {
                role = Objects.requireNonNull(event.getJDA().getRoleById(event.getArgs()[2])).getId(); // check for roleid
            } catch (Exception ignored2) {
            }
            try {
                if (event.getMessage().getMentions().getRoles().size() != 0) { // check if the message contains a mentioned role
                    role = event.getMessage().getMentions().getRoles().get(0).getId();
                } else if (event.getMessage().getMentions().getMembers().size() != 0) { // check if the message contains a mentioned member
                    member = event.getMessage().getMentions().getMembers().get(0).getId();
                }
            } catch (Exception ignored3) {
            }
            if (member == null && role == null || Objects.equals(member, event.getGuild().getId()) || Objects.equals(role, event.getGuild().getId())) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No valid user or role was given.")).queue();
                return;
            }
            if (role == null) {
                DJUsers.remove(member);
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Removed " + event.getArgs()[2] + " from the list of DJ members.")).queue();
                ((JSONArray) GuildContents.get("users")).remove(member);
            }
            if (member == null) {
                DJRoles.remove(role);
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Removed " + event.getArgs()[2] + " from the list of DJ roles.")).queue();
                ((JSONArray) GuildContents.get("roles")).remove(role);
            }
            json.put(event.getGuild().getId(), GuildContents);
            try {
                FileWriter writer = new FileWriter("DJs.json");
                writer.write(json.toJSONString());
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Invalid arguments.")).queue();
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
        return "<list> || <add/remove> <ID/Mention>";
    }
}