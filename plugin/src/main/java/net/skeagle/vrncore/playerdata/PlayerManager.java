package net.skeagle.vrncore.playerdata;

import com.google.gson.JsonObject;
import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.trail.TrailType;
import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnlib.misc.UserCache;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerManager {

    private final Map<UUID, PlayerData> playerData = Collections.synchronizedMap(new ConcurrentHashMap<>());
    private final Map<UUID, CompletableFuture<PlayerData>> pending = new ConcurrentHashMap<>();
    private final VRNCore plugin;

    public PlayerManager(VRNCore plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<PlayerData> getData(final UUID uuid) {
        PlayerData data = playerData.get(uuid);
        if (data != null) {
            return CompletableFuture.completedFuture(data);
        }
        CompletableFuture<PlayerData> retrieved = pending.get(uuid);
        if (retrieved != null) {
            return retrieved;
        }
        retrieved = this.load(uuid);
        pending.put(uuid, retrieved);
        return retrieved.whenComplete((res, ex) -> {
            pending.remove(uuid);
            if (res != null) {
                playerData.put(uuid, res);
            }
        });
    }

    private CompletableFuture<PlayerData> load(final UUID uuid) {
        final SQLHelper db = plugin.getDB();
        return CompletableFuture.supplyAsync(() -> {
            SQLHelper.Results res = db.queryResults("SELECT * FROM playerdata WHERE id = (?)", uuid.toString());
            if (res.isEmpty()) {
                return new PlayerData(uuid);
            }
            final JsonObject playerTrailData = VRNUtil.GSON.fromJson(res.getString(3), JsonObject.class);
            final JsonObject arrowTrailData = VRNUtil.GSON.fromJson(res.getString(4), JsonObject.class);
            final PlayerStates states = VRNUtil.GSON.fromJson(res.getString(5), PlayerStates.class);
            return new PlayerData(UUID.fromString(res.getString(1)), res.getString(2),
                    playerTrailData != null ? TrailData.deserialize(TrailType.PLAYER, playerTrailData) : new TrailData(TrailType.PLAYER),
                    arrowTrailData != null ? TrailData.deserialize(TrailType.PROJECTILE, arrowTrailData) : new TrailData(TrailType.PROJECTILE),
                    states != null ? states : new PlayerStates(), res.getLong(6));
        });
    }

    /*public CompletableFuture<Boolean> offlineNickConflicts(String nick) {
        return this.getOfflinePlayer(nick).thenComposeAsync(offPlayer -> {
            if (offPlayer != null) {
                return null;
            }
            return CompletableFuture.supplyAsync(() -> plugin.getDB().queryResultStringList("SELECT nick FROM playerdata WHERE nick REGEXP '&(?:[0-9a-fA-Fk-oK-ORr]|#([0-9a-fA-F]{6}))'"));
        }).thenApply(nickname -> {
            System.out.println(nickname);
            return nickname == null || !nickname.isEmpty();
        });
    }*/

    public CompletableFuture<OfflinePlayer> getOfflinePlayer(String name) {
        final Player player = Bukkit.getPlayer(name);
        if (player != null)
            return CompletableFuture.completedFuture(player);
        final OfflinePlayer offPlayer = UserCache.getOfflinePlayer(name);
        if (offPlayer != null)
            return CompletableFuture.completedFuture(offPlayer);
        return this.checkHasPlayed(name);
    }

    private CompletableFuture<OfflinePlayer> checkHasPlayed(String name) {
        return CompletableFuture.supplyAsync(() -> Bukkit.getOfflinePlayer(name)).handleAsync((res, ex) -> {
            if (res != null && res.hasPlayedBefore()) {
                return res;
            }
            return null;
        });
    }

    public void save() {
        playerData.values().forEach(PlayerData::save);
    }
}
