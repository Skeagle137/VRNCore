package net.skeagle.vrncore.homes;

import net.skeagle.vrnlib.misc.LocationUtils;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Location;

import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class Home {
    private final String name;
    private final UUID owner;
    private final Location location;

    public Home(String name, UUID owner, Location location) {
        this.name = name;
        this.owner = owner;
        this.location = location;
    }

    public CompletableFuture<Void> save(SQLHelper db) {
        return CompletableFuture.runAsync(() ->
                db.execute("INSERT INTO homes (name, owner, location) VALUES (?, ?, ?)",
                        name, owner, LocationUtils.toString(location)));
    }

    public String name() {
        return name;
    }

    public UUID owner() {
        return owner;
    }

    public Location location() {
        return location;
    }
}
