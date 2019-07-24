package me.slimelab;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static org.bukkit.ChatColor.*;

public final class PvPInvite extends JavaPlugin implements Listener {

    public static ArrayList<UUID> pvpers = new ArrayList<>();
    public static HashMap<UUID, PvPPlayer> invites = new HashMap<>();


    public static String need_invite, wait_for_accept,invite,invite_OverTime,accept,acceptTo,deny,denyTo,choose_accept,choose_deny,chooseCommand,pvpStart,pvpEnd;

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


        getServer().getPluginManager().registerEvents(this, this);
        getCommand("pvp").setExecutor(new Commands(this));
    }

    @Override
    public void onDisable() {
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent e){

        if(e.getDamager() instanceof Player && e.getEntity() instanceof Player){
            Player damager = (Player)e.getDamager();
            Player damagee = (Player)e.getEntity();
            if(invites.get(damager.getUniqueId()) == null){
            //if(!pvpers.contains(damager.getUniqueId())){
                if(damager.isSneaking()){
                    e.setCancelled(true);
                    damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(wait_for_accept.replaceAll("%player%", damagee.getDisplayName())));
                    send(damagee, invite.replaceAll("%player%", damager.getDisplayName()).split("%NEWLINE%"));
                    sendChoose(damagee,chooseCommand.replaceAll("%player%",damager.getDisplayName()).split(","),new String[]{choose_accept,choose_deny});

                    PvPPlayer pvpPlayer = new PvPPlayer(damager.getUniqueId());
                    invites.put(damager.getUniqueId(),pvpPlayer);
                }else {
                    e.setCancelled(true);
                    damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(need_invite.replaceAll("%player%", damagee.getDisplayName())));

                }
            }else if(invites.get(damager.getUniqueId())!=null && invites.get(damagee.getUniqueId())!=null ){
                if(invites.get(damagee.getUniqueId()).opponents.contains(damager.getUniqueId()) &&
                        invites.get(damager.getUniqueId()).opponents.contains(damagee.getUniqueId())){
                    if(damagee.getHealth()-e.getDamage()<=0){
                        sendEndPVP(damager,damagee,damager.getDisplayName());
                        //PVP 資料移除
                        invites.remove(damager.getUniqueId());
                        invites.remove(damagee.getUniqueId());
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

    private void sendEndPVP(Player sender, Player target, String winner) {
        String[] title = PvPInvite.pvpEnd.replaceAll("%player%",winner).split(",");
        sender.sendTitle(title[0],title[1],0,60,0);
        target.sendTitle(title[0],title[1],0,60,0);
    }
}
