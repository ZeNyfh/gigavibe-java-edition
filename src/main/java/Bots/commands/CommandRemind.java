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
        if (event.getArgs()[1].equalsIgnoreCase("list")) {
            for (Object reminder : reminders.keySet()) {
                String[] finalReminders = reminders.get(reminder).toString().substring(0,reminders.get(reminder).toString().length()-1).substring(1).replaceAll("\"","").split(",");
                builder.append("<t:").append(Long.parseLong(finalReminders[0])/1000).append(":f>");
                if (finalReminders.length == 4){
                    builder.append(" | ").append(finalReminders[3]);
                }
                builder.append("\n");
            }
            event.replyEmbeds(createQuickEmbed("**Reminders**", String.valueOf(builder)));
            return;
        }
        Long timeNow = System.currentTimeMillis();
        Long reminderTime = timeNow;
        JSONArray finalArrayList = new JSONArray();
        if (event.getArgs().length <= 1) {
            event.replyEmbeds(createQuickError("Invalid Arguments."));
            return;
        }
        List<Integer> intValues = new ArrayList<>();
        List<String> values;
        if (event.getArgs()[1].contains(":")) {
            values = List.of(event.getArgs()[1].split(":"));
        } else {
            values = List.of(event.getArgs()[1]);
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
        if (event.getArgs().length > 2) {
            builder = new StringBuilder();
            int i = 0;
            for (String arg : event.getArgs()) {
                if (i == event.getArgs().length - 1) {
                    builder.append(arg);
                    break;
                }
                if (i > 1) {
                    builder.append(arg).append(" ");
                }
                i++;
            }
            finalArrayList.add(String.valueOf(builder));
        }
        reminders.put(timeNow, finalArrayList); // timeNow, {unixMillisRemind, channelID, userID, [reminderMessage]}
        event.replyEmbeds(createQuickEmbed("**I will remind you!**", "You will be reminded on <t:" + reminderTime / 1000 + ":f>"));
    }

    @Override
    public String[] getNames() {
        return new String[]{"remind", "reminder", "r", "alarm", "timer", "schedule"};
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
        return "[DD:][HH:][MM:]<SS> [MESSAGE]";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addSubcommands(
                new SubcommandData("add", "Adds a reminder.").addOptions(
                        new OptionData(OptionType.STRING, "timestamp", "[DD:][HH:][MM:]<SS> (brackets should be omitted)", true),
                        new OptionData(OptionType.STRING, "message", "The message that you would like to be included in the reminder.", false)
                ),
                new SubcommandData("list", "Lists your reminders.").addOptions()
        );
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
