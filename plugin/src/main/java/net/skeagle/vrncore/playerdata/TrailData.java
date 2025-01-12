package net.skeagle.vrncore.playerdata;

import com.google.gson.JsonObject;
import net.skeagle.vrncore.trail.Particles;
import net.skeagle.vrncore.trail.Style;
import net.skeagle.vrncore.trail.TrailColors;
import net.skeagle.vrncore.trail.style.TrailStyle;
import net.skeagle.vrncore.event.TrailDataUpdateEvent;
import net.skeagle.vrncore.trail.TrailType;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Player;

public class TrailData {

    private final TrailType type;
    private Particles particle;
    private Color color;
    private Color fade;
    private double size;
    private Style style;
    private TrailStyle trailStyle;
    private double note;

    public TrailData(TrailType type) {
        this.type = type;
        this.color = Color.RED;
        this.fade = Color.WHITE;
        this.size = 1.0;
        this.style = Style.DEFAULT;
        this.trailStyle = Style.DEFAULT.create(this);
        this.note = TrailColors.RED.getNote();
    }

    public TrailData(TrailType type, Particles particle, Color color, Color fade, double size, Style style) {
        this.type = type;
        this.particle = particle;
        this.color = color;
        this.fade = fade;
        this.size = size;
        this.style = style;
        this.trailStyle = style.create(this);
        TrailColors trailColor = TrailColors.getFromColor(color);
        this.note = trailColor != null ? trailColor.getNote() : TrailColors.RED.getNote();
    }

    public TrailType getType() {
        return type;
    }

    public Particles getParticle() {
        return particle;
    }

    public void setParticle(Player player, Particles particle) {
        this.particle = particle;
        Bukkit.getPluginManager().callEvent(new TrailDataUpdateEvent(player, this));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Player player, Color color) {
        this.color = color;
        TrailColors trailColor = TrailColors.getFromColor(color);
        if (trailColor != null) {
            this.note = trailColor.getNote();
        }
        Bukkit.getPluginManager().callEvent(new TrailDataUpdateEvent(player, this));
    }

    public Color getFade() {
        return fade;
    }

    public void setFade(Player player, Color fade) {
        this.fade = fade;
        Bukkit.getPluginManager().callEvent(new TrailDataUpdateEvent(player, this));
    }

    public double getSize() {
        return size;
    }

    public void setSize(Player player, double size) {
        this.size = size;
        Bukkit.getPluginManager().callEvent(new TrailDataUpdateEvent(player, this));
    }

    public Style getStyle() {
        return style;
    }

    public void setStyle(Player player, Style style) {
        this.style = style;
        this.trailStyle = style.create(this);
        Bukkit.getPluginManager().callEvent(new TrailDataUpdateEvent(player, this));
    }

    public TrailStyle getTrailStyle() {
        return trailStyle;
    }

    public double getNote() {
        return note;
    }

    JsonObject serialize() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("particle", particle == null ? null : particle.toString());
        jsonObject.addProperty("color", color.asRGB());
        jsonObject.addProperty("fade", fade.asRGB());
        jsonObject.addProperty("size", size);
        jsonObject.addProperty("style", style.toString());
        return jsonObject;
    }

    static TrailData deserialize(TrailType type, JsonObject jsonObject) {
        try {
            return new TrailData(type, Particles.valueOf(jsonObject.get("particle").getAsString()),
                    Color.fromRGB(jsonObject.get("color").getAsInt()), Color.fromRGB(jsonObject.get("fade").getAsInt()),
                    jsonObject.get("size").getAsDouble(), Style.valueOf(jsonObject.get("style").getAsString()));
        }
        catch (Exception ex) {
            return new TrailData(type);
        }
    }
}
