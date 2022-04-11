package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;

import static Bots.Main.createQuickEmbed;

public class CommandBoosterDJ implements ICommand {
    public static Boolean boosterDJ = false;

    @Override
    public void execute(ExecuteArgs event) {
        //NOTE: Guild specific behaviour is gonna be needed for this at a later date -9382
        boosterDJ = !boosterDJ;
        if (boosterDJ) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ \uD83C\uDFA7", "Boosters now have DJ permissions.")).queue();
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ \uD83C\uDFA7", "Boosters no longer have DJ permissions.")).queue();
        }
    }

    @Override
    public String getName() {
        return "boosterdj";
    }

    @Override
    public String helpMessage() {
        return "Makes it so boosters have DJ permissions.";
    }

    @Override
    public boolean needOwner() {
        return false;
    }
}
