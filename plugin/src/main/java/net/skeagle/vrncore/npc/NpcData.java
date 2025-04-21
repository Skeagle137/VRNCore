package net.skeagle.vrncore.npc;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.api.Npc;
import net.skeagle.vrncore.api.VRNCoreNMS;
import net.skeagle.vrncore.utils.Skin;
import net.skeagle.vrnlib.misc.LocationUtils;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public class NpcData {
    private final VRNCoreNMS api;
    private final UUID uuid;
    private String name;
    private String display;
    private Location location;
    private Skin skin;
    private boolean rotateHead;
    private Npc npc;

    public NpcData(VRNCoreNMS api, UUID uuid, String name, String display, Location location, Skin skin, boolean rotateHead) {
        this.uuid = uuid;
        this.api = api;
        this.name = name;
        this.display = display;
        this.location = location;
        this.skin = skin;
        this.rotateHead = rotateHead;
    }

    public void createNPCForPlayer(Player player) {
        String texture = skin != null ? skin.getTexture() : null;
        String signature = skin != null ? skin.getSignature() : null;
        npc = api.createNpc(uuid, name, display, location, texture, signature);
        npc.updateForPlayer(player);
    }

    public void removeForPlayer(Player player) {
        npc.removeForPlayer(player);
    }

    public void save() {
        SQLHelper db = VRNCore.getInstance().getDB();
        Task.asyncDelayed(() -> {
            db.execute("REPLACE INTO npc (id, name, display, location, skin, rotatehead) VALUES (?, ?, ?, ?, ?, ?)",
                    uuid.toString(), name, display, LocationUtils.toString(location, true), skin != null ? skin.serialize() : null, rotateHead);
        });
    }

    public void updateForAll() {
        Bukkit.getOnlinePlayers().forEach(npc::updateForPlayer);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        npc.setName(name);
        this.updateForAll();
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
        npc.setDisplayName(display);
        this.updateForAll();
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public Skin getSkin() {
        return skin;
    }

    public void setSkin(Skin skin) {
        this.skin = skin;
        String texture = skin != null ? skin.getTexture() : null;
        String signature = skin != null ? skin.getSignature() : null;
        npc.setSkin(texture, signature);
        if (skin != null) {
            this.updateForAll();
        }
    }

    public boolean shouldRotateHead() {
        return rotateHead;
    }

    public void setRotateHead(boolean rotateHead) {
        this.rotateHead = rotateHead;
    }
}
