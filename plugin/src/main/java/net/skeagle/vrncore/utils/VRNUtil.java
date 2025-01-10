package net.skeagle.vrncore.utils;

import com.google.gson.Gson;
import net.skeagle.vrncore.configurable.Settings;
import net.skeagle.vrnlib.VRNLib;
import net.skeagle.vrnlib.messages.Messages;
import net.skeagle.vrnlib.misc.FormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

import java.util.logging.Level;
import java.util.stream.Collectors;

public final class VRNUtil {

    public static final Gson GSON = new Gson();
    public static final String NOPERM = Messages.msg("noPermission");

    public static void say(CommandSender cs, String... message) {
        if (cs == null) return;
        for (String msg : message)
            cs.sendMessage(FormatUtils.color(Messages.msg(VRNLib.getInstance(), "prefix") + msg));
    }

    public static void log(String... messages) {
        log(Level.INFO, messages);
    }

    public static void log(Level level, String... messages) {
        if (messages == null) return;
        for (String s : messages)
            Bukkit.getLogger().log(level, FormatUtils.color(s));
    }

    public static int getLimitForPerm(Player player, String startsWith, int max) {
        int highest = 0;
        if (!Settings.alternatePermLimitCheck) {
            for (int i = max; i > 0; --i) {
                if (player.hasPermission(startsWith + "." + i)) {
                    return i;
                }
            }
        }
        else {
            for (String perm : player.getEffectivePermissions().stream().map(PermissionAttachmentInfo::getPermission).collect(Collectors.toList())) {
                if (perm.startsWith(startsWith)) {
                    String[] spl = perm.split("\\.");
                    if (spl.length == 4) {
                        try {
                            int limit = Integer.parseInt(spl[3]);
                            if (highest < limit && limit <= max) {
                                highest = limit;
                            }
                        }
                        catch (NumberFormatException ex) {
                            VRNUtil.log(Level.WARNING, "could not parse perm limit for " + startsWith + ": \"" + spl[3] + "\" is not a number");
                        }
                    }
                }
            }
        }
        return highest;
    }

    public static Block getStandingBlock(Location loc) {
        Block standing = loc.clone().getBlock().getRelative(BlockFace.DOWN);
        double x = loc.getX() % 1;
        double z = loc.getZ() % 1;
        if (blockCheck(standing))
            return standing;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                if (i == 0 && j == 0) continue;
                Block b = standing.getLocation().clone().add(i, 0, j).getBlock();
                if (blockCheck(b)) {
                    if (x > 0.7 && z > 0.7) {
                        return b;
                    }
                    if (x < 0.3 && z > 0.7) {
                        return b;
                    }
                    if (x > 0.7 && z < 0.3) {
                        return b;
                    }
                    if (x < 0.3 && z < 0.3) {
                        return b;
                    }
                    if (x > 0.7) {
                        return b;
                    }
                    if (x < 0.3) {
                        return b;
                    }
                    if (z > 0.7) {
                        return b;
                    }
                    if (z < 0.3) {
                        return b;
                    }
                }
            }
        }
        return null;
    }

    private static boolean blockCheck(Block b) {
        return (!b.getType().isAir() && b.getType().isSolid());
    }
}
