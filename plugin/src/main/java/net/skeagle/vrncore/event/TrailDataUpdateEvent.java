package net.skeagle.vrncore.event;

import net.skeagle.vrncore.playerdata.TrailData;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;

public class TrailDataUpdateEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();
    private final TrailData data;

    public TrailDataUpdateEvent(Player player, TrailData data) {
        super(player);
        this.data = data;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public TrailData getTrailData() {
        return data;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

}
