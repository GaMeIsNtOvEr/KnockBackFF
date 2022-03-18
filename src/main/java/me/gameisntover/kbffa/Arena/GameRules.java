package me.gameisntover.kbffa.Arena;
import me.gameisntover.kbffa.api.KnockbackFFAAPI;
import me.gameisntover.kbffa.api.KnockbackFFAKit;
import me.gameisntover.kbffa.customconfigs.ArenaConfiguration;
import me.gameisntover.kbffa.customconfigs.MessageConfiguration;
import me.gameisntover.kbffa.KnockbackFFA;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupArrowEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BoundingBox;

import java.util.List;

public class GameRules implements Listener
{
    @EventHandler
    public void onPlayerItemPickup(EntityPickupItemEvent e){
        if (e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();
            if (KnockbackFFAAPI.isInGame(player)||KnockbackFFAAPI.BungeeMode()){
                e.setCancelled(true);
            }
        }
    }
    @EventHandler
    public void onPlayerDamages(EntityDamageEvent e){
        if (e.getEntity() instanceof Player){
            Player player = (Player) e.getEntity();
            for (String sz : ArenaConfiguration.get().getStringList("registered-safezones")) {
                if (ArenaConfiguration.get().getString("Safezones." + sz + ".world") != null) {
                    Location loc1= ArenaConfiguration.get().getLocation("Safezones." + sz + ".pos1");
                    Location loc2= ArenaConfiguration.get().getLocation("Safezones." + sz + ".pos2");
                    BoundingBox s1box = new BoundingBox(loc1.getX(), loc1.getY(), loc1.getZ(), loc2.getX(), loc2.getY(), loc2.getZ());
                    World s1world = Bukkit.getWorld(ArenaConfiguration.get().getString("Safezones." + sz + ".world"));
                    Location location = player.getLocation();
                    if (s1box.contains(location.toVector()) && player.getWorld() == s1world) {
                        e.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player player = e.getPlayer();
            List<String> voids = ArenaConfiguration.get().getStringList("registered-voids");
            for (String vd: voids){
                    if (ArenaConfiguration.get().getLocation("voids." + vd + ".pos1") != null) {
                        Location pos1 = ArenaConfiguration.get().getLocation("voids." + vd + ".pos1");
                        Location pos2 = ArenaConfiguration.get().getLocation("voids." + vd + ".pos2");
                        BoundingBox bb = new BoundingBox(pos1.getX(), pos1.getY(), pos1.getZ(), pos2.getX(), pos2.getY(), pos2.getZ());
                        if (bb.contains(player.getLocation().toVector()) && player.getWorld() == pos1.getWorld()) {
                            Integer damage = ArenaConfiguration.get().getInt("voids." + vd + ".damage");
                            if (damage != null) {
                              new BukkitRunnable(){
                                  @Override
                                  public void run() {
                                      if (player.isDead() || !bb.contains(player.getLocation().toVector()) || player.getWorld() != pos1.getWorld()) {
                                          cancel();
                                      } else {
                                          player.damage(damage);
                                          player.setLastDamageCause(new EntityDamageEvent(player, EntityDamageEvent.DamageCause.VOID, damage));
                                      }
                                  }
                              }.runTaskTimer(KnockbackFFA.getInstance(), 0, 20);

                            } else {
                                throw new NullPointerException("Void " + vd + " damage is null");
                            }
                        }
                    }
                }
            }

    @EventHandler
    public void onArrowOnGround(PlayerPickupArrowEvent e) {
        if (KnockbackFFAAPI.BungeeMode() || KnockbackFFAAPI.isInGame(e.getPlayer())) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onBowUse(ProjectileLaunchEvent e) {
        if (e.getEntity().getShooter() instanceof Player) {
            Player player = (Player) e.getEntity().getShooter();
            if (KnockbackFFAAPI.BungeeMode() || KnockbackFFAAPI.isInGame(player)) {
                if (player.getInventory().getItemInMainHand().getType().equals(Material.BOW)) {
                    new BukkitRunnable()
                    {
                        int timer = 10;
                        @Override
                        public void run() {
                            timer--;
                            if (timer == 10 || timer == 9 || timer == 8 || timer == 7 || timer == 6 || timer == 5 || timer == 4 || timer == 3 || timer == 2 || timer == 1) {
                                if (player.getInventory().contains(Material.ARROW)|| !player.getInventory().contains(Material.BOW)) {
                                    cancel();
                                    timer = 10;
                                } else {
                                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(MessageConfiguration.get().getString("bowuse").replace("%timer%", String.valueOf(timer)).replace("%player%", player.getName()).replace("&", "§")));
                                }
                            }
                            if (timer == 0) {
                                if (player.getInventory().contains(Material.ARROW) || !player.getInventory().contains(Material.BOW)) {
                                    cancel();
                                    timer = 10;
                                } else {
                                    KnockbackFFAKit kitManager = new KnockbackFFAKit();
                                    player.getInventory().addItem(kitManager.kbbowArrow());
                                    cancel();
                                    timer = 10;
                                }
                            }
                        }
                    }.runTaskTimer(KnockbackFFA.getInstance(), 0, 20);
                }
            }
        }
    }
}