package PluginBukkitBridge;

import java.io.File;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.craftbukkit.help.SimpleHelpMap;
import org.bukkit.craftbukkit.metadata.EntityMetadataStore;
import org.bukkit.craftbukkit.metadata.PlayerMetadataStore;
import org.bukkit.craftbukkit.metadata.WorldMetadataStore;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockDispenseEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityBreakDoorEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginLoadOrder;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.ServicesManager;
import org.bukkit.plugin.SimpleServicesManager;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.bukkit.plugin.messaging.StandardMessenger;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitWorker;
import org.bukkit.util.Vector;

import PluginBukkitBridge.block.FakeBlock;
import PluginBukkitBridge.block.FakeBlockState;
import PluginBukkitBridge.commands.MyCommandMap;
import PluginBukkitBridge.entity.FakeEntity;
import PluginBukkitBridge.entity.FakePlayer;
import PluginBukkitBridge.logging.MyLogHandler;
import PluginReference.MC_Block;
import PluginReference.MC_Container;
import PluginReference.MC_DamageType;
import PluginReference.MC_DirectionNESWUD;
import PluginReference.MC_EnchantmentType;
import PluginReference.MC_Entity;
import PluginReference.MC_EventInfo;
import PluginReference.MC_HangingEntityType;
import PluginReference.MC_ItemStack;
import PluginReference.MC_Location;
import PluginReference.MC_MiscGriefType;
import PluginReference.MC_Player;
import PluginReference.MC_Server;
import PluginReference.MC_Sign;
import PluginReference.MC_World;
import PluginReference.PluginInfo;

@Deprecated
public class MyPlugin extends PluginReference.PluginBase {
    public static MC_Server server = null;
    public static Logger logger;
    public final static FakeConsoleCommandSender consoleCommandSender = new FakeConsoleCommandSender();
    public final static ServicesManager servicesManager = new SimpleServicesManager();
    public final static StandardMessenger messenger = new StandardMessenger();
    public final static FakeScheduler scheduler = new FakeScheduler();
    public final static FakeCraftServer fakeServer = new FakeCraftServer();
    public final static SimpleCommandMap commandMap = new MyCommandMap(fakeServer);
    public final static PluginManager pluginManager = new MyPluginManager(fakeServer, commandMap);
    public static SimpleHelpMap helpMap;
	public static BukkitBridgeConfig bridgeConfig;

    public final static File pluginDir = new File("plugins");
    public final static File updateDir = new File(pluginDir, "update");

    public static boolean DebugMode = false;

    List<Runnable> invokeLater = new ArrayList<>();

    public static MyPlugin instance;

    public static WorldMetadataStore worldMetadataStorage = new WorldMetadataStore();
    public static PlayerMetadataStore playerMetadataStore = new PlayerMetadataStore();
    public static EntityMetadataStore entityMetadataStore = new EntityMetadataStore();

    public static void fixme() {
        if (DebugMode) {
            logger.log(Level.INFO, "FIXME: stub method", new UnsupportedOperationException());
        } else {
            logger.info("FIXME: stub method at " + new UnsupportedOperationException().getStackTrace()[1].toString());
        }
    }

    public static void fixme(String s) {
        if (DebugMode) {
            logger.log(Level.INFO, String.format("FIXME: %s", s), new UnsupportedOperationException());
        } else {
            logger.info("FIXME: " + s + " at " + new UnsupportedOperationException().getStackTrace()[1].toString());
        }
    }

    public MyPlugin() {
        super();
        instance = this;
        //SimpleFormatter formatter = new SimpleFormatter();
        //Handler handler = new StreamHandler(System.out, new MyLogFormatter());
        logger = Logger.getLogger("");
        for (Handler h : logger.getHandlers()) logger.removeHandler(h);
        logger.addHandler(new MyLogHandler());


        //logger = new MyLogger("", null);//Logger.getLogger("BukkitBridge");
        //logger.setLevel(Level.ALL);
        // logger = Logger.getLogger("Minecraft");

        pluginDir.mkdirs();
        updateDir.mkdirs();
        helpMap = new SimpleHelpMap(fakeServer);
		bridgeConfig = new BukkitBridgeConfig();
    }

    public void onStartup(MC_Server argServer) {
	    logger.warn("****************************************");
		logger.warn(" ! BukkitBridge is now Discontinued. ! ");
		logger.warn("On 2025.11.07, NogyangSpigot Replaces");
		logger.warn("Rainbow and BukkitBridge. This Plugin is");
		logger.warn("no longer supported. We Recommend your");
		logger.warn("Switch to NogyangSpigot. Just go switch.");
	    logger.warn("****************************************");
        logger.info("BukkitBridge v2.4 --- Starting up...");
        server = argServer;

        setupEnchantments();

        setupPotionEffectTypes();

        // Initialize Bukkit server object...
        fakeServer.server = server;
        Bukkit.setServer(fakeServer);

        // Load plugin JARs...
        loadPlugins();

        helpMap.clear();
        helpMap.initializeGeneralTopics();

        // Call onEnable for plugins...
        enablePlugins(PluginLoadOrder.STARTUP);
    }

    private void setupPotionEffectTypes() {
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(1, "SPEED", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(2, "SLOWNESS", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(3, "HASTE", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(4, "MINING_FATIGUE", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(5, "STRENGTH", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(6, "INSTANT_HEALTH", true));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(7, "INSTANT_DAMAGE", true));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(8, "JUMP_BOOST", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(9, "NAUSEA", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(10, "REGENERATION", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(11, "RESISTANCE", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(12, "FIRE_RESISTANCE", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(13, "WATER_BREATHING", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(14, "INVISIBILITY", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(15, "BLINDNESS", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(16, "NIGHT_VISION", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(17, "HUNGER", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(18, "WEAKNESS", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(19, "POISON", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(20, "WITHER", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(21, "HEALTH_BOOST", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(22, "ABSORPTION", false));
        PotionEffectType.registerPotionEffectType(new FakePotionEffectType(23, "SATURATION", false));
        PotionEffectType.stopAcceptingRegistrations();
    }

    private void setupEnchantments() {
        Enchantment.registerEnchantment(new FakeEnchantment(0, MC_EnchantmentType.PROTECTION, "PROTECTION", 1, 4, EnchantmentTarget.ARMOR, Arrays.asList(1,3,4)));
        Enchantment.registerEnchantment(new FakeEnchantment(1, MC_EnchantmentType.FIRE_PROTECTION, "FIRE_PROTECTION", 1, 4, EnchantmentTarget.ARMOR, Arrays.asList(0,3,4)));
        Enchantment.registerEnchantment(new FakeEnchantment(2, MC_EnchantmentType.FEATHER_FALLING, "FEATHER_FALLING", 1, 4, EnchantmentTarget.ARMOR_FEET, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(3, MC_EnchantmentType.BLAST_PROTECTION, "BLAST_PROTECTION", 1, 4, EnchantmentTarget.ARMOR, Arrays.asList(0,1,4)));
        Enchantment.registerEnchantment(new FakeEnchantment(4, MC_EnchantmentType.PROJECTILE_PROTECTION, "PROJECTILE_PROTECTION", 1, 4, EnchantmentTarget.ARMOR, Arrays.asList(0,1,3)));
        Enchantment.registerEnchantment(new FakeEnchantment(5, MC_EnchantmentType.RESPIRATION, "RESPIRATION", 1, 3, EnchantmentTarget.ARMOR_HEAD, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(6, MC_EnchantmentType.AQUA_AFFINITY, "AQUA_AFFINITY", 1, 1, EnchantmentTarget.ARMOR_HEAD, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(7, MC_EnchantmentType.THORNS, "THORNS", 1, 3, EnchantmentTarget.ARMOR, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(8, MC_EnchantmentType.DEPTH_STRIDER, "DEPTH_STRIDER", 1, 3, EnchantmentTarget.ARMOR_FEET, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(16, MC_EnchantmentType.SHARPNESS, "SHARPNESS", 1, 5, EnchantmentTarget.WEAPON, Arrays.asList(17,18)));
        Enchantment.registerEnchantment(new FakeEnchantment(17, MC_EnchantmentType.SMITE, "SMITE", 1, 5, EnchantmentTarget.WEAPON, Arrays.asList(16,18)));
        Enchantment.registerEnchantment(new FakeEnchantment(18, MC_EnchantmentType.BANE_OF_ARTHROPODS, "BANE_OF_ARTHROPODS", 1, 5, EnchantmentTarget.WEAPON, Arrays.asList(16,17)));
        Enchantment.registerEnchantment(new FakeEnchantment(19, MC_EnchantmentType.KNOCKBACK, "KNOCKBACK", 1, 2, EnchantmentTarget.WEAPON, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(20, MC_EnchantmentType.FIRE_ASPECT, "FIRE_ASPECT", 1, 2, EnchantmentTarget.WEAPON, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(21, MC_EnchantmentType.LOOTING, "LOOTING", 1, 3, EnchantmentTarget.WEAPON, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(32, MC_EnchantmentType.EFFICIENCY, "EFFICIENCY", 1, 5, EnchantmentTarget.TOOL, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(33, MC_EnchantmentType.SILK_TOUCH, "SILK_TOUCH", 1, 1, EnchantmentTarget.TOOL, Arrays.asList(35)));
        Enchantment.registerEnchantment(new FakeEnchantment(34, MC_EnchantmentType.UNBREAKING, "UNBREAKING", 1, 3, EnchantmentTarget.TOOL, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(35, MC_EnchantmentType.FORTUNE, "FORTUNE", 1, 3, EnchantmentTarget.TOOL, Arrays.asList(33)));
        Enchantment.registerEnchantment(new FakeEnchantment(48, MC_EnchantmentType.POWER, "POWER", 1, 5, EnchantmentTarget.BOW, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(49, MC_EnchantmentType.PUNCH, "PUNCH", 1, 2, EnchantmentTarget.BOW, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(50, MC_EnchantmentType.FLAME, "FLAME", 1, 1, EnchantmentTarget.BOW, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(51, MC_EnchantmentType.INFINITY, "INFINITY", 1, 1, EnchantmentTarget.BOW, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(61, MC_EnchantmentType.LUCK_OF_THE_SEA, "LUCK_OF_THE_SEA", 1, 3, EnchantmentTarget.FISHING_ROD, new ArrayList<Integer>()));
        Enchantment.registerEnchantment(new FakeEnchantment(62, MC_EnchantmentType.LURE, "LURE", 1, 3, EnchantmentTarget.FISHING_ROD, new ArrayList<Integer>()));
        Enchantment.stopAcceptingRegistrations();
    }

    @Override
    public void onServerFullyLoaded() {
        super.onServerFullyLoaded();
        WorldManager.refresh();
        // load all plugins postworld

        enablePlugins(PluginLoadOrder.POSTWORLD);

        helpMap.initializeCommands();
    }

    public void onShutdown() {
        logger.info("BukkitBridge v2.4 --- Shutting down...");
        pluginManager.disablePlugins();
    }

    public PluginInfo getPluginInfo() {
        PluginInfo info = new PluginInfo();
        info.description = "Partial Bukkit support (rbow.org)";
        info.eventSortOrder = 10000.0f; // call way later, lowest priority <-- Bukkit thinks that is highest priority
        return info;
    }

    public static void loadPlugins() {
        pluginManager.registerInterface(JavaPluginLoader.class);

        Plugin[] plugins = pluginManager.loadPlugins(pluginDir);
        for (Plugin plugin : plugins) {
            try {
                String message = String.format("[BukkitBridge] Loading Bukkit plugin: %s", plugin.getDescription().getFullName());
                logger.info(message);
                plugin.onLoad();
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }

    }

    public static void enablePlugins(PluginLoadOrder type) {
        Plugin[] plugins = pluginManager.getPlugins();

        for (Plugin plugin : plugins) {
            if ((!plugin.isEnabled()) && (plugin.getDescription().getLoad() == type)) {
                loadPlugin(plugin);
            }
        }

    }

    private static void loadPlugin(Plugin plugin) {
        try {
            pluginManager.enablePlugin(plugin);

            List<Permission> perms = plugin.getDescription().getPermissions();

            for (Permission perm : perms) {
                try {
                    pluginManager.addPermission(perm);
                } catch (IllegalArgumentException ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Throwable ex) {
            ex.printStackTrace();
        }
    }

    public void reload() {
        pluginManager.clearPlugins();
        commandMap.clearCommands();

        int pollCount = 0;

        // Wait for at most 2.5 seconds for plugins to close their threads
        while (pollCount < 50 && scheduler.getActiveWorkers().size() > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
            }
            pollCount++;
        }

        List<BukkitWorker> overdueWorkers = scheduler.getActiveWorkers();
        for (BukkitWorker worker : overdueWorkers) {
            Plugin plugin = worker.getOwner();
            String author = "<NoAuthorGiven>";
            if (plugin.getDescription().getAuthors().size() > 0) {
                author = plugin.getDescription().getAuthors().get(0);
            }
            logger.log(Level.SEVERE, String.format(
                    "Nag author: '%s' of '%s' about the following: %s",
                    author,
                    plugin.getDescription().getName(),
                    "This plugin is not properly shutting down its async tasks when it is being reloaded.  This may cause conflicts with the newly loaded version of the plugin"
            ));
        }

        helpMap.clear();
        helpMap.initializeGeneralTopics();
        loadPlugins();
        enablePlugins(PluginLoadOrder.STARTUP);
        enablePlugins(PluginLoadOrder.POSTWORLD);

        helpMap.initializeCommands();
    }

    public void onTick(int tickNumber) {
        for (Runnable r : invokeLater) {
            try {
                r.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        invokeLater.clear();
        scheduler.mainThreadHeartbeat(tickNumber);
        // refresh all player references, to avoid the cache deleting them
        for(MC_Player player: server.getPlayers()){
            PlayerManager.getPlayer(player);
        }
    }

    @Override
    public void onPlayerLogin(String playerName, UUID uuid, InetAddress address, MC_EventInfo ei) {
        String ip = address.toString();

        if (DebugMode) {
            String logMsg = String.format("%s onPlayerLogin from IP %s. UUID=%s", playerName, ip, uuid.toString());
            logger.info("BukkitBridge -- " + logMsg);
        }

        // don't call events if no one listens
        if (AsyncPlayerPreLoginEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        if (ip.startsWith("/")) {
            ip = ip.substring(1);
        }
        AsyncPlayerPreLoginEvent event = new AsyncPlayerPreLoginEvent(playerName, new InetSocketAddress(ip, 0).getAddress(), uuid);
        event.setLoginResult(ei.isCancelled ? AsyncPlayerPreLoginEvent.Result.KICK_OTHER : AsyncPlayerPreLoginEvent.Result.ALLOWED);
        event.setKickMessage(ei.tag);
        pluginManager.callEvent(event);
        if (event.getLoginResult() != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            ei.isCancelled = true;
            ei.tag = event.getKickMessage();
        }
    }

    public void onPlayerLogout(String playerName, UUID uuid) {
        if (DebugMode) {
            String logMsg = String.format("%s onPlayerLogout. UUID=%s", playerName, uuid.toString());
            logger.info("BukkitBridge -- " + logMsg);
        }

        // don't call events if no one listens
        if (PlayerQuitEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        // fixme message
        Player player = PlayerManager.getPlayer(uuid);
        if (player == null) {
            logger.warning("player is null");
        }
        pluginManager.callEvent(new PlayerQuitEvent(player, ""));
        PlayerManager.removePlayer(uuid);
    }

    // PlayerCommandPreprocessEvent
    public void onPlayerInput(MC_Player plr, String msg, MC_EventInfo ei) {
        if (msg == null) return;
        msg = msg.trim();
        if (msg.length() <= 0) return;

        if (DebugMode) {
            String logMsg = String.format("%s onPlayerInput: %s", plr.getName(), msg);
            logger.info("BukkitBridge -- " + logMsg);
        }

        super.onPlayerInput(plr, msg, ei);
        Matcher match = Pattern.compile(" */(.*)").matcher(msg);
        Player player = PlayerManager.getPlayer(plr);
        player.getLocation();
        if (match.matches() && !bridgeConfig.getIgnoredCommands().contains(match.group(1).split(" ")[0])) {

            // don't call events if no one listens
            if (PlayerCommandPreprocessEvent.getHandlerList().getRegisteredListeners().length != 0) {
                PlayerCommandPreprocessEvent event = new PlayerCommandPreprocessEvent(player, msg);
                event.setCancelled(ei.isCancelled);
                pluginManager.callEvent(event);
                ei.isCancelled = event.isCancelled();
            }
            if (!ei.isCancelled) {
                // fixme message might be changed
                if (commandMap.dispatch(PlayerManager.getPlayer(plr), match.group(1))) ei.isCancelled = true;
            }
        } else {
            if (AsyncPlayerChatEvent.getHandlerList().getRegisteredListeners().length > 0) {
                AsyncPlayerChatEvent event = new AsyncPlayerChatEvent(false, player, msg, new HashSet<>(Bukkit.getOnlinePlayers()));
                event.setCancelled(ei.isCancelled);
                pluginManager.callEvent(event);
                ei.isCancelled = event.isCancelled();
            }
        }

    } // end of onPlayerInput


    public void onAttemptEntityDamage(MC_Entity ent, MC_DamageType dmgType, double amt, MC_EventInfo ei) {
        if (DebugMode) {
            String logMsg = String.format("onAttemptEntityDamage: %s %s for %.2f", ent.getName(), dmgType.toString(), amt);
            logger.info("BukkitBridge -- " + logMsg);
        }

        // don't call events if no one listens
        if (EntityDamageEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        Entity fakeEnt = Util.wrapEntity(ent);

        // check noDamageTicks
        if(fakeEnt instanceof Player){
            if(((Player)fakeEnt).getNoDamageTicks() > 0)return;
        }

        EntityDamageEvent event = new EntityDamageEvent(fakeEnt, FakeHelper.GetDamageCause(dmgType), amt);

        if (fakeEnt instanceof HumanEntity && !ei.isCancelled) {
            fakeEnt.setLastDamageCause(event);
        }

        event.setCancelled(ei.isCancelled);
        pluginManager.callEvent(event);
        ei.isCancelled = event.isCancelled();


    }

    public void onPlayerDeath(MC_Player plrVictim, MC_Player plrKiller, MC_DamageType dmgType, String deathMsg) {
        if (DebugMode) {
            String logMsg = String.format("onPlayerDeath. player=%s, killer=%s, damage=%s, deathMsg=%s", plrVictim.getName(), plrKiller.getName(), dmgType.toString(), deathMsg);
            logger.info("BukkitBridge -- " + logMsg);
        }

        // don't call events if no one listens
        if (PlayerDeathEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        // fixme set killer
        PlayerDeathEvent event = new PlayerDeathEvent(PlayerManager.getPlayer(plrVictim), new ArrayList<ItemStack>(), 0, deathMsg);
        // fixme use result
        pluginManager.callEvent(event);
    }

    public void onPlayerRespawn(MC_Player plr) {
        if (DebugMode) {
            String logMsg = String.format("onPlayerRespawn. player=%s", plr.getName());
            logger.info("BukkitBridge -- " + logMsg);
        }

        // don't call events if no one listens
        if (PlayerRespawnEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        // fixme location, bed spawn
        PlayerRespawnEvent event = new PlayerRespawnEvent(PlayerManager.getPlayer(plr), Util.getLocation(plr.getLocation()), false);
        pluginManager.callEvent(event);
        // fixme result
    }

    public void onConsoleInput(String cmd, MC_EventInfo ei) {
        if (commandMap.dispatch(consoleCommandSender, cmd)) ei.isCancelled = true;
    }

    public void onAttemptBlockBreak(MC_Player plr, MC_Location loc, MC_EventInfo ei) {
        if (DebugMode) {
            String logMsg = String.format("PlayerInteractEvent. player=%s, action=LEFT_CLICK_BLOCK, loc=%s", plr.getName(), loc.toString());
            logger.info("BukkitBridge -- " + logMsg);
        }
        // Interact Event
        // fixme blockFace

        // don't call events if no one listens
        if (PlayerInteractEvent.getHandlerList().getRegisteredListeners().length != 0) {
            PlayerInteractEvent event = new PlayerInteractEvent(PlayerManager.getPlayer(plr), Action.LEFT_CLICK_BLOCK,
                    PlayerManager.getPlayer(plr).getItemInHand(), new FakeBlock(Location.locToBlock(loc.x), Location.locToBlock(loc.y), Location.locToBlock(loc.z), plr.getWorld()), BlockFace.DOWN);
            event.setCancelled(ei.isCancelled);
            pluginManager.callEvent(event);
            ei.isCancelled = event.isCancelled();
        }

        // BlockDamageEvent
        if (!ei.isCancelled) {
            if (DebugMode) {
                String logMsg = String.format("BlockDamageEvent. player=%s, loc=%s", plr.getName(), loc.toString());
                logger.info("BukkitBridge -- " + logMsg);
            }

            // don't call events if no one listens
            if (BlockDamageEvent.getHandlerList().getRegisteredListeners().length != 0) {
                BlockDamageEvent event2 = new BlockDamageEvent(PlayerManager.getPlayer(plr), new FakeBlock(Location.locToBlock(loc.x), Location.locToBlock(loc.y), Location.locToBlock(loc.z),
                        server.getWorld(loc.dimension)), PlayerManager.getPlayer(plr).getItemInHand(), false);
                event2.setCancelled(ei.isCancelled);
                pluginManager.callEvent(event2);
                ei.isCancelled = event2.isCancelled();
            }
        }

        // BlockBreakEvent
        if (!ei.isCancelled) {
            if (DebugMode) {
                String logMsg = String.format("BlockBreakEvent. player=%s, loc=%s", plr.getName(), loc.toString());
                logger.info("BukkitBridge -- " + logMsg);
            }

            // don't call events if no one listens
            if (BlockBreakEvent.getHandlerList().getRegisteredListeners().length != 0) {
                BlockBreakEvent event2 = new BlockBreakEvent(new FakeBlock(Location.locToBlock(loc.x), Location.locToBlock(loc.y), Location.locToBlock(loc.z),
                        server.getWorld(loc.dimension)), PlayerManager.getPlayer(plr));
                event2.setCancelled(ei.isCancelled);
                pluginManager.callEvent(event2);
                ei.isCancelled = event2.isCancelled();
            }
        }
    }

    boolean skipItemUse = false;

    public void onAttemptPlaceOrInteract(MC_Player plr, MC_Location loc, MC_EventInfo ei, MC_DirectionNESWUD dir) {
        skipItemUse = true;
        if (DebugMode) {
            String logMsg = String.format("PlayerInteractEvent. player=%s, action=RIGHT_CLICK_BLOCK, loc=%s", plr.getName(), loc.toString());
            logger.info("BukkitBridge -- " + logMsg);
        }

        // don't call events if no one listens
        if (PlayerInteractEvent.getHandlerList().getRegisteredListeners().length == 0) return;
        PlayerInteractEvent event = new PlayerInteractEvent(PlayerManager.getPlayer(plr), Action.RIGHT_CLICK_BLOCK,
                PlayerManager.getPlayer(plr).getItemInHand(), new FakeBlock(Location.locToBlock(loc.x), Location.locToBlock(loc.y), Location.locToBlock(loc.z), plr.getWorld()), Util.getFace(dir));
        event.setCancelled(ei.isCancelled);
        pluginManager.callEvent(event);
        ei.isCancelled = event.isCancelled();
    }

    boolean allowTeleport = false;

    public void onAttemptPlayerTeleport(MC_Player plr, MC_Location loc, MC_EventInfo ei) {
        if (allowTeleport) return;

        // don't call events if no one listens
        if (PlayerTeleportEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        PlayerTeleportEvent event = new PlayerTeleportEvent(PlayerManager.getPlayer(plr), Util.getLocation(plr.getLocation()), Util.getLocation(loc));
        event.setCancelled(ei.isCancelled);
        pluginManager.callEvent(event);
        ei.isCancelled = event.isCancelled();
    }

    public void onAttemptPlayerMove(final MC_Player plr, MC_Location locFrom, MC_Location locTo, MC_EventInfo ei) {

        // don't call events if no one listens
        if (PlayerMoveEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        PlayerMoveEvent event = new PlayerMoveEvent(PlayerManager.getPlayer(plr), Util.getLocation(locFrom), Util.getLocation(locTo));
        event.setCancelled(ei.isCancelled);
        pluginManager.callEvent(event);
        final Location to = event.getTo();
        if (!event.isCancelled() && (to.getX() != locTo.x || to.getY() != locTo.y || to.getZ() != locTo.z)) {
            invokeLater.add(new Runnable() {
                @Override
                public void run() {
                    allowTeleport = true;
                    plr.teleport(Util.getLocation(to));
                    allowTeleport = false;
                }
            });
        }
        ei.isCancelled = event.isCancelled();
    }

    public void onPlayerJoin(MC_Player plr) {
        PlayerManager.addPlayer(plr);

        PlayerLoginEvent event1 = new PlayerLoginEvent(PlayerManager.getPlayer(plr), plr.getIPAddress(), ((InetSocketAddress) plr.getSocketAddress()).getAddress());
        pluginManager.callEvent(event1);

        // fixme result
        PlayerJoinEvent event = new PlayerJoinEvent(PlayerManager.getPlayer(plr), "");
        pluginManager.callEvent(event);
    }

    public void onItemPlaced(MC_Player plr, MC_Location loc, MC_ItemStack isHandItem, MC_Location locPlacedAgainst, MC_DirectionNESWUD dir) {
        if (DebugMode) logger.info("BukkitBridge -- onItemPlaced to BlockPlaceEvent");

        // don't call events if no one listens
        if (BlockPlaceEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        Player who = new FakePlayer(plr);
        MC_ItemStack isHand = plr.getItemInHand();
        ItemStack isPlaced = new ItemStack(isHand.getId(), isHand.getCount());

        MC_World world = plr.getWorld();
        int x = Location.locToBlock(loc.x);
        int y = Location.locToBlock(loc.y);
        int z = Location.locToBlock(loc.z);

        int x2 = Location.locToBlock(locPlacedAgainst.x);
        int y2 = Location.locToBlock(locPlacedAgainst.y);
        int z2 = Location.locToBlock(locPlacedAgainst.z);

        FakeBlock fakeBlockPlaced = new FakeBlock(x, y, z, world);

        if (fakeBlockPlaced.getType() == Material.AIR) return;

        FakeBlock fakeBlockAgainst = new FakeBlock(x2, y2, z2, world);

        if (DebugMode) {
            String logMsg = String.format("BlockPlaceEvent. player=%s, loc=%s", plr.getName(), loc.toString());
            logger.info("BukkitBridge -- " + logMsg);
        }

        BlockPlaceEvent event = new BlockPlaceEvent(fakeBlockPlaced, new FakeBlockState(fakeBlockPlaced.getLocation(), 0, (byte) 0), fakeBlockAgainst, isPlaced, who, true);
        pluginManager.callEvent(event);
        if (event.isCancelled()) {
            PlayerManager.getPlayer(plr).getInventory().addItem(new ItemStack(fakeBlockPlaced.getType()));
            fakeBlockPlaced.setType(Material.AIR);
        }
    }

    @Override
    public void onAttemptItemUse(MC_Player plr, MC_ItemStack is, MC_EventInfo ei) {
        if (DebugMode) {
            String logMsg = String.format("onAttemptItemUse: PlayerInteractEvent. player=%s, action=RIGHT_CLICK_AIR", plr.getName());
            logger.info("BukkitBridge -- " + logMsg);
            if (skipItemUse) logger.info("BukkitBridge -- onAttemptItemUse: skipped");
        }

        if (skipItemUse) {
            skipItemUse = false;
            return;
        }

        // don't call events if no one listens
        if (PlayerInteractEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        Player player = PlayerManager.getPlayer(plr);

        PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_AIR,
                Util.getItemStack(is), null, null);
        event.setCancelled(ei.isCancelled);
        pluginManager.callEvent(event);
        ei.isCancelled = event.isCancelled();
    }

    @Override
    public void onAttemptItemDrop(MC_Player plr, MC_ItemStack is, MC_EventInfo ei) {

        // don't call events if no one listens
        if (PlayerDropItemEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        PlayerDropItemEvent event = new PlayerDropItemEvent(PlayerManager.getPlayer(plr), new FakedFakeItem(Util.getItemStack(is), Util.getLocation(plr.getLocation())));
        event.setCancelled(ei.isCancelled);
        pluginManager.callEvent(event);
        ei.isCancelled = event.isCancelled();
    }

    @Override
    public void onAttemptItemPickup(MC_Player plr, MC_ItemStack is, boolean isXpOrb, MC_EventInfo ei) {
		if(isXpOrb){
			// this is not an item!!!
			return;
		}

        // don't call events if no one listens
        if (PlayerPickupItemEvent.getHandlerList().getRegisteredListeners().length == 0) return;

        PlayerPickupItemEvent event = new PlayerPickupItemEvent(PlayerManager.getPlayer(plr), new FakedFakeItem(Util.getItemStack(is), Util.getLocation(plr.getLocation())), 0);
        event.setCancelled(ei.isCancelled);
        pluginManager.callEvent(event);
        ei.isCancelled = event.isCancelled();
    }

    @Override
    public boolean onAttemptExplodeSpecific(MC_Entity ent, List<MC_Location> locs) {

        // don't call events if no one listens
        if (EntityExplodeEvent.getHandlerList().getRegisteredListeners().length == 0) return false;

        // fixme yield, cancel
        List<Block> blocks = new ArrayList<>();
        for (MC_Location loc : locs) {
            Location l = Util.getLocation(loc);
            blocks.add(l.getWorld().getBlockAt(l));
        }
        EntityExplodeEvent event = new EntityExplodeEvent(Util.wrapEntity(ent), Util.getLocation(ent.getLocation()), blocks, 1);
        pluginManager.callEvent(event);
        return false;
    }

    public void handlePluginMessage(MC_Player player, String tag, byte[] data, MC_EventInfo mc_eventInfo) {
        if (DebugMode) logger.info("handlePluginMessage " + player.getName() + ": " + tag);
        Player sender = PlayerManager.getPlayer(player);
        if (DebugMode && sender == null) logger.info("Player is null :-(");
        if (sender != null) messenger.dispatchIncomingMessage(sender, tag, data);
    }

    @Override
    public Boolean onRequestPermission(String playerKey, String permission) {

        if (DebugMode) logger.info("onRequestPermission(" + playerKey + ", " + permission + ")");

        if (playerKey.equals("*")) return null;

        MC_Player p0;
        if (playerKey.length() <= 16) p0 = server.getOnlinePlayerByName(server.getPlayerExactName(playerKey));
        else p0 = server.getOnlinePlayerByName(server.getLastKnownPlayerNameFromUUID(playerKey));

        if (p0 == null) return null;

        FakePlayer player = (FakePlayer) PlayerManager.getPlayer(p0);

        if (player == null) return null;

        if (!player.permissions.isPermissionSet(permission)) return null;

        return player.permissions.hasPermission(permission);
    }

    @Override
    public void onAttemptDamageHangingEntity(MC_Player plr, MC_Location loc, MC_HangingEntityType entType, MC_EventInfo ei) {
        if (HangingBreakByEntityEvent.getHandlerList().getRegisteredListeners().length > 0) {
            HangingBreakByEntityEvent event = new HangingBreakByEntityEvent(new FakedFakeHanging(loc, entType), PlayerManager.getPlayer(plr));
            event.setCancelled(ei.isCancelled);
            pluginManager.callEvent(event);
            ei.isCancelled = event.isCancelled();
        }
    }

	boolean inEvent = false;
    @Override
    public void onAttemptEntitySpawn(MC_Entity ent, MC_EventInfo ei) {
        if (EntitySpawnEvent.getHandlerList().getRegisteredListeners().length > 0) {
            EntitySpawnEvent event = new EntitySpawnEvent(Util.wrapEntity(ent));
            event.setCancelled(ei.isCancelled);
            pluginManager.callEvent(event);
            ei.isCancelled = event.isCancelled();
        }
		if(CreatureSpawnEvent.getHandlerList().getRegisteredListeners().length > 0 && !inEvent){
			Entity ent2 = Util.wrapEntity(ent);
			if(ent2 instanceof LivingEntity) {
				CreatureSpawnEvent event = new CreatureSpawnEvent((LivingEntity) ent2, CreatureSpawnEvent.SpawnReason.NATURAL);
				event.setCancelled(ei.isCancelled);
				inEvent = true;
				pluginManager.callEvent(event);
				inEvent = false;
				ei.isCancelled = event.isCancelled();
			}
		}
    }

    @Override
    public void onAttemptDispense(MC_Location loc, int idxItem, MC_Container container, MC_EventInfo ei) {
        if (BlockDispenseEvent.getHandlerList().getRegisteredListeners().length > 0) {
            BlockDispenseEvent event = new BlockDispenseEvent(new FakeBlock(Location.locToBlock(loc.x), Location.locToBlock(loc.y),
                    Location.locToBlock(loc.z), server.getWorld(loc.dimension)), new ItemStack(idxItem), new Vector());
            event.setCancelled(ei.isCancelled);
            pluginManager.callEvent(event);
            ei.isCancelled = event.isCancelled();
        }
    }

    @Override
    public void onAttemptEntityMiscGrief(MC_Entity ent, MC_Location loc, MC_MiscGriefType griefType, MC_EventInfo ei) {
        switch (griefType) {
            case ENDERDRAGON_BRUSH:
                break;
            case ENDERMAN_PICKUP_BLOCK:
                break;
            case ENDERMAN_PLACE_CARRIED_BLOCK:
                break;
            case SHEEP_GRAZING_GRASS:
                break;
            case SILVERFISH_BREAK_MONSTER_EGG_BLOCK:
                break;
            case RABBIT_EATS_CARROT:
                break;
            case VILLAGER_HARVEST:
                break;
            case VILLAGER_PLANT_SEEDS:
                break;
            case WITHER_BREAK:
                break;
            case ZOMBIE_DOOR_BREAK:
                if(EntityBreakDoorEvent.getHandlerList().getRegisteredListeners().length > 0) {
                    EntityBreakDoorEvent event = new EntityBreakDoorEvent((org.bukkit.entity.LivingEntity) Util.wrapEntity(ent), new FakeBlock(Location.locToBlock(loc.x),
                            Location.locToBlock(loc.y), Location.locToBlock(loc.z), server.getWorld(loc.dimension)));
                    event.setCancelled(ei.isCancelled);
                    pluginManager.callEvent(event);
                    ei.isCancelled = event.isCancelled();
                }
                break;
        }
    }

    @Override
    public void onAttemptFishingReel(MC_Player plr, MC_ItemStack isCatch, MC_Entity entCatch, boolean groundCatch, MC_EventInfo ei) {
        if(PlayerFishEvent.getHandlerList().getRegisteredListeners().length > 0){
            PlayerFishEvent event = new PlayerFishEvent(PlayerManager.getPlayer(plr), Util.wrapEntity(entCatch), null, PlayerFishEvent.State.CAUGHT_ENTITY);
            event.setCancelled(ei.isCancelled);
            pluginManager.callEvent(event);
            ei.isCancelled = event.isCancelled();
        }
    }

    @Override
    public void onPlayerBedLeave(MC_Player plr, MC_Block blkBed, MC_Location locBed) {
        if(PlayerBedLeaveEvent.getHandlerList().getRegisteredListeners().length > 0){
            PlayerBedLeaveEvent event = new PlayerBedLeaveEvent(PlayerManager.getPlayer(plr), new FakeBlock(Location.locToBlock(locBed.x),
                    Location.locToBlock(locBed.y), Location.locToBlock(locBed.z), server.getWorld(locBed.dimension)));
            pluginManager.callEvent(event);
        }
    }

    @Override
    public void onPlayerBedEnter(MC_Player plr, MC_Block blkBed, MC_Location locBed) {
        if(PlayerBedEnterEvent.getHandlerList().getRegisteredListeners().length > 0){
            PlayerBedEnterEvent event = new PlayerBedEnterEvent(PlayerManager.getPlayer(plr), new FakeBlock(Location.locToBlock(locBed.x),
                    Location.locToBlock(locBed.y), Location.locToBlock(locBed.z), server.getWorld(locBed.dimension)));
            pluginManager.callEvent(event);
        }
    }

    @Override
    public void onAttemptEntityInteract(MC_Player plr, MC_Entity ent, MC_EventInfo ei) {
        if(PlayerInteractEntityEvent.getHandlerList().getRegisteredListeners().length > 0){
            PlayerInteractEntityEvent event = new PlayerInteractEntityEvent(PlayerManager.getPlayer(plr), Util.wrapEntity(ent));
            event.setCancelled(ei.isCancelled);
            pluginManager.callEvent(event);
            ei.isCancelled = event.isCancelled();
        }
    }

    @Override
    public void onNonPlayerEntityDeath(MC_Entity entVictim, MC_Entity entKiller, MC_DamageType dmgType) {
        if(entVictim.getHealth() <= 0 && EntityDeathEvent.getHandlerList().getRegisteredListeners().length > 0){
            EntityDeathEvent event = new EntityDeathEvent((org.bukkit.entity.LivingEntity) Util.wrapEntity(entVictim), new ArrayList<ItemStack>(), 0);
			((FakeEntity)event.getEntity()).setDamageCause(FakeHelper.GetDamageCause(dmgType));
            pluginManager.callEvent(event);
        }
    }

	@Override public void onSignChanging(MC_Player plr, MC_Sign sign, MC_Location loc, List<String> newLines, MC_EventInfo ei) {
		if(SignChangeEvent.getHandlerList().getRegisteredListeners().length > 0) {
			String[] lines = new String[newLines.size()];
			for (int i = 0; i < newLines.size(); i++) {
				String s = newLines.get(i);
				lines[i] = s.replaceFirst("^(§0)*", "");
			}
			SignChangeEvent event = new SignChangeEvent(new FakeBlock(loc.getBlockX(), loc.getBlockY(),
					loc.getBlockZ(), server.getWorld(loc.dimension)), PlayerManager.getPlayer(plr), lines);
			event.setCancelled(ei.isCancelled);
			pluginManager.callEvent(event);
			ei.isCancelled = event.isCancelled();
			ei.isModified = true;
			newLines.clear();
			Collections.addAll(newLines, lines);
		}
	}

    public void onWorldLoaded(MC_World world){
        WorldManager.refresh();
    }
}
