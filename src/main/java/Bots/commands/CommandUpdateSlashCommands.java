package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

import static Bots.Main.*;

public class CommandUpdateSlashCommands extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws Exception {
        event.replyEmbeds(createQuickEmbed("WARNING", "This command will take some time and *may* cause lag."));
        printlnTime("Starting slash command updating, expect red text in the console.");
        for (Guild guild : event.getJDA().getGuilds()) {
            List<CommandData> data = new ArrayList<>();
            for (BaseCommand Command : commands) {
                SlashCommandData slashCommand = Commands.slash(Command.getNames()[0], Command.getDescription());
                Command.ProvideOptions(slashCommand);
                Command.slashCommand = slashCommand;
                data.add(slashCommand);
            }
            guild.updateCommands().addCommands(data).queue();
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"updateslash"};
    }

    @Override
    public Category getCategory() {
        return Category.Dev;
    }

    @Override
    public String getDescription() {
        return "Updates slash commands in all guilds, this should be not done unless command options were changed.";
    }

    @Override
    public long getRatelimit() {
        return 100000;
    }
}
