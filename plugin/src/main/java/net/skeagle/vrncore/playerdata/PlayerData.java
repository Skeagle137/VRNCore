package net.skeagle.vrncore.playerdata;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.hook.HookManager;
import net.skeagle.vrncore.trail.TrailType;
import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrncore.configurable.Settings;
import net.skeagle.vrnlib.misc.FormatUtils;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private String nickname;
    private final TrailData playerTrailData;
    private final TrailData arrowTrailData;
    private final PlayerStates states;
    private long timePlayed;
    private Player player;

    PlayerData(final UUID uuid) {
        this.uuid = uuid;
        this.playerTrailData = new TrailData(TrailType.PLAYER);
        this.arrowTrailData = new TrailData(TrailType.PROJECTILE);
        this.states = new PlayerStates();
    }

    PlayerData(final UUID uuid, final String nickname, final TrailData playerTrailData, final TrailData arrowTrailData, final PlayerStates states, final long timePlayed) {
        this.uuid = uuid;
        this.nickname = nickname;
        this.playerTrailData = playerTrailData;
        this.arrowTrailData = arrowTrailData;
        this.states = states;
        this.timePlayed = timePlayed;
    }

    public UUID getUUID() {
        return uuid;
    }

    public String getNick() {
        return nickname;
    }

    public void setNick(final String nickname) {
        this.nickname = nickname;
        this.updateName();
    }

    public String getName() {
        return nickname != null ? FormatUtils.color(nickname + "&r") : this.getPlayer().getName();
    }

    public void updateName() {
        this.updateName(this.getPlayer());
    }

    public void updateName(Player player) {
        if (player != null) {
            player.setDisplayName(this.getName());
            String listname = null;
            if (HookManager.isVaultLoaded()) {
                listname = HookManager.format(this, Settings.listFormat);
            }
            player.setPlayerListName(FormatUtils.color(listname != null ? listname : this.getName()));
        }
    }

    public TrailData getPlayerTrailData() {
        return playerTrailData;
    }

    public TrailData getArrowTrailData() {
        return arrowTrailData;
    }

    public PlayerStates getStates() {
        return states;
    }

    public long getTimePlayed() {
        return timePlayed;
    }

    public void setTimePlayed(final long timeplayed) {
        this.timePlayed = timeplayed;
    }

    public Player getPlayer() {
        if (player == null) {
            this.player = Bukkit.getPlayer(uuid);
        }
        return player;
    }

    public void save() {
        final SQLHelper db = VRNCore.getInstance().getDB();
        db.execute("REPLACE INTO playerdata (id, nick, playerTrailData, arrowTrailData, playerStates, timePlayed) VALUES (?, ?, ?, ?, ?, ?)",
                uuid.toString(), nickname, VRNUtil.GSON.toJson(playerTrailData.serialize()), VRNUtil.GSON.toJson(arrowTrailData.serialize()), VRNUtil.GSON.toJson(states), timePlayed);
    }
}
