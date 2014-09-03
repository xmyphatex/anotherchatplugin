package de.myphate.anotherchatplugin;

import java.util.List;
import java.util.UUID;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final ACP plugin; 
    
    public PlayerListener(ACP instance){
        this.plugin = instance;
    }
    
    @EventHandler
    public void onLeave(PlayerQuitEvent e){
        if(plugin.ChatPlayer.isEmpty()){
            return;
        }
        
        Player p = e.getPlayer();
        UUID pid = p.getUniqueId();
        
        if(!plugin.ChatPlayer.containsKey(pid)){
            return;
        }
        
        Integer cid = plugin.ChatPlayer.get(pid);
        ChatRoom cr = plugin.Chatrooms.get(cid);
        if(cr.isLast()){
            cr.closeCR();
            plugin.Chatrooms.remove(cid);
            cr = null;
        } else {
            cr.leave(pid);
        }
        plugin.ChatPlayer.remove(pid);
    }
    
    @EventHandler // Für neue Methode anpassen.
    public void onChat(AsyncPlayerChatEvent e){
        if(plugin.ChatPlayer.isEmpty()){
            return;
        }
        
        Player p = e.getPlayer();
        UUID pid = p.getUniqueId(); 
        
        if(plugin.ChatPlayer.containsKey(pid)){
            Integer cid = plugin.ChatPlayer.get(pid);
            ChatRoom cr = plugin.Chatrooms.get(cid); 
            e.getRecipients().clear();
            for(UUID uid : cr.getMember()){
                Player rep = plugin.getServer().getPlayer(uid);
                e.getRecipients().add(rep);
            }
            e.getRecipients().add(p);
        } else {
            List<UUID> chatuser = (List<UUID>) plugin.ChatPlayer.keySet();
            for (UUID uid : chatuser){
                Player rep = plugin.getServer().getPlayer(uid);
                e.getRecipients().remove(rep);
            }
        }
    }
}
