package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static Bots.Main.*;

public class CommandSendUsage extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (event.getUser().getIdLong() != 211789389401948160L && event.getUser().getIdLong() != 260016427900076033L) {
            event.replyEmbeds(createQuickError("You do not have the permissions for this."));
            return;
        }
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
        for (int i = values.length - 1; i >= 0; i--) {
            String reference = InverseReference.get(values[i]).remove(0);
            eb.appendDescription(reference + ": " + values[i] + "\n");
        }
        eb.appendDescription("```");
        event.replyEmbeds(eb.build());
    }

    @Override
    public String[] getNames() {
        return new String[]{"sendusage", "usage", "sendusagelog", "usagelog"};
    }

    @Override
    public Category getCategory() {
        return Category.Dev;
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
