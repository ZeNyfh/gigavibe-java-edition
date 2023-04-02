package Bots.commands;

import Bots.BaseCommand;
import Bots.Main;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import org.json.simple.JSONObject;
import static java.util.Collections.reverseOrder;

import java.util.*;
import java.util.Map.Entry;



import java.io.IOException;
import java.util.stream.Collectors;

import static Bots.Main.*;

public class CommandSendUsage extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        Long[] values = (Long[]) commandUsageTracker.values().toArray(new Long[0]);
        Arrays.sort(values);
        HashMap<Long, List<String>> InverseReference = new HashMap<>();
        for (Object name : commandUsageTracker.keySet()) {
            Object value = commandUsageTracker.get(name);
            InverseReference.putIfAbsent((Long) value, new ArrayList<>());
            InverseReference.get((Long) value).add((String) name);
        }
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        eb.setTitle("**Usage Logs**");
        eb.appendDescription("```js\n");
        for (int i = values.length-1; i >= 0; i--) {
            String reference = InverseReference.get(values[i]).remove(0);
            eb.appendDescription(reference + ": " + values[i] + "\n");
        }
        eb.appendDescription("```");
        event.replyEmbeds(eb.build());
    }

    @Override
    public String[] getNames() {
        return new String[]{"sendusage","usage","sendusagelog","usagelog"};
    }

    @Override
    public String getCategory() {
        return "Dev";
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public String getDescription() {
        return "Sends the usage log";
    }

    @Override
    public long getRatelimit() {
        return 10000;
    }
}
