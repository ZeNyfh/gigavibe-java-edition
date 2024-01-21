package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.ItemComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static Bots.Main.*;

public class CommandHelp extends BaseCommand {
    List<ItemComponent> CategoryButtons = new ArrayList<>();

    public String getCommands(Category category) {
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

    private void BuildEmbedFromCategory(EmbedBuilder embed, String category) {
        int i = 0;
        for (BaseCommand Command : commands) {
            if (Command.getCategory().name().equalsIgnoreCase(category)) {
                i++;
                StringBuilder aliases = new StringBuilder();
                if (Command.getNames().length == 2) {
                    aliases.append("\n`Alias:` ");
                } else if (Command.getNames().length > 2) {
                    aliases.append("\n`Aliases:` ");
                }
                int j = 0;
                for (String name : Command.getNames()) {
                    j++;
                    if (j == 1) {
                        continue;
                    }
                    if (j != Command.getNames().length) {
                        aliases.append("**(").append(name).append(")**, ");
                    } else {
                        aliases.append("**(").append(name).append(")**");
                    }
                }
                embed.appendDescription("`" + i + ")` **" + Command.getNames()[0] + " " + Command.getOptions() + "** - " + Command.getDescription() + aliases + "\n\n");
            }
        }
    }

    private void HandleButtonEvent(ButtonInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        eb.setFooter("Syntax: \"<>\" is a required argument, \"[]\" is an optional argument. \"()\" is an alternate word for the command.");
        String ButtonCategory = Objects.requireNonNull(event.getButton().getId()).split("help-")[1];
        BuildEmbedFromCategory(eb, ButtonCategory);
        switch (ButtonCategory) {
            case "general" -> eb.setTitle("\uD83D\uDCD6 **General**");
            case "music" -> eb.setTitle("\uD83D\uDD0A **Music**");
            case "dj" -> eb.setTitle("\uD83C\uDFA7 **DJ**");
            case "admin" -> eb.setTitle("\uD83D\uDCD1 **Admin**");
        }
        event.getInteraction().editMessageEmbeds().setEmbeds(eb.build()).queue();
    }

    @Override
    public void Init() {
        String[] ExpectedButtonIDs = new String[Category.values().length];
        for (int i = 0; i < Category.values().length; i++) {
            Category category = Category.values()[i];
            ExpectedButtonIDs[i] = "help-" + category.name().toLowerCase();
            if (category != Category.Dev)
                CategoryButtons.add(Button.secondary("help-"+category.name().toLowerCase(), category.name()));
        }
        registerButtonInteraction(ExpectedButtonIDs, this::HandleButtonEvent);
    }

    @Override
    public void execute(MessageEvent event) {
        String Arg;
        try {
            Arg = event.getArgs()[1].toLowerCase();
        } catch (Exception ignored) {
            Arg = "";
        }
        EmbedBuilder embed = new EmbedBuilder();
        embed.setColor(botColour);
        embed.setFooter("Syntax: \"<>\" is a required argument, \"[]\" is an optional argument. \"()\" is an alternate word for the command.");
        BuildEmbedFromCategory(embed, Arg);
        switch (Arg) {
            case "general" -> embed.setTitle("\uD83D\uDCD6 **General**");
            case "music" -> embed.setTitle("\uD83D\uDD0A **Music**");
            case "dj" -> embed.setTitle("\uD83C\uDFA7 **DJ**");
            case "admin" -> embed.setTitle("\uD83D\uDCD1 **Admin**");
            case "dev" -> embed.setTitle("\uD83D\uDD28 **Dev**");
            default -> {
                embed.setTitle("\uD83D\uDCD4 **Commands**");
                embed.setDescription("");
                for (Category category : Category.values()) {
                    if (category != Category.Dev)
                        embed.appendDescription("**" + category.name() + "**\n" + getCommands(category) + "\n\n");
                }
                embed.setFooter("Click the buttons to get more information on a group.");
            }
        }
        if (!event.isSlash()) { //Incredibly hacky fix because I don't want to implement all the backend just for this
            ((MessageReceivedEvent) event.getCoreEvent()).getMessage().replyEmbeds(embed.build()).queue(
                    a -> a.editMessageComponents().setActionRow(CategoryButtons).queue()
            );
        } else {
            ((SlashCommandInteractionEvent) event.getCoreEvent()).replyEmbeds(embed.build()).queue(
                    a -> a.editOriginalComponents().setActionRow(CategoryButtons).queue()
            );
        }
    }

    @Override
    public Category getCategory() {
        return Category.General;
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
