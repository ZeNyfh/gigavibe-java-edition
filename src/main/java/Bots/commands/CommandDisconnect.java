package Bots.commands;

import Bots.BaseCommand;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class CommandDisconnect implements BaseCommand {
    @Override
    public void execute(MessageReceivedEvent event) {
        event.getJDA().getAudioManagers().get(0).closeAudioConnection(); // this will need to be changed at a later date, I cannot think of an alternative solution rn
    } // this will also need to be checked with DJ permissions in the future

    @Override
    public String getName() {
        return "disconnect";
    }

    @Override
    public String getCategory() {
        return "DJ";
    }

    @Override
    public String getDescription() {
        return "Makes the bot forcefully leave the vc.";
    }
}
