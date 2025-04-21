package net.skeagle.vrncore.api;

import com.mojang.authlib.properties.Property;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;

public interface VRNCoreNMS {

    void showDemoMenu(Player player);

    void showCredits(Player player);

    void showHallucination(Player player);

    Property getTexturesProperty(Player player);

    void replaceProperty(Player player, String name, Property oldProperty, Property newProperty);

    void reloadSkin(Player player);

    Npc createNpc(UUID uuid, String name, String displayName, Location location, String skinTexture, String skinSignature);
}
