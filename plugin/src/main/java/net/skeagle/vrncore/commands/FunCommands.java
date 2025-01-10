package net.skeagle.vrncore.commands;

import com.mojang.authlib.properties.Property;
import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.api.VRNCoreNMS;
import net.skeagle.vrncore.utils.Skin;
import net.skeagle.vrncore.utils.SkinUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import revxrsal.commands.annotation.Default;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import static net.skeagle.vrnlib.VRNLib.say;

import java.util.*;

public class FunCommands {

    private final VRNCoreNMS api;
    private final Map<UUID, Property> skinCache;

    public FunCommands(VRNCoreNMS api) {
        this.api = api;
        skinCache = new HashMap<>();
    }

    @VRNCommand(cmd = "demo", desc = "Shows the demo screen to a player.")
    public void onDemo(BukkitCommandActor actor, Player target) {
        api.showDemoMenu(target);
        actor.reply("Now showing &a" + target.getName() + "&7 the demo menu.");
    }

    @VRNCommand(cmd = "sudo", desc = "Forces a user to chat or execute a command.")
    public void onSudo(BukkitCommandActor actor, Player target, String command) {
        if (!command.startsWith("/")) {
            target.chat(command);
            return;
        }
        Bukkit.dispatchCommand(target, command.substring(1));
        actor.reply("Made &a" + target.getName() + "&7 execute command: &a" + command);
    }

    @VRNCommand(cmd = {"hallucinate", "hallu"}, desc = "Makes a player see a hallucination.")
    public void onHallucinate(BukkitCommandActor actor, Player target) {
        api.showHallucination(target);
        actor.reply(target == actor.sender() ? "Sent yourself a hallucination. " +
                "Why you would ever want this is beyond me." : "Sent &a" + target.getName() + "&7 a hallucination.");
    }

    @VRNCommand(cmd = "push", desc = "Pushes a player and sends them flying backwards.")
    public void onPush(BukkitCommandActor actor, Player target, @Range(min = 1, max = 20) @Default("3") int multiplier) {
        double d = 0.5 * multiplier;
        target.setVelocity(target.getLocation().getDirection().multiply(-3.5 * d).setY(1.5 * d));
        actor.reply("You pushed &a" + target.getName() + "&7!");
    }

    @VRNCommand(cmd = "skin", desc = "Changes your skin to the specified player's skin (including offline players).")
    public void onSkin(BukkitCommandActor actor, @Optional String name, @Default("@s") Player target) {
        if (name == null || name.equalsIgnoreCase(target.getName())) {
            if (!skinCache.containsKey(target.getUniqueId())) {
                actor.error((target == actor.sender() ? "You do" : target.getName() + " does") +
                        " not currently have a different skin active.");
                return;
            }
            replaceSkin(target, null);
            say(target, "&7Your skin has been reset.");
            if (target == actor.sender()) return;
            actor.reply("&a" + target.getName() + "&7's skin has been reset.");
            return;
        }
        final Skin skin = SkinUtil.getSkin(name);
        if (skin == null) {
            actor.reply("Could not be retrieve skin URL. Likely there is no player with this name.");
            return;
        }
        replaceSkin(target, skin);
        actor.reply((target == actor.sender() ? "Your" : "&a" + target.getName() + "&7's") + " skin has been changed.");
    }

    private void replaceSkin(Player player, Skin skin) {
        player.getPlayerProfile().getTextures();
        final Property property = api.getTexturesProperty(player);
        if (!skinCache.containsKey(player.getUniqueId())) {
            skinCache.put(player.getUniqueId(), property);
        }
        Property newProperty = skin != null ? new Property("textures", skin.getTexture(), skin.getSignature()) : skinCache.remove(player.getUniqueId());
        api.replaceProperty(player, "textures", property, newProperty);
        api.reloadSkin(player);
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            if (player != pl) {
                pl.hidePlayer(VRNCore.getInstance(), player);
                pl.showPlayer(VRNCore.getInstance(), player);
            }
        }
        player.updateInventory();
        PlayerInventory inventory = player.getInventory();
        inventory.setHeldItemSlot(inventory.getHeldItemSlot());
        player.setExp(player.getExp());
        player.setTotalExperience(player.getTotalExperience());
        inventory.setItemInMainHand(inventory.getItemInMainHand());
        inventory.setItemInOffHand(inventory.getItemInOffHand());
        player.setFlySpeed(player.getFlySpeed());
        player.setWalkSpeed(player.getWalkSpeed());
    }
}
