package net.skeagle.vrncore.commands;

import net.skeagle.vrncore.VRNCore;
import net.skeagle.vrncore.playerdata.PlayerStates;
import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrnlib.messages.Messages;
import net.skeagle.vrnlib.misc.EventListener;
import net.skeagle.vrnlib.misc.Task;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerTeleportEvent;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static net.skeagle.vrnlib.VRNLib.say;

public class TpCommands {

    private final TpaUtil tpaUtil;
    private final BackCache backCache;

    public TpCommands() {
        tpaUtil = new TpaUtil();
        backCache = new BackCache();
        new EventListener<>(VRNCore.getInstance(), PlayerTeleportEvent.class, e -> {
            if (e.getCause() == PlayerTeleportEvent.TeleportCause.COMMAND || e.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN)
                backCache.setBackLoc(e.getPlayer().getUniqueId(), e.getFrom());
        });
    }

    @VRNCommand(cmd = "tpall", desc = "Teleports every online player to your location.")
    public void onTpAll(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        for (final Player pl : Bukkit.getOnlinePlayers()) {
            if (pl != player) {
                pl.teleport(player.getLocation());
            }
        }
        actor.reply(Messages.msg("teleportedAll"));
    }

    @VRNCommand(cmd = "tphere", desc = "Teleports a player to your location.")
    public void onTpHere(BukkitCommandActor actor, Player target) {
        Player player = actor.requirePlayer();
        actor.reply(Messages.msg("teleporting"));
        target.teleport(player.getLocation());
    }

    @VRNCommand(cmd = "top", desc = "Teleports to the highest block above you.")
    public void onTop(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        final int y;
        final Block b = VRNUtil.getStandingBlock(player.getLocation());
        if (b != null)
            y = player.getWorld().getHighestBlockYAt(b.getLocation());
        else
            y = player.getWorld().getHighestBlockYAt(player.getLocation());
        actor.reply(Messages.msg("teleporting"));
        final Location loc = player.getLocation().clone();
        loc.setY(y + 1);
        player.teleport(loc);
    }

    @VRNCommand(cmd = "back", desc = "Teleports back to a player's previous location.")
    public void onBack(BukkitCommandActor actor, Player target) {
        Player player = actor.requirePlayer();
        final Location backLoc = backCache.getBackLoc(target.getUniqueId());
        if (backLoc == null) {
            actor.error(target == player ? "You do not have anywhere to teleport back to."
                    : target.getName() + " does not have a saved last location.");
            return;
        }
        final Location newLoc = player.getLocation();
        backCache.teleToBackLoc(player, target);
        backCache.setBackLoc(player.getUniqueId(), newLoc);
        actor.reply("Teleported to " + (target == player ? "your" : "&a" + target.getName() + "&7's") + " last location.");
        backCache.setBackLoc(player.getUniqueId(), newLoc);
    }

    @VRNCommand(cmd = "tpa", desc = "Requests to teleport to a player.")
    public void onTpa(BukkitCommandActor actor, Player target) {
        Player player = actor.requirePlayer();
        if (player == target) {
            actor.error("You cannot teleport to yourself.");
            return;
        }
        if (tpaUtil.getRequestWhereSender(player) != null) {
            actor.error("You already have a pending teleport request.");
            return;
        }
        tpaUtil.addRequest(player, target, TpaUtil.RequestType.THERE).thenAccept(sent -> {
            if (!sent) {
                actor.error(target.getName() + " has teleport requests disabled.");
                return;
            }
            actor.reply("Teleport request sent.");
            say(target, "&a" + player.getName() + " &7is requesting to teleport to you. " +
                    "Do /tpaccept to accept the request or /tpdeny to deny it. This request will expire in 2 minutes.");
        });
    }

    @VRNCommand(cmd = "tpadeny", desc = "Denies a pending teleport request from another player.")
    public void onTpdeny(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        if (tpaUtil.getRequestWhereReciever(player) == null) {
            actor.error("You do not have any pending teleport requests.");
            return;
        }
        final TpaUtil.TpaRequest request = tpaUtil.getRequestWhereReciever(player);
        final OfflinePlayer sender = Bukkit.getOfflinePlayer(request.sender);
        actor.reply("Denied the teleport request from &a" + sender.getName() + "&7.");
        if (sender.isOnline())
            say((Player) sender, "&cYour teleport request was denied.");
        tpaUtil.deleteRequest(request, false);
    }

    @VRNCommand(cmd = "tpaccept", desc = "Accepts a pending teleport request from another player.")
    public void onTpaccept(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        if (tpaUtil.getRequestWhereReciever(player) == null) {
            actor.error("You do not have any pending teleport requests.");
            return;
        }
        final TpaUtil.TpaRequest request = tpaUtil.getRequestWhereReciever(player);
        final OfflinePlayer sender = Bukkit.getOfflinePlayer(request.sender);
        if (!sender.isOnline()) {
            actor.error("Could not teleport " + sender.getName() + " since they are offline.");
            return;
        }
        tpaUtil.teleportPlayers(request);
        actor.reply("Accepted the teleport request from &a" + sender.getName() + "&7.");
    }

    @VRNCommand(cmd = "tpahere", desc = "Requests a player to teleport to you.")
    public void onTpaHere(BukkitCommandActor actor, Player target) {
        Player player = actor.requirePlayer();
        if (player == target) {
            actor.error("You cannot teleport to yourself.");
            return;
        }
        if (tpaUtil.getRequestWhereSender(player) != null) {
            actor.error("You already have a pending teleport request.");
            return;
        }
        tpaUtil.addRequest(player, target, TpaUtil.RequestType.HERE).thenAccept(sent -> {
            if (!sent) {
                actor.error(target.getName() + " has teleport requests disabled.");
                return;
            }
            actor.reply("Teleport request sent.");
            say(target, "&a" + player.getName() + " &7is requesting for you to teleport to them. " +
                    "Do /tpaccept to accept the request or /tpdeny to deny it. This request will expire in 2 minutes.");
        });
    }

    @VRNCommand(cmd = "tpcancel", desc = "Cancels a teleport request that you have sent.")
    public void onTpcancel(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        if (tpaUtil.getRequestWhereSender(player) == null) {
            actor.error("You already have a pending teleport request.");
            return;
        }
        final TpaUtil.TpaRequest request = tpaUtil.getRequestWhereSender(player);
        final OfflinePlayer reciever = Bukkit.getOfflinePlayer(request.reciever);
        if (reciever.isOnline())
            say((Player) reciever, "&c" + player.getName() + " cancelled their teleport request sent to you.");
        tpaUtil.deleteRequest(request, false);
        actor.reply("Cancelled the teleport request.");
    }

    @VRNCommand(cmd = "tptoggle", desc = "Toggles being able to receive teleport requests from other players.")
    public void onTpToggle(BukkitCommandActor actor) {
        Player player = actor.requirePlayer();
        VRNCore.getPlayerData(player.getUniqueId()).thenAccept(data -> {
            PlayerStates states = data.getStates();
            states.setTpDisabled(!states.isTpDisabled());
            actor.reply("Teleport requests from other players are now " + (states.isTpDisabled() ? "&cdisabled" : "&aenabled") + "&7.");
        });
    }

    private static class BackCache {
        private final Map<UUID, Location> backLoc = new HashMap<>();

        Location getBackLoc(final UUID id) {
            return backLoc.get(id);
        }

        void setBackLoc(final UUID id, final Location loc) {
            backLoc.remove(id);
            backLoc.put(id, loc);
        }

        void teleToBackLoc(final Player p, final Player targetLoc) {
            p.teleport(backLoc.get(targetLoc.getUniqueId()));
        }
    }

    private static class TpaUtil {
        private final Map<TpaRequest, Task> requests = new HashMap<>();

        TpaRequest getRequestWhereReciever(final Player p) {
            return requests.keySet().stream().filter(r -> r.reciever.equals(p.getUniqueId()) && r.active).findFirst().orElse(null);
        }

        TpaRequest getRequestWhereSender(final Player p) {
            return requests.keySet().stream().filter(r -> r.sender.equals(p.getUniqueId())).findFirst().orElse(null);
        }

        private CompletableFuture<Boolean> addRequest(final Player player, final Player target, final RequestType type) {
            return VRNCore.getPlayerData(target.getUniqueId()).thenApply(data -> {
                PlayerStates states = data.getStates();
                if (states.isTpDisabled()) {
                    return false;
                }
                this.sendRequest(player, target, type);
                return true;
            });
        }

        private void sendRequest(Player player, Player target, RequestType type) {
            final TpaRequest r = getRequestWhereReciever(target);
            if (r != null)
                r.active = false;
            final TpaRequest request = new TpaRequest(player, target, type);
            requests.put(request, Task.syncDelayed(() -> deleteRequest(request, true), 20 * 120));
        }

        void teleportPlayers(final TpaRequest request) {
            final Player sender = Bukkit.getPlayer(request.sender);
            final Player reciever = Bukkit.getPlayer(request.reciever);
            if (request.type == RequestType.THERE)
                sender.teleport(reciever);
            else
                reciever.teleport(sender);
            deleteRequest(request, false);
        }

        void deleteRequest(final TpaRequest request, final boolean showmsg) {
            if (showmsg) {
                final OfflinePlayer sender = Bukkit.getOfflinePlayer(request.sender);
                final Player reciever = Bukkit.getPlayer(request.reciever);
                if (sender.isOnline())
                    say((Player) sender, "&cThe teleport request has expired.");
                if (reciever != null)
                    say(reciever, "&cThe teleport request from " + sender.getName() + " has expired.");
            }
            requests.get(request).cancel();
            requests.remove(request);
        }

        private enum RequestType {
            THERE,
            HERE
        }

        private static class TpaRequest {
            private final UUID sender;
            private final UUID reciever;
            private final RequestType type;
            private boolean active;

            TpaRequest(final Player sender, final Player reciever, final RequestType type) {
                this.sender = sender.getUniqueId();
                this.reciever = reciever.getUniqueId();
                this.type = type;
                this.active = true;
            }
        }
    }
}
