package Bots.commands;

import ca.tristan.jdacommands.ExecuteArgs;
import ca.tristan.jdacommands.ICommand;
import org.json.simple.JSONArray;

import static Bots.Main.createQuickEmbed;

public class CommandBoosterDJ implements ICommand {
    public static Boolean boosterDJ = false;
    public static JSONArray DJList = new JSONArray(); // i could not get this to work

    @Override
    public void execute(ExecuteArgs event) {
        boosterDJ = !boosterDJ;
        if (boosterDJ){
              event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ \uD83C\uDFA7", "Boosters now have DJ permissions.")).queue();
        }  else {
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
