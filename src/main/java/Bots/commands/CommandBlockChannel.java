package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
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

public class CommandBlockChannel extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!Objects.requireNonNull(event.getMember()).hasPermission(Permission.MESSAGE_MANAGE)) {
            event.replyEmbeds(createQuickEmbed("❌ **Insufficient permissions**", "you do not have the permission to use this command."));
            return;
        }
        String[] args = event.getArgs();
        printlnTime(args, args.length);
        if (args.length == 1 || !args[1].equalsIgnoreCase("list")) {
            if (args.length < 3) {
                event.replyEmbeds(createQuickEmbed("❌ **Invalid arguments.**", "The valid usage is: `blockchannel <remove/add> <ChannelID>` or `blockchannel list`"));
                return;
            }
        }
        JSONObject config = event.getConfig();
        JSONArray blockedChannels = (JSONArray) config.get("BlockedChannels");

        if (args[1].equalsIgnoreCase("add") || args[1].equalsIgnoreCase("remove")) {
            String targetChannel = args[2];
            Pattern pattern = Pattern.compile("^<#(\\d+)>$"); //To support rawtext #channel additions (slash commands auto convert to just ID which is really nice)
            Matcher matcher = pattern.matcher(args[2]);
            if (matcher.matches()) {
                targetChannel = matcher.group(1);
            }
            for (int i = 0; i < event.getGuild().getChannels().size(); i++) {
                GuildChannel guildChannel = event.getGuild().getChannels().get(i);
                if (guildChannel.getId().equals(targetChannel)) {
                    if (args[1].equalsIgnoreCase("add")) {
                        if (blockedChannels.contains(guildChannel.getId())) {
                            event.replyEmbeds(createQuickError("This channel is already blocked."));
                            return;
                        }
                        blockedChannels.add(targetChannel);
                        event.replyEmbeds(createQuickEmbed(" ", "✅ Added " + guildChannel.getIdLong() + " aka \"" + guildChannel.getName() + "\" to the list."));
                    } else if (args[1].equalsIgnoreCase("remove")) {
                        if (!blockedChannels.contains(guildChannel.getId())) {
                            event.replyEmbeds(createQuickError("This channel is not blocked."));
                            return;
                        }
                        blockedChannels.remove(targetChannel);
                        event.replyEmbeds(createQuickEmbed(" ", "✅ Removed " + guildChannel.getIdLong() + " aka \"" + guildChannel.getName() + "\" from the list."));
                    }
                    return;
                }
            }
            event.replyEmbeds(createQuickError("This channel was not found in this discord server."));
        } else if (args[1].equalsIgnoreCase("list")) {
            EmbedBuilder eb = new EmbedBuilder();
            eb.setColor(botColour);
            eb.setTitle("Blocked channels for " + event.getGuild().getName() + ":");
            if (blockedChannels.size() == 0) {
                eb.appendDescription("**None.**");
                event.replyEmbeds(eb.build());
                return;
            }
            for (int j = 0; j < blockedChannels.size(); ) {
                eb.appendDescription("**" + blockedChannels.get(j) + "** - " + Objects.requireNonNull(event.getJDA().getTextChannelById((String) blockedChannels.get(j))).getName() + "\n");
                j++;
            }
            event.replyEmbeds(eb.build());
        } else {
            event.replyEmbeds(createQuickError("Invalid arguments.\n\nThe valid usage is: `blockchannel <remove/add> <ChannelID>` or `blockchannel list`"));
        }
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
    public String getOptions() {
        return "list | add OR remove <Channel>";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addSubcommands(
                new SubcommandData("list", "Lists the blocked channels"),
                new SubcommandData("add", "Add a channel to the blocklist").addOptions(
                        new OptionData(OptionType.CHANNEL, "channel-id", "The channel for the command", true)
                ),
                new SubcommandData("remove", "Remove a channel from the blocklist").addOptions(
                        new OptionData(OptionType.CHANNEL, "channel-id", "The channel for the command", true)
                )
        );
        //TODO: System can now handle sub-commands. This was roughly adjusted but the command itself needs to be able to handle the new options. -9382
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}
