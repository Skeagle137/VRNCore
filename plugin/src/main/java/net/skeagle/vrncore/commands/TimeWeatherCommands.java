package net.skeagle.vrncore.commands;

import org.bukkit.WeatherType;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

public class TimeWeatherCommands {

    @VRNCommand(cmd = "day", desc = "Sets the time to day.", perm = "time")
    public void onDay(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.getLocation().getWorld().setTime(1000);
        actor.reply("Time set to day.");
    }

    @VRNCommand(cmd = "night", desc = "Sets the time to night.", perm = "time")
    public void onNight(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.getLocation().getWorld().setTime(13000);
        actor.reply("Time set to night.");
    }

    @VRNCommand(cmd = "sun", desc = "Sets the weather to sun.", perm = "weather")
    public void onSun(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.getLocation().getWorld().setStorm(false);
        player.getLocation().getWorld().setThundering(false);
        actor.reply("Weather set to sun.");
    }

    @VRNCommand(cmd = "rain", desc = "Sets the weather to rain.", perm = "weather")
    public void onRain(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.getLocation().getWorld().setStorm(true);
        player.getLocation().getWorld().setThundering(false);
        actor.reply("Weather set to rain.");
    }

    @VRNCommand(cmd = "thunder", desc = "Sets the weather to thunder.", perm = "weather")
    public void onThunder(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.getLocation().getWorld().setStorm(true);
        player.getLocation().getWorld().setThundering(true);
        actor.reply("Weather set to thunder.");
    }

    @VRNCommand(cmd = "pweather", sub = "sun", desc = "Sets personal weather to sun.", perm = "pweather")
    public void onPweatherSun(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.setPlayerWeather(WeatherType.CLEAR);
        actor.reply("Personal weather set to sun.");
    }

    @VRNCommand(cmd = "pweather", sub = "rain", desc = "Sets personal weather to rain.", perm = "pweather")
    public void onPweatherRain(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.setPlayerWeather(WeatherType.DOWNFALL);
        actor.reply("Personal weather set to rain.");
    }

    @VRNCommand(cmd = "pweather", sub = "reset", desc = "Resets personal weather state.", perm = "pweather")
    public void onPweatherReset(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.resetPlayerWeather();
        actor.reply("Personal weather has been reset.");
    }

    @VRNCommand(cmd = "ptime", sub = "day", desc = "Sets personal time to day.", perm = "ptime")
    public void onPtimeDay(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.setPlayerTime(6000, false);
        actor.reply("Personal time set to day.");
    }

    @VRNCommand(cmd = "ptime", sub = "night", desc = "Sets personal time to night.", perm = "ptime")
    public void onPtimeNight(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.setPlayerTime(18000, false);
        actor.reply("Personal time set to night.");
    }

    @VRNCommand(cmd = "ptime", sub = "reset", desc = "Resets personal time state.", perm = "ptime")
    public void onPtimeReset(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        player.resetPlayerTime();
        actor.reply("Personal time has been reset.");
    }
}
