package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.managers.AudioManager;

import static Bots.Main.*;

public class CommandLoopQueue implements BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        final AudioManager audioManager = event.getGuild().getAudioManager();

        if (!audioManager.isConnected()) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickError("I am not playing anything.")).queue();
            return;
        }

        if (LoopQueueGuilds.contains(event.getGuild().getId())) {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("❌ \uD83D\uDD01", "No longer looping the current queue.")).queue();
            LoopQueueGuilds.remove(event.getGuild().getId());
        } else {
            event.getChannel().asTextChannel().sendMessageEmbeds(createQuickEmbed("✅ \uD83D\uDD01", "Looping the current queue.")).queue();
            LoopQueueGuilds.add(event.getGuild().getId());
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"loopqueue", "loopq"};
    }

    @Override
    public String getCategory() {
        return "Music";
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
