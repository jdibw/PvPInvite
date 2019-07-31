package me.slimelab;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.entity.Trident;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public final class PvPInvite extends JavaPlugin implements Listener {

    public static PvPInvite pvpInvite;
    public static HashMap<UUID, PvPPlayer> invites = new HashMap<>();


    public static String need_invite, wait_for_accept,invite,invite_OverTime,
            accept,acceptTo,deny,denyTo,choose_accept,choose_deny,chooseCommand,
            pvpStart,pvpEnd,command_Permission,
            target_Offline,command;

    @Override
    public void onEnable() {
        pvpInvite = this;
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
        command_Permission = translateAlternateColorCodes('&', getConfig().getString("messages.Command_Permission"));
        target_Offline = translateAlternateColorCodes('&', getConfig().getString("messages.Target_Offline"));
        command = translateAlternateColorCodes('&', getConfig().getString("messages.Command"));

        getServer().getPluginManager().registerEvents(this, this);
        getCommand("pvp").setExecutor(new Commands());
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        Player player = e.getPlayer();
        EndPVP(player);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e){
        Player player = e.getEntity();
        EndPVP(player);
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e){

        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player){
            Player player = (Player)e.getDamager();
            Player target = (Player)e.getEntity();
            if(invites.get(player.getUniqueId()) == null){
                e.setCancelled(true);
                if(player.isSneaking()){
                    if(invites.get(player.getUniqueId()).invites.contains(target.getUniqueId())){
                        target.sendMessage(acceptTo.replaceAll("%player%",player.getDisplayName()));
                        player.sendMessage(accept.replaceAll("%player%",target.getDisplayName()));
                        //同意則開始倒數並把接受決鬥的玩家加入
                        sendStartPVP(player,target);
                        acceptPVP(player, target);
                    }else{
                        send(player,wait_for_accept.replaceAll("%player%", target.getDisplayName()));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(wait_for_accept.replaceAll("%player%", target.getDisplayName())));
                        send(target, invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                        sendChoose(target, chooseCommand.replaceAll("%player%", player.getDisplayName()).split(","), new String[]{choose_accept, choose_deny});
                        invitesPVP(player, target);
                    }
                }else {
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(need_invite.replaceAll("%player%", target.getDisplayName())));
                }
            }else if(invites.get(player.getUniqueId()).opponents.contains(target.getUniqueId()) &&
                    invites.get(target.getUniqueId()).opponents.contains(player.getUniqueId()) &&
                    invites.get(player.getUniqueId()).pvping && invites.get(target.getUniqueId()).pvping){
                //雙方接受PVP後可以互相傷害
            }else{
                e.setCancelled(true);
            }
        }else if((e.getDamager() instanceof Arrow || e.getDamager() instanceof Trident)
                && e.getEntity() instanceof Player){
            Player player = null;
            if(e.getDamager() instanceof Arrow){
                Arrow arrow = (Arrow) e.getDamager();
                player = (Player) arrow.getShooter();
            }else if(e.getDamager() instanceof Trident){
                Trident trident = (Trident) e.getDamager();
                player = (Player) trident.getShooter();
            }
            Player target = (Player) e.getEntity();
            if(invites.get(player.getUniqueId())!=null && invites.get(target.getUniqueId())!=null ){
                if(invites.get(target.getUniqueId()).opponents.contains(player.getUniqueId()) &&
                        invites.get(player.getUniqueId()).opponents.contains(target.getUniqueId())){
                    if(target.getHealth()-e.getDamage()<=0){
                        EndPVP(target);
                    }
                }
            }else{
                e.setCancelled(true);
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(need_invite.replaceAll("%player%", target.getDisplayName())));
            }
        }//else{
        //    e.setCancelled(true);
        //}
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

    public static void invitesPVP(Player sender, Player target){
        //invites.get(sender.getUniqueId()).addInvites(target.getUniqueId());
        invites.get(target.getUniqueId()).addInvites(sender.getUniqueId());
        //在受邀請者中加入邀請
        Integer delay = 10;
        //10秒後邀請無效
        Bukkit.getScheduler().runTaskLater(pvpInvite, new Runnable() {
            @Override
            public void run() {
                if(!invites.get(sender.getUniqueId()).pvping){
                    if(sender.isOnline())
                        sender.sendMessage(invite_OverTime);
                }
                if(!invites.get(sender.getUniqueId()).pvping){
                    if(target.isOnline())
                        target.sendMessage(invite_OverTime);
                }
            }
        }, delay*20L);
    }

    public static void acceptPVP(Player sender, Player target){
        invites.get(sender.getUniqueId()).addOpponent(target.getUniqueId());
        invites.get(sender.getUniqueId()).setPVPing(true);
        //PvPPlayer pvpPlayer = invites.get(sender.getUniqueId());
        //pvpPlayer.addOpponent(target.getUniqueId());
        //pvpPlayer.setPVPing(true);
        //invites.put(sender.getUniqueId(),pvpPlayer);

        invites.get(target.getUniqueId()).addOpponent(sender.getUniqueId());
        invites.get(target.getUniqueId()).setPVPing(true);
        //pvpPlayer = invites.get(target.getUniqueId());
        //pvpPlayer.addOpponent(sender.getUniqueId());
        //pvpPlayer.setPVPing(true);
        //invites.put(target.getUniqueId(),pvpPlayer);

    }

    public static void sendStartPVP(Player sender, Player target) {
        Integer delay = 0;
        for(int i = 3 ; i > 0 ; i--) {
            Integer I = i;
            Bukkit.getScheduler().runTaskLater(pvpInvite, new Runnable() {
                @Override
                public void run() {
                    String title = pvpStart.replaceAll("%time%",I.toString());
                    sender.sendTitle(title,"",0,20,0);
                    target.sendTitle(title,"",0,20,0);
                }
            }, delay*20L);
            delay++;
        }
    }

    public static void removePVP(Player sender, Player target){
        invites.get(sender.getUniqueId()).removeOpponent(target.getUniqueId());
        invites.get(target.getUniqueId()).removeOpponent(sender.getUniqueId());
    }

    private static void EndPVP(Player sender) {
        if(invites.get(sender.getUniqueId()).pvping){
            for(UUID uuid :invites.get(sender.getUniqueId()).opponents){
                Player target = Bukkit.getPlayer(uuid);
                String[] title = PvPInvite.pvpEnd.replaceAll("%player%",target.getDisplayName()).split(",");
                if(sender.isOnline())
                    sender.sendTitle(title[0],title[1],0,100,0);
                if(target.isOnline())
                    target.sendTitle(title[0],title[1],0,100,0);
                removePVP(sender, target);
            }
        }
    }

}
