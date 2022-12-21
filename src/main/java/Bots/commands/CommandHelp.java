package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Arrays;

import static Bots.Main.botColour;
import static Bots.Main.commands;

public class CommandHelp implements BaseCommand {
    String Arg = "";

    public String getCommands(String category) {
        String Commands = "";
        for (BaseCommand Command : commands) {
            if (Command.getCategory().equals(category)) {
                Commands = (Commands + " `" + Command.getNames()[0] + "`");
            }
        }
        if (Commands.equals("")) {
            return "none";
        } else {
            return Commands;
        }
    }

    @Override
    public void execute(MessageEvent event) {
        int i = 0;
        try {
            Arg = event.getArgs()[1].toLowerCase();
        } catch (Exception ignored) {
            Arg = "";
        }
        EmbedBuilder embed = new EmbedBuilder();
        StringBuilder builder = new StringBuilder();
        embed.setColor(botColour);
        embed.setFooter("Syntax: \"<>\" is a required argument, \"[]\" is an optional argument. \"()\" is an alternate word for the command.");
        for (BaseCommand Command : commands) {
            if (Command.getCategory().toLowerCase().equals(Arg)) {
                i++;
                builder = new StringBuilder();
                if (Command.getNames().length == 2) {
                    builder.append("\n`Alias:` ");
                } else if (Command.getNames().length > 2) {
                    builder.append("\n`Aliases:` ");
                }
                int j = 0;
                for (String name : Command.getNames()) {
                    j++;
                    if (Command.getNames().length == 1 || j == 1) {
                        continue;
                    }
                    if (j != Command.getNames().length) {
                        builder.append("**(").append(name).append(")**, ");
                    } else {
                        builder.append("**(").append(name).append(")**");
                    }
                }
                embed.appendDescription("`" + i + ")` **" + Command.getNames()[0] + " " + Arrays.toString(Command.getOptions()) + "** - " + Command.getDescription() + builder + "\n\n");
            }
        }
        if ("general".equals(Arg)) {
            embed.setTitle("\uD83D\uDCD6 **General**");
        } else if ("music".equals(Arg)) {
            embed.setTitle("\uD83D\uDD0A **Music**");
        } else if ("dj".equals(Arg)) {
            embed.setTitle("\uD83C\uDFA7 **DJ**");
        } else if ("admin".equals(Arg)) {
            embed.setTitle("\uD83D\uDCD1 **Admin**");
        } else if ("dev".equals(Arg)) {
            embed.setTitle("\uD83D\uDD28 **Dev**");
        } else {
            embed.setTitle("\uD83D\uDCD4 **Commands**");
            embed.setDescription("");
            embed.appendDescription("**General**\n" + getCommands("General") + "\n\n");
            embed.appendDescription("**Music**\n" + getCommands("Music") + "\n\n");
            embed.appendDescription("**DJ**\n" + getCommands("DJ") + "\n\n");
            embed.appendDescription("**Admin**\n" + getCommands("Admin"));
            embed.setFooter("Click the buttons to get more information on a group.");
        }
        event.getChannel().asTextChannel().sendMessageEmbeds(embed.build()).queue(a -> a.editMessageComponents().setActionRow(Button.secondary("general", "General"), Button.secondary("music", "Music"), Button.secondary("DJ", "DJ"), Button.secondary("admin", "Admin️")).queue());
        embed.clear();
    }

    @Override
    public String getCategory() {
        return "General";
    }

    @Override
    public String[] getNames() {
        return new String[]{"help"};
    }

    @Override
    public String getDescription() {
        return "Shows you a list of commands.";
    }

    @Override
    public OptionData[] getOptions() {
        return new OptionData[]{
                new OptionData(OptionType.STRING, "category", "Subcategory of commands to get information on")
        };
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}
