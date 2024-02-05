package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.entities.Guild;

import static Bots.Main.*;

public class CommandUpdateSlashCommands extends BaseCommand {
    @Override
    public void execute(MessageEvent event) throws Exception {
        // WARNING: THIS ACTUALLY REMOVES COMMANDS FROM GUILDS
        // SINCE WE ARE DEFINING THESE UNDER THE BOT NOW
        if (event.getUser().getIdLong() != 211789389401948160L && event.getUser().getIdLong() != 260016427900076033L) {
            event.replyEmbeds(createQuickError("You do not have the permissions for this."));
            return;
        }
        event.replyEmbeds(createQuickEmbed("WARNING", "Purging guild-defined commands..."));
        printlnTime("Starting slash command purging, expect red text in the console.");
        for (Guild guild : event.getJDA().getGuilds()) {
            guild.updateCommands().queue();
        }
        printlnTime("Finished updating slash commands");
    }

    @Override
    public String[] getNames() {
        return new String[]{"removeslash"};
    }

    @Override
    public Category getCategory() {
        return Category.Dev;
    }

    @Override
    public String getDescription() {
        return "Removes slash commands in all guilds (we are changing system, this is to be removed later)";
    }

    @Override
    public long getRatelimit() {
        return 100000;
    }
}
