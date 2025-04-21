package net.skeagle.vrncore.nms.v1_21_R2;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.skeagle.vrncore.api.Npc;
import net.skeagle.vrncore.api.VRNCoreNMS;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class VRNCoreNMS1_21_R2 implements VRNCoreNMS {

    @Override
    public void showDemoMenu(Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.DEMO_EVENT, 0));
    }

    @Override
    public void showCredits(Player player) {
        ((CraftPlayer) player).getHandle().connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.WIN_GAME, 0));
    }

    @Override
    public void showHallucination(Player player) {
        final ServerPlayer entityPlayer = ((CraftPlayer) player).getHandle();
        final EnderDragon dragon = new EnderDragon(EntityType.ENDER_DRAGON, entityPlayer.level());
        dragon.setInvulnerable(true);
        entityPlayer.connection.send(new ClientboundAddEntityPacket(dragon.getId(), dragon.getUUID(), entityPlayer.getX(),
                entityPlayer.getY(), entityPlayer.getZ(), 0, entityPlayer.getYRot() + 180, dragon.getType(), 0,
                dragon.getDeltaMovement(), dragon.getYHeadRot()));
        dragon.remove(Entity.RemovalReason.DISCARDED);
    }

    @Override
    public Property getTexturesProperty(Player player) {
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        return nmsPlayer.getGameProfile().getProperties().get("textures").iterator().next();
    }

    @Override
    public void replaceProperty(Player player, String name, Property oldProperty, Property newProperty) {
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.getGameProfile().getProperties().remove(name, oldProperty);
        nmsPlayer.getGameProfile().getProperties().put(name, newProperty);
    }

    @Override
    public void reloadSkin(Player player) {
        final Location loc = player.getLocation().clone();
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        final ServerLevel level = (ServerLevel) nmsPlayer.level();
        final PlayerList list = nmsPlayer.server.getPlayerList();
        nmsPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(player.getUniqueId())));
        nmsPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, nmsPlayer));
        nmsPlayer.connection.send(new ClientboundRespawnPacket(nmsPlayer.createCommonSpawnInfo(level), ClientboundRespawnPacket.KEEP_ENTITY_DATA));
        player.teleport(loc);
        list.sendLevelInfo(nmsPlayer, level);
        list.sendPlayerPermissionLevel(nmsPlayer);
        nmsPlayer.getBukkitEntity().updateScaledHealth(true);
        list.sendAllPlayerInfo(nmsPlayer);
        nmsPlayer.onUpdateAbilities();
        for (var mobEffect : nmsPlayer.getActiveEffects()) {
            nmsPlayer.connection.send(new ClientboundUpdateMobEffectPacket(nmsPlayer.getId(), mobEffect, false));
        }
    }

    @Override
    public Npc createNpc(UUID uuid, String name, String displayName, Location location, String skinTexture, String skinSignature) {
        final GameProfile profile = new GameProfile(uuid, name);
        MinecraftServer server = ((CraftServer) Bukkit.getServer()).getServer();
        final ServerPlayer npc = new ServerPlayer(server, ((CraftWorld) location.getWorld()).getHandle(), profile, ClientInformation.createDefault());
        npc.connection = new ServerGamePacketListenerImpl(server, new Connection(PacketFlow.SERVERBOUND), npc, CommonListenerCookie.createInitial(profile, false));
        npc.forceSetPositionRotation(location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        if (displayName != null) {
            npc.displayName = displayName;
        }
        return new NMSNpc(profile, npc, skinTexture, skinSignature);
    }
}
