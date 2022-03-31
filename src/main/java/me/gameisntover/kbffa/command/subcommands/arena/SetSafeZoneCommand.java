package me.gameisntover.kbffa.command.subcommands.arena;

import me.gameisntover.kbffa.command.SubCommand;
import me.gameisntover.kbffa.customconfig.ArenaConfiguration;
import me.gameisntover.kbffa.customconfig.Knocker;
import me.gameisntover.kbffa.listeners.WandListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class SetSafeZoneCommand extends SubCommand {
    @Override
    public String getName() {
        return "setsafezone";
    }

    @Override
    public String getDescription() {
        return ChatColor.translateAlternateColorCodes('&',"&5Setups a new safezone with the selected positions (from wand)");
    }

    @Override
    public String getSyntax() {
        return "/setsafezone";
    }

    @Override
    public String getPermission() {
        return "knockbackffa.command.setsafezone";
    }

    @Override
    public void perform(Knocker knocker, String[] args) {
        Player p = knocker.getPlayer();
        if (WandListener.pos2m.get(p) != null && WandListener.pos1m.get(p) == null) {
            p.sendMessage(ChatColor.RED + "You must choose some positions before using this command use /wand");
            return;
        }
        List<String> safezones = ArenaConfiguration.get().getStringList("registered-safezones");
        int sz;
        if (safezones.size() == 0) sz = 1;
        else {
            String szstring = safezones.get(safezones.size() - 1);
            sz = Integer.parseInt(szstring);
            sz++;
        }
        String world = p.getWorld().getName();
        Location loc1 = WandListener.pos1m.get(p);
        Location loc2 = WandListener.pos2m.get(p);
        ArenaConfiguration.get().set("Safezones." + sz + ".world", world);
        ArenaConfiguration.get().set("Safezones." + sz + ".pos1", loc1);
        ArenaConfiguration.get().set("Safezones." + sz + ".pos2", loc2);
        safezones.add(sz + "");
        ArenaConfiguration.get().set("registered-safezones", safezones);
        ArenaConfiguration.save();
        p.sendMessage(ChatColor.GREEN + "Safezone " + sz + " has been saved in the arena config file!");
    }

    @Override
    public List<String> tabComplete(Knocker knocker, String[] args) {
        return null;
    }
}