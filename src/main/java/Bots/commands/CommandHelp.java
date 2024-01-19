package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.Objects;

import static Bots.Main.*;

public class CommandHelp extends BaseCommand {
    String Arg = "";

    public String getCommands(String category) {
        StringBuilder Commands = new StringBuilder();
        for (BaseCommand Command : commands) {
            if (Command.getCategory().equals(category)) {
                Commands.append(" `").append(Command.getNames()[0]).append("`");
            }
        }
        if (Commands.isEmpty()) {
            return "none";
        } else {
            return Commands.toString();
        }
    }

    private void HandleButtonEvent(ButtonInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        eb.setFooter("Syntax: \"<>\" is a required argument, \"[]\" is an optional argument. \"()\" is an alternate word for the command.");
        int i = 0;
        for (BaseCommand Command : commands) {
            if (Command.getCategory().equalsIgnoreCase(Objects.requireNonNull(event.getButton().getId()))) {
                i++;
                StringBuilder builder = new StringBuilder();
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
                eb.appendDescription("`" + i + ")` **" + Command.getNames()[0] + " " + Command.getOptions() + "** - " + Command.getDescription() + builder + "\n\n");
            }
        }
        switch (Objects.requireNonNull(event.getButton().getId())) {
            case "general":
                eb.setTitle("\uD83D\uDCD6 **General**");
            case "music":
                eb.setTitle("\uD83D\uDD0A **Music**");
            case "dj":
                eb.setTitle("\uD83C\uDFA7 **DJ**");
            case "admin":
                eb.setTitle("\uD83D\uDCD1 **Admin**");
        }
        event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
    }

    @Override
    public void Init() {
        registerButtonInteraction(new String[]{"general", "music", "dj", "admin"}, this::HandleButtonEvent);
    }

    @Override
    public void execute(MessageEvent event) {
        try {
            Arg = event.getArgs()[1].toLowerCase();
        } catch (Exception ignored) {
            Arg = "";
        }
        EmbedBuilder embed = new EmbedBuilder();
        StringBuilder builder;
        embed.setColor(botColour);
        embed.setFooter("Syntax: \"<>\" is a required argument, \"[]\" is an optional argument. \"()\" is an alternate word for the command.");
        int i = 0;
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
                embed.appendDescription("`" + i + ")` **" + Command.getNames()[0] + " " + Command.getOptions() + "** - " + Command.getDescription() + builder + "\n\n");
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
        if (!event.isSlash()) { //Incredibly hacky fix because I don't want to implement all the backend just for this
            ((MessageReceivedEvent) event.getCoreEvent()).getMessage().replyEmbeds(embed.build()).queue(
                    a -> a.editMessageComponents().setActionRow(
                            Button.secondary("general", "General"), Button.secondary("music", "Music"), Button.secondary("dj", "DJ"), Button.secondary("admin", "Admin")
                    ).queue()
            );
        } else {
            ((SlashCommandInteractionEvent) event.getCoreEvent()).replyEmbeds(embed.build()).queue(
                    a -> a.editOriginalComponents().setActionRow(
                            Button.secondary("general", "General"), Button.secondary("music", "Music"), Button.secondary("dj", "DJ"), Button.secondary("admin", "Admin")
                    ).queue()
            );
        }
        embed.clear();
    }

    @Override
    public String getCategory() {
        return Categories.General.name();
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
    public String getOptions() {
        return "[Category]";
    }

    @Override
    public void ProvideOptions(SlashCommandData slashCommand) {
        slashCommand.addOption(OptionType.STRING, "category", "Subcategory of commands to get information on", false);
    }

    @Override
    public long getRatelimit() {
        return 1000;
    }
}
