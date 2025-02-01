package Bots;

import Bots.lavaplayer.GuildMusicManager;
import Bots.lavaplayer.PlayerManager;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion;
import net.dv8tion.jda.api.exceptions.InsufficientPermissionException;
import net.dv8tion.jda.api.managers.AudioManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.Arrays;
import java.util.Objects;

import static Bots.CommandEvent.localise;

// A simple checker designed to check common cases in commands
// Can either be called manually or handled automatically by overriding getChecks
public class CommandStateChecker {
    private static final CheckResult success = new CheckResult(true, "(You should never see this message)");

    public static CheckResult PerformChecks(CommandEvent event, Check... checks) {
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

    private static CheckResult IsUserInAnyVc(CommandEvent event) {
        return new CheckResult(
                Objects.requireNonNull(event.getMember().getVoiceState()).inAudioChannel(),
                localise("You are not in a voice channel.","CmdSCheck.notInVC")
        );
    }

    // IS_USER_IN_ANY_VC -> Checks if the user is in any VC at all
    // IS_BOT_IN_ANY_VC -> Checks if the bot is in any VC at all
    // IS_IN_SAME_VC -> Checks if the user is in the same VC as the bot. If either is not in any VC, this fails
    // TRY_JOIN_VC -> Checks if the user is in the same VC or, if not, attempts to join their VC if reasonable
    // IS_DJ -> Checks if the user is eligible for DJ status
    // IS_CHANNEL_BLOCKED -> Checks if the channel the command is in is blocked
    // IS_PLAYING -> Checks if the bot is currently playing any audio
    // IS_DEV -> Checks if the user invoking the command is defined as a developer

    private static CheckResult IsBotInAnyVc(CommandEvent event) {
        return new CheckResult(
                Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).inAudioChannel(),
                localise("The bot is not in a voice channel.","CmdSCheck.botNotInVC")
        );
    }

    //-- The actual testing methods --//

    private static CheckResult IsInSameVc(CommandEvent event) {
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember().getVoiceState());
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());

        if (!memberState.inAudioChannel()) {
            return new CheckResult(false, localise("You are not in a voice channel.","CmdSCheck.notInVC"));
        }
        if (selfState.getChannel() == null || memberState.getChannel() != selfState.getChannel()) {
            return new CheckResult(false, localise("The bot is not in a voice channel.","CmdSCheck.botNotInVC"));
        }
        return success;
    }

    // This will cause the bot to join the VC if checks pass, so make sure this occurs later on
    private static CheckResult TryJoinVc(CommandEvent event) {
        AudioManager audioManager = event.getGuild().getAudioManager();
        GuildVoiceState memberState = Objects.requireNonNull(event.getMember().getVoiceState());
        GuildVoiceState selfState = Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState());

        if (!memberState.inAudioChannel()) {
            return new CheckResult(false, localise("The bot is not in your voice channel.","CmdSCheck.notInVC"));
        }
        if (selfState.getChannel() != null && memberState.getChannel() != selfState.getChannel()) {
            GuildMusicManager manager = PlayerManager.getInstance().getMusicManager(selfState.getGuild());
            if (manager.audioPlayer.getPlayingTrack() == null && manager.scheduler.queue == null) {
                return success;
            }
            return new CheckResult(false, localise("The bot is already busy in another voice channel.","CmdSCheck.botBusy"));
        }
        if (memberState.getChannel() != selfState.getChannel()) {
            try {
                audioManager.openAudioConnection(memberState.getChannel());
                return success;
            } catch (InsufficientPermissionException e) {
                return new CheckResult(false, localise("The bot is unable to join the voice channel.","CmdSCheck.cannotJoin"));
            }
        } else {
            return success;
        }
    }

    private static CheckResult IsDJ(CommandEvent event) {
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
        return new CheckResult(check, localise("You do not have a DJ permissions.","CmdSCheck.noDJ"));
    }

    private static CheckResult IsChannelBlocked(CommandEvent event) {
        JSONObject config = GuildDataManager.GetGuildConfig(event.getGuild().getIdLong());
        JSONArray blockedChannels = (JSONArray) config.get("BlockedChannels");
        for (Object blockedChannel : blockedChannels) {
            if (event.getChannel().getId().equals(blockedChannel)) {
                return new CheckResult(false, localise("This command is blocked in this channel.","CmdSCheck.commandBlocked"));
            }
        }
        return success;
    }

    private static CheckResult IsPlaying(CommandEvent event) {
        return new CheckResult(
                PlayerManager.getInstance().getMusicManager(event.getGuild()).audioPlayer.getPlayingTrack() != null,
                localise("The bot is not currently playing anything.","CmdSCheck.isNotPlaying")
        );
    }

    private static CheckResult IsDev(CommandEvent event) { // Would BOT_ADMINS be more appropriate?
        Dotenv dotenv = Dotenv.load();
        var matchesAny = false;

        long[] developers = Arrays.stream(dotenv.get("DEVELOPERS", "211789389401948160,260016427900076033").split(","))
                .mapToLong(Long::parseLong).toArray(); // Preserve original IDs unless explicitly set by hoster

        for (long l : developers)
            if (l == event.getUser().getIdLong())
                matchesAny = true;

        return new CheckResult(matchesAny, localise("This command is for developers only.","CmdSCheck.devOnly"));
    }

    public enum Check {
        IS_USER_IN_ANY_VC, IS_BOT_IN_ANY_VC, IS_IN_SAME_VC, TRY_JOIN_VC, IS_DJ, IS_CHANNEL_BLOCKED, IS_PLAYING, IS_DEV
    }

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
}
