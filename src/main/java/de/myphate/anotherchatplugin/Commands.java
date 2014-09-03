package de.myphate.anotherchatplugin;

import static de.myphate.anotherchatplugin.ACP.ReplaceColor;
import static de.myphate.anotherchatplugin.ACP.log;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {
    private final ACP plugin;
    private final HashMap<UUID, Integer> RQ_PY_TIME = new HashMap<>();
    private final HashMap<UUID, Integer> RQ_PY_CR = new HashMap<>();
    
    public final List<String> cmdlist = Arrays.asList(new String[]{
                                                        "craccept", "cra",
                                                        "crinvite", "cri", 
                                                        "closechat", "crcc",
                                                        "leavechat", "crlc",
                                                        "opennewchat", "croc",
                                                        "rmchatuser", "crrcu",
                                                        "showchatrooms", "crsc"});

    public Commands(ACP instance){
        this.plugin = instance;
        
        try {
            plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, new cleanup(), 0L, 1200L);
        }catch (IllegalArgumentException e){
            log.log(Level.SEVERE, "Couldn't start Cleanup-Task.", e);
        }
    } 

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] args) {     
        String cmd = cmnd.getName().toLowerCase();
        if(!cmdlist.contains(cmd)){
            return true;
        }
        
        if(!(cs instanceof Player)){
            cs.sendMessage(ChatColor.RED + "This can only a Player.");
            return true;
        }
        
        if(!cs.hasPermission("acp." + cmd)){
            cs.sendMessage(ReplaceColor(plugin.conf.getString("msg.nopermision")));
            return true;
        }
        
        Player CMDSender = (Player)cs;
        UUID CMDSenderID = CMDSender.getUniqueId();
        ChatRoom cr = null; // Chatroom
        Integer crID = null; // ChatroomID
        Player cPlayer = null; // Named player
        
        switch(cmd){
            case "cra":
            case "craccept":
                if(plugin.ChatPlayer.containsKey(CMDSenderID)){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.isinchat")));
                    return true;               
                }
                
                if(!RQ_PY_TIME.containsKey(CMDSenderID)){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.norequest")));
                    return true;
                }
                                
                crID = RQ_PY_CR.get(CMDSenderID);
                RQ_PY_TIME.remove(CMDSenderID);
                RQ_PY_CR.remove(CMDSenderID);    
                
                if (plugin.Chatrooms.containsKey(crID)){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.nochatroom")));
                    return true;
                }
                
                plugin.ChatPlayer.put(CMDSenderID, crID);
                cr = plugin.Chatrooms.get(crID);
                cr.addMember(CMDSenderID);
                return true;
                
            case "cri":
            case "crinvite":
                if(!plugin.ChatPlayer.containsKey(CMDSenderID)){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.notinchatroom")));
                    return true;
                }
                
                if(args.length == 0){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.invalidinput")));
                    return false;                    
                }
                
                cPlayer = ACP.getPlayerbyName(args[0]);
                if (cPlayer == null){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.playernotonline")));
                    return true;
                }
                
                cPlayer.sendMessage(CMDSender.getName() + ReplaceColor(plugin.conf.getString("msg.chatrequest")));
                RQ_PY_TIME.put(cPlayer.getUniqueId(), 1200);
                RQ_PY_CR.put(cPlayer.getUniqueId(), crID);
                
                return true;
                                
            case "crcc":
            case "closechat": 
                if(args.length == 0){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.invalidinput")));
                    return false;                    
                }

                try{
                    crID = Integer.parseInt(args[0]);
                }catch(NumberFormatException e) {
                    CMDSender.sendMessage(ChatColor.RED + "This is not a valid number!");
                    return true;
                }
                
                if(!plugin.Chatrooms.containsKey(crID)){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.nochatroom")));
                    return true;
                }
                
                cr = plugin.Chatrooms.get(crID);
                plugin.Chatrooms.remove(crID);
                
                List<UUID> member = cr.getMember();
                cr.closeCR();
                
                for(UUID pID : member){
                    plugin.ChatPlayer.remove(pID);
                }
                return true;
                
            case "crjc":
            case "joinchat":
                if(args.length == 0){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.invalidinput")));
                    return false;                    
                }
                
                try{
                    crID = Integer.parseInt(args[0]);
                }catch(NumberFormatException e) {
                    CMDSender.sendMessage("This is not a valid number!");
                    return true;
                }                
                
                if(!plugin.Chatrooms.containsKey(crID)){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.nochatroom")));
                    return true;
                }
                
                cr = plugin.Chatrooms.get(crID);
                plugin.ChatPlayer.put(CMDSenderID, crID);
                cr.addMember(CMDSenderID);
                return true;
                
            case "crlc":
            case "leavechat":
                if(!plugin.ChatPlayer.containsKey(CMDSenderID)){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.notinchatroom")));
                    return true;
                }
                
                crID = plugin.ChatPlayer.get(CMDSenderID);
                cr = plugin.Chatrooms.get(crID);
                if(cr.isLast()){
                    cr.closeCR();
                    plugin.Chatrooms.remove(crID);
                    cr = null;
                } else {
                    cr.leave(CMDSenderID);
                }
                plugin.ChatPlayer.remove(CMDSenderID);
                return true;
                
            case "croc":
            case "opennewchat":
                crID = plugin.Chatrooms.size() + 1;
                cr = new ChatRoom(crID, plugin);
                cr.addMember(CMDSenderID);
                plugin.Chatrooms.put(crID, cr);
                plugin.ChatPlayer.put(CMDSenderID, crID);
                return true;
                
            case "crrcu":
            case "rmchatuser":
                if (args.length == 0){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.invalidinput")));
                    return false;
                }
                
                cPlayer = ACP.getPlayerbyName(args[0]);
                if (cPlayer == null){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.playernotonline")));
                    return true;
                }

                UUID cPID = cPlayer.getUniqueId();
                if (!plugin.ChatPlayer.containsKey(cPID)){
                    CMDSender.sendMessage(ReplaceColor(plugin.conf.getString("msg.playernotinchat")));
                    return true;
                }
                
                crID = plugin.ChatPlayer.get(cPID); // Get ChatroomID
                cr = plugin.Chatrooms.get(crID); // Get Chatroom
                cr.leave(cPID); // Let Player leave Chatroom
                cPlayer.sendMessage(ReplaceColor(plugin.conf.getString("msg.hasbeenremoved")));
                plugin.ChatPlayer.remove(cPID); // 
                return true;
                
            case "crsc":
            case "showchatrooms":
                CMDSender.sendMessage(ChatColor.GOLD + "Chatrooms:");
                CMDSender.sendMessage(ChatColor.GOLD + "~~~~~~~~~~~~~~~~~~~~~~~~");
                if (plugin.Chatrooms.isEmpty()){
                    return true;
                }
                Collection<Integer> CRIDs = plugin.Chatrooms.keySet();
                
                for(Integer i : CRIDs){
                    String prefix = ChatColor.GOLD + "[Chat " + i + "]: ";
                    cr = plugin.Chatrooms.get(i);
                    String mlist = "";
                    List<UUID> members = cr.getMember();
                    for(UUID pid : members){
                        Player p = plugin.getServer().getPlayer(pid);
                        mlist += p.getName()+ ", ";
                    }
                    CMDSender.sendMessage(prefix + mlist);
                }
                return true;   
                
            default:
                return false;
        }
    }
        
    private class cleanup implements Runnable{

        @Override
        public void run() {
            if(RQ_PY_TIME.isEmpty()){
                return;
            }
            
            List<UUID> norequest = new ArrayList<>();
            for(UUID id : RQ_PY_TIME.keySet()){
                Integer time = RQ_PY_TIME.get(id);
                if(time - 600 <= 0){
                    norequest.add(id);
                }
            }
            for(UUID id : norequest){
                RQ_PY_TIME.remove(id);
                RQ_PY_CR.remove(id);
            }
        }
    };    
    
}
