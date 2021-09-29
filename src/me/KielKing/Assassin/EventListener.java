package me.KielKing.Assassin;

import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class EventListener implements Listener{

    private final Assassin plugin;

    public EventListener(Assassin plugin){
        this.plugin = plugin;

        if(plugin.getConfig().getBoolean("assassin")){
            new BukkitRunnable(){
                @Override
                public void run(){
                    for(String target_name : plugin.victims){
                        Player target = plugin.getServer().getPlayerExact(target_name);
                        if(target == null){
                            continue;
                        }

                        Player player = getTargetPlayer(target, 100);
                        if(player == null || target.isDead()){
                            plugin.frozen.remove(target_name);
                            continue;
                        }
                        if(!plugin.hunters.contains(player.getName())){
                            continue;
                        }

                        if(!plugin.frozen.containsValue(player.getName())){
                            plugin.frozen.put(target_name, player.getName());
                        }
                        Ray ray = new Ray(target.getEyeLocation().toVector(), target.getLocation().getDirection());
                        ray.highlight(target.getWorld(), target.getLocation().distance(player.getLocation()), 1);
                    }
                }
            }.runTaskTimer(plugin, 0, 5);
        }
    }

    public static Player getTargetPlayer(Player player, int max){
        List<Player> possible = player.getNearbyEntities(max, max, max).stream().filter(entity -> entity instanceof Player).map(entity -> (Player) entity).filter(player::hasLineOfSight).collect(Collectors.toList());
        Ray ray = Ray.from(player);
        double d = -1;
        Player closest = null;
        for(Player player1 : possible){
            double dis = BoundingBox.from(player1).collidesD(ray, 0, max);
            if(dis != -1){
                if(dis < d || d == -1){
                    d = dis;
                    closest = player1;
                }
            }
        }
        return closest;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent ev){
        Player player = ev.getPlayer();

        if(!plugin.frozen.containsValue(player.getName())){
            return;
        }

        ev.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent ev){
        Entity victim = ev.getEntity();
        Entity damager = ev.getDamager();

        if(!(victim instanceof Player) || !(damager instanceof Player)){
            return;
        }
        if(!plugin.victims.contains(victim.getName())){
            return;
        }
        if(!plugin.getConfig().getBoolean("assassin")){
            return;
        }

        ((Player) victim).setHealth(0);
        plugin.getServer().broadcastMessage(ChatColor.RED + damager.getName() + " has assassinated " + victim.getName());
        plugin.victims.remove(victim.getName());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerInteract(PlayerInteractEvent ev){
        Player player = ev.getPlayer();
        ItemStack item = ev.getItem();

        NamespacedKey key = new NamespacedKey(plugin, "Assassin_Tracker");
        if(item == null){
            return;
        }
        ItemMeta item_meta = item.getItemMeta();
        if(item_meta == null){
            return;
        }
        PersistentDataContainer container = item_meta.getPersistentDataContainer();
        if(!container.has(key, PersistentDataType.STRING)){
            return;
        }

        Location compass_target = null;
        Player tracking = null;
        Location player_location = player.getLocation();
        for(String target_name : plugin.victims){
            Player target = Bukkit.getPlayerExact(target_name);
            if(target == null){
                continue;
            }
            Location target_location = target.getLocation();
            if(compass_target == null || player_location.distance(target_location) < player_location.distance(compass_target)){
                compass_target = target_location;
                tracking = target;
            }
        }
        if(tracking != null){
            player.setCompassTarget(compass_target);
            player.sendMessage(ChatColor.GREEN + "You are tracking " + tracking.getName() + ".");
        }else{
            player.sendMessage(ChatColor.RED + "There are no players to track.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerDeath(PlayerDeathEvent ev){
        Player player = ev.getEntity();
        for(ItemStack drop : new ArrayList<>(ev.getDrops())){
            NamespacedKey key = new NamespacedKey(plugin, "Assassin_Tracker");
            ItemMeta item_meta = drop.getItemMeta();
            if(item_meta == null){
                continue;
            }
            PersistentDataContainer container = item_meta.getPersistentDataContainer();
            if(!container.has(key, PersistentDataType.STRING)){
                continue;
            }

            ev.getDrops().remove(drop);
            break;
        }

        if(!plugin.victims.contains(player.getName())){
            return;
        }
        if(!plugin.getConfig().getBoolean("assassin")){
            return;
        }

        player.setGameMode(GameMode.SPECTATOR);
        plugin.victims.remove(player.getName());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryTransaction(PlayerDropItemEvent ev){
        Item item = ev.getItemDrop();
        ItemStack item_stack = item.getItemStack();

        NamespacedKey key = new NamespacedKey(plugin, "Assassin_Tracker");
        ItemMeta item_meta = item_stack.getItemMeta();
        if(item_meta == null){
            return;
        }
        PersistentDataContainer container = item_meta.getPersistentDataContainer();
        if(!container.has(key, PersistentDataType.STRING)){
            return;
        }

        ev.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent ev){
        if(!(ev.getWhoClicked() instanceof Player)){
            return;
        }
        Player player = (Player) ev.getWhoClicked();
        ItemStack item_stack = ev.getCurrentItem();
        if(Objects.equals(ev.getClickedInventory(), player.getInventory())){
            return;
        }
        if(item_stack == null){
            return;
        }
        if(!plugin.hunters.contains(player.getName())){
            return;
        }
        NamespacedKey key = new NamespacedKey(plugin, "Assassin_Tracker");
        ItemMeta item_meta = item_stack.getItemMeta();
        if(item_meta == null){
            return;
        }
        PersistentDataContainer container = item_meta.getPersistentDataContainer();
        if(!container.has(key, PersistentDataType.STRING)){
            return;
        }

        ev.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerJoin(PlayerJoinEvent ev){
        Player player = ev.getPlayer();

        if(!this.plugin.hunters.contains(player.getName())){
            return;
        }

        this.plugin.addCompass(player);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerRespawn(PlayerRespawnEvent ev){
        Player player = ev.getPlayer();

        if(!this.plugin.hunters.contains(player.getName())){
            return;
        }

        this.plugin.addCompass(player);
    }
}
