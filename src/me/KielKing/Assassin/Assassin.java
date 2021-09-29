package me.KielKing.Assassin;

import me.KielKing.Assassin.command.AssassinCommand;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Assassin extends JavaPlugin{

    public ArrayList<String> hunters = new ArrayList<>();
    public ArrayList<String> victims = new ArrayList<>();
    public HashMap<String, String> frozen = new HashMap<>();

    @Override
    public void onEnable(){
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new EventListener(this), this);

        Objects.requireNonNull(this.getCommand("assassin")).setExecutor(new AssassinCommand(this));
    }

    public void addCompass(Player player){
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta item_meta = item.getItemMeta();
        assert item_meta != null;
        item_meta.setDisplayName(ChatColor.GREEN + "Tracker");
        NamespacedKey key = new NamespacedKey(this, "Assassin_Tracker");
        item_meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "");
        item.setItemMeta(item_meta);

        player.getInventory().setItem(8, item);
    }
}
