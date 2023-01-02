package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static Bots.ConfigManager.GetConfig;
import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

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
            if (value.matches("[^\\d.]")) {
                event.replyEmbeds(createQuickError("Invalid Arguments, was the time given correctly formatted? **`[DD:][HH:][MM:]<SS>`**"));
                return;
            }
            intValues.add(Integer.parseInt(value));
        }
        reminderTime = convertToMillis(reminderTime, intValues);
        finalArrayList.add(String.valueOf(reminderTime));
        finalArrayList.add(event.getChannel().asTextChannel().getId());
        finalArrayList.add(event.getMember().getUser().getId());
        if (event.getArgs().length > 2) {
            StringBuilder builder = new StringBuilder();
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
        return new String[]{"remind", "reminder", "r", "alarm" ,"timer", "schedule"};
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
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "timestamp", "[DD:][HH:][MM:]<SS> (brackets should be omitted)", true);
        slashCommand.addOption(OptionType.STRING, "message", "The message that you would like to be included in the reminder.", false);
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
