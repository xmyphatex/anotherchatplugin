package de.myphate.anotherchatplugin;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * This is a small Chatroom-Plugin made for Bukkit.
 * It has one Dependency Bukkit 1.7.9!
 * 
 * @author xmyPhatex
 * @version 0.7.3
 */

public class ACP extends JavaPlugin {
    public static ACP instance;
    public Configuration conf;
    public static Logger log;
    
    public HashMap<UUID, Integer> ChatPlayer = new HashMap<>();
    public HashMap<Integer, ChatRoom> Chatrooms = new HashMap<>();
    
    private static final HashMap<ChatColor, String> ColorMap = new HashMap<>();
    
    public ACP(){
        instance = this;
        log = instance.getLogger();
    }
    
    @Override
    public void onEnable(){
        try{
            loadConfig();
        }catch (FileNotFoundException e){
            log.severe("Couldn't load config-File.");
            return;
        }
        
        AddColors();
        
        try {
            Commands cmds = new Commands(this);
            for (String s : cmds.cmdlist){
                instance.getServer().getPluginCommand(s).setExecutor(cmds);
            }
            
            PlayerListener pl = new PlayerListener(this);
            instance.getServer().getPluginManager().registerEvents(pl, this);

            log.info("Successfully loaded.");
        } catch (Exception e){
            log.log(Level.SEVERE, "Something goes wrong." ,e);
        }
    }
    
    @Override
    public void onDisable(){
        for(Integer i : Chatrooms.keySet()){
            ChatRoom cr = Chatrooms.get(i);
            cr.closeCR();
        }
        
        ChatPlayer.clear();
        Chatrooms.clear();
    }
    
    public void loadConfig() throws FileNotFoundException{
        conf = instance.getConfig();
        if (conf == null)
            throw new FileNotFoundException();
        
        conf.options().copyDefaults(true);
        conf.addDefault("debug", false);
        conf.addDefault("msg.chatrequest", " has send you a chatroomrequest. Accept with /acceptrequest");
        conf.addDefault("msg.chatroomclosed", "This chatroom has been closed.");
        conf.addDefault("msg.chatroomleave", " leaved the chatroom.");
        conf.addDefault("msg.hasbeenremoved", "You have been removed from the chatroom.");
        conf.addDefault("msg.invalidinput", "You have done something wrong.");
        conf.addDefault("msg.isinchat", "You are already in a chatroom.");
        conf.addDefault("msg.newchatuser", " has joined the chatroom.");
        conf.addDefault("msg.nochatroom", "This chatroom doesn't exist.");
        conf.addDefault("msg.nopermision", "You don't have the permission to do that.");
        conf.addDefault("msg.norequest", "There is not or not anymore a request for you.");
        conf.addDefault("msg.notinchatroom", ChatColor.RED + "You are not in a chatroom.");
        conf.addDefault("msg.playernotinchat", "Player is in no chatroom.");
        conf.addDefault("msg.welcomemsg", "You are now in a chatroom. You can leave with /leaveChat");
        instance.saveConfig();
    } 
    
    /*
    *
    * @param Util for getting a onlineplayer by its name.
    * @return Player which is online or null
    *
    */
    
    public static Player getPlayerbyName(String pname) {
        Player oplayer = null;
        Player[] op = instance.getServer().getOnlinePlayers();
        for (Player p : op) {
            if (p.getName().equalsIgnoreCase(pname)) {
                return p;
            }     
        }
        return oplayer;
    }
    
    public static String ReplaceColor(String msg) {
            for(ChatColor color : ChatColor.values()) {
                if (ColorMap.containsKey(color)) {
                    msg = msg.replaceAll(ColorMap.get(color), "" + color);
                }
            }
        return msg;
    }
    
    private void AddColors() {
        ColorMap.put(ChatColor.RED, "#0");
        ColorMap.put(ChatColor.GREEN, "#1");
        ColorMap.put(ChatColor.BLUE, "#2");
        ColorMap.put(ChatColor.AQUA, "#3");
        ColorMap.put(ChatColor.YELLOW, "#4");
        ColorMap.put(ChatColor.BLACK, "#5");
        ColorMap.put(ChatColor.DARK_RED, "#6");
        ColorMap.put(ChatColor.DARK_AQUA, "#7");
        ColorMap.put(ChatColor.DARK_GREEN, "#8");
        ColorMap.put(ChatColor.DARK_BLUE, "#9");
        ColorMap.put(ChatColor.DARK_PURPLE, "#a");
        ColorMap.put(ChatColor.DARK_GRAY, "#b");
        ColorMap.put(ChatColor.GOLD, "#c");
        ColorMap.put(ChatColor.GRAY, "#d");
        ColorMap.put(ChatColor.LIGHT_PURPLE, "#e");   
        ColorMap.put(ChatColor.WHITE, "#f");
    }
}
