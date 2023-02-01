package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static Bots.ConfigManager.GetConfig;
import static Bots.Main.*;

public class CommandRemind extends BaseCommand {
    public static long convertToMillis(Long unixMillis, List<Integer> time) {
        if (time.size() >= 1) {
            unixMillis += time.get(time.size() - 1) * 1000;
        }
        if (time.size() >= 2) {
            unixMillis += time.get(time.size() - 2) * 60000;
        }
        if (time.size() >= 3) {
            unixMillis += time.get(time.size() - 3) * 3600000;
        }
        if (time.size() >= 4) {
            unixMillis += time.get(time.size() - 4) * 86400000;
        }
        return unixMillis;
    }

    @Override
    public void execute(MessageEvent event) {
        JSONObject reminders = GetConfig("reminders");
        StringBuilder builder = new StringBuilder();
        ArrayList<String> args;

        // custom args
        if (event.getArgs()[0].contains("/")) {
            args = new ArrayList<>(List.of(event.getArgs()[0].substring(1).split("/")));
        } else {
            args = new ArrayList<>();
            args.add(event.getArgs()[0]);
        }
        if (event.getArgs().length > 1) {
            int i = 0;
            for (String string : event.getArgs()) {
                if (i == 0){
                    i++;
                    continue;
                }
                args.add(string);
            }
        }
        if (args.size() == 1){
            event.replyEmbeds(createQuickError("No arguments were given."));
            return;
        }

        // list
        try {
            if (args.get(1).equalsIgnoreCase("list")) {
                int i = 0;
                for (Object reminder : reminders.keySet()) {
                    String[] finalReminders = reminders.get(reminder).toString().substring(0, reminders.get(reminder).toString().length() - 1).substring(1).replaceAll("\"", "").split(",");
                    if (!Objects.equals(finalReminders[2], event.getMember().getId())) {
                        continue;
                    }
                    i++;
                    builder.append("**").append(i).append(":** <t:").append(Long.parseLong(finalReminders[0]) / 1000).append(":f>");
                    if (finalReminders.length >= 4) {
                        builder.append(" | ").append(finalReminders[3]);
                    }
                    builder.append("\n");
                }
                if (i == 0){
                    builder.append("You have no reminders.");
                }
                event.replyEmbeds(createQuickEmbed("**Reminders**", String.valueOf(builder)));
                return;
            }
        } catch (Exception ignored){}
        Long timeNow = System.currentTimeMillis();

        // remove
        if (args.get(1).equalsIgnoreCase("remove")){
            ArrayList<String[]> finalReminders = new ArrayList<>();
            ArrayList<Object> toRemove = new ArrayList<>();
            for (Object reminder : reminders.keySet()) {
                String[] userReminders = reminders.get(reminder).toString().substring(0, reminders.get(reminder).toString().length() - 1).substring(1).replaceAll("\"", "").split(",");
                if (!Objects.equals(userReminders[2], event.getMember().getId())) {
                    continue;
                }
                if (args.size() <= 2 || !args.get(2).matches("^\\d+$")) {
                    event.replyEmbeds(createQuickError("Invalid Arguments, was the index correct?"));
                    return;
                }
                finalReminders.add(userReminders);
                toRemove.add(reminder);
            }
            if (Integer.parseInt(args.get(2)) > finalReminders.size() || Integer.parseInt(args.get(2)) <= 0){
                event.replyEmbeds(createQuickError("Invalid Arguments, the index was invalid."));
                return;
            }
            String[] finalReminder = finalReminders.get(Integer.parseInt(args.get(2)) - 1);
            builder = new StringBuilder();
            long reminderTime = Long.parseLong(finalReminder[0]) / 1000;
            builder.append("<t:").append(reminderTime).append(":f>");
            if (finalReminder.length >= 4){
                builder.append(" | ").append(finalReminder[3]);
            }
            event.replyEmbeds(createQuickEmbed("**Removed reminder number " + args.get(2) + "**", String.valueOf(builder)));
            finalReminders.remove(Integer.parseInt(args.get(2)) - 1);
            reminders.remove(toRemove.get(Integer.parseInt(args.get(2)) - 1));
            for (String[] oldReminder : finalReminders) {
                reminders.put(timeNow, oldReminder);
            }
            return;
        }

        Long reminderTime = timeNow;
        JSONArray finalArrayList = new JSONArray();
        if (args.get(1).equalsIgnoreCase("add")) {
            List<Integer> intValues = new ArrayList<>();
            List<String> values;
            if (args.get(2).contains(":")) {
                values = List.of(args.get(2).split(":"));
            } else {
                values = List.of(args.get(2));
            }
            for (String value : values) {
                if (!value.matches("^\\d+$")) {
                    event.replyEmbeds(createQuickError("Invalid Arguments, was the time given correctly formatted? **`[DD:][HH:][MM:]<SS>`**"));
                    return;
                } else {
                    intValues.add(Integer.parseInt(value));
                }
            }
            reminderTime = convertToMillis(reminderTime, intValues);
            finalArrayList.add(String.valueOf(reminderTime));
            finalArrayList.add(event.getChannel().asTextChannel().getId());
            finalArrayList.add(event.getMember().getUser().getId());
            builder = new StringBuilder();
            int i = 0;
            for (String arg : args) {
                printlnTime(arg);
                if (i == args.size() - 1) {
                    builder.append(arg);
                    break;
                }
                if (i > 2) {
                    builder.append(arg).append(" ");
                }
                i++;
            }
            finalArrayList.add(String.valueOf(builder));
            reminders.put(timeNow, finalArrayList); // timeNow, {unixMillisRemind, channelID, userID, [reminderMessage]}
            event.replyEmbeds(createQuickEmbed("**I will remind you!**", "You will be reminded on <t:" + reminderTime / 1000 + ":f>"));
            return;
        }
        event.replyEmbeds(createQuickError("Invalid arguments."));
    }

    @Override
    public String[] getNames() {
        return new String[]{"reminder", "remind", "r", "alarm", "timer", "schedule"};
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String getDescription() {
        return "Reminds the user with a message after X amount of time passes.";
    }

    @Override
    public String getOptions() {
        return "<add> [DD:][HH:][MM:]<SS> [MESSAGE] | <remove> <INDEX> | <list>";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addSubcommands(
                new SubcommandData("add", "Adds a reminder.").addOptions(
                        new OptionData(OptionType.STRING, "timestamp", "[DD:][HH:][MM:]<SS> (brackets should be omitted)", true),
                        new OptionData(OptionType.STRING, "message", "The message that you would like to be included in the reminder.", false)
                ),
                new SubcommandData("remove", "removes a reminder based on list index.").addOptions(
                        new OptionData(OptionType.INTEGER, "index","Use \"/reminder list\" to see indexes.")
                ),
                new SubcommandData("list", "Lists your reminders.")
        );
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
