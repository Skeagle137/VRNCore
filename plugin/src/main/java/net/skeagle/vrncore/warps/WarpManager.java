package net.skeagle.vrncore.warps;

import net.skeagle.vrnlib.misc.LocationUtils;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;

public class WarpManager {

    private final SQLHelper db;
    private Set<Warp> warps;

    public WarpManager(SQLHelper db) {
        this.db = db;
        this.warps = Collections.synchronizedSet(new HashSet<>());
        load().thenApplyAsync(warps -> {
            if (warps != null)
                this.warps = warps;
            return null;
        });
    }

    public CompletableFuture<Set<Warp>> load() {
        return CompletableFuture.supplyAsync(() -> {
            SQLHelper.Results res = db.queryResults("SELECT * FROM warps");
            Set<Warp> set = new HashSet<>();
            if (res.isEmpty()) return set;
            res.forEach(warp -> {
                String name = warp.getString(2);
                UUID owner = UUID.fromString(warp.getString(3));
                Location loc = LocationUtils.fromString(warp.getString(4));
                set.add(new Warp(name, owner, loc));
            });
            return set;
        });
    }

    public void createWarp(Player p, String name) {
        Warp warp = new Warp(name, p.getUniqueId(), p.getLocation());
        warps.add(warp);
        warp.save(db);
    }

    public Warp getWarp(String name) {
        return warps.stream().filter(w -> w.getName().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public long getWarpsOwned(Player player) {
        return warps.stream().filter(w -> w.getOwner().equals(player.getUniqueId())).count();
    }

    public CompletableFuture<Void> deleteWarp(Warp warp) {
        warps.remove(warp);
        return CompletableFuture.runAsync(() -> db.execute("DELETE FROM warps WHERE name = ? AND owner = ?", warp.getName(), warp.getOwner()));
    }

    public Set<Warp> getWarps() {
        return warps;
    }
}
