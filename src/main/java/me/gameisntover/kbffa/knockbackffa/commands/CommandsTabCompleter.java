package me.gameisntover.kbffa.knockbackffa.commands;

 import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

 import java.util.ArrayList;
 import java.util.List;

public class CommandsTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete( CommandSender sender,  Command command,  String alias,  String[] args) {
 if (command.getName().equalsIgnoreCase("gotoworld")) {
            if (args.length == 1) {
                List<String> worldList = Bukkit.getWorlds().stream().map(world -> world.getName()).collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
                return worldList;
            }
        }
        return null;
    }
}
