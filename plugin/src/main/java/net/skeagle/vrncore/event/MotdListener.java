package net.skeagle.vrncore.event;

import net.skeagle.vrncore.configurable.Settings;
import net.skeagle.vrnlib.misc.FormatUtils;
import net.skeagle.vrnlib.misc.TextResource;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MotdListener implements Listener {

    private final Plugin plugin;
    private Random random;
    private List<String> motds;

    public MotdListener(Plugin plugin) {
        this.plugin = plugin;
        this.loadMotds();
    }

    public void loadMotds() {
        if (Settings.randomMotd) {
            random = new Random();
            motds = TextResource.load(plugin, "motds.txt");
        }
        else
            motds = new ArrayList<>();
    }

    @EventHandler
    public void onList(ServerListPingEvent e) {
        String secondLine = !motds.isEmpty() ? Settings.motdPrefix + motds.get(random.nextInt(motds.size())) : Settings.secondLineText;
        e.setMotd(FormatUtils.color(Settings.firstLineShown ? Settings.firstLineText + "\n" + secondLine : secondLine));
    }
}
