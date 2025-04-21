package net.skeagle.vrncore.npc;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.api.VRNCoreNMS;
import net.skeagle.vrncore.utils.Skin;
import net.skeagle.vrncore.utils.SkinUtil;
import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrnlib.misc.EventListener;
import net.skeagle.vrnlib.misc.LocationUtils;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NpcManager {

    private final VRNCoreNMS api;
    private final List<NpcData> npcList;

    public NpcManager(VRNCoreNMS api) {
        this.api = api;
        this.npcList = new ArrayList<>();
        this.load();
        new EventListener<>(VRNCore.getInstance(), PlayerJoinEvent.class,
                e -> loadNpcsForPlayer(e.getPlayer()));
    }

    protected void load() {
        final SQLHelper db = VRNCore.getInstance().getDB();
        final SQLHelper.Results res = db.queryResults("SELECT * FROM npc");
        res.forEach(npc -> {
            final UUID id = UUID.fromString(npc.getString(1));
            final String name = npc.getString(2);
            final String display = npc.getString(3);
            final Location loc = LocationUtils.fromString(npc.getString(4));
            final Skin skin = npc.getString(5) != null ? VRNUtil.GSON.fromJson(npc.getString(5), Skin.class) : null;
            final boolean rotateHead = npc.getBoolean(6);
            npcList.add(new NpcData(api, id, name, display, loc, skin, rotateHead));
        });
    }

    public void loadNpcsForPlayer(final Player player) {
        npcList.forEach(npc -> npc.createNPCForPlayer(player));
    }

    public void deleteNpc(final NpcData npc) {
        final SQLHelper db = VRNCore.getInstance().getDB();
        db.execute("DELETE FROM npc WHERE name = (?)", npc.getName());
        npcList.remove(npc);
        Bukkit.getOnlinePlayers().forEach(npc::removeForPlayer);
    }

    public NpcData createNPC(final String name, final Player player) {
        final NpcData npc = new NpcData(api, UUID.randomUUID(), name, name, player.getLocation().clone(), SkinUtil.getSkin(name), false);
        npcList.add(npc);
        npc.save();
        return npc;
    }

    public NpcData getNpc(final String s) {
        return this.npcList.stream().filter(npc -> npc.getName().equalsIgnoreCase(s)).findFirst().orElse(null);
    }

    public List<NpcData> getNpcs() {
        return npcList;
    }
}
