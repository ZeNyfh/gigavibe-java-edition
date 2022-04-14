package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import static Bots.Main.commands;

public class CommandHelp implements BaseCommand {
    public void execute(MessageReceivedEvent event) {
        List<String> generalCommands = new ArrayList<>();
        String finalGeneralCommands = "";
        List<String> musicCommands = new ArrayList<>();
        String finalMusicCommands = "";
        List<String> DJCommands = new ArrayList<>();
        String finalDJCommands = "";
        List<String> adminCommands = new ArrayList<>();
        String finalAdminCommands = "";
        for (BaseCommand Command : commands) {
            if (Command.getCategory().equals("General")) {
                generalCommands.add(Command.getName());
            }
            if (Command.getCategory().equals("Music")) {
                musicCommands.add(Command.getName());
            }
            if (Command.getCategory().equals("DJ")) {
                DJCommands.add(Command.getName());
            }
            if (Command.getCategory().equals("Admin")) {
                adminCommands.add(Command.getName());
            }
        }
        finalGeneralCommands = "`" + String.join("` `", generalCommands) + "`";
        finalMusicCommands = "`" + String.join("` `", musicCommands) + "`";
        finalDJCommands = "`" + String.join("` `", DJCommands) + "`";
        if (adminCommands.isEmpty()) { // temporary check until an admin command gets added
            finalAdminCommands = "~~`none`~~";
        } else {
            finalAdminCommands = "`" + String.join("` `", adminCommands) + "`";
        }
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("\uD83D\uDCD4 **Commands**");
        embed.setDescription("Click the buttons to get more information on a group.\n\n**General**\n" + finalGeneralCommands + "\n\n**Music**\n" + finalMusicCommands + "\n\n**DJ**\n" + finalDJCommands + "\n\n**Admin**\n" + finalAdminCommands); // buttons will be added later
        embed.setColor(new Color(0, 0, 255));
        event.getTextChannel().sendMessageEmbeds(embed.build()).queue();
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
}
