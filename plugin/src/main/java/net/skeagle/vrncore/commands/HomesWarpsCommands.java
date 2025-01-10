package net.skeagle.vrncore.commands;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.GUIs.HomesGUI;
import net.skeagle.vrncore.GUIs.WarpsGUI;
import net.skeagle.vrncore.configurable.Settings;
import net.skeagle.vrncore.homes.Home;
import net.skeagle.vrncore.homes.HomeManager;
import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrncore.warps.Warp;
import net.skeagle.vrncore.warps.WarpManager;
import net.skeagle.vrnlib.messages.Messages;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.concurrent.CompletableFuture;

import static net.skeagle.vrncore.utils.VRNUtil.say;

public class HomesWarpsCommands {

    private final VRNCore plugin;

    public HomesWarpsCommands(VRNCore plugin) {
        this.plugin = plugin;
    }

    @VRNCommand(cmd = "home", desc = "Teleports you to the specified home.")
    public void onHome(BukkitCommandActor actor, Home home) {
        Player player = actor.requirePlayer();
        sayTp(player);
        player.teleport(home.location());
    }

    @VRNCommand(cmd = "sethome", desc = "Creates a home.")
    public void onSetHome(BukkitCommandActor actor, String name) {
        Player player = actor.requirePlayer();
        if (!player.hasPermission("vrn.homes.limit.*")) {
            int limit = VRNUtil.getLimitForPerm(player, "vrn.homes.limit", Settings.maxHomes);
            if (limit != 0 && plugin.getHomeManager().getHomeNames(player).size() >= limit) {
                say(player, "&cYou can only set a maximum of " + limit + " homes. Delete some of your homes if you want to set more.");
                return;
            }
        }
        if (plugin.getHomeManager().getHome(name, player.getUniqueId()) != null) {
            say(player, "&cA home with that name already exists.");
            return;
        }
        plugin.getHomeManager().createHome(name, player);
        say(player, "&7Home set, teleport to it with &a/home " + name + "&7.");
    }

    @VRNCommand(cmd = "delhome", desc = "Removes a home.")
    public void onDelHome(BukkitCommandActor actor, Home home) {
        Player player = actor.requirePlayer();
        say(player, "&7Home &a" + home.name() + "&7 successfully deleted.");
        plugin.getHomeManager().deleteHome(home);
    }

    @VRNCommand(cmd = "homes", desc = "Lists all available homes that a player has.")
    public CompletableFuture<Void> onHomes(BukkitCommandActor actor, @Default("@s") OfflinePlayer target) {
        Player player = actor.requirePlayer();
        HomeManager manager = plugin.getHomeManager();
        return manager.getHomesCount(target).thenAccept(count -> {
            if (count >= 1) {
                Task.syncDelayed(() -> new HomesGUI(manager, player, target));
                return;
            }
            say(player, "&c" + (target == player ? "You do" : target.getName() + " does") + " not have any homes available.");
        });
    }

    @VRNCommand(cmd = "warp", desc = "Teleports you to the specified warp.")
    public void onWarp(BukkitCommandActor actor, Warp warp) {
        Player player = actor.requirePlayer();
        sayTp(player);
        player.teleport(warp.getLocation());
    }

    @VRNCommand(cmd = "setwarp", desc = "Creates a warp.")
    public void onSetWarp(BukkitCommandActor actor, String name) {
        Player player = actor.requirePlayer();
        if (!player.hasPermission("vrn.warps.limit.*")) {
            int limit = VRNUtil.getLimitForPerm(player, "vrn.warps.limit", Settings.maxWarps);
            if (limit != 0 && plugin.getWarpManager().getWarpsOwned(player) >= limit) {
                actor.error("&cYou can only set a maximum of " + limit + " warps. Delete some of your warps if you want to set more.");
                return;
            }
        }
        if (plugin.getWarpManager().getWarp(name) != null) {
            actor.error("&cA warp with that name already exists.");
            return;
        }
        plugin.getWarpManager().createWarp(player, name);
        actor.reply("&7Warp set, teleport to it with &a/warp " + name + "&7.");
    }

    @VRNCommand(cmd = "delwarp", desc = "Removes a warp.")
    public void onDelWarp(BukkitCommandActor actor, Warp w) {
        actor.reply("&7Warp &a" + w.getName() + "&7 successfully deleted.");
        plugin.getWarpManager().deleteWarp(w);
    }

    @VRNCommand(cmd = "warps", desc = "Lists all available warps.")
    public void onWarps(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        WarpManager manager = plugin.getWarpManager();
        if (!plugin.getWarpManager().getWarps().isEmpty()) {
            new WarpsGUI(manager, player);
            return;
        }
        say(player, "&cThere are no warps available.");
    }

    private void sayTp(Player player) {
        say(player, Messages.msg("teleporting"));
    }
}
