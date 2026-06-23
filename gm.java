import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
//import org.bukkit.inventory.ItemStack;
//import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;


/** by Semka2012

* Этот скрипт переводит игрока
*  в творческий режим при заходе, 
*  если у него в левой руке находятся 
*  13 камня(не булыжника) и 30 булыжника 
*  в правой. 
* Чтобы перейти обратно в режим
*  выживания, нужни написать
*  ключ-фразу в чат.

*/
public class gm implements Listener{
	public static final String survivalkey = "%survivalkey%";
	private final JavaPlugin plugin;
	public static void inject(JavaPlugin plugin) {
		plugin.getServer().getPluginManager().registerEvents(new gm(plugin), plugin);
	}
	private gm(JavaPlugin plugin) {
		this.plugin = plugin;
	}
	@EventHandler
	public static void onJoin(PlayerJoinEvent event) {
		Player player = event.getPlayer();
		if (
			player.getInventory().getItemInMainHand().getType() == Material.COBBLESTONE
			&& player.getInventory().getItemInMainHand().getAmount() == 30
			&& player.getInventory().getItemInOffHand().getType() == Material.STONE
			&& player.getInventory().getItemInOffHand().getAmount() == 13
		) {
			player.setGameMode(GameMode.CREATIVE);
			player.sendMessage("Ваш режим игры был изменён на творческий режим.\nДля выхода напишите в чат "+survivalkey);
		}
	}
	@EventHandler
	public static void onChat(PlayerChatEvent event) {
		Player player = event.getPlayer();
		if (event.getMessage().equalsIgnoreCase(survivalkey.trim()) && player.getGameMode() == GameMode.CREATIVE) {
			event.setCancelled(true);
			player.setGameMode(GameMode.SURVIVAL);
			player.sendMessage("Ваш режим игры был изменён на режим выживания.");
		}
	}
}