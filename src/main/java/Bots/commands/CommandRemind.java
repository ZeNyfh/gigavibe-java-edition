package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.ConfigManager.GetConfig;
import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

public class CommandRemind extends BaseCommand {
    public static long processReminderTime(String timeText) {
        long output = 0L;
        HashMap<String, Long> UnitToSeconds = new HashMap<>();
        UnitToSeconds.put("second", 1L);
        UnitToSeconds.put("minute", 60L);
        UnitToSeconds.put("hour", 3600L);
        UnitToSeconds.put("day", 86400L);
        UnitToSeconds.put("week", 604800L);
        UnitToSeconds.put("year", 31536000L); //why
        for (String key : UnitToSeconds.keySet().toArray(new String[0])) { //Alternative forms
            UnitToSeconds.put(key.substring(0, 1), UnitToSeconds.get(key));
            UnitToSeconds.put(key + "s", UnitToSeconds.get(key));
        }
        String storage = "";
        for (String term : timeText.strip().split(" ")) {
            if (storage.equals("")) { //No stored term
                if (term.matches("^\\d+$")) { //Unit is in the next term
                    storage = term;
                } else {
                    Matcher matcher = Pattern.compile("^\\d+").matcher(term);
                    matcher.find(); //This should never be false if the input was ran through timestampMatcher
                    String multiplier = matcher.group();
                    term = term.substring(multiplier.length()); //Expert-tier string manipulation
                    output += UnitToSeconds.get(term) * Integer.parseInt(multiplier);
                }
            } else {
                //Multiplier is the stored term, Unit is the current term
                output += UnitToSeconds.get(term) * Integer.parseInt(storage);
                storage = "";
            }
        }
        return output * 1000; //Milliseconds
    }

    @Override
    public void execute(MessageEvent event) {
        JSONObject reminders = GetConfig("reminders");
        StringBuilder builder = new StringBuilder();
        String[] args = event.getArgs();

        if (args.length == 1) {
            event.replyEmbeds(createQuickError("No arguments were given."));
            return;
        }

        // list
        try {
            if (args[1].equalsIgnoreCase("list")) {
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
                if (i == 0) {
                    builder.append("You have no reminders.");
                }
                event.replyEmbeds(createQuickEmbed("**Reminders**", String.valueOf(builder)));
                return;
            }
        } catch (Exception ignored) {
        }
        Long timeNow = System.currentTimeMillis();

        // remove
        if (args[1].equalsIgnoreCase("remove")) {
            ArrayList<String[]> finalReminders = new ArrayList<>();
            ArrayList<Object> toRemove = new ArrayList<>();
            for (Object reminder : reminders.keySet()) {
                String[] userReminders = reminders.get(reminder).toString().substring(0, reminders.get(reminder).toString().length() - 1).substring(1).replaceAll("\"", "").split(",");
                if (!Objects.equals(userReminders[2], event.getMember().getId())) {
                    continue;
                }
                if (args.length <= 2 || !args[2].matches("^\\d+$")) {
                    event.replyEmbeds(createQuickError("Invalid Arguments, was the index correct?"));
                    return;
                }
                finalReminders.add(userReminders);
                toRemove.add(reminder);
            }
            int wantedIndex = Integer.parseInt(args[2]);
            if (wantedIndex > finalReminders.size() || wantedIndex <= 0) {
                event.replyEmbeds(createQuickError("Invalid Arguments, the index was invalid."));
                return;
            }
            String[] finalReminder = finalReminders.get(wantedIndex - 1);
            builder = new StringBuilder();
            long reminderTime = Long.parseLong(finalReminder[0]) / 1000;
            builder.append("<t:").append(reminderTime).append(":f>");
            if (finalReminder.length >= 4) {
                builder.append(" | ").append(finalReminder[3]);
            }
            event.replyEmbeds(createQuickEmbed("**Removed reminder number " + wantedIndex + "**", String.valueOf(builder)));
            finalReminders.remove(wantedIndex - 1);
            reminders.remove(toRemove.get(wantedIndex - 1));
            for (String[] oldReminder : finalReminders) {
                reminders.put(timeNow, oldReminder);
            }
            return;
        }

        if (args[1].equalsIgnoreCase("add")) {
            JSONArray finalArrayList = new JSONArray();
            String timeLength;
            String reminderText = "";
            String timestampMatcher = "(?:\\d+ ?(?:years?|weeks?|days?|hours?|minutes?|seconds?|[ywdhms])\\s*)+";
            if (event.isSlash()) { //Avoid parsing when we have an easy approach
                timeLength = event.getOptions()[0].getAsString();
                if (!timeLength.matches("^\\s*" + timestampMatcher + "\\s*$")) {
                    event.replyEmbeds(createQuickError("Invalid timestamp duration given"));
                    return;
                }
                if (event.getOptions().length > 1)
                    reminderText = event.getOptions()[1].getAsString();
            } else {
                String rawContent = event.getContentRaw();
                Pattern pattern = Pattern.compile("^[^\\w]+reminder add (" + timestampMatcher + ")");
                Matcher matcher = pattern.matcher(rawContent);
                if (matcher.find()) {
                    timeLength = matcher.group(1);
                    reminderText = rawContent.substring(matcher.end());
                } else {
                    event.replyEmbeds(createQuickError("Invalid arguments, argument <duration> is an integer followed by years, weeks, days, hours, minutes, and seconds, but allows for short-form as well (1d 2h)"));
                    return;
                }
            }
            long reminderTime = timeNow + processReminderTime(timeLength);
            finalArrayList.add(String.valueOf(reminderTime));
            finalArrayList.add(event.getChannel().getId());
            finalArrayList.add(event.getMember().getUser().getId());
            finalArrayList.add(reminderText);
            reminders.put(timeNow, finalArrayList); // timeNow, {unixMillisRemind, channelID, userID, [reminderMessage]}
            event.replyEmbeds(createQuickEmbed("**I will remind you!**", "You will be reminded on <t:" + reminderTime / 1000 + ":f>"));
            return;
        }
        event.replyEmbeds(createQuickError("Invalid arguments. Argument <duration> is an integer followed by years, weeks, days, hours, minutes, and seconds, but allows for short-form as well (1d 2h)"));
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
        return "Reminds the user after a duration with an optional message";
    }

    @Override
    public String getOptions() {
        return "add <Duration> [Message] | remove <Index> | list\n";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addSubcommands(
                new SubcommandData("add", "Adds a reminder.").addOptions(
                        new OptionData(OptionType.STRING, "timestamp", "(To set)", true),
                        new OptionData(OptionType.STRING, "message", "The message that you would like to be included in the reminder.", false)
                ),
                new SubcommandData("remove", "removes a reminder based on list index.").addOptions(
                        new OptionData(OptionType.INTEGER, "index", "Use \"/reminder list\" to see indexes.")
                ),
                new SubcommandData("list", "Lists your reminders.")
        );
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
