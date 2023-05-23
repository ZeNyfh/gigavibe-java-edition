package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;

import static Bots.Main.printlnTime;

public class CommandDevTests extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        printlnTime("Its dev time");
        if (event.getUser().getIdLong() == 211789389401948160L || event.getUser().getIdLong() == 260016427900076033L) {
            String[] args = event.getArgs();
            if (args.length == 1) {
                event.reply("No dev command provided");
            } else {
                String command = args[1];
                if (command.equalsIgnoreCase("dirty-config")) { //Adds an illegal object to the json to invalidate it
                    event.getConfig().put("bad-value", new Exception());
                    event.reply("Added something nonsensical to the config");
                } else {
                    event.reply("Unrecognised dev command " + command);
                }
            }
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"devtools", "dev"};
    }

    @Override
    public String getCategory() {
        return "Dev";
    }

    @Override
    public String getOptions() {
        return "(dirty-config)"; //(Command1 | Command2 | Command3) - add them here once they exist
    }

    @Override
    public String getDescription() {
        return "A suite of testing tools for making sure features work";
    }
}
