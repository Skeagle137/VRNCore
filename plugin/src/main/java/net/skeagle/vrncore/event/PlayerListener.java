package net.skeagle.vrncore.event;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.hook.HookManager;
import net.skeagle.vrncore.utils.AFKManager;
import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrncore.configurable.Settings;
import net.skeagle.vrnlib.messages.Messages;
import net.skeagle.vrnlib.misc.FormatUtils;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {

    private final VRNCore plugin;

    public PlayerListener(VRNCore plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(final AsyncPlayerPreLoginEvent e) {
        if (e.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) return;
        plugin.getPlayerManager().getData(e.getUniqueId()).whenComplete((res, ex) -> {
            if (ex == null) {
                e.setLoginResult(AsyncPlayerPreLoginEvent.Result.ALLOWED);
                return;
            }
            e.setLoginResult(AsyncPlayerPreLoginEvent.Result.KICK_OTHER);
        }).join();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(final PlayerJoinEvent e) {
        plugin.getPlayerManager().getData(e.getPlayer().getUniqueId()).thenAccept(data -> {
            data.updateName(e.getPlayer());
            if (!e.getPlayer().hasPlayedBefore() && Settings.joinLeaveEnabled && Settings.welcomeMessageEnabled) {
                e.setJoinMessage(Messages.msg("welcomeMsg", data.getName()));
            } else if (Settings.joinLeaveEnabled) {
                e.setJoinMessage(Messages.msg("joinMsg", data.getName()));
                if (Settings.returnMessageEnabled)
                    Task.syncDelayed(() -> VRNUtil.say(e.getPlayer(), Messages.msg("returnMsg", data.getName())), 2);
            }
            e.getPlayer().setPlayerListHeaderFooter(Messages.msg("tabListHeader", data.getName()),
                    Messages.msg("tabListFooter", data.getName()));
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(final PlayerQuitEvent e) {
        Player player = e.getPlayer();
        if (Settings.joinLeaveEnabled) {
            plugin.getPlayerManager().getData(player.getUniqueId()).thenAccept(data ->
                    e.setQuitMessage(Messages.msg("leaveMsg", data.getName())));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onAsyncPlayerChat(final AsyncPlayerChatEvent e) {
        if (!Settings.chatEnabled)
            return;
        if (!e.getPlayer().hasPermission("vrn.chat.allow") && Settings.chatPermission) {
            e.setCancelled(true);
            VRNUtil.say(e.getPlayer(), "&cYou do not have permission to use the chat.");
        }
        if (e.getPlayer().hasPermission("vrn.chat.color") || !Settings.colorPermission)
            e.setMessage(FormatUtils.color(e.getMessage()));
        if (!HookManager.isVaultLoaded())
            return;
        plugin.getPlayerManager().getData(e.getPlayer().getUniqueId()).thenAccept(data -> {
            String msg = HookManager.format(data);
            msg = msg.replaceAll("%", "%%");
            msg = msg.replace("%%message", "%2$s");
            e.setFormat(FormatUtils.color(msg));
        });
    }

    @EventHandler
    public void onGodDamage(final EntityDamageEvent e) {
        if (!(e.getEntity() instanceof Player player)) return;
        plugin.getPlayerManager().getData(player.getUniqueId()).thenAccept(data -> {
            if (data.getStates().hasGodmode()) {
                e.setCancelled(true);
            }
        });
    }
}
