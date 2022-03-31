package me.gameisntover.kbffa.command.subcommands.kit;

import me.gameisntover.kbffa.command.SubCommand;
import me.gameisntover.kbffa.customconfig.Kits;
import me.gameisntover.kbffa.customconfig.Knocker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class CreateKitCommand extends SubCommand {
    @Override
    public String getName() {
        return "createkit";
    }

    @Override
    public String getDescription() {
        return ChatColor.AQUA + "Creates kit with the specified name";
    }

    @Override
    public String getSyntax() {
        return "/createkit <kitname>";
    }

    @Override
    public String getPermission() {
        return "knockbackffa.command.createkit";
    }

    @Override
    public void perform(Knocker knocker, String[] args) {
        Player p = knocker.getPlayer();
        if (args.length == 0){
            p.sendMessage(ChatColor.RED + "Usage: /createkit <kitname>");
        return;
        }
        if (args.length == 1) {
            Kits kit = Kits.create(args[0]);
            kit.get().set("KitContents", Arrays.asList(Arrays.stream(p.getInventory().getContents()).filter(Objects::nonNull).toArray(ItemStack[]::new)));
            kit.get().set("Price", 100);
            kit.get().set("KitName", args[0]);
            if (p.getInventory().getItemInMainHand().getType() != Material.AIR) kit.get().set("KitIcon", p.getInventory().getItemInMainHand().getType().toString());
            else kit.get().set("KitIcon", "BARRIER");
            kit.get().set("KitDescription", Arrays.asList(ChatColor.GRAY + "Another cool kit!",ChatColor.GRAY + "Must be configured in plugins/KnockbackFFA/kits !"));
            kit.save();
            p.sendMessage(ChatColor.GREEN + "I've created the kit " + args[0] + "! now you need to configure it in the plugins plugins/KnockbackFFA/kits!");
        }
    }

    @Override
    public List<String> tabComplete(Knocker knocker, String[] args) {
        return null;
    }
}
