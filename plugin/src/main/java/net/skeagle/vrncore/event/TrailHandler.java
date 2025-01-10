package net.skeagle.vrncore.event;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.playerdata.TrailData;
import net.skeagle.vrncore.trail.Particles;
import net.skeagle.vrncore.trail.TrailType;
import net.skeagle.vrncore.trail.style.idle.IdleStyle;
import net.skeagle.vrncore.utils.AFKManager;
import net.skeagle.vrnlib.misc.EventListener;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TrailHandler implements Listener {

    private Task trailTask;
    private final Map<Player, TrailData> trailCache;
    private final Map<Player, TrailData> projectileCache;
    private final Map<Player, Set<Projectile>> activeProjectiles;

    public TrailHandler(VRNCore plugin) {
        trailCache = Collections.synchronizedMap(new ConcurrentHashMap<>());
        projectileCache = Collections.synchronizedMap(new ConcurrentHashMap<>());
        activeProjectiles = Collections.synchronizedMap(new ConcurrentHashMap<>());
        this.startParticleTask();
        new EventListener<>(PlayerJoinEvent.class, e -> {
            plugin.getPlayerManager().getData(e.getPlayer().getUniqueId()).thenAccept(data -> {
                if (data.getPlayerTrailData() != null) {
                    trailCache.put(e.getPlayer(), data.getPlayerTrailData());
                }
                if (data.getArrowTrailData() != null) {
                    projectileCache.put(e.getPlayer(), data.getArrowTrailData());
                }
            });
        });
        new EventListener<>(PlayerQuitEvent.class, e -> {
            trailCache.remove(e.getPlayer());
            projectileCache.remove(e.getPlayer());
            activeProjectiles.remove(e.getPlayer());
        });
        new EventListener<>(TrailDataUpdateEvent.class, e -> {
            if (e.getTrailData().getType() == TrailType.PLAYER) {
                if (e.getTrailData().getParticle() == null) {
                    trailCache.remove(e.getPlayer());
                    return;
                }
                trailCache.put(e.getPlayer(), e.getTrailData());
            }
            else {
                if (e.getTrailData().getParticle() == null) {
                    projectileCache.remove(e.getPlayer());
                    activeProjectiles.remove(e.getPlayer());
                    return;
                }
                projectileCache.put(e.getPlayer(), e.getTrailData());
            }

            e.getTrailData().getTrailStyle().updateData(e.getTrailData());
        });
    }

    public void startParticleTask() {
        if (trailTask != null) {
            trailTask.cancel();
        }

        trailTask = Task.syncRepeating(() -> {
            trailCache.keySet().forEach(p -> {
                this.handleTrail(p, TrailType.PLAYER, (player, target, data) -> {
                    if (data.getTrailStyle() instanceof IdleStyle && !AFKManager.getAfkManager(player).isIdle()) return;
                    data.getTrailStyle().tick(player, player.getLocation());
                    data.getTrailStyle().step();
                });
            });
            projectileCache.keySet().forEach(p -> {
                Set<Projectile> projectiles = activeProjectiles.get(p);
                if (projectiles == null) return;
                int size = projectiles.size();
                projectiles.removeIf(projectile -> !projectile.isValid() || projectile.isOnGround());
                if (projectiles.size() < size) {
                    activeProjectiles.put(p, projectiles);
                }
                for (Projectile projectile : projectiles)
                    this.handleTrail(projectile, TrailType.PROJECTILE, (player, target, data) ->
                            data.getTrailStyle().tick(target, target.getLocation()));
                TrailData data = projectileCache.get(p);
                if (data != null)
                    data.getTrailStyle().step();
            });

        }, 1, 1);
    }

    @EventHandler
    public void onArrowShot(final ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() == null || !(e.getEntity().getShooter() instanceof Player player) ||
                !(e.getEntity() instanceof ThrowableProjectile || e.getEntity() instanceof Arrow)) return;
        Set<Projectile> updated = activeProjectiles.getOrDefault(player, new HashSet<>());
        updated.add(e.getEntity());
        activeProjectiles.put(player, updated);
    }

    private void handleTrail(Entity target, TrailType type, TrailConsumer consumer) {
        Player player = (Player) (type == TrailType.PLAYER ? target : ((Projectile) target).getShooter());
        TrailData data = (type == TrailType.PLAYER ? trailCache : projectileCache).get(player);
        if (data == null) {
            trailCache.remove(player);
            return;
        }
        final Particles trail = data.getParticle();
        if (trail == null) {
            data.setParticle(player, null);
            return;
        }
        if (!player.hasPermission(trail.getPermission(type))) {
            data.setParticle(player, null);
            return;
        }
        consumer.handle(player, target, data);
    }

    private interface TrailConsumer {
        void handle(Player player, Entity target, TrailData data);
    }
}
