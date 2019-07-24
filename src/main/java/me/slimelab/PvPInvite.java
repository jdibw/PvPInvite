package me.slimelab;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.*;
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

    public PvPPlayer pvpPlayer;
    public static ArrayList<UUID> pvpers = new ArrayList<>();
    public static HashMap<UUID, UUID> invites = new HashMap<>();

    public static String need_invite, wait_for_accept,invite,chooseString, accept,deny,chooseCommand,pvpStart,pvpEnd;


    @Override
    public void onEnable() {
        saveDefaultConfig();
        need_invite = translateAlternateColorCodes('&', getConfig().getString("messages.Need_Invite"));
        wait_for_accept = translateAlternateColorCodes('&', getConfig().getString("messages.Wait_for_Accept"));
        invite = translateAlternateColorCodes('&', getConfig().getString("messages.Invite"));
        accept = translateAlternateColorCodes('&', getConfig().getString("messages.Accept"));
        deny = translateAlternateColorCodes('&', getConfig().getString("messages.Deny"));
        chooseString = translateAlternateColorCodes('&', getConfig().getString("messages.ChooseString"));
        chooseCommand = translateAlternateColorCodes('&', getConfig().getString("messages.ChooseCommand"));
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
            if(!pvpers.contains(damager.getUniqueId())){
                if(damager.isSneaking()){
                    e.setCancelled(true);
                    damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(wait_for_accept.replaceAll("%player%", damagee.getDisplayName())));
                    send(damagee, invite.replaceAll("%player%", damager.getDisplayName()).split("%NEWLINE%"));
                    sendChoose(damagee,chooseString.replaceAll("%player%",damager.getDisplayName()).split(","),"[同意],[拒絕]".split(","));
                }else {
                    e.setCancelled(true);
                    damager.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(need_invite.replaceAll("%player%", damagee.getDisplayName())));

                }
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
                messages[0]).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, onClick[0])).color(ChatColor.DARK_GREEN).append(
                messages[1]).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, onClick[1])).color(ChatColor.RED).create());
    }

    public static void send(Player player, String[] messages) {
        for (String m : messages) {
            player.sendMessage(m);
        }
    }

}
