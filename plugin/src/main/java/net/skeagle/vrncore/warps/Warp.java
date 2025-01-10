package net.skeagle.vrncore.warps;

import net.skeagle.vrnlib.misc.LocationUtils;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Location;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class Warp {
    private final String name;
    private final UUID owner;
    private final Location location;

    public Warp(String name, UUID owner, Location location) {
        this.name = name;
        this.location = location;
        this.owner = owner;
    }

    public CompletableFuture<Void> save(SQLHelper db) {
        return CompletableFuture.runAsync(() ->
                db.execute("INSERT INTO warps (name, owner, location) VALUES (?, ?, ?)",
                        name, owner, LocationUtils.toString(location)));
    }

    public String getName() {
        return this.name;
    }

    public Location getLocation() {
        return this.location;
    }

    public UUID getOwner() {
        return owner;
    }
}
