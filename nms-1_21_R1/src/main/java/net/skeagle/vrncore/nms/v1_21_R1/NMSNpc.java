package net.skeagle.vrncore.nms.v1_21_R1;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.skeagle.vrncore.api.Npc;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;

class NMSNpc implements Npc {

    private final GameProfile profile;
    private final ServerPlayer npcPlayer;

    public NMSNpc(GameProfile profile, ServerPlayer npcPlayer) {
        this.profile = profile;
        this.npcPlayer = npcPlayer;
    }

    @Override
    public void updateForPlayer(Player player, String skinTexture, String skinSignature) {
        final ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npcPlayer));
        if (skinTexture != null && skinSignature != null) {
            profile.getProperties().put("textures", new Property("textures", skinTexture, skinSignature));
            SynchedEntityData data = npcPlayer.getEntityData();
            npcPlayer.getEntityData().set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 127);
            nmsPlayer.connection.send(new ClientboundSetEntityDataPacket(npcPlayer.getId(), data.getNonDefaultValues()));
        }
        nmsPlayer.connection.send(new ClientboundAddEntityPacket(npcPlayer.getId(), npcPlayer.getUUID(), npcPlayer.getX(),
                npcPlayer.getY(), npcPlayer.getZ(), npcPlayer.getXRot(), npcPlayer.getYRot(), npcPlayer.getType(),
                0, npcPlayer.getDeltaMovement(), npcPlayer.getYHeadRot()));
        nmsPlayer.connection.send(new ClientboundRotateHeadPacket(npcPlayer, (byte) (npcPlayer.getYHeadRot() * 256 / 360)));
        nmsPlayer.connection.send(new ClientboundMoveEntityPacket.Rot(npcPlayer.getId(), (byte) (npcPlayer.getYRot() * 256 / 360), (byte) (npcPlayer.getXRot() * 256 / 360), true));
        Task.syncDelayed(() -> nmsPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(npcPlayer.getUUID()))), 20);
    }

    @Override
    public void removeForPlayer(Player player) {
        final ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(npcPlayer.getId());
        ((CraftPlayer) player).getHandle().connection.send(destroy);
    }
}
