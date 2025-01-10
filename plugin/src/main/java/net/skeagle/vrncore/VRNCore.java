package net.skeagle.vrncore;

import net.luckperms.api.event.node.NodeMutateEvent;
import net.luckperms.api.model.user.User;
import net.skeagle.vrncore.api.VRNCoreNMS;
import net.skeagle.vrncore.commands.*;
import net.skeagle.vrncore.configurable.GuiConfig;
import net.skeagle.vrncore.configurable.Settings;
import net.skeagle.vrncore.event.TrailHandler;
import net.skeagle.vrncore.event.MotdListener;
import net.skeagle.vrncore.event.PlayerListener;
import net.skeagle.vrncore.homes.Home;
import net.skeagle.vrncore.homes.HomeManager;
import net.skeagle.vrncore.hook.HookManager;
import net.skeagle.vrncore.nms.v1_21_R1.VRNCoreNMS1_21_R1;
import net.skeagle.vrncore.nms.v1_21_R2.VRNCoreNMS1_21_R2;
import net.skeagle.vrncore.nms.v1_21_R3.VRNCoreNMS1_21_R3;
import net.skeagle.vrncore.npc.NpcData;
import net.skeagle.vrncore.npc.NpcManager;
import net.skeagle.vrncore.playerdata.PlayerData;
import net.skeagle.vrncore.playerdata.PlayerManager;
import net.skeagle.vrncore.configurable.rewards.RewardManager;
import net.skeagle.vrncore.utils.VRNUtil;
import net.skeagle.vrncore.warps.Warp;
import net.skeagle.vrncore.warps.WarpManager;
import net.skeagle.vrnlib.VRNLib;
import net.skeagle.vrnlib.config.ConfigManager;
import net.skeagle.vrnlib.messages.Messages;
import net.skeagle.vrnlib.misc.Task;
import net.skeagle.vrnlib.misc.UserCache;
import net.skeagle.vrnlib.sql.SQLHelper;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.sqlite.Function;
import revxrsal.commands.Lamp;
import revxrsal.commands.annotation.*;
import revxrsal.commands.annotation.dynamic.Annotations;
import revxrsal.commands.autocomplete.SuggestionProvider;
import revxrsal.commands.bukkit.BukkitLamp;
import revxrsal.commands.bukkit.actor.BukkitCommandActor;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.exception.CommandErrorException;
import revxrsal.commands.exception.EnumNotFoundException;

import java.lang.annotation.Annotation;
import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.regex.Pattern;

import static net.skeagle.vrnlib.VRNLib.sayNoPrefix;

public final class VRNCore extends JavaPlugin {

    private VRNCoreNMS api;
    private ConfigManager config, guiConfig;
    private PlayerManager playerManager;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private RewardManager rewardManager;
    private NpcManager npcManager;
    private SQLHelper db;
    private MotdListener motdListener;

    @Override
    public void onEnable() {
        //NMS
        int version = VRNLib.VERSION;
        if (version >= 2104 && version < 2200) {
            api = new VRNCoreNMS1_21_R3();
        }
        else if (version >= 2102) {
            api = new VRNCoreNMS1_21_R2();
        }
        else if (version >= 2100) {
            api = new VRNCoreNMS1_21_R1();
        }

        if (api == null) {
            VRNUtil.log(Level.SEVERE, "&cThe current server version is not supported by VRNCore. The plugin has been disabled.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        //messages and config
        Messages.load(this);
        config = ConfigManager.create(this).target(Settings.class).saveDefaults().load();
        guiConfig = ConfigManager.create(this, "gui-config.yml").target(GuiConfig.class).saveDefaults().load();
        //hooks
        HookManager.loadHooks();
        //database setup
        db = new SQLHelper(SQLHelper.openSQLite(getDataFolder().toPath().resolve("vrn_data.db")));
        db.execute("CREATE TABLE IF NOT EXISTS playerdata (id STRING PRIMARY KEY, nick STRING, playerTrailData STRING, arrowTrailData STRING, " +
                "playerStates STRING, timePlayed BIGINT);");
        db.execute("CREATE TABLE IF NOT EXISTS homes (id INTEGER PRIMARY KEY AUTOINCREMENT, name STRING, owner STRING, location STRING);");
        db.execute("CREATE TABLE IF NOT EXISTS warps (id INTEGER PRIMARY KEY AUTOINCREMENT, name STRING, owner STRING, location STRING);");
        db.execute("CREATE TABLE IF NOT EXISTS npc (id INTEGER PRIMARY KEY AUTOINCREMENT, name STRING, display STRING, " +
                "location STRING, skin STRING, rotateHead BOOLEAN);");
        if (Settings.uniqueNicknames) {
            try {
                Function.create(db.getConnection(), "REGEXP", new Function() {
                    @Override
                    protected void xFunc() throws SQLException {
                        String expression = value_text(0);
                        String value = value_text(1);
                        if (value == null)
                            value = "";

                        Pattern pattern = Pattern.compile(expression);
                        result(pattern.matcher(value).find() ? 1 : 0);
                    }
                });
            }
            catch (Exception e) {
                e.printStackTrace();
                VRNUtil.log(Level.SEVERE, "&cSQLite REGEXP extension could not be enabled. Unique nickname detection will be disabled.");
            }
        }
        //managers and tasks
        UserCache.asyncInit();
        playerManager = new PlayerManager(this);
        homeManager = new HomeManager(db);
        warpManager = new WarpManager(db);
        rewardManager = new RewardManager(this);
        npcManager = new NpcManager(api);
        Tasks.init(this);
        //commands
        Lamp<BukkitCommandActor> lamp = BukkitLamp.builder(this)
                .parameterTypes(builder -> {
                    builder.addParameterType(GameMode.class, (input, ctx) -> {
                        String s = input.readString();
                        return switch (s.toLowerCase()) {
                            case "survival", "s", "0" -> GameMode.SURVIVAL;
                            case "creative", "c", "1" -> GameMode.CREATIVE;
                            case "adventure", "a", "2" -> GameMode.ADVENTURE;
                            case "spectator", "sp", "3" -> GameMode.SPECTATOR;
                            default -> throw new EnumNotFoundException("'" + s + "' is not a valid gamemode.");
                        };
                    });
                    builder.addParameterType(Home.class, (input, ctx) -> {
                        String name = input.readString();
                        Home home = homeManager.getHome(name, ctx.actor().uniqueId());
                        if (home == null)
                            throw new CommandErrorException(Messages.msg("homeNotFound", name));
                        return home;
                    });
                    builder.addParameterType(Warp.class, (input, ctx) -> {
                        String name = input.readString();
                        Warp warp = warpManager.getWarp(name);
                        if (warp == null)
                            throw new CommandErrorException(Messages.msg("warpNotFound", name));
                        return warp;
                    });
                    builder.addParameterType(NpcData.class, (input, ctx) -> {
                        String name = input.readString();
                        NpcData npc = npcManager.getNpc(name);
                        if (npc == null)
                            throw new CommandErrorException(Messages.msg("npcNotFound", name));
                        return npc;
                    });
                })
                .suggestionProviders(providers -> {
                    providers.addProvider(Home.class, SuggestionProvider.fromAsync(ctx ->
                            homeManager.getHomes(ctx.actor().uniqueId()).thenApply(s -> s.stream().map(Home::name).toList()))
                    );
                    providers.addProvider(Warp.class, ctx ->
                            warpManager.getWarps().stream().map(Warp::getName).toList()
                    );
                    providers.addProvider(NpcData.class, ctx ->
                            npcManager.getNpcs().stream().map(NpcData::getName).toList()
                    );
                })
                .annotationReplacer(VRNCommand.class, (elem, anno) -> {
                    Command cmd = Annotations.create(Command.class, "value", anno.cmd());
                    Description desc = Annotations.create(Description.class, "value", anno.desc());
                    List<Annotation> annos = new ArrayList<>(List.of(cmd, desc));
                    if (anno.sub().length > 0) {
                        Subcommand sub = Annotations.create(Subcommand.class, "value", anno.sub());
                        annos.add(sub);
                    }
                    if (!elem.isAnnotationPresent(AnyoneCanUse.class)) {
                        CommandPermission perm = Annotations.create(CommandPermission.class, "value",
                                "vrn." + (anno.perm().isEmpty() ? anno.cmd()[0] : anno.perm()));
                        annos.add(perm);
                    }
                    return annos;
                })
                .defaultMessageSender((actor, s) -> VRNLib.say(actor.sender(), s))
                .defaultErrorSender((actor, s) -> VRNLib.say(actor.sender(), "&c" + s))
                .build();
        lamp.register(this, new AdminCommands(), new FunCommands(api), new NickCommands(this), new TimeWeatherCommands(),
                new NpcCommands(this), new MiscCommands(), new TpCommands(), new HomesWarpsCommands(this));

        this.reloadMotds();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this), this);
        Bukkit.getPluginManager().registerEvents(new TrailHandler(this), this);
        if (HookManager.isLuckPermsLoaded()) {
            HookManager.getLuckPermsHook().getLuckperms().getEventBus().subscribe(this, NodeMutateEvent.class, e -> {
                if (!e.isUser()) return;
                Player player = Bukkit.getPlayer(((User) e.getTarget()).getUniqueId());
                if (player == null) return;
                playerManager.getData(player.getUniqueId()).thenAccept(data -> Task.syncDelayed(() -> data.updateName()));
            });
        }
    }

    @Override
    public void onDisable() {
        if (api == null) return;
        playerManager.save();
        rewardManager.save();
    }

    private void reloadMotds() {
        if (Settings.motdEnabled && motdListener == null) {
            motdListener = new MotdListener(this);
            Bukkit.getPluginManager().registerEvents(motdListener, this);
        }
        else if (!Settings.motdEnabled && motdListener != null) {
            motdListener = null;
            ServerListPingEvent.getHandlerList().unregister(this);
        }
        if (motdListener != null) {
            motdListener.loadMotds();
        }
    }

    public static VRNCore getInstance() {
        return VRNCore.getPlugin(VRNCore.class);
    }

    public static CompletableFuture<PlayerData> getPlayerData(UUID uuid) {
        return VRNCore.getInstance().getPlayerManager().getData(uuid);
    }

    public SQLHelper getDB() {
        return db;
    }

    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public NpcManager getNpcManager() {
        return npcManager;
    }

    @Command("vrn")
    @Description("Displays plugin information.")
    public void onVRN(CommandSender sender) {
        sayNoPrefix(sender,
                "&9-----------------------------------------------",
                "&aVRNCore &7is developed and maintained by &dSkeagle&7.",
                "&7This server is currently running version &b" + getDescription().getVersion() + "&7.",
                "&7To view the changelog, go to this link:",
                "&bhttps://github.com/Skeagle137/vrncorereloaded/commits",
                "&9-----------------------------------------------");
    }

    @Command("vrn reload")
    @Description("Reloads the plugin's files.")
    @CommandPermission("vrn.reload")
    public void onReload(BukkitCommandActor actor) {
        Messages.load(this);
        guiConfig.reload();
        config.reload();
        rewardManager.reload();
        this.reloadMotds();
        actor.reply("&aConfigs, messages, and rewards reloaded.");
    }
}

