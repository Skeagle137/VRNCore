package net.skeagle.vrncore.commands;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.GUIs.ExpTradeGUI;
import net.skeagle.vrncore.GUIs.TrailsGUI;
import net.skeagle.vrnlib.itemutils.ItemUtils;
import net.skeagle.vrnlib.messages.Messages;
import net.skeagle.vrnlib.misc.FormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.*;

import static net.skeagle.vrnlib.VRNLib.say;
import static net.skeagle.vrnlib.VRNLib.sayNoPrefix;

public class MiscCommands {

    private final Map<UUID, UUID> lastReplies = new HashMap<>();

    @VRNCommand(cmd = {"message", "msg"}, desc = "Sends a direct message to a player.")
    public void onMessage(Player player, Player target, String message) {
        VRNCore.getPlayerData(player.getUniqueId()).thenAcceptAsync(playerData -> {
            VRNCore.getPlayerData(target.getUniqueId()).thenAccept(targetData -> {
                String outgoing = Messages.msg("senderMsgPlayerPrefix", targetData.getName()) + message;
                String incoming = Messages.msg("playerMsgSenderPrefix", playerData.getName()) + message;
                sayNoPrefix(player, outgoing);
                sayNoPrefix(target, incoming);
                lastReplies.put(player.getUniqueId(), target.getUniqueId());
                lastReplies.put(target.getUniqueId(), player.getUniqueId());
            });
        });
    }

    @VRNCommand(cmd = {"reply", "r"}, desc = "Sends a reply to the last player that messaged you.", perm = "message")
    public void onReply(BukkitCommandActor actor, String message) {
        Player player = actor.requirePlayer();
        if (lastReplies.get(player.getUniqueId()) == null) {
            actor.error("You have not messaged someone to be able to reply to them yet.");
            return;
        }
        onMessage(player, Bukkit.getPlayer(lastReplies.get(player.getUniqueId())), message);
    }

    @VRNCommand(cmd = "craft", desc = "Opens a crafting table interface.")
    public void onCraft(Player player) {
        player.openWorkbench(null, true);
    }

    @VRNCommand(cmd = {"trails", "trail"}, desc = "Opens a GUI for selecting and customizing arrow and player trails.")
    public void onTrails(Player player, @Default("@s") Player target) {
        new TrailsGUI(player, target);
    }

    @VRNCommand(cmd = "exptrade", desc = "Opens a GUI for trading items for experience points.")
    public void onExpTrade(Player player) {
        new ExpTradeGUI(player);
    }

    @VRNCommand(cmd = "rename", desc = "Renames the currently held item in your main hand.")
    public void onRename(BukkitCommandActor actor, String name) {
        Player player = actor.requirePlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            say(player, "You must have an item in your main hand.");
            return;
        }
        String formatted = FormatUtils.color(name);
        player.getInventory().setItemInMainHand(ItemUtils.rename(player.getInventory().getItemInMainHand(), formatted));
        actor.reply("Item renamed to " + formatted + "&7.");
    }
}
