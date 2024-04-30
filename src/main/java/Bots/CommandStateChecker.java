package Bots;

import Bots.lavaplayer.PlayerManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Objects;

// A simple checker designed to check common cases in commands
// Can either be called manually or handled automatically by overriding getChecks
public class CommandStateChecker {
    public static final class CheckResult {
        private final boolean succeeded;
        private final String message;

        public CheckResult(boolean s, String m) {
            this.succeeded = s;
            this.message = m;
        }

        @SuppressWarnings("BooleanMethodIsAlwaysInverted")
        public boolean Succeeded() {
            return this.succeeded;
        }

        public String GetMessage() {
            return this.message;
        }
    }
    private static final CheckResult success = new CheckResult(true, "(You should never see this message)");

    public enum Check {
        IS_USER_IN_ANY_VC, IS_BOT_IN_ANY_VC, IS_IN_SAME_VC, TRY_JOIN_VC, IS_DJ, IS_CHANNEL_BLOCKED, IS_PLAYING, IS_DEV
    }

    // IS_USER_IN_ANY_VC -> Checks if the user is in any VC at all
    // IS_BOT_IN_ANY_VC -> Checks if the bot is in any VC at all
    // IS_IN_SAME_VC -> Checks if the user is in the same VC as the bot. If either is not in any VC, this fails
    // TRY_JOIN_VC -> Checks if the user is in the same VC or, if not, attempts to join their VC if reasonable
    // IS_DJ -> Checks if the user is eligible for DJ status
    // IS_CHANNEL_BLOCKED -> Checks if the channel the command is in is blocked
    // IS_PLAYING -> Checks if the bot is currently playing any audio
    // IS_DEV -> Checks if the user invoking the command is defined as a developer

    public static CheckResult PerformChecks(MessageEvent event, Check... checks) {
        CheckResult result = success;
        for (Check check : checks) {
            switch (check) {
                case IS_USER_IN_ANY_VC -> result = IsUserInAnyVc(event);
                case IS_BOT_IN_ANY_VC -> result = IsBotInAnyVc(event);
                case IS_IN_SAME_VC -> result = IsInSameVc(event);
                case TRY_JOIN_VC -> result = TryJoinVc(event);
                case IS_DJ -> result = IsDJ(event);
                case IS_CHANNEL_BLOCKED -> result = IsChannelBlocked(event);
                case IS_PLAYING -> result = IsPlaying(event);
                case IS_DEV -> result = IsDev(event);
                default -> System.err.println("Skipping unhandled check for Check " + check.name());
            }
            if (!result.Succeeded())
                break;
        }
        return result;
    }

    //-- The actual testing methods --//

    private static CheckResult IsUserInAnyVc(MessageEvent event) {
        return new CheckResult(
                Objects.requireNonNull(event.getMember().getVoiceState()).inAudioChannel(),
                "You aren't in a VC."
        );
    }

    private static CheckResult IsBotInAnyVc(MessageEvent event) {
        return new CheckResult(
                Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).inAudioChannel(),
                "The bot isn't in a VC."
        );
    }

    private static CheckResult IsInSameVc(MessageEvent event) {
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember().getVoiceState());
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());

        if (!memberState.inAudioChannel()) {
            return new CheckResult(false, "You aren't in a VC.");
        }
        if (selfState.getChannel() == null || memberState.getChannel() != selfState.getChannel()) {
            return new CheckResult(false, "The bot isn't in your VC.");
        }
        return success;
    }

    // This will cause the bot to join the VC if checks pass, so make sure this occurs later on
    private static CheckResult TryJoinVc(MessageEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember().getVoiceState());
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());

        if (!memberState.inAudioChannel()) {
            return new CheckResult(false, "You aren't in a VC.");
        }
        if (selfState.getChannel() != null && memberState.getChannel() != selfState.getChannel()) {
            //QUESTION: Should we also check if its actively playing anything? -9382
            return new CheckResult(false, "The bot is already busy in another VC.");
        }
        try {
            audioManager.openAudioConnection(memberState.getChannel());
            return success;
        } catch (InsufficientPermissionException e) {
            return new CheckResult(false, "The bot is unable to join the VC.");
        }
    }

    private static CheckResult IsDJ(MessageEvent event) {
        Member member = event.getMember();
        Guild guild = event.getGuild();
        if (member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
            // Make sure there's no cross-channel DJ shenanigans, even though IS_IN_SAME_VC should be used in unison
            AudioChannelUnion botChannel = Objects.requireNonNull(guild.getSelfMember().getVoiceState()).getChannel();
            if (botChannel == null || botChannel == member.getVoiceState().getChannel()) {
                int people = 0;
                for (Member vcMember : member.getVoiceState().getChannel().getMembers()) {
                    if (!vcMember.getUser().isBot()) {
                        people++;
                        if (people > 1)
                            break;
                    }
                }
                if (people == 1) { //People alone in a VC are allowed to use VC DJ commands
                    return success;
                }
            }
        }
        JSONObject config = GuildDataManager.GetGuildConfig(guild.getIdLong());
        JSONArray DJRoles = (JSONArray) config.get("DJRoles");
        JSONArray DJUsers = (JSONArray) config.get("DJUsers");
        boolean check = false;
        for (Object DJRole : DJRoles) {
            if ((long) DJRole == guild.getIdLong() || member.getRoles().contains(guild.getJDA().getRoleById((Long) DJRole))) {
                check = true;
                break;
            }
        }
        if (!check) {
            for (Object DJUser : DJUsers) {
                if (DJUser.equals(member.getIdLong())) {
                    check = true;
                    break;
                }
            }
        }
        return new CheckResult(check, "You do not have a DJ permissions.");
    }

    private static CheckResult IsChannelBlocked(MessageEvent event) {
        JSONObject config = GuildDataManager.GetGuildConfig(event.getGuild().getIdLong());
        JSONArray blockedChannels = (JSONArray) config.get("BlockedChannels");
        for (Object blockedChannel : blockedChannels) {
            if (event.getChannel().getId().equals(blockedChannel)) {
                return new CheckResult(false, "This command is blocked in this channel.");
            }
        }
        return success;
    }

    private static CheckResult IsPlaying(MessageEvent event) {
        return new CheckResult(
                PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() != null,
                "The bot is not currently playing anything."
        );
    }

    private static CheckResult IsDev(MessageEvent event) {
        //TODO: This should be dynamic, probably based on .env? Can't have others hosting it without them being a dev
        return new CheckResult(
                event.getUser().getIdLong() == 211789389401948160L || event.getUser().getIdLong() == 260016427900076033L,
                "This command is for developers only."
        );
    }
}
