package de.myphate.anotherchatplugin;

import static de.myphate.anotherchatplugin.ACP.ReplaceColor;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class ChatRoom {
    private final ACP plugin;
    private final Integer CRID;
    private final List<UUID> member = new ArrayList<>();
    private final String prefix;
    
    public ChatRoom(Integer id, ACP instance){
        CRID = id;
        this.plugin = instance;
        prefix = ChatColor.GOLD + "[Chat " + CRID + "] ";
    }
    
    public Integer getID(){
        return CRID;
    }   
    
    public void addMember(UUID uid){
        Player p = null;
        p = plugin.getServer().getPlayer(uid);
        p.sendMessage(prefix + ReplaceColor(plugin.conf.getString("msg.welcomemsg")));
        if (!member.isEmpty()){
            for(UUID id : member){
                p = plugin.getServer().getPlayer(id);
                p.sendMessage(prefix + p.getName() +
                        ReplaceColor(plugin.conf.getString("msg.newchatuser")));
            }
        }
        member.add(uid);
    }
    
    public List<UUID> getMember(){
        return member;
    }
    
    public void closeCR(){
        if (member.isEmpty()){
            return;
        }
        
        for(UUID u : member){
            Player p = plugin.getServer().getPlayer(u);
            p.sendMessage(prefix + ReplaceColor(plugin.conf.getString("msg.chatroomclosed")));
        }
        member.clear();
    }
    
    public Boolean isLast(){
        return member.size() == 1;
    }

    public void leave(UUID pID){  
        member.remove(pID);
        Player leaveP = plugin.getServer().getPlayer(pID);
        String lPName = leaveP.getName();
        String msg = plugin.conf.getString("msg.chatroomleave");
        for(UUID u : member){
            Player p = plugin.getServer().getPlayer(u);
            p.sendMessage(prefix + lPName + ReplaceColor(msg));
        }
    }
}
