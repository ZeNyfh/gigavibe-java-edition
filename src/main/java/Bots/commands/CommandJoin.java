package Bots.commands;

import Bots.BaseCommand;
import Bots.CommandEvent;
import Bots.CommandStateChecker.Check;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.Objects;

import static Bots.CommandEvent.localise;
import static Bots.Main.createQuickEmbed;
import static Bots.Main.createQuickError;

public class CommandJoin extends BaseCommand {
    @Override
    public Check[] getChecks() {
        return new Check[]{Check.IS_DJ, Check.IS_USER_IN_ANY_VC};
    }

    @Override
    public void execute(CommandEvent event) {
        // Don't use the check system since we want to specifically allow this even if the bot is active in another VC
        try {
            event.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(event.getMember().getVoiceState()).getChannel());
        } catch (InsufficientPermissionException e) {
            event.replyEmbeds(createQuickError(localise("CommandJoin.noAccess")));
            return;
        }
        event.replyEmbeds(createQuickEmbed(" ", "✅ " + localise("CommandJoin.joined")));

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
}
