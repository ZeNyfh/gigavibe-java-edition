package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
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
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.Main.*;

public class CommandDJ extends BaseCommand {

    @Override
    public void execute(MessageEvent event) {
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
            if (DJRoles.isEmpty()) {
                builder.append("None.");
            } else {
                for (Object role : DJRoles) {
                    i++;
                    String mention = Objects.requireNonNull(event.getJDA().getRoleById((long) role)).getAsMention();
                    if (i == DJRoles.size()) {
                        builder.append(mention);
                    } else {
                        builder.append(mention).append(", ");
                    }
                }
            }
            builder.append("\n\n**Users:**\n"); // list DJ users in embed
            i = 0;
            if (DJUsers.isEmpty()) {
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
            event.replyEmbeds(eb.build());
        } else if (isAdding || isRemoving) { //Adding or Removing DJs. Shares similar functionality so we merge them initially
            if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
                event.replyEmbeds(createQuickEmbed("❌ **Insufficient permissions**", "You do not have the permission to use this command."));
                return;
            }
            Pattern mentionRegex = Pattern.compile("(?:<@&?)?(\\d+)>?");
            List<Long> FoundMembers = new ArrayList<>();
            List<Long> FoundRoles = new ArrayList<>();
            for (int i = 2; i < event.getArgs().length; i++) {
                String arg = event.getArgs()[i];
                Matcher matcher = mentionRegex.matcher(arg);
                if (matcher.matches()) {
                    long ID = Long.parseLong(matcher.group(1));
                    if (event.getGuild().getMemberById(ID) != null) {
                        FoundMembers.add(ID);
                    } else {
                        if (event.getGuild().getRoleById(ID) != null) {
                            FoundRoles.add(ID);
                        }
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
                        DJUsers.add(member);
                    }
                }
                for (long role : FoundRoles) {
                    if (!DJRoles.contains(role)) {
                        DJRoles.add(role);
                    }
                }
            } else { // Removing instead
                for (long member : FoundMembers) {
                    DJUsers.remove(member);
                }
                for (long role : FoundRoles) {
                    DJRoles.remove(role);
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