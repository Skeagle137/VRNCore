package net.skeagle.vrncore.trail;

import net.skeagle.vrnlib.misc.FormatUtils;
import org.bukkit.*;

public enum Particles {
    FIREWORK(Particle.FIREWORK, Material.FIREWORK_ROCKET, ParticleProperties.DIRECTIONAL),
    SPLASH(Particle.SPLASH, Material.FISHING_ROD),
    MYCELIUM(Particle.MYCELIUM, Material.MYCELIUM),
    CRIT(Particle.CRIT, Material.IRON_SWORD, 0.7),
    CRIT_ENCHANTED("Crit (Enchanted)", Particle.ENCHANTED_HIT, Material.DIAMOND_SWORD, 0.7),
    SMOKE(Particle.LARGE_SMOKE, Material.SMOKER, ParticleProperties.DIRECTIONAL),
    POTION("Potion Effect", Particle.ENTITY_EFFECT, Material.LINGERING_POTION, ParticleProperties.COLOR),
    WITCH("Witch Potion", Particle.WITCH, Material.SPLASH_POTION),
    DRIP_WATER("Water Drip", Particle.DRIPPING_WATER, Material.WATER_BUCKET),
    DRIP_LAVA("Lava Drip", Particle.DRIPPING_LAVA, Material.LAVA_BUCKET),
    DRIP_HONEY("Honey Drip", Particle.DRIPPING_HONEY, Material.HONEY_BOTTLE),
    FALLING_NECTAR(Particle.FALLING_NECTAR, Material.BEEHIVE),
    FALLING_HONEY(Particle.FALLING_HONEY, Material.BEE_NEST),
    OBSIDIAN_TEAR(Particle.DRIPPING_OBSIDIAN_TEAR, Material.CRYING_OBSIDIAN),
    STORMY(Particle.ANGRY_VILLAGER, Material.FIRE_CHARGE),
    EMERALD(Particle.HAPPY_VILLAGER, Material.EMERALD),
    NOTE(Particle.NOTE, Material.NOTE_BLOCK, ParticleProperties.COLOR),
    ENCHANTMENT_GLYPH(Particle.ENCHANT, Material.ENCHANTING_TABLE, 1.2),
    FLAME(Particle.FLAME, Material.CAMPFIRE, ParticleProperties.DIRECTIONAL),
    SOUL_FLAME(Particle.SOUL_FIRE_FLAME, Material.SOUL_CAMPFIRE, ParticleProperties.DIRECTIONAL),
    SMALL_FLAME(Particle.SMALL_FLAME, Material.CANDLE, 0.1),
    LAVA(Particle.LAVA, Material.MAGMA_BLOCK),
    REDSTONE("Redstone Dust", Particle.DUST, Material.REDSTONE, ParticleProperties.COLOR),
    TRANSITION_DUST(Particle.DUST_COLOR_TRANSITION, Material.MAGENTA_CONCRETE_POWDER, ParticleProperties.COLOR_TRANSITION),
    SNOW(Particle.ITEM_SNOWBALL, Material.SNOWBALL),
    SLIME(Particle.ITEM_SLIME, Material.SLIME_BALL),
    HEART(Particle.HEART, Material.POPPY),
    END_ROD(Particle.END_ROD, Material.END_ROD, 0.12),
    SOUL(Particle.SOUL, Material.SOUL_SAND, 0.1),
    SPORE_CRIMSON("Spore (Crimson)", Particle.CRIMSON_SPORE, Material.CRIMSON_HYPHAE),
    SPORE_WARPED("Spore (Warped)", Particle.WARPED_SPORE, Material.WARPED_HYPHAE),
    SPORE_BLOSSOM(Particle.SPORE_BLOSSOM_AIR, Material.FLOWERING_AZALEA),
    WAX_ON(Particle.WAX_ON, Material.WAXED_COPPER_BLOCK, 12.0),
    WAX_OFF(Particle.WAX_OFF, Material.OXIDIZED_COPPER, 12.0),
    PORTAL(Particle.PORTAL, Material.OBSIDIAN, 1.0),
    REVERSE_PORTAL(Particle.REVERSE_PORTAL, Material.CRYING_OBSIDIAN, 0.1),
    DAMAGE_INDICATOR(Particle.DAMAGE_INDICATOR, Material.STONE_AXE, 0.45),
    CHERRY_LEAVES(Particle.CHERRY_LEAVES, Material.CHERRY_LEAVES),
    TRIAL_SPAWNER(Particle.TRIAL_SPAWNER_DETECTION, Material.TRIAL_KEY),
    TRIAL_SPAWNER_OMINOUS("Trial Spawner (Ominous)", Particle.TRIAL_SPAWNER_DETECTION_OMINOUS, Material.OMINOUS_TRIAL_KEY),
    TRIAL_OMEN(Particle.TRIAL_OMEN, Material.TRIAL_SPAWNER),
    GUST(Particle.SMALL_GUST, Material.WIND_CHARGE);

    private final String name;
    private final Particle particle;
    private final Material mat;
    private double spreadSpeed;
    private ParticleProperties[] properties = {};

    Particles(final Particle particle, final Material mat) {
        this.name = FormatUtils.toTitleCase(this.name().replaceAll("_", " "));
        this.particle = particle;
        this.mat = mat;
    }

    Particles(final Particle particle, final Material mat, final ParticleProperties... properties) {
        this(particle, mat);
        this.properties = properties;
    }

    Particles(final String name, final Particle particle, final Material mat) {
        this.name = name;
        this.particle = particle;
        this.mat = mat;
    }

    Particles(final String name, final Particle particle, final Material mat, final ParticleProperties... properties) {
        this(name, particle, mat);
        this.properties = properties;
    }

    Particles(final Particle particle, final Material mat, final double spreadSpeed) {
        this(particle, mat, ParticleProperties.DIRECTIONAL);
        this.spreadSpeed = spreadSpeed;
    }

    Particles(final String name, final Particle particle, final Material mat, final double spreadSpeed) {
        this(name, particle, mat, ParticleProperties.DIRECTIONAL);
        this.spreadSpeed = spreadSpeed;
    }

    public String getParticleName() {
        return name;
    }

    public Particle get() {
        return particle;
    }

    public Material getMaterial() {
        return mat;
    }

    public double getSpreadSpeed() {
        return spreadSpeed;
    }

    public ParticleProperties[] getProperties() {
        return properties;
    }

    public String getPermission(TrailType type) {
        return "vrn.trails." + toString().toLowerCase() + "." + type.name().toLowerCase();
    }

    public enum ParticleProperties {
        DIRECTIONAL,
        COLOR,
        COLOR_TRANSITION
    }
}