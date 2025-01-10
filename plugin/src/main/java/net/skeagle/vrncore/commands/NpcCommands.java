package net.skeagle.vrncore.commands;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.npc.NpcData;
import net.skeagle.vrncore.npc.NpcManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class NpcCommands {

    private final NpcManager npcManager;

    public NpcCommands(VRNCore plugin) {
        this.npcManager = plugin.getNpcManager();
    }

    @VRNCommand(cmd = "npc", sub = {"create", "add"}, desc = "Creates a npc.", perm = "npc")
    public void onCreateNpc(BukkitCommandActor actor, String name) {
        Player player = actor.requirePlayer();
        if (npcManager.getNpc(name) != null) {
            actor.error("That npc name already exists.");
            return;
        }
        final NpcData npc = npcManager.createNPC(name, player);
        Bukkit.getOnlinePlayers().forEach(npc::createNPCForPlayer);
        actor.reply("NPC created.");
    }

    @VRNCommand(cmd = "npc", sub = {"delete", "remove"}, desc = "Creates a npc.", perm = "npc")
    public void ondeleteNpc(BukkitCommandActor actor, NpcData npc) {
        npcManager.deleteNpc(npc);
        actor.reply("NPC removed.");
    }
}
