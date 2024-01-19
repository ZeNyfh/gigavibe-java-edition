package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import static Bots.Main.*;

public class CommandLoopQueue extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();

        if (!audioManager.isConnected()) {
            event.replyEmbeds(createQuickError("I am not playing anything."));
            return;
        }

        if (LoopQueueGuilds.contains(event.getGuild().getId())) {
            event.replyEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current queue."));
            LoopQueueGuilds.remove(event.getGuild().getId());
        } else {
            event.replyEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current queue."));
            LoopQueueGuilds.add(event.getGuild().getId());
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"loopqueue", "loopq"};
    }

    @Override
    public String getOptions() {
        return "";
    }

    @Override
    public Category getCategory() {
        return Category.Music;
    }

    @Override
    public String getDescription() {
        return "Loops the current queue.";
    }

    @Override
    public long getRatelimit() {
        return 2500;
    }
}
