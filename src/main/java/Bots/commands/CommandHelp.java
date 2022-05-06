package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.commands;

public class CommandHelp extends BaseCommand {
    String description = "";
    List<String> generalCommands = new ArrayList<>();
    String finalGeneralCommands = "";
    List<String> musicCommands = new ArrayList<>();
    String finalMusicCommands = "";
    List<String> DJCommands = new ArrayList<>();
    String finalDJCommands = "";
    List<String> adminCommands = new ArrayList<>();
    String finalAdminCommands = "";
    List<String> devCommands = new ArrayList<>();
    String finalDevCommands = "";
    List<String> generalDescription = new ArrayList<>();
    List<String> musicDescription = new ArrayList<>();
    List<String> DJDescription = new ArrayList<>();
    List<String> adminDescription = new ArrayList<>();
    List<String> devDescription = new ArrayList<>();

    public void execute(MessageReceivedEvent event) {
        generalCommands.clear();
        musicCommands.clear();
        DJCommands.clear();
        adminCommands.clear();
        devCommands.clear();
        for (BaseCommand Command : commands) {
            if (Command.getCategory().equals("General")) {
                generalCommands.add(Command.getName());
                generalDescription.add(Command.getDescription());
            }
            if (Command.getCategory().equals("Music")) {
                musicCommands.add(Command.getName());
                musicDescription.add(Command.getDescription());
            }
            if (Command.getCategory().equals("DJ")) {
                DJCommands.add(Command.getName());
                DJDescription.add(Command.getDescription());
            }
            if (Command.getCategory().equals("Admin")) {
                adminCommands.add(Command.getName());
                adminDescription.add(Command.getDescription());
            }
            if (Command.getCategory().equals("Dev")) {
                devCommands.add(Command.getName());
                devDescription.add(Command.getDescription());
            }
        }

        finalGeneralCommands = "`" + String.join("` `", generalCommands) + "`";
        finalMusicCommands = "`" + String.join("` `", musicCommands) + "`";
        finalDJCommands = "`" + String.join("` `", DJCommands) + "`";
        finalAdminCommands = "`" + String.join("` `", adminCommands) + "`";
        finalDevCommands = "`" + String.join("` `", devCommands) + "`";

        EmbedBuilder eb = new EmbedBuilder();
        if (event.getMessage().getContentRaw().toLowerCase().contains("general")) {
            eb.setTitle("📖 `General`");
            for (BaseCommand command : commands) {
                if (Objects.equals(command.getCategory(), "General")) {
                    description = ("`" + command.getName() + command.getDescription() + "\n");
                }
            }
            eb.setDescription(description);
            eb.setColor(new Color(0, 0, 255));
            event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
            return;
        } else if (event.getMessage().getContentRaw().toLowerCase().contains("music")) {
            eb.setTitle("\uD83D\uDD0A `Music`");
            for (BaseCommand command : commands) {
                if (Objects.equals(command.getCategory(), "Music")) {
                    description = ("`" + command.getName() + command.getDescription() + "\n");
                }
            }
            eb.setDescription("`" + musicCommands.listIterator().next() + musicDescription.listIterator().next());
            eb.setColor(new Color(0, 0, 255));
            event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
            return;
        } else if (event.getMessage().getContentRaw().toLowerCase().contains("dj")) {
            eb.setTitle("\uD83C\uDFA7 `DJ`");
            for (BaseCommand command : commands) {
                if (Objects.equals(command.getCategory(), "DJ")) {
                    description = ("`" + command.getName() + command.getDescription() + "\n");
                }
            }
            eb.setDescription("`" + DJCommands.listIterator().next() + DJDescription.listIterator().next());
            eb.setColor(new Color(0, 0, 255));
            event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
            return;
        } else if (event.getMessage().getContentRaw().toLowerCase().contains("admin")) {
            eb.setTitle("\uD83D\uDCD1 `Admin`");
            for (BaseCommand command : commands) {
                if (Objects.equals(command.getCategory(), "Admin")) {
                    description = ("`" + command.getName() + command.getDescription() + "\n");
                }
            }
            eb.setDescription("`" + adminCommands.listIterator().next() + adminDescription.listIterator().next());
            eb.setColor(new Color(0, 0, 255));
            event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
            return;
        } else if (event.getMessage().getContentRaw().toLowerCase().contains("dev")) {
            if (Objects.requireNonNull(event.getMember()).getId().equals("211789389401948160") || Objects.requireNonNull(event.getMember()).getId().equals("260016427900076033")) {
                eb.setTitle("\uD83D\uDD27 `Dev`");
                for (BaseCommand command : commands) {
                    if (Objects.equals(command.getCategory(), "Dev")) {
                        description = ("`" + command.getName() + command.getDescription() + "\n");
                    }
                }
                eb.setDescription("`" + devCommands.listIterator().next() + devDescription.listIterator().next());
                eb.setColor(new Color(0, 0, 255));
                event.getTextChannel().sendMessageEmbeds(eb.build()).queue();
                return;
            }
        }
        eb.clear();
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("\uD83D\uDCD4 **Commands**");
        embed.setDescription("Click the buttons to get more information on a group.\n\n**General**\n" + finalGeneralCommands + "\n\n**Music**\n" + finalMusicCommands + "\n\n**DJ**\n" + finalDJCommands + "\n\n**Admin**\n" + finalAdminCommands); // buttons will be added later
        embed.setColor(new Color(0, 0, 255));
        event.getTextChannel().sendMessageEmbeds(embed.build()).queue(a -> {
            a.editMessageComponents().setActionRow(net.dv8tion.jda.api.interactions.components.buttons.Button.secondary("general", "◄️"), Button.secondary("queueForward", "►️")).queue();
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
        return "<Category>` - Shows you a list of commands, accepts arguments for the lazy people.";
    }
}
