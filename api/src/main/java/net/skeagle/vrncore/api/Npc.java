package net.skeagle.vrncore.api;

import org.bukkit.entity.Player;

public interface Npc {

    void setName(String name);

    void setDisplayName(String displayName);

    void setSkin(String skinTexture, String skinSignature);

    void updateForPlayer(Player player);

    void removeForPlayer(Player player);
}
