package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.Main.*;

public class CommandDJ extends BaseCommand {
    Pattern mentionRegex = Pattern.compile("(?:<@&?)?(\\d+)>?");

    @Override
    public void execute(CommandEvent event) {
        JSONObject config = event.getConfig();
        JSONArray DJRoles = (JSONArray) config.get("DJRoles");
        JSONArray DJUsers = (JSONArray) config.get("DJUsers");

        boolean isAdding = event.getArgs()[1].equalsIgnoreCase("add");
        boolean isRemoving = event.getArgs()[1].equalsIgnoreCase("remove");

        if (event.getArgs()[1].equalsIgnoreCase("list")) { // list djs
            EmbedBuilder eb = new EmbedBuilder();
            StringBuilder builder = new StringBuilder();
            builder.append("**Roles:**\n"); // list DJ roles in embed
            if (DJRoles.isEmpty()) {
                builder.append("None.");
            } else {
                int i = 0;
                for (Object role : DJRoles) {
                    i++;
                    if ((long) role == event.getGuild().getIdLong()) {
                        builder.append("@everyone"); //edge-case fix
                    } else {
                        builder.append("<@&").append(role).append(">");
                    }
                    if (i != DJRoles.size()) {
                        builder.append(", ");
                    }
                }
            }
            builder.append("\n\n**Users:**\n"); // list DJ users in embed
            if (DJUsers.isEmpty()) {
                builder.append("None.");
            } else {
                int i = 0;
                for (Object user : DJUsers) {
                    i++;
                    builder.append("<@").append(user).append(">");
                    if (i != DJUsers.size()) {
                        builder.append(", ");
                    }
                }
            }
            eb.setColor(botColour);
            eb.setTitle("DJs for " + event.getGuild().getName());
            eb.appendDescription(builder);
            event.replyEmbeds(eb.build());
        } else if (isAdding || isRemoving) { //Adding or Removing DJs. Shares similar functionality so we merge them initially
            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                event.replyEmbeds(createQuickEmbed("❌ **Insufficient permissions**", "You do not have the permission to use this command."));
                return;
            }
            List<Long> FoundMembers = new ArrayList<>();
            List<Long> FoundRoles = new ArrayList<>();
            for (int i = 2; i < event.getArgs().length; i++) {
                String arg = event.getArgs()[i];
                Matcher matcher = mentionRegex.matcher(arg);
                if (matcher.matches()) {
                    long ID = Long.parseLong(matcher.group(1));
                    if (event.getGuild().getMemberById(ID) != null) {
                        FoundMembers.add(ID);
                    } else if (event.getGuild().getRoleById(ID) != null) {
                        FoundRoles.add(ID);
                    }
                }
            }
            if (FoundMembers.size() + FoundRoles.size() == 0) {
                event.replyEmbeds(createQuickError("No members or roles were specified."));
                return;
            }

            if (isAdding) {
                for (long member : FoundMembers) {
                    if (!DJUsers.contains(member)) {
                        modifyDJ(GuildObjectType.member.ordinal(), member, true, config);
                    }
                }
                for (long role : FoundRoles) {
                    if (!DJRoles.contains(role)) {
                        modifyDJ(GuildObjectType.role.ordinal(), role, true, config);
                    }
                }
            } else { // Removing instead
                for (long member : FoundMembers) {
                    modifyDJ(GuildObjectType.member.ordinal(), member, false, config);
                }
                for (long role : FoundRoles) {
                    modifyDJ(GuildObjectType.role.ordinal(), role, false, config);
                }
            }
            String memberText = FoundMembers.size() == 1 ? "member" : "members";
            String roleText = FoundRoles.size() == 1 ? "role" : "roles";
            String msg;
            if (!FoundMembers.isEmpty()) {
                if (!FoundRoles.isEmpty()) {
                    msg = String.format("%d %s and %d %s", FoundMembers.size(), memberText, FoundRoles.size(), roleText);
                } else {
                    msg = String.format("%d %s", FoundMembers.size(), memberText);
                }
            } else {
                msg = String.format("%d %s", FoundRoles.size(), roleText);
            }
            if (isAdding) {
                event.replyEmbeds(createQuickEmbed("✅ **Success**", "Added " + msg + " to the DJ list."));
            } else {
                event.replyEmbeds(createQuickEmbed("✅ **Success**", "Removed " + msg + " from the DJ list."));
            }
        } else {
            event.replyEmbeds(createQuickError("Invalid arguments."));
        }
    }

    private enum GuildObjectType {
        role, member
    }

    private synchronized void modifyDJ(int type, long id, boolean isAdding, JSONObject config) {
        if (type == 0) { // role
            JSONArray DJRoles = (JSONArray) config.get("DJRoles");
            if (isAdding) { // is adding
                DJRoles.add(id);
            } else { // is removing
                DJRoles.remove(id);
            }
        } else { // member
            JSONArray DJUsers = (JSONArray) config.get("DJUsers");
            if (isAdding) { // is adding
                DJUsers.add(id);
            } else { // is removing
                DJUsers.remove(id);
            }
        }
    }

    @Override
    public Category getCategory() {
        return Category.Admin;
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
    public String getOptions() {
        return "add <User/Role> | remove <User/Role>";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addSubcommands(
                new SubcommandData("add", "Adds someone/a role from DJ.").addOptions(
                        new OptionData(OptionType.USER, "user", "Gives DJ to the user.", false),
                        new OptionData(OptionType.ROLE, "role", "Gives DJ to the role.", false)
                ),
                new SubcommandData("remove", "Removes someone/a role from DJ.").addOptions(
                        new OptionData(OptionType.USER, "user", "Removes DJ from the user.", false),
                        new OptionData(OptionType.ROLE, "role", "Removes DJ from the role.", false)
                ),
                new SubcommandData("list", "Lists the DJs.")
        );
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}