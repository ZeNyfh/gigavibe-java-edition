package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.GuildChannel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.Objects;

import static Bots.Main.*;

public class CommandBlockChannel extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws IOException {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Insufficient permissions**", "you do not have the permission to use this command.")).queue();
            return;
        }
        if (!event.getArgs()[1].equalsIgnoreCase("list")) {
            if (event.getArgs().length != 3) {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Invalid arguments.**", "The valid usage is: `blockchannel <remove/add> <ChannelID>` or `blockchannel list`")).queue();
                return;
            }
        }
        JSONObject config = event.getConfig();
        JSONArray blockedChannels = (JSONArray) config.get("BlockedChannels");

        for (int i = 0; i < event.getGuild().getChannels().size(); i++) {
            printlnTime(String.valueOf(i));
            GuildChannel guildChannel = event.getGuild().getChannels().get(i);
            if (event.getArgs()[1].equalsIgnoreCase("add")) {
                if (guildChannel.getId().equals(event.getArgs()[2])) {
                    if (blockedChannels.contains(guildChannel.getId())) {
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This channel is already blocked.")).queue();
                        return;
                    }
                    blockedChannels.add(event.getArgs()[2]);
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ added " + guildChannel.getIdLong() + " aka \"" + guildChannel.getName() + "\" to the list.")).queue();
                    return;
                }
            } else if (event.getArgs()[1].equalsIgnoreCase("remove")) {
                if (guildChannel.getId().equals(event.getArgs()[2])) {
                    if (!blockedChannels.contains(guildChannel.getId())) {
                        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This channel is not blocked.")).queue();
                        return;
                    }
                    blockedChannels.remove(event.getArgs()[2]);
                    event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed(" ", "✅ Removed " + guildChannel.getIdLong() + " aka \"" + guildChannel.getName() + "\" from the list.")).queue();
                    return;
                }
            } else if (event.getArgs()[1].equalsIgnoreCase("list")) {
                EmbedBuilder eb = new EmbedBuilder();
                eb.setColor(botColour);
                eb.setTitle("Blocked channels for " + event.getGuild().getName() + ":");
                if (blockedChannels.size() == 0) {
                    eb.appendDescription("**None.**");
                    event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();
                    return;
                }
                for (int j = 0; j < blockedChannels.size(); ) {
                    eb.appendDescription("**" + blockedChannels.get(j) + "** - " + Objects.requireNonNull(event.getJDA().getTextChannelById((String) blockedChannels.get(j))).getName() + "\n");
                    j++;
                }
                event.getChannel().asTextChannel().sendMessageEmbeds(eb.build()).queue();
                return;
            } else {
                event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("Invalid arguments.\n\nThe valid usage is: `blockchannel <remove/add> <ChannelID>`")).queue();
                return;
            }
        }
        event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("This channel was not found in this discord server.")).queue();
    }

    @Override
    public String[] getNames() {
        return new String[]{"blockchannel"};
    }

    @Override
    public String getCategory() {
        return "Admin";
    }

    @Override
    public String getDescription() {
        return "Disallows certain commands to be used in specified channel/s (eg: play)";
    }

    @Override
    public String getParams() {
        return "<list> || <add/remove> <channelID>";
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}
