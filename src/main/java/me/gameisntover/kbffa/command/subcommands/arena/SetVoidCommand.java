package me.gameisntover.kbffa.command.subcommands.arena;

import me.gameisntover.kbffa.command.SubCommand;
import me.gameisntover.kbffa.customconfig.ArenaConfiguration;
import me.gameisntover.kbffa.customconfig.Knocker;
import me.gameisntover.kbffa.listeners.WandListener;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.List;

public class SetVoidCommand extends SubCommand {
    @Override
    public String getName() {
        return "setvoid";
    }

    @Override
    public String getDescription() {
        return ChatColor.AQUA+ "sets a damage zone which must be selected with wand before using the command!";
    }

    @Override
    public String getSyntax() {
        return "/setvoid";
    }

    @Override
    public String getPermission() {
        return "knockbackffa.command.setvoid";
    }

    @Override
    public void perform(Knocker knocker, String[] args) {
        Player p = knocker.getPlayer();
        if (WandListener.pos1m.get(p) != null && WandListener.pos2m.get(p) != null) {
            Location pos1 = WandListener.pos1m.get(p);
            Location pos2 = WandListener.pos2m.get(p);
            int vd;
            List<String> voids = ArenaConfiguration.get().getStringList("registered-voids");
            if (voids.size() == 0) vd = 1;
            else {
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
                p.sendMessage(ChatColor.GREEN + "Void " + vd + " has been set and now players will get damage if they go there");
            }
        } else p.sendMessage(ChatColor.RED + "You have to set two positions first!");

    }

    @Override
    public List<String> tabComplete(Knocker knocker, String[] args) {
        return null;
    }
}