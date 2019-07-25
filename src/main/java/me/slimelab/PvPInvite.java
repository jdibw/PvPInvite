package me.slimelab;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public final class PvPInvite extends JavaPlugin implements Listener {

    private PvPPlayer pvpPlayer;
    public static ArrayList<UUID> pvpers = new ArrayList<>();
    public static HashMap<UUID, PvPPlayer> invites = new HashMap<>();


    public static String need_invite, wait_for_accept,invite,invite_OverTime,
            accept,acceptTo,deny,denyTo,choose_accept,choose_deny,chooseCommand,
            pvpStart,pvpEnd,pvping_invite,pvping_Accept;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        need_invite = translateAlternateColorCodes('&', getConfig().getString("messages.Need_Invite"));
        wait_for_accept = translateAlternateColorCodes('&', getConfig().getString("messages.Wait_for_Accept"));
        invite = translateAlternateColorCodes('&', getConfig().getString("messages.Invite"));
        invite_OverTime = translateAlternateColorCodes('&', getConfig().getString("messages.Invite_OverTime"));
        accept = translateAlternateColorCodes('&', getConfig().getString("messages.Accept"));
        deny = translateAlternateColorCodes('&', getConfig().getString("messages.Deny"));
        acceptTo = translateAlternateColorCodes('&', getConfig().getString("messages.AcceptTo"));
        denyTo = translateAlternateColorCodes('&', getConfig().getString("messages.DenyTo"));
        chooseCommand = translateAlternateColorCodes('&', getConfig().getString("messages.ChooseCommand"));
        choose_accept = translateAlternateColorCodes('&', getConfig().getString("messages.ChooseAccept"));
        choose_deny= translateAlternateColorCodes('&', getConfig().getString("messages.ChooseDeny"));
        pvpStart = translateAlternateColorCodes('&', getConfig().getString("messages.PVPStart"));
        pvpEnd = translateAlternateColorCodes('&', getConfig().getString("messages.PVPEnd"));
        pvping_invite = translateAlternateColorCodes('&', getConfig().getString("messages.PVPing_invite"));
        pvping_Accept = translateAlternateColorCodes('&', getConfig().getString("messages.PVPing_Accept"));

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("pvp").setExecutor(new Commands(this));
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        if(invites.get(player.getUniqueId())!=null &&
                invites.get(player.getUniqueId()).pvping){
            for(UUID uuid :invites.get(player.getUniqueId()).opponents){
                Player target = Bukkit.getPlayer(uuid);
                sendEndPVP(player, target, target.getDisplayName());
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        if(invites.get(player.getUniqueId())!=null &&
                invites.get(player.getUniqueId()).pvping){
            for(UUID uuid :invites.get(player.getUniqueId()).opponents){
                Player target = Bukkit.getPlayer(uuid);
                sendEndPVP(player, target, target.getDisplayName());
            }
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e){

        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player){
            Player player = (Player)e.getDamager();
            Player target = (Player)e.getEntity();
            if(invites.get(player.getUniqueId()) == null){
                if(player.isSneaking()){
                    e.setCancelled(true);
                    if(invites.get(target.getUniqueId())!=null && invites.get(target.getUniqueId()).pvping){
                        //對方已在對戰無法邀請
                        player.sendMessage(pvping_invite);
                    }else {
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(wait_for_accept.replaceAll("%player%", target.getDisplayName())));
                        send(target, invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                        sendChoose(target, chooseCommand.replaceAll("%player%", player.getDisplayName()).split(","), new String[]{choose_accept, choose_deny});

                        addPVP(player, target);
                    }
                }else {
                    e.setCancelled(true);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(need_invite.replaceAll("%player%", target.getDisplayName())));

                }
            }else if(invites.get(player.getUniqueId())!=null && invites.get(target.getUniqueId())!=null ){
                if(invites.get(target.getUniqueId()).opponents.contains(player.getUniqueId()) &&
                        invites.get(player.getUniqueId()).opponents.contains(target.getUniqueId())){
                    if(target.getHealth()-e.getDamage()<=0){
                        sendEndPVP(player,target,player.getDisplayName());
                        //PVP 資料移除
                        invites.remove(player.getUniqueId());
                        invites.remove(target.getUniqueId());
                    }
                }
            }else{
                e.setCancelled(true);
            }
        }
    }

    public static void send(Player player, String onClick, String[] messages) {
        for (String m : messages) {
            BaseComponent[] comps = TextComponent.fromLegacyText(translateAlternateColorCodes('&', m));
            for (BaseComponent comp : comps) {
                comp.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, onClick));
            }
            player.spigot().sendMessage(comps);
        }
    }

    public static void sendChoose(Player player, String[] onClick, String[] messages) {
        player.spigot().sendMessage(new ComponentBuilder(
                messages[0]).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, onClick[0])).append(
                messages[1]).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, onClick[1])).create());
    }

    public static void send(Player player, String[] messages) {
        for (String m : messages) {
            player.sendMessage(m);
        }
    }

    public static void send(Player player, String messages) {
        player.sendMessage(messages);
    }


    public void addPVP(Player player, Player target){
        pvpPlayer = new PvPPlayer(player.getUniqueId());
        invites.put(player.getUniqueId(),pvpPlayer);
        Integer delay = 15;
        Bukkit.getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                if(!invites.get(player.getUniqueId()).pvping){
                    player.sendMessage(invite_OverTime);
                    removePVP(player, target);
                }
            }
        }, delay*20L);
    }

    public void acceptPVP(Player sender, Player target){
        PvPPlayer pvpPlayer = new PvPPlayer(sender.getUniqueId());
        pvpPlayer.addOpponent(target.getUniqueId());
        pvpPlayer.pvping = true;
        invites.put(sender.getUniqueId(),pvpPlayer);

        pvpPlayer = new PvPPlayer(target.getUniqueId());
        pvpPlayer.addOpponent(sender.getUniqueId());
        pvpPlayer.pvping = true;
        invites.put(target.getUniqueId(),pvpPlayer);
    }

    public void removePVP(Player sender, Player target){
        invites.remove(sender.getUniqueId());
        invites.remove(target.getUniqueId());
    }
    private void sendEndPVP(Player sender, Player target, String winner) {
        String[] title = PvPInvite.pvpEnd.replaceAll("%player%",winner).split(",");
        sender.sendTitle(title[0],title[1],0,60,0);
        target.sendTitle(title[0],title[1],0,60,0);
    }


}
