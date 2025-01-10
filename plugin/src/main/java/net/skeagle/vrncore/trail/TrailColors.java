package net.skeagle.vrncore.trail;

import net.skeagle.vrnlib.misc.FormatUtils;
import org.bukkit.ChatColor;
import org.bukkit.Color;
import org.bukkit.Material;

public enum TrailColors {
    RED(Material.RED_WOOL, Material.RED_DYE, 6.0),
    GOLD(Material.ORANGE_WOOL, Material.ORANGE_DYE, 3.24),
    YELLOW(Material.YELLOW_WOOL, Material.YELLOW_DYE, 2.28),
    DARK_GREEN(Material.GREEN_WOOL, Material.GREEN_DYE, 1.32),
    GREEN(Material.LIME_WOOL, Material.LIME_DYE, 23.52),
    DARK_BLUE(Material.BLUE_WOOL, Material.BLUE_DYE, 13.0),
    DARK_AQUA(Material.CYAN_WOOL, Material.CYAN_DYE, 17.76),
    AQUA(Material.LIGHT_BLUE_WOOL, Material.LIGHT_BLUE_DYE, 18.768),
    DARK_PURPLE(Material.PURPLE_WOOL, Material.PURPLE_DYE, 11.0),
    LIGHT_PURPLE(Material.MAGENTA_WOOL, Material.MAGENTA_DYE, 8.4),
    WHITE(Material.WHITE_WOOL, Material.WHITE_DYE),
    BLACK(Material.BLACK_WOOL, Material.BLACK_DYE);

    private final Material wool;
    private final Material dye;
    private final double note;
    private final Color color;

    TrailColors(Material wool, Material dye) {
        this(wool, dye, 0);
    }

    TrailColors(Material wool, Material dye, double note) {
        this.wool = wool;
        this.dye = dye;
        this.note = note;
        this.color = Color.fromRGB(this.getChatColor().asBungee().getColor().getRed(), this.getChatColor().asBungee().getColor().getGreen(),
                this.getChatColor().asBungee().getColor().getBlue());
    }

    public Material getWool() {
        return wool;
    }

    public Material getDye() {
        return dye;
    }

    public double getNote() {
        return note;
    }

    public static TrailColors getFromColor(Color color) {
        java.awt.Color awtColor = new java.awt.Color(color.getRed(), color.getGreen(), color.getBlue());
        for (ChatColor chatcolor : ChatColor.values()) {
            if (chatcolor.isColor() && chatcolor.asBungee().getColor().getRGB() == awtColor.getRGB()) {
                return TrailColors.valueOf(chatcolor.name());
            }
        }
        return null;
    }

    public String getDisplayName() {
        return this.getChatColor() + FormatUtils.toTitleCase(this.name().replaceAll("_", " "));
    }

    public Color getColor() {
        return color;
    }

    private ChatColor getChatColor() {
        return ChatColor.valueOf(this.name());
    }
}
