package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import com.fasterxml.jackson.databind.ser.Serializers;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static Bots.Main.botPrefix;
import static Bots.Main.commands;

public class CommandHelp extends BaseCommand {
    String Arg = "";

    public String getCommands(String category) {
        String Commands = "";
        for (BaseCommand Command : commands) {
            if (Command.getCategory().equals(category)) {
                Commands = (Commands + " `" + Command.getName() + "`");
            }
        }
        if (Commands.equals("")) {
            return "none";
        } else {
            return Commands;
        }
    }

    public void execute(MessageEvent event) {
        int i = 0;
        try {
            Arg = Arrays.toString(event.getArgs()).substring(8).replace("]", "").toLowerCase();
        } catch (Exception ignored){Arg = "";}
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(new Color(0, 0, 255));
        embed.setFooter("Syntax: \"<>\" is a required argument, \"[]\" is an optional argument.");
        for (BaseCommand Command : commands) {
            if (Command.getCategory().toLowerCase().equals(Arg)){
                i++;
                embed.appendDescription("`"+ i + ")` **" + Command.getName() + " " + Command.getParams() + "** - " + Command.getDescription() + "\n");
            }
        }
        if ("general".equals(Arg)) {
            embed.setTitle("\uD83D\uDCD6 **General**");
        } else if ("music".equals(Arg)){
            embed.setTitle("\uD83D\uDD0A **Music**");
        } else if ("dj".equals(Arg)){
            embed.setTitle("\uD83C\uDFA7 **DJ**");
        } else if ("admin".equals(Arg)){
            embed.setTitle("\uD83D\uDCD1 **Admin**");
        } else if ("dev".equals(Arg)){
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
        event.getTextChannel().sendMessageEmbeds(embed.build()).queue(a -> {
            a.editMessageComponents().setActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.secondary("general", "General"), Button.secondary("music", "Music"), Button.secondary("DJ", "DJ"), Button.secondary("admin", "Admin️")).queue();
        });
        embed.clear();
    }

    public String getCategory() {
        return "General";
    }

    public String getName() {
        return "help";
    }

    public String getDescription() {
        return "Shows you a list of commands.";
    }

    public String getParams() { return "[Category]";}
}
