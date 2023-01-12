package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

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
            if (DJRoles.size() == 0) {
                builder.append("None.");
            } else {
                for (Object role : DJRoles) {
                    i++;
                    String mention = Objects.requireNonNull(event.getJDA().getRoleById((long) role)).getAsMention();
                    if (i == DJRoles.size()) {
                        try {
                            builder.append(mention);
                        } catch (Exception ignored) {
                        }
                    } else {
                        try {
                            builder.append(mention).append(", ");
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
            event.replyEmbeds(eb.build());
        } else if (isAdding || isRemoving) { //Adding or Removing DJs. Shares similar code so we merge them initially
            if (event.getArgs().length < 3) {
                event.replyEmbeds(createQuickError("No user or role were specified."));
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
                event.replyEmbeds(createQuickError("No user or role were specified."));
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
                event.replyEmbeds(createQuickError("No valid user or role was given."));
                return;
            }
            if (isAdding) { //Adding
                if (member != 0) {
                    if (DJUsers.contains(member)) {
                        event.replyEmbeds(createQuickError("This member is already in the DJ list!"));
                    } else {
                        DJUsers.add(member);
                        event.replyEmbeds(createQuickEmbed("✅ **Success**", "Added " + suspectedUser.getAsMention() + " to the list of DJ members."));
                    }
                } else {
                    if (DJRoles.contains(role)) {
                        event.replyEmbeds(createQuickError("This role is already in the DJ list!"));
                    } else {
                        DJRoles.add(role);
                        event.replyEmbeds(createQuickEmbed("✅ **Success**", "Added " + suspectedRole.getAsMention() + " to the list of DJ roles."));
                    }
                }
            } else { //Removing
                if (member != 0) {
                    DJUsers.remove(member);
                    event.replyEmbeds(createQuickEmbed("✅ **Success**", "Removed " + suspectedUser.getAsMention() + " from the list of DJ members."));
                } else {
                    DJRoles.remove(role);
                    event.replyEmbeds(createQuickEmbed("✅ **Success**", "Removed " + suspectedRole.getAsMention() + " from the list of DJ roles."));
                }
            }
        } else {
            event.replyEmbeds(createQuickError("Invalid arguments."));
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
    public String getOptions() {
        return "";
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
                )
        );
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}