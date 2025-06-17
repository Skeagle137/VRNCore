package net.skeagle.vrncore.nms.v1_21_R5;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import net.minecraft.network.protocol.game.*;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
import net.skeagle.vrncore.api.Npc;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnlib.nms.NMSObject;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Set;

class NMSNpc implements Npc {

    private final static Scoreboard SCOREBOARD = new Scoreboard();
    private final PlayerTeam team;
    private final GameProfile profile;
    private final ServerPlayer npcPlayer;
    private String skinTexture;
    private String skinSignature;
    private NMSObject profileWrapper;

    public NMSNpc(GameProfile profile, ServerPlayer npcPlayer, String skinTexture, String skinSignature) {
        this.team = new PlayerTeam(SCOREBOARD, "display");
        this.profile = profile;
        this.npcPlayer = npcPlayer;
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
    }

    @Override
    public void setName(String name) {
        if (profileWrapper == null) {
            profileWrapper = new NMSObject(profile);
        }
        profileWrapper.setField("name", name);
    }

    @Override
    public void setDisplayName(String displayName) {

    }

    @Override
    public void setSkin(String skinTexture, String skinSignature) {
        this.skinTexture = skinTexture;
        this.skinSignature = skinSignature;
        if (profile.getProperties().containsKey("textures")) {
            Property property = profile.getProperties().get("textures").iterator().next();
            profile.getProperties().remove("textures", property);
        }
        if (skinTexture != null && skinSignature != null) {
            profile.getProperties().put("textures", new Property("textures", skinTexture, skinSignature));
        }
    }

    @Override
    public void updateForPlayer(Player player) {
        ServerPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
        nmsPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.ADD_PLAYER, npcPlayer));
        nmsPlayer.connection.send(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, npcPlayer));
        //nmsPlayer.connection.send(ClientboundSetPlayerTeamPacket.createPlayerPacket(team, player.getName(), ClientboundSetPlayerTeamPacket.Action.ADD));
        if (skinTexture != null && skinSignature != null) {
            npcPlayer.getEntityData().set(new EntityDataAccessor<>(17, EntityDataSerializers.BYTE), (byte) 127);
            Task.syncDelayed(() -> nmsPlayer.connection.send(new ClientboundSetEntityDataPacket(npcPlayer.getId(), npcPlayer.getEntityData().packAll())), 5);
        }
        ServerEntity serverEntity = new ServerEntity(npcPlayer.level(), npcPlayer, 0, false, packet -> {}, (p, l) -> {}, Set.of());
        nmsPlayer.connection.send(npcPlayer.getAddEntityPacket(serverEntity));
        byte headRot = Mth.packDegrees(npcPlayer.getYHeadRot());
        nmsPlayer.connection.send(new ClientboundRotateHeadPacket(npcPlayer, headRot));
        Task.syncDelayed(() -> nmsPlayer.connection.send(new ClientboundPlayerInfoRemovePacket(List.of(npcPlayer.getUUID()))), 15);
    }

    @Override
    public void removeForPlayer(Player player) {
        final ClientboundRemoveEntitiesPacket destroy = new ClientboundRemoveEntitiesPacket(npcPlayer.getId());
        ((CraftPlayer) player).getHandle().connection.send(destroy);
    }
}
