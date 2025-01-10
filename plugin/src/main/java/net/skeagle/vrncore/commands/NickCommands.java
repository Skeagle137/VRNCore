package net.skeagle.vrncore.commands;

import net.skeagle.vrncore.VRNCore;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import static net.skeagle.vrnlib.VRNLib.say;

public class NickCommands {

    private final VRNCore plugin;

    public NickCommands(VRNCore plugin) {
        this.plugin = plugin;
    }

    @VRNCommand(cmd = "nick", desc = "Sets a nickname for a player.")
    public void onNick(BukkitCommandActor actor, String nickname, @Default("@s") Player target) {
        String stripped = ChatColor.stripColor(nickname);
        if (stripped.equalsIgnoreCase(actor.name())) {
            actor.error("The nickname can't be your own name.");
            return;
        }
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            if (stripped.equals(ChatColor.stripColor(pl.getDisplayName()))) {
                actor.error("Another player already has this nickname.");
                return;
            }
        }
        plugin.getPlayerManager().getData(target.getUniqueId()).thenAccept(data -> {
            data.setNick(nickname);
            actor.reply("Set " + (actor.isPlayer() && data.getPlayer() == actor.asPlayer() ?
                    "your" : "&a" + target.getName() + "&7's") + " nick to " + nickname + "&7.");
            if (actor.sender() == target) return;
            say(target, "&7Your nickname was changed to " + data.getName() + "&7.");
        });
    }

    @VRNCommand(cmd = "realname", desc = "Shows a player's real name from their nickname.")
    public void onRealname(BukkitCommandActor actor, String nick) {
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            String stripped = ChatColor.stripColor(pl.getDisplayName());
            if (stripped.equals(ChatColor.stripColor(nick))) {
                if (pl.getName().equals(stripped)) {
                    actor.error(pl.getName() + " does not have a nickname.");
                    return;
                }
                plugin.getPlayerManager().getData(pl.getUniqueId()).thenAcceptAsync(data ->
                        actor.reply("The real name of " + pl.getDisplayName() + "&7 is &a" + pl.getName() + "&7."));
                return;
            }
        }
        actor.error("There is no player online that has that nickname.");
    }

    @VRNCommand(cmd = "removenick", desc = "Removes a nickname from a player.", perm = "nick")
    public void onRemoveNick(BukkitCommandActor actor, @Default("@s") Player target) {
        plugin.getPlayerManager().getData(target.getUniqueId()).thenAcceptAsync(data -> {
            data.setNick(null);
            actor.reply("Removed " + (actor.sender() == target ? "your&7" : "&a" + data.getName() + "&7's") + " nickname.");
            if (actor.sender() == target) return;
            say(target, "Your nickname was removed.");
        });
    }
}
