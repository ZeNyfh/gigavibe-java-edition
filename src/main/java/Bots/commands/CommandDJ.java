package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.Main.*;

public class CommandDJ extends BaseCommand {

    @Override
    public void execute(MessageEvent event) throws IOException {
        JSONObject config = event.getConfig();
        JSONArray DJRoles = (JSONArray) config.get("DJRoles");
        JSONArray DJUsers = (JSONArray) config.get("DJUsers");

        boolean isAdding = event.getArgs()[1].equalsIgnoreCase("add");
        boolean isRemoving = event.getArgs()[1].equalsIgnoreCase("remove");

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
                        builder.append(Objects.requireNonNull(event.getJDA().getUserById((Long) user)).getAsMention());
                    } else {
                        builder.append(Objects.requireNonNull(event.getJDA().getUserById((Long) user)).getAsMention()).append(", ");
                    }
                }
            }
            eb.setColor(botColour);
            eb.setTitle("DJs for " + event.getGuild().getName());
            eb.appendDescription(builder);
            event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();
        } else if (isAdding || isRemoving) { //Adding or Removing DJs. Shares similar code so we merge them initially
            if (event.getArgs().length < 3) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No user or role were specified.")).queue();
                return;
            }
            long member = 0;
            long role = 0;
            long suspectedID;
            Pattern pattern = Pattern.compile("(<@&?)?(\\d+)>?"); //matches <@USER-ID>, <@&ROLE-ID>, and AMBIGUOUS-ID
            Matcher matcher = pattern.matcher(event.getArgs()[2]);
            if (matcher.find()) {
                suspectedID = Long.parseLong(matcher.group(2));
            } else {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No user or role were specified.")).queue();
                return;
            }
            User suspectedUser = event.getJDA().getUserById(suspectedID);
            if (suspectedUser != null) {
                member = suspectedUser.getIdLong(); // check for userid
            }
            Role suspectedRole = event.getJDA().getRoleById(suspectedID);
            if (suspectedRole != null) {
                role = suspectedRole.getIdLong(); // check for userid
            }
            if (member == 0 && role == 0) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("No valid user or role was given.")).queue();
                return;
            }
            if (isAdding) { //Adding
                if (member != 0) {
                    if (DJUsers.contains(member)) {
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This member is already in the DJ list!")).queue();
                    } else {
                        DJUsers.add(member);
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Added " + suspectedUser.getAsMention() + " to the list of DJ members.")).queue();
                    }
                } else {
                    if (DJRoles.contains(role)) {
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This role is already in the DJ list!")).queue();
                    } else {
                        DJRoles.add(role);
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Added " + suspectedRole.getAsMention() + " to the list of DJ roles.")).queue();
                    }
                }
            } else { //Removing
                if (member != 0) {
                    DJUsers.remove(member);
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Removed " + suspectedUser.getAsMention() + " from the list of DJ members.")).queue();
                } else {
                    DJRoles.remove(role);
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ **Success**", "Removed " + suspectedRole.getAsMention() + " from the list of DJ roles.")).queue();
                }
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

    @Override
    public long getRatelimit() {
        return 1000;
    }
}