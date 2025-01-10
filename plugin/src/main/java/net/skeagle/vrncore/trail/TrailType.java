package net.skeagle.vrncore.trail;

import net.skeagle.vrnlib.misc.FormatUtils;

public enum TrailType {
    PLAYER,
    PROJECTILE;

    public String getName() {
        return FormatUtils.toTitleCase(this.name());
    }
}
