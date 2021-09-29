package me.KielKing.Assassin.command;

import me.KielKing.Assassin.Assassin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AssassinCommand implements CommandExecutor{

    private final Assassin plugin;

    public AssassinCommand(Assassin plugin){
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args){
        Assassin plugin = this.plugin;

        if(args.length < 1){
            sender.sendMessage(ChatColor.RED + "Please specify an option.\nadd\nremove\nlist");
            return false;
        }
        if(args[0].toLowerCase().equals("list")){
            this.sendList(sender);
            return true;
        }
        if(args.length < 2){
            sender.sendMessage(ChatColor.RED + "Please specify an option.\nvictim\nhunter");
            return false;
        }
        if(args.length < 3){
            sender.sendMessage(ChatColor.RED + "Please specify a target (Case-Sensitive).");
            return false;
        }

        Player target;
        String name;
        switch(args[0].toLowerCase()){
            case "add":
                switch(args[1].toLowerCase()){

                    case "victim":
                        if(plugin.victims.contains(args[2])){
                            sender.sendMessage(ChatColor.RED + "That target is already a victim.");
                            return false;
                        }
                        target = Bukkit.getPlayer(args[2]);
                        if(target != null){
                            name = target.getName();
                        }else{
                            name = args[2];
                        }
                        plugin.victims.add(name);
                        sender.sendMessage(ChatColor.GREEN + "Added " + name + " to the victims list.");
                        break;
                    case "hunter":
                        if(plugin.hunters.contains(args[2])){
                            sender.sendMessage(ChatColor.RED + "That target is already a hunter.");
                            return false;
                        }
                        target = plugin.getServer().getPlayer(args[2]);
                        if(target != null){
                            plugin.addCompass(target);
                            name = target.getName();
                        }else{
                            name = args[2];
                        }
                        plugin.hunters.add(name);

                        sender.sendMessage(ChatColor.GREEN + "Added " + args[2] + " to the hunter list.");
                        break;
                }
                break;
            case "remove":
                switch(args[1].toLowerCase()){
                    case "victim":
                        plugin.victims.remove(args[2]);
                        sender.sendMessage(ChatColor.GREEN + "Removed " + args[2] + " from the victims list.");
                        break;
                    case "hunter":
                        plugin.hunters.remove(args[2]);
                        sender.sendMessage(ChatColor.GREEN + "Removed " + args[2] + " from the hunters list.");
                        break;
                }
                break;
        }

        return true;
    }

    public void sendList(CommandSender sender){
        Assassin plugin = this.plugin;
        sender.sendMessage(ChatColor.GREEN + "List:\n  Hunters: " + String.join(", ", plugin.hunters) + "\n  Victims: " + String.join(", ", plugin.victims));
    }
}
