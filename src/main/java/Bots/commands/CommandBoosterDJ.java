package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import static Bots.Main.createQuickEmbed;

public class CommandBoosterDJ implements BaseCommand {
    public static Boolean boosterDJ = false;

    public void execute(MessageReceivedEvent event) {
        //NOTE: Guild specific behaviour is gonna be needed for this at a later date -9382
        boosterDJ = !boosterDJ;
        if (boosterDJ) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ \uD83C\uDFA7", "Boosters now have DJ permissions.")).queue();
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ \uD83C\uDFA7", "Boosters no longer have DJ permissions.")).queue();
        }
    }

    public String getCategory() {
        return "Admin";
    }

    public String getName() {
        return "boosterdj";
    }

    public String getDescription() {
        return "Makes it so boosters have DJ permissions.";
    }
}
