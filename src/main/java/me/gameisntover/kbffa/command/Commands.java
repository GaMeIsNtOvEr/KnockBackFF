package me.gameisntover.kbffa.command;

import me.clip.placeholderapi.PlaceholderAPI;
import me.gameisntover.kbffa.KnockbackFFA;
import me.gameisntover.kbffa.api.KnockbackFFAKit;
import me.gameisntover.kbffa.arena.Arena;
import me.gameisntover.kbffa.arena.TempArenaManager;
import me.gameisntover.kbffa.arena.VoidChunkGenerator;
import me.gameisntover.kbffa.customconfig.*;
import me.gameisntover.kbffa.listeners.WandListener;
import me.gameisntover.kbffa.util.Message;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Commands implements CommandExecutor {

    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        Player p = (Player) sender;
        Knocker knocker = KnockbackFFA.getINSTANCE().getKnocker(p);
        if (Objects.requireNonNull(KnockbackFFA.getINSTANCE().getCommand("createkit")).getName().equalsIgnoreCase(command.getName())) {
            if (p.hasPermission("kbffa.command.createkit")) {
                if (args.length == 0) {
                    p.sendMessage(ChatColor.RED + "Usage: /createkit <kitname>");
                }
                if (args.length == 1) {
                    Kits kit = Kits.create(args[0]);
                    List<ItemStack> kitItems = Arrays.asList(Arrays.stream(p.getInventory().getContents()).filter(Objects::nonNull).toArray(ItemStack[]::new));
                    kit.get().set("KitContents", kitItems);
                    kit.get().set("Price", 100);
                    kit.get().set("KitName", args[0]);
                    if (p.getInventory().getItemInMainHand().getType() != Material.AIR) {
                        kit.get().set("KitIcon", p.getInventory().getItemInMainHand().getType().toString());
                    } else {
                        kit.get().set("KitIcon", "BARRIER");
                    }
                    List<String> lore = new ArrayList<>();
                    lore.add("this is a kit");
                    List<String> defaultKitLore = new ArrayList<>();
                    defaultKitLore.add(ChatColor.GRAY + "Another cool kit!");
                    defaultKitLore.add(ChatColor.GRAY + "Must be configured in plugins/KnockbackFFA/kits !");
                    kit.get().set("KitDescription", defaultKitLore);
                    kit.save();
                    p.sendMessage(ChatColor.GREEN + "I've created the kit " + args[0] + "! now you need to configure it in the plugins plugins/KnockbackFFA/kits!");
                }
            } else {
                p.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            }
        }
        if (Objects.requireNonNull(KnockbackFFA.getINSTANCE().getCommand("delkit")).getName().equalsIgnoreCase(command.getName())) {
            if (p.hasPermission("kbffa.command.delkit")) {
                if (args.length == 0) {
                    p.sendMessage(ChatColor.RED + "Usage: /delkit <kitname>");
                } else if (args.length == 1) {
                    Kits.getfile().delete();
                    p.sendMessage(ChatColor.GREEN + "I've deleted the kit " + args[0] + "!");
                }
            }
        }
        if (command.getName().equals("join")){
            if (!KnockbackFFA.getINSTANCE().BungeeMode() && !knocker.isInGame()) {
                String joinText = Message.ARENA_JOIN.toString().replace("&", "§");
                joinText = PlaceholderAPI.setPlaceholders(p, joinText);
                sender.sendMessage(joinText);
                if (KnockbackFFA.getINSTANCE().getConfig().getBoolean("save-inventory-on-join")) {
                    knocker.getConfig().set("inventory", p.getPlayer().getInventory().getContents());
                    knocker.getConfig().set("armor", p.getPlayer().getInventory().getArmorContents());
                    knocker.saveConfig();
                    p.getPlayer().getInventory().clear();
                    p.setFoodLevel(20);
                    KnockbackFFAKit kit = new KnockbackFFAKit();
                    kit.lobbyItems(p);
                    knocker.showScoreBoard();
                    knocker.setInGame(true);
                }
                KnockbackFFA.getINSTANCE().getTempArenaManager().teleportPlayerToArena(p);
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', Message.ALREADY_INGAME.toString()));
            }
        }
        if (command.getName().equals("leave")) {
            if (!KnockbackFFA.getINSTANCE().BungeeMode() && knocker.isInGame()) {
                String leaveText = Message.ARENA_LEAVE.toString().replace("&", "§");
                leaveText = PlaceholderAPI.setPlaceholders(p, leaveText);
                sender.sendMessage(leaveText);
                KnockbackFFA.getINSTANCE().getTempArenaManager().teleportToMainLobby(p);
                p.getInventory().clear();
                if (KnockbackFFA.getINSTANCE().getConfig().getBoolean("save-inventory-on-join")) {
                    List<ItemStack> items = (List<ItemStack>) knocker.getConfig().get("inventory");
                    Objects.requireNonNull(items).stream().filter(Objects::nonNull);
                    List<ItemStack> armor = (List<ItemStack>) knocker.getConfig().get("armor");
                    assert armor != null;
                    armor.stream().filter(Objects::nonNull);
                    assert items != null;
                    p.getInventory().setContents(items.toArray(new ItemStack[0]));
                    p.getInventory().setArmorContents(armor.toArray(new ItemStack[0]));
                }
                knocker.hideScoreBoard();
                knocker.setInGame(false);
            } else {
                p.sendMessage(Message.CAN_NOT_LEAVE.toString().replace("&", "§"));
            }
        }
        if (command.getName().equals("setmainlobby")) {
            sender.sendMessage(ChatColor.GREEN + "Main lobby spawn has been set");
            Location loc = p.getLocation();
            ArenaConfiguration.get().set("mainlobby.x", loc.getX());
            ArenaConfiguration.get().set("mainlobby.y", loc.getY());
            ArenaConfiguration.get().set("mainlobby.z", loc.getZ());
            String world = Objects.requireNonNull(loc.getWorld()).getName();
            ArenaConfiguration.get().set("mainlobby.world", world);
            ArenaConfiguration.save();
        }
        if (command.getName().equals("resetarena")) {
            if (args.length > 0) {
                File file = new File(KnockbackFFA.getINSTANCE().getTempArenaManager().getfolder() + File.separator + args[0] + ".yml");
                if (file.exists()) {
                    Arena arena = new TempArenaManager().load(args[0]);
                    arena.resetArena();
                    sender.sendMessage(ChatColor.GREEN + "Arena has been reset!");
                } else {
                    sender.sendMessage(ChatColor.RED + "Arena does not exist");
                }
            }
        }
        if (Objects.requireNonNull(KnockbackFFA.getINSTANCE().getCommand("createworld")).getName().equalsIgnoreCase(command.getName())) {
            if (args.length > 0) {
                WorldCreator wc = new WorldCreator(args[0]);
                wc.generateStructures(false);
                wc.generator(new VoidChunkGenerator());
                wc.createWorld();
                World world = Bukkit.getWorld(args[0]);
                if (world != null) {
                    world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
                }

                assert world != null;
                Block stone = world.getSpawnLocation().getBlock();
                if (stone.getType() == Material.AIR) {
                    stone.setType(Material.STONE);
                }
                sender.sendMessage(ChatColor.GREEN + "World " + args[0] + " has been loaded");
            }
        }
        if (Objects.requireNonNull(KnockbackFFA.getINSTANCE().getCommand("reload")).getName().equalsIgnoreCase(command.getName())) {
            KnockbackFFA.getINSTANCE().reloadConfig();
            KnockbackFFA.getINSTANCE().loadMessages();
            KnockbackFFA.getINSTANCE().loadSounds();
            ArenaConfiguration.reload();
            ScoreboardConfiguration.reload();
            ItemConfiguration.reload();
            CosmeticConfiguration.reload();
            sender.sendMessage(ChatColor.AQUA + "Configs are reloaded!");
        }
        if (Objects.requireNonNull(KnockbackFFA.getINSTANCE().getCommand("setvoid")).getName().equalsIgnoreCase(command.getName())) {
            if (WandListener.pos1m.get(p) != null && WandListener.pos2m.get(p) != null) {
                Location pos1 = WandListener.pos1m.get(p);
                Location pos2 = WandListener.pos2m.get(p);
                int vd;
                List<String> voids = ArenaConfiguration.get().getStringList("registered-voids");
                if (voids.size() == 0) {
                    vd = 1;
                } else {
                    String szstring = voids.get(voids.size() - 1);
                    vd = Integer.parseInt(szstring);
                    vd++;
                }
                voids.add(vd + "");
                if (ArenaConfiguration.get().getString("voids." + vd) == null) {
                    ArenaConfiguration.get().set("voids." + vd + ".pos1", pos1);
                    ArenaConfiguration.get().set("voids." + vd + ".pos2", pos2);
                    ArenaConfiguration.get().set("voids." + vd + ".damage", 8);
                    ArenaConfiguration.get().set("registered-voids", voids);
                    ArenaConfiguration.save();
                    sender.sendMessage(ChatColor.GREEN + "Void " + vd + " has been set and now players will get damage if they go there");
                }
            } else {
                sender.sendMessage(ChatColor.RED + "You have to set two positions first!");
            }
        }
        if (Objects.requireNonNull(KnockbackFFA.getINSTANCE().getCommand("specialitems")).getName().equalsIgnoreCase(command.getName())) {
            Inventory specialItems = Bukkit.createInventory(null, 9, "Special Items");
            KnockbackFFAKit kits = new KnockbackFFAKit();
            specialItems.addItem(kits.kbStick());
            specialItems.addItem(kits.kbBow());
            specialItems.addItem(kits.kbbowArrow());
            specialItems.addItem(kits.JumpPlate());
            specialItems.addItem(kits.EnderPearl());
            specialItems.addItem(kits.BuildingBlock());
            p.openInventory(specialItems);
        }
        return false;
    }
}

