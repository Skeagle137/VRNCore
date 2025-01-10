package net.skeagle.vrncore.hook;

import net.milkbowl.vault.chat.Chat;
import net.skeagle.vrncore.configurable.Settings;
import net.skeagle.vrncore.playerdata.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class VaultHook {

    private final Chat chat;

    VaultHook() {
        final RegisteredServiceProvider<Chat> rsp = Bukkit.getServicesManager().getRegistration(Chat.class);
        chat = rsp.getProvider();
    }

    private String getPrefix(final Player p) {
        if (!Settings.multiplePrefix)
            return chat.getPlayerPrefix(p.getWorld().getName(), p);
        final List<String> prefixes = new ArrayList<>();
        Arrays.asList(chat.getPlayerGroups(p)).forEach(group -> {
            final String prefix = chat.getGroupPrefix(p.getWorld(), group);
            if (prefix != null && !prefix.isEmpty())
                prefixes.add(prefix);
        });
        return String.join(" ", prefixes);
    }

    private String getSuffix(final Player p) {
        if (!Settings.multipleSuffix)
            return chat.getPlayerSuffix(p.getWorld().getName(), p);
        final List<String> suffixes = new ArrayList<>();
        Arrays.asList(chat.getPlayerGroups(p)).forEach(group -> {
            final String suffix = chat.getGroupSuffix(p.getWorld(), group);
            if (suffix != null && !suffix.isEmpty())
                suffixes.add(suffix);
        });
        return String.join(" ", suffixes);
    }

    private String[] getGroups(final Player p) {
        return chat.getPlayerGroups(p);
    }

    public String getName() {
        return chat.getName();
    }

    public String format(PlayerData data, String s) {
        if (s == null) {
            s = Settings.format;
        }
        s = s.replaceAll("%prefix", getPrefix(data.getPlayer()));
        s = s.replaceAll("%player", data.getName());
        s = s.replaceAll("%suffix", getSuffix(data.getPlayer()));
        s = s.replaceAll("%world", data.getPlayer().getWorld().getName());
        s = s.replaceAll("%group", getGroups(data.getPlayer()).length != 0 ? getGroups(data.getPlayer())[0] : "");
        return s;
    }
}
