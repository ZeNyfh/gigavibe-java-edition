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
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static Bots.ConfigManager.GetConfig;
import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

/* Reminder data array structure
Index 0: reminderTime - The time at which the post should be sent
Index 1: channelID - The ID of the channel the reminder should be posted in
Index 2: userID - The ID of the user who made the reminder
Index 3: reminderText - The text of the reminder. If none was provided, this will be an empty string
*/

public class CommandRemind extends BaseCommand {
    public static long processReminderTime(String timeText) {
        HashMap<String, Long> UnitToSeconds = new HashMap<>();
        UnitToSeconds.put("second", 1L);
        UnitToSeconds.put("minute", 60L);
        UnitToSeconds.put("hour", 3600L);
        UnitToSeconds.put("day", 86400L);
        UnitToSeconds.put("week", 604800L);
        UnitToSeconds.put("year", 31536000L); //why
        for (String key : UnitToSeconds.keySet().toArray(new String[0])) { //Alternative forms
            UnitToSeconds.put(key.substring(0, 1), UnitToSeconds.get(key)); //First character
            UnitToSeconds.put(key + "s", UnitToSeconds.get(key)); //Plural
        }
        Pattern pattern = Pattern.compile("(\\d+) ?([^\\d ]+)");
        Matcher segments = pattern.matcher(timeText.strip());
        long output = 0L;
        for (MatchResult match : segments.results().toList()) {
            String multiplier = match.group(1);
            String term = match.group(2);
            //printlnTime("Term",term,"multiplier",multiplier);
            output += UnitToSeconds.get(term) * Integer.parseInt(multiplier);
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

        long timeNow = System.currentTimeMillis();
        // list
        if (args[1].equalsIgnoreCase("list")) {
            int i = 0;
            for (Object reminder : reminders.keySet()) {
                JSONArray reminderData = (JSONArray) reminders.get(reminder);
                if (!event.getMember().getId().equals(reminderData.get(2))) {
                    continue;
                }
                i++;
                builder.append("**").append(i).append(":** <t:").append(Long.parseLong((String) reminderData.get(0)) / 1000).append(":f>");
                if (reminderData.size() >= 4) {
                    builder.append(" | ").append(reminderData.get(3));
                }
                builder.append("\n");
            }
            if (i == 0) {
                builder.append("You have no reminders.");
            }
            event.replyEmbeds(createQuickEmbed("**Reminders**", String.valueOf(builder)));
        } else if (args[1].equalsIgnoreCase("remove")) { //remove
            if (args.length <= 2 || !args[2].matches("^\\d+$")) {
                event.replyEmbeds(createQuickError("Invalid Arguments, was the index correct?"));
                return;
            }
            ArrayList<JSONArray> userReminders = new ArrayList<>();
            ArrayList<Object> indexReference = new ArrayList<>();
            for (Object reminder : reminders.keySet()) {
                JSONArray reminderData = (JSONArray) reminders.get(reminder);
                if (!event.getMember().getId().equals(reminderData.get(2))) {
                    continue;
                }
                userReminders.add(reminderData);
                indexReference.add(reminder);
            }
            int wantedIndex = Integer.parseInt(args[2]);
            if (wantedIndex > userReminders.size() || wantedIndex <= 0) {
                event.replyEmbeds(createQuickError("The index provided doesn't exist."));
                return;
            }
            JSONArray unwantedReminder = userReminders.get(wantedIndex - 1);
            builder = new StringBuilder();
            long reminderTime = Long.parseLong((String) unwantedReminder.get(0)) / 1000;
            builder.append("<t:").append(reminderTime).append(":f>");
            if (unwantedReminder.size() >= 4) {
                builder.append(" | ").append(unwantedReminder.get(3));
            }
            event.replyEmbeds(createQuickEmbed("**Removed reminder number " + wantedIndex + "**", String.valueOf(builder)));
            reminders.remove(indexReference.get(wantedIndex - 1));
        } else if (args[1].equalsIgnoreCase("add")) { //add
            String timeLength;
            String reminderText = "";
            String timestampMatcher = "(?:\\d+ ?(?:(?:year|week|day|hour|minute|second)s?|[ywdhms])\\s*)+";
            if (event.isSlash()) { //Avoid parsing when we have an easy approach
                timeLength = event.getOptions()[0].getAsString();
                if (!timeLength.matches("^\\s*" + timestampMatcher + "\\s*$")) {
                    event.replyEmbeds(createQuickError("Invalid duration given. The duration must be integers followed by either years, weeks, days, hours, minutes, seconds, or their short-hand equivalent, such as '1day 5h'"));
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
                    event.replyEmbeds(createQuickError("No duration found. The duration must be integers followed by either years, weeks, days, hours, minutes, seconds, or their short-hand equivalent, such as '1day 5h'"));
                    return;
                }
            }
            long reminderTime;
            try {
                reminderTime = timeNow + processReminderTime(timeLength);
            } catch (Exception e) {
                event.replyEmbeds(createQuickError("Unable to parse the duration '" + timeLength + "'"));
                e.printStackTrace();
                return;
            }
            JSONArray finalArrayList = new JSONArray();
            finalArrayList.add(String.valueOf(reminderTime));
            finalArrayList.add(event.getChannel().getId());
            finalArrayList.add(event.getMember().getUser().getId());
            finalArrayList.add(reminderText);
            reminders.put(timeNow, finalArrayList); // timeNow, {unixMillisRemind, channelID, userID[, reminderMessage]}
            event.replyEmbeds(createQuickEmbed("**I will remind you!**", "You will be reminded on <t:" + reminderTime / 1000 + ":f>"));
        } else {
            event.replyEmbeds(createQuickError("Invalid arguments"));
        }
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
