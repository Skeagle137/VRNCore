package net.skeagle.vrncore.commands;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.playerdata.PlayerStates;
import net.skeagle.vrnlib.messages.Messages;
import net.skeagle.vrnlib.misc.FormatUtils;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnlib.misc.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import revxrsal.commands.annotation.*;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.parameters.EntitySelector;

import java.util.concurrent.CompletableFuture;

import static net.skeagle.vrnlib.VRNLib.say;
import static net.skeagle.vrnlib.misc.TimeUtil.timeToMessage;

public class AdminCommands {

    @VRNCommand(cmd = {"broadcast", "bc"}, desc = "Broadcasts a message to the entire server.")
    public void onBroadcast(CommandSender sender, String message) {
        Bukkit.broadcastMessage(FormatUtils.color(Messages.msg("broadcastPrefix") + message));
    }

    @VRNCommand(cmd = "echest", desc = "Opens a player's ender chest.")
    public void onEchest(BukkitCommandActor actor, @Default("@s") Player target) {
        Player player = actor.requirePlayer();
        player.openInventory(target.getEnderChest());
        actor.reply(target != player ? "Now showing &a" + target.getName() + "&7's ender chest." : "Now showing your ender chest.");
    }

    @VRNCommand(cmd = "smite", desc = "Strikes a selection of players by summoning lightning at their location.")
    public void onSmite(BukkitCommandActor actor, EntitySelector<Player> players) {
        if (actor.isPlayer()) {
            players.remove(actor.requirePlayer());
        }
        players.forEach(p -> p.getWorld().strikeLightning(p.getLocation()));
        actor.reply("Smited player(s).");
    }

    @VRNCommand(cmd = "spawnmob", desc = "Spawns a mob where the player is looking or at a specific location.")
    public void onSpawnmob(BukkitCommandActor actor, EntityType type, @Range(min = 1, max = 100) @Default("1") int count) {
        Player player = actor.requirePlayer();
        if (!type.isSpawnable() && !type.isAlive()) {
            actor.reply("&cThat entity type cannot be spawned.");
        }
        Block b = player.getTargetBlock(null, 50);

        for (int i = 0; i < count; i++) {
            player.getWorld().spawnEntity(b.getLocation().clone().add(0.5, 1.0, 0.5), type);
        }
        actor.reply("Spawned " + count + " " + type.toString().toLowerCase() + " at &a" +
                b.getLocation().getX() + "&7, &a" + b.getLocation().getY() + "&7, &a" + b.getLocation().getZ() + "&7.");
    }

    @VRNCommand(cmd = "heal", desc = "Heals a player.")
    public void onHeal(BukkitCommandActor actor, @Default("@s") Player target) {
        Player player = actor.requirePlayer();
        target.setHealth(target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue());
        target.setFoodLevel(20);
        target.setSaturation(5);
        target.setFireTicks(0);
        target.getActivePotionEffects().clear();
        say(target, "Your health and hunger are now full.");
        if (target == player) return;
        actor.reply("&a" + target.getName() + "&7's health and hunger are now full.");
    }

    @VRNCommand(cmd = "speed", desc = "Changes a player's fly and walk speed.")
    public void onSpeed(BukkitCommandActor actor, @Range(min = 0, max = 10) @Default("1") int speed, @Default("@s") Player target) {
        if (speed == 1) {
            if (target.isFlying())
                target.setFlySpeed(0.1f);
            else
                target.setWalkSpeed(0.2f);
            say(target, "Your " + (target.isFlying() ? "flying" : "walking") + " speed has been reset.");
            if (target == actor.sender()) {
                return;
            }
            actor.reply("&a" + target.getName() + "&7's " + (target.isFlying() ? "flying" : "walking") + " speed has been reset.");
        } else {
            if (target.isFlying()) {
                target.setFlySpeed(speed * 0.1f);
            } else {
                target.setWalkSpeed(speed * 0.1f);
            }
            say(target, "Your " + (target.isFlying() ? "flying" : "walking") + " speed has been set to &a" + speed + "&7.");
            if (target == actor.sender()) return;
            actor.reply("&a" + target.getName() + "&7's " + (target.isFlying() ? "flying" : "walking") + " speed has been set to &a" + speed + "&7.");
        }
    }

    @VRNCommand(cmd = "timeplayed get", desc = "Checks the time that a player has been on the server.")
    public CompletableFuture<Void> onTimePlayedGet(BukkitCommandActor actor, @Default("@s") OfflinePlayer target) {
        return VRNCore.getPlayerData(target.getUniqueId()).thenAcceptAsync(data -> {
            VRNCore.getPlayerData(target.getUniqueId());
            actor.reply((target == actor.sender() ? "Your" : "&a" + target.getName() + "&7's") +
                    " time played is &a" + timeToMessage(data.getTimePlayed()) + "&7.");
        });
    }

    @VRNCommand(cmd = "timeplayed set", desc = "Sets the time that a player has been on the server.")
    public CompletableFuture<Void> onTimePlayedSet(BukkitCommandActor actor, String time, @Default("@s") OfflinePlayer target) {
        long totalsec;
        try {
            totalsec = TimeUtil.parseTimeString(time);
        } catch (TimeUtil.TimeFormatException e) {
            return CompletableFuture.runAsync(() -> actor.error(e.getMessage()));
        }
        return VRNCore.getPlayerData(target.getUniqueId()).thenAcceptAsync(data -> {
            data.setTimePlayed(totalsec);
            actor.reply("Time played set to &a" + TimeUtil.timeToMessage(totalsec) +
                    (actor.sender() == target ? "&7." : "&7 for &a" + target.getName() + "&7."));
        });
    }

    @VRNCommand(cmd = "timeplayed add", desc = "Adds to the time that a player has been on the server.")
    public CompletableFuture<Void> onTimePlayedAdd(BukkitCommandActor actor, String time, @Default("@s") OfflinePlayer target) {
        long totalsec;
        try {
            totalsec = TimeUtil.parseTimeString(time);
        } catch (TimeUtil.TimeFormatException e) {
            return CompletableFuture.runAsync(() -> actor.error(e.getMessage()));
        }
        return VRNCore.getPlayerData(target.getUniqueId()).thenAcceptAsync(data -> {
            long total = data.getTimePlayed() + totalsec;
            data.setTimePlayed(total);
            actor.reply("Added &a" + TimeUtil.timeToMessage(totalsec) + "&7 to " +
                    (actor.sender() == target ? "your" : "&a" + target.getName() + "&7's") + " time. " +
                    (actor.sender() == target ? "Your" : "Their") + " total time is now &a" + TimeUtil.timeToMessage(total) + "&7.");
        });
    }

    @VRNCommand(cmd = "timeplayed subtract", desc = "Subtracts from the time that a player has been on the server.")
    public CompletableFuture<Void> onTimePlayedSubtract(BukkitCommandActor actor, String time, @Default("@s") OfflinePlayer target) {
        long totalsec;
        try {
            totalsec = TimeUtil.parseTimeString(time);
        } catch (final TimeUtil.TimeFormatException e) {
            return CompletableFuture.runAsync(() -> actor.error(e.getMessage()));
        }
        return VRNCore.getPlayerData(target.getUniqueId()).thenAcceptAsync(data -> {
            long finalTotalSec = totalsec;
            long timeplayed = data.getTimePlayed();
            if (finalTotalSec > timeplayed) {
                finalTotalSec = timeplayed;
            }
            long total = timeplayed - finalTotalSec;
            data.setTimePlayed(total);
            String timeString = TimeUtil.timeToMessage(total);
            if (total < 1) {
                timeString = "0 seconds";
            }
            actor.reply("Subtracted &a" + TimeUtil.timeToMessage(finalTotalSec) + "&7 from " +
                    (actor.sender() == target ? "your" : "&a" + target.getName() + "&7's") + " time. " +
                    (actor.sender() == target ? "Your" : "Their") + " total time is now &a" + timeString + "&7.");
        });
    }

    @VRNCommand(cmd = "fly", desc = "Toggles flight for a player.")
    public void onFly(BukkitCommandActor actor, @Default("@s") Player target) {
        target.setAllowFlight(!target.getAllowFlight());
        say(target, "Flight mode is now " + (target.getAllowFlight() ? "&aenabled" : "&cdisabled") + "&7.");
        if (target == actor.sender()) return;
        actor.reply( "&a" + target.getName() + "&7's flight mode has been " + (target.getAllowFlight() ? "&aenabled" : "&cdisabled") + "&7.");
    }

    @VRNCommand(cmd = {"gamemode", "gm"}, desc = "Changes a player's game mode.")
    public void onGamemode(BukkitCommandActor actor, GameMode gamemode, @Default("@s") Player target) {
        target.setGameMode(gamemode);
        say(target, "You are now in &a" + gamemode.name().toLowerCase() + "&7 mode.");
        if (actor.sender() == target) return;
        actor.reply( "&a" + target.getName() + " &7is now in &a" + gamemode.name().toLowerCase() + "&7 mode.");
    }

    @VRNCommand(cmd = "god", desc = "Makes a player invulnerable.")
    public CompletableFuture<Void> onGod(BukkitCommandActor actor, @Default("@s") OfflinePlayer target) {
        return VRNCore.getPlayerData(target.getUniqueId()).thenAcceptAsync(data -> {
            PlayerStates states = data.getStates();
            states.setGodmode(!states.hasGodmode());
            if (target.getPlayer() != null) {
                say(target.getPlayer(), "You are " + (states.hasGodmode() ? "now" : "no longer") + " invulnerable.");
            }
            if (target == actor.sender()) return;
            actor.reply("&a" + target.getName() + " &7is " + (states.hasGodmode() ? "now" : "no longer") + " invulnerable.");
        }, Task::syncDelayed);
    }

    @VRNCommand(cmd = "repair", desc = "Repairs items in a player's inventory.")
    public void onRepair(BukkitCommandActor actor, EquipmentSlot slot, @Default("@s") Player target) {
        ItemStack item = target.getInventory().getItem(slot);
        if (item.getType().isAir()) {
            actor.reply("&cNo item in the " + slot.name().toLowerCase() + " slot was found.");
            return;
        }
        ItemMeta meta = item.getItemMeta();
        if (meta == null || item.getType().getMaxDurability() < 1 || !(meta instanceof Damageable damageable)) {
            actor.reply("&cThis item cannot be repaired.");
            return;
        }
        else if (damageable.getDamage() < 1) {
            actor.reply("&cNothing to repair; item is already at full durability.");
            return;
        }
        damageable.setDamage(0);
        item.setItemMeta(meta);
        target.getInventory().setItem(slot, item);
        actor.reply("&aRepaired " + (target == actor.sender() ? "your" : target.getName() + "'s") + " item in the " + slot.name().toLowerCase() + " slot.");
    }
}
