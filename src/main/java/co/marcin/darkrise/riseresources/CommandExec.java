package co.marcin.darkrise.riseresources;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandExec implements CommandExecutor, TabCompleter {
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 0 || !args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.DARK_GRAY + "/" + ChatColor.GRAY + "resources reload");
            return true;
        }

        RiseResourcesPlugin.getInstance().reloadConfigs();
        sender.sendMessage(ChatColor.GRAY + "Configuration " + ChatColor.DARK_AQUA + "reloaded!");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return new ArrayList<>(Arrays.asList("reload"));
    }
}
