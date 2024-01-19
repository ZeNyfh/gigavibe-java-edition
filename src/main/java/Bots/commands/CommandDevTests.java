package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import static Bots.Main.*;

public class CommandDevTests extends BaseCommand {
    private void HandleButtonEvent(ButtonInteractionEvent event) {
        EmbedBuilder eb = new EmbedBuilder();
        eb.setColor(botColour);
        eb.setDescription("*new* description text");
        event.getInteraction().editMessageEmbeds(eb.build()).queue();
    }

    @Override
    public void Init() {
        registerButtonInteraction("dev-button", this::HandleButtonEvent);
    }

    @Override
    public void execute(MessageEvent event) {
        if (event.getUser().getIdLong() == 211789389401948160L || event.getUser().getIdLong() == 260016427900076033L) {
            printlnTime("Its dev time");
            String[] args = event.getArgs();
            if (args.length == 1) {
                event.reply("No dev command provided");
            } else {
                String command = args[1];
                if (command.equalsIgnoreCase("dirty-config")) { //Adds an illegal object to the json to invalidate it
                    event.getConfig().put("bad-value", new Exception());
                    event.reply("Added something nonsensical to the config");
                } else if (command.equalsIgnoreCase("test-buttons")) { //Testing for the button registration system
                    EmbedBuilder eb = new EmbedBuilder();
                    eb.setColor(botColour);
                    eb.setDescription("description text");
                    event.getChannel().sendMessageEmbeds(eb.build()).queue(
                            message -> message.editMessageComponents().setActionRow(
                                    Button.secondary("dev-button", "Me"), Button.secondary("not-dev-button", "Not Me")
                            ).queue()
                    );
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
        return Categories.Dev.name();
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
