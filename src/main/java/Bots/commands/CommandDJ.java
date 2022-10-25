package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Objects;

import static Bots.Main.*;

public class CommandDJ extends BaseCommand {

    @Override
    public void execute(MessageEvent event) throws IOException {
        JSONObject config = event.getConfig();
        JSONArray DJRoles = (JSONArray) config.get("DJRoles");
        JSONArray DJUsers = (JSONArray) config.get("DJUsers");

        if (event.getArgs()[1].equalsIgnoreCase("list")) { // list djs
            EmbedBuilder eb = new EmbedBuilder();
            StringBuilder builder = new StringBuilder();
            builder.append("**Roles:**\n"); // list DJ roles in embed
            int i = 0;
            if (DJRoles.size() == 0) {
                builder.append("None.");
            } else {
                for (Object role : DJRoles) {
                    i++;
                    if (i == DJRoles.size()) {
                        try {
                            builder.append(event.getJDA().getRoleById((long) role).getAsMention());
                        } catch (Exception ignored) {
                        }
                    } else {
                        try {
                            builder.append(event.getJDA().getRoleById((long) role).getAsMention() + ", ");
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
            builder.append("\n\n**Users:**\n"); // list DJ users in embed
            i = 0;
            if (DJUsers.size() == 0) {
                builder.append("None.");
            } else {
                for (Object user : DJUsers) {
                    i++;
                    if (i == DJUsers.size()) {
                        builder.append(event.getJDA().getUserById((long) user).getAsMention());
                    } else {
                        builder.append(event.getJDA().getUserById((long) user).getAsMention() + ", ");
                    }
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
            long member = 0;
            long role = 0;
            try { // check to see if its a role or member or mention of a role or member
                member = Objects.requireNonNull(event.getJDA().getUserById(event.getArgs()[2])).getIdLong(); // check for userid
            } catch (Exception ignored1) {
            }
            try {
                role = Objects.requireNonNull(event.getJDA().getRoleById(event.getArgs()[2])).getIdLong(); // check for roleid
            } catch (Exception ignored2) {
            }
            try {
                if (event.getMessage().getMentions().getRoles().size() != 0) { // check if the message contains a mentioned role
                    role = event.getMessage().getMentions().getRoles().get(0).getIdLong();
                } else if (event.getMessage().getMentions().getMembers().size() != 0) { // check if the message contains a mentioned member
                    member = event.getMessage().getMentions().getMembers().get(0).getIdLong();
                }
            } catch (Exception ignored3) {
            }
            if (member == 0 && role == 0) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No valid user or role was given.")).queue();
                return;
            }
            if (member != 0) {
                if (DJUsers.contains(member)) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This member is already in the DJ list!")).queue();
                } else {
                    DJUsers.add(member);
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Added " + Objects.requireNonNull(event.getJDA().getUserById(member)).getAsMention() + " to the list of DJ members.")).queue();
                }
            }
            if (role != 0) {
                if (DJRoles.contains(role)) {
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This role is already in the DJ list!")).queue();
                } else {
                    DJRoles.add(role);
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Added " + Objects.requireNonNull(event.getJDA().getRoleById(role)).getAsMention() + " to the list of DJ roles.")).queue();
                }
            }
        } else if (event.getArgs()[1].equalsIgnoreCase("remove")) { // removing djs
            if (event.getArgs()[2] == null) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No user or role were specified.")).queue();
                return;
            }
            long member = 0;
            long role = 0;
            try { // check to see if its a role or member or mention of a role or member
                member = Objects.requireNonNull(event.getJDA().getUserById(event.getArgs()[2])).getIdLong(); // check for userid
            } catch (Exception ignored1) {
            }
            try {
                role = Objects.requireNonNull(event.getJDA().getRoleById(event.getArgs()[2])).getIdLong(); // check for roleid
            } catch (Exception ignored2) {
            }
            try {
                if (event.getMessage().getMentions().getRoles().size() != 0) { // check if the message contains a mentioned role
                    role = event.getMessage().getMentions().getRoles().get(0).getIdLong();
                } else if (event.getMessage().getMentions().getMembers().size() != 0) { // check if the message contains a mentioned member
                    member = event.getMessage().getMentions().getMembers().get(0).getIdLong();
                }
            } catch (Exception ignored3) {
            }
            if (member == 0 && role == 0) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No valid user or role was given.")).queue();
                return;
            }
            if (member != 0) {
                DJUsers.remove(member);
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Removed " + Objects.requireNonNull(event.getJDA().getUserById(member)).getAsMention() + " from the list of DJ members.")).queue();
            }
            if (role != 0) {
                DJRoles.remove(role);
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Removed " + Objects.requireNonNull(event.getJDA().getRoleById(role)).getAsMention() + " from the list of DJ roles.")).queue();
            }
        } else {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Invalid arguments.")).queue();
        }
    }

    @Override
    public String getCategory() {
        return "Admin";
    }

    @Override
    public String[] getNames() {
        return new String[]{"dj"};
    }

    @Override
    public String getDescription() {
        return "Adds or removes a user or role from the DJ list. Alternatively, shows you the DJ roles/users.";
    }

    @Override
    public String getParams() {
        return "<list> || <add/remove> <ID/Mention>";
    }
}