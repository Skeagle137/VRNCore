package net.skeagle.vrncore.commands;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.npc.NpcData;
import net.skeagle.vrncore.npc.NpcManager;
import net.skeagle.vrncore.utils.Skin;
import net.skeagle.vrncore.utils.SkinUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class NpcCommands {

    private final NpcManager npcManager;

    public NpcCommands(VRNCore plugin) {
        this.npcManager = plugin.getNpcManager();
    }

    @VRNCommand(cmd = "npc", sub = "create", desc = "Creates an NPC.", perm = "npc")
    public void onCreateNpc(BukkitCommandActor actor, String name) {
        Player player = actor.requirePlayer();
        if (npcManager.getNpc(name) != null) {
            actor.error("That npc name already exists.");
            return;
        }
        NpcData npc = npcManager.createNPC(name, player);
        Bukkit.getOnlinePlayers().forEach(npc::createNPCForPlayer);
        actor.reply("NPC created.");
    }

    @VRNCommand(cmd = "npc", sub = "remove", desc = "Removes an NPC.", perm = "npc")
    public void onRemoveNpc(BukkitCommandActor actor, NpcData npc) {
        npcManager.deleteNpc(npc);
        actor.reply("NPC removed.");
    }

    @VRNCommand(cmd = "npc", sub = "name", desc = "Sets the name of an NPC.", perm = "npc")
    public void onSetNpcName(BukkitCommandActor actor, NpcData npc, String name) {
        npc.setName(name);
        actor.reply("Set NPC name to &a" + name + "&7.");
    }

    @VRNCommand(cmd = "npc", sub = "display", desc = "Sets the display name of an NPC.", perm = "npc")
    public void onSetNpcDisplayName(BukkitCommandActor actor, NpcData npc, String displayName) {
        npc.setDisplay(displayName);
        actor.reply("Set NPC display name to &a" + displayName + "&r&7.");
    }

    @VRNCommand(cmd = "npc", sub = "skin", desc = "Sets the skin for an NPC.", perm = "npc")
    public void onSetNpcSkin(BukkitCommandActor actor, NpcData npc, String skinName) {
        Skin skin = SkinUtil.getSkin(skinName);
        if (skin == null) {
            actor.error("Could not be retrieve skin URL. Likely there is no player with this name.");
            return;
        }
        npc.setSkin(skin);
        actor.reply("NPC skin changed.");
    }
}
