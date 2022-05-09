package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import static Bots.Main.createQuickEmbed;

public class CommandLoopQueue extends BaseCommand {
    public static Boolean loopQueue = false;

    public void execute(MessageEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();

        if (!audioManager.isConnected()) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ **Error**", "I am not playing anything.")).queue();
            return;
        }

        loopQueue = !loopQueue;
        if (loopQueue) {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current queue.")).queue();
        } else {
            event.getTextChannel().sendMessageEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current queue.")).queue();
        }
    }

    public String getName() {
        return "loopqueue";
    }

    public String getCategory() {
        return "Music";
    }

    public String getDescription() {
        return "**- Loops the current queue.";
    }
}
