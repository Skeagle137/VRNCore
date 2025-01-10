package net.skeagle.vrncore.configurable.rewards;

import net.skeagle.vrnlib.config.annotations.ConfigMappable;
import net.skeagle.vrnlib.config.annotations.ConfigPath;
import net.skeagle.vrnlib.misc.EventListener;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.meta.FireworkMeta;

import java.util.Random;

@ConfigMappable
public class Reward {

    @ConfigPath
    private String name;
    private boolean permission;
    String message;
    String title;
    String subtitle;
    RewardAction action;
    String group;
    private long time;

    private Reward() {
    }

    public Reward(final String name, final boolean permission, final String message,
                  final String title, final String subtitle, final RewardAction action, final String group, final long time) {
        this.name = name;
        this.permission = permission;
        this.message = message;
        this.title = title;
        this.subtitle = subtitle;
        this.action = action;
        this.group = group;
        this.time = time;
    }

    public boolean checkPerm(final Player player) {
        return !permission || player.hasPermission("vrn.rewards." + name);
    }

    String replaceVars(Player player, String s) {
        s = s.replaceAll("%player%", player.getName());
        s = s.replaceAll("%group%", group);
        return s;
    }

    private FireworkEffect buildFireworkEffect() {
        final FireworkEffect.Builder fwB = FireworkEffect.builder();
        final Random r = new Random();
        fwB.flicker(r.nextBoolean());
        fwB.trail(r.nextBoolean());
        fwB.with(FireworkEffect.Type.BALL);
        fwB.withColor(Color.fromRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
        fwB.withFade(Color.fromRGB(r.nextInt(256), r.nextInt(256), r.nextInt(256)));
        return fwB.build();
    }

    void sendFirework(final Location loc) {
        final Firework fw = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK_ROCKET);
        final String id = String.valueOf(fw.getEntityId());
        new EventListener<>(EntityDamageByEntityEvent.class, (l, e) -> {
            if (e.getDamager().getType() == EntityType.FIREWORK_ROCKET && e.getDamager().getScoreboardTags().stream().anyMatch(id::equals)) {
                e.setCancelled(true);
            }
            Task.asyncDelayed(l::unregister, 60);
        });
        final FireworkMeta fwmeta = fw.getFireworkMeta();
        fwmeta.setPower(5);
        fwmeta.addEffect(buildFireworkEffect());
        fw.addScoreboardTag(id);
        fw.setFireworkMeta(fwmeta);
        fw.setTicksLived(2);
        fw.detonate();
    }

    public String getName() {
        return name;
    }

    public long getTime() {
        return time;
    }
}
