package me.gameisntover.kbffa.command.subcommands.worlds;

import me.gameisntover.kbffa.arena.VoidChunkGenerator;
import me.gameisntover.kbffa.command.SubCommand;
import me.gameisntover.kbffa.customconfig.Knocker;
import org.bukkit.*;
import org.bukkit.block.Block;

import java.util.ArrayList;
import java.util.List;

public class CreateWorldCommand extends SubCommand {
    @Override
    public String getName() {
        return "createworld";
    }

    @Override
    public String getDescription() {
        return ChatColor.translateAlternateColorCodes('&',"Generates / load a custom world which is useful for creating arenas!");
    }

    @Override
    public String getSyntax() {
        return "/createworld <worldname>";
    }

    @Override
    public String getPermission() {
        return "knockbackffa.command.createworld";
    }

    @Override
    public void perform(Knocker knocker, String[] args) {
        if (args.length == 0){
            knocker.getPlayer().sendMessage(ChatColor.RED + "You need to select a name..." + getSyntax());
        return;
        }
            WorldCreator wc = new WorldCreator(args[0]);
            wc.generateStructures(false);
            wc.generator(new VoidChunkGenerator());
            wc.createWorld();
            World world = Bukkit.getWorld(args[0]);
            if (world != null) world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
            assert world != null;
            Block stone = world.getSpawnLocation().getBlock();
            if (stone.getType() == Material.AIR) stone.setType(Material.STONE);
            knocker.getPlayer().sendMessage(ChatColor.GREEN + "World " + args[0] + " has been loaded");
    }

    @Override
    public List<String> tabComplete(Knocker knocker, String[] args) {
        return Bukkit.getWorlds().stream().map(world -> world.getName()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
    }
}
