package me.gameisntover.kbffa.arena;

import lombok.Data;
import lombok.SneakyThrows;
import me.gameisntover.kbffa.KnockbackFFA;
import me.gameisntover.kbffa.api.KnockbackFFAKit;
import me.gameisntover.kbffa.api.event.PlayerTeleportsToArenaEvent;
import me.gameisntover.kbffa.customconfig.ArenaConfiguration;
import me.gameisntover.kbffa.customconfig.Knocker;
import me.gameisntover.kbffa.util.Message;
import me.gameisntover.kbffa.util.Sounds;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.*;

@Data
public class TempArenaManager {
    private String name;
    private Arena enabledArena;
    private File cfile;
    private FileConfiguration config;
    private File folder = new File(KnockbackFFA.getINSTANCE().getDataFolder(), "ArenaData" + File.separator);
    private File df = KnockbackFFA.getINSTANCE().getDataFolder();
    private List<Arena> arenas = new ArrayList<>();
    private Map<String, Arena> arenaHandler = new HashMap<>();
    @SneakyThrows
    public Arena create(String arenaName, Location pos1, Location pos2) {
        cfile = new File(df, "ArenaData" + File.separator + arenaName + ".yml");
        if (!df.exists()) df.mkdir();
        if (!cfile.exists()) cfile.createNewFile();
        config = YamlConfiguration.loadConfiguration(cfile);
        Arena arena = new Arena(arenaName,pos1,pos2);
        arenaHandler.put(arenaName,arena);
        name = arenaName;
        return arena;
    }
    public Arena load(String arenaName) {
        if (arenaHandler.containsKey(arenaName)) return arenaHandler.get(arenaName);
        else{
            Arena arena = new Arena(arenaName);
            arenaHandler.put(arenaName,arena);
            return arena;
        }
    }


    /**
     * sets the arena enabled
     *
     * @param @arenaName
     */
    public void setEnabledArena(String arenaName) {
        enabledArena = load(arenaName);
    }

    /**
     * sets the arena enabled
     *
     * @param @arena
     */
    public void setEnabledArena(Arena arena) {
        enabledArena = arena;
    }

    /**
     * returns player to the main lobby
     * <p>
     * needs @param @player
     */
    public void teleportToMainLobby(Player player) {
        if (ArenaConfiguration.get().getString("mainlobby.world") == null) return;
        double x = ArenaConfiguration.get().getDouble("mainlobby.x");
        double y = ArenaConfiguration.get().getDouble("mainlobby.y");
        double z = ArenaConfiguration.get().getDouble("mainlobby.z");
        World world = Bukkit.getWorld(ArenaConfiguration.get().getString("mainlobby.world"));
        if (world != null) player.teleport(new Location(world, x, y, z));
        else player.teleport(Bukkit.getWorld("world").getSpawnLocation());
    }

    /**
     * teleports player to the enabled arena
     */
    public void teleportPlayerToArena(Player player) {
        if (getFolder().list().length > 0) {
            PlayerTeleportsToArenaEvent event = new PlayerTeleportsToArenaEvent(player, getEnabledArena());
            Bukkit.getPluginManager().callEvent(event);
            Location spawnLoc = getEnabledArena().getSpawnLocation();
            if (event.isCancelled()) return;
            if (spawnLoc.getWorld() != null) player.teleport(spawnLoc);
            else {
                WorldCreator wc = new WorldCreator(getEnabledArena().getConfig().getString("arena.spawn.world"));
                wc.generateStructures(false);
                wc.generator(new VoidChunkGenerator());
                World world1 = wc.createWorld();
                world1.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                Bukkit.getWorlds().add(world1);
                world1.loadChunk(0, 0);
                System.out.println(world1.getName() + " successfully loaded!");
                player.teleport(new Location(world1, spawnLoc.getX(), spawnLoc.getY(), spawnLoc.getZ()));
            }
        } else System.out.println("[KnockbackFFA] There are no arenas to teleport the player there!");

    }

    /**
     * Returns a random arena name
     *
     * @return String
     */
    public String randomArena() {
        String[] arenas = getFolder().list();
        int random = new Random().nextInt(arenas.length);
        return arenas[random];
    }

    public List<Arena> getArenaList() {
        List<Arena> arenas = new ArrayList<>();
        for (String arena : getFolder().list()) {
            arenas.add(load(arena.replace(".yml", "")));
        }
        return arenas;
    }

    /**
     * Changes the arena to another arena
     */
    public void changeArena(Arena arena) {
        String arenaName = arena.getName();
        setEnabledArena(arenaName);
        for (Player p : Bukkit.getServer().getOnlinePlayers()) {
            Knocker knocker = KnockbackFFA.getINSTANCE().getKnocker(p);
            if (!knocker.isInGame()) return;
            p.getInventory().clear();
            KnockbackFFAKit kitManager = new KnockbackFFAKit();
            kitManager.lobbyItems(p);
            teleportPlayerToArena(p);
            p.playSound(p.getLocation(), Sound.valueOf(Sounds.ARENA_CHANGE.toString()), 1, 1);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', Message.ARENA_CHANGE.toString()).replace("%arena%", arenaName));
        }
        if (arena.getConfig().getBoolean("auto-reset")) arena.resetArena();
    }

}
