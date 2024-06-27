package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandStateChecker.Check;
import Bots.MessageEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;
public class CommandJoin extends BaseCommand implements Runnable {
    private static MessageEvent event;
    private static final ExecutorService executor = Executors.newCachedThreadPool();
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_USER_IN_ANY_VC};
    }

    @Override
    public void run() {
        // Don't use the check system since we want to specifically allow this even if the bot is active in another VC
        try {
            event.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(event.getMember().getVoiceState()).getChannel());
        } catch (InsufficientPermissionException e) {
            event.replyEmbeds(createQuickError("The bot can't access your channel"));
            return;
        }
        event.replyEmbeds(createQuickEmbed(" ", "✅ Joined your vc."));

    }

    @Override
    public String[] getNames() {
        return new String[]{"connect", "join"};
    }

    @Override
    public Category getCategory() {
        return Category.DJ;
    }

    @Override
    public String getDescription() {
        return "Makes the bot forcefully join your vc.";
    }

    @Override
    public long getRatelimit() {
        return 5000;
    }

    @Override
    public void execute(MessageEvent e) throws InterruptedException {
        event = e;
        executor.submit(new CommandJoin());

    }
}
