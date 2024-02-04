package Bots.commands;

import Bots.BaseCommand;
import Bots.MessageEvent;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;

import java.util.Objects;

import static Bots.Main.*;

public class CommandJoin extends BaseCommand {
    @Override
    public void execute(MessageEvent event) {
        if (!IsDJ(event.getGuild(), event.getChannel(), event.getMember())) {
            return;
        }
        if (!Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).inAudioChannel()) {
            event.replyEmbeds(createQuickError("You are not in a vc."));
            return;
        }
        try {
            event.getGuild().getAudioManager().openAudioConnection(event.getMember().getVoiceState().getChannel());
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
}
