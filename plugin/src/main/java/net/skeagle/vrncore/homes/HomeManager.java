package net.skeagle.vrncore.homes;

import net.skeagle.vrnlib.misc.EventListener;
import net.skeagle.vrnlib.misc.LocationUtils;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.autocomplete.AsyncSuggestionProvider;
import revxrsal.commands.node.ExecutionContext;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class HomeManager {

    private final SQLHelper db;
    private final Map<UUID, Set<Home>> homesMap;

    public HomeManager(SQLHelper db) {
        homesMap = new ConcurrentHashMap<>();
        this.db = db;
        new EventListener<>(PlayerJoinEvent.class, e ->
                this.load(e.getPlayer().getUniqueId()).thenApplyAsync(homes -> {
                    if (homes != null)
                        this.homesMap.put(e.getPlayer().getUniqueId(), homes);
                    return null;
                }));
        new EventListener<>(PlayerQuitEvent.class, e -> homesMap.remove(e.getPlayer().getUniqueId()));
    }

    public CompletableFuture<Set<Home>> load(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            SQLHelper.Results res = db.queryResults("SELECT * FROM homes WHERE owner = ?", uuid);
            Set<Home> set = new HashSet<>();
            if (res.isEmpty()) return set;
            res.forEach(home -> {
                String name = home.getString(2);
                UUID owner = UUID.fromString(home.getString(3));
                Location loc = LocationUtils.fromString(home.getString(4));
                set.add(new Home(name, owner, loc));
            });
            return set;
        });
    }

    public void createHome(String name, Player player) {
        Home home = new Home(name, player.getUniqueId(), player.getLocation());
        Set<Home> set = homesMap.containsKey(player.getUniqueId()) ? homesMap.get(player.getUniqueId()) : new HashSet<>();
        set.add(home);
        homesMap.put(player.getUniqueId(), set);
        home.save(db);
    }

    public void deleteHome(Home home) {
        Set<Home> set = homesMap.get(home.owner());
        set.remove(home);
        homesMap.put(home.owner(), set);
        CompletableFuture.runAsync(() -> db.execute("DELETE FROM homes WHERE name = ? AND owner = ?", home.name(), home.owner()));
    }

    public Home getHome(String name, UUID uuid) {
        return homesMap.get(uuid).stream().filter(h -> h.name().equalsIgnoreCase(name)).findFirst().orElse(null);
    }

    public CompletableFuture<Integer> getHomesCount(OfflinePlayer player) {
        if (homesMap.containsKey(player.getUniqueId())) {
            return CompletableFuture.completedFuture(homesMap.size());
        }
        return this.load(player.getUniqueId()).thenApplyAsync(Set::size);
    }

    public List<String> getHomeNames(Player player) {
        return homesMap.get(player.getUniqueId()).stream().map(Home::name).collect(Collectors.toList());
    }

    public CompletableFuture<Set<Home>> getHomes(UUID uuid) {
        if (homesMap.containsKey(uuid)) {
            return CompletableFuture.completedFuture(homesMap.get(uuid));
        }
        return this.load(uuid);
    }
}
