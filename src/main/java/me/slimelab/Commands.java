package me.slimelab;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.lang.*;

import java.util.UUID;

public class Commands implements CommandExecutor {

    private PvPInvite pvpInvite;
    private PvPPlayer pvpPlayer;
    public Commands(PvPInvite pvpInvite ) {
        this.pvpInvite = pvpInvite;
    }

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd,String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pvp")) { // If the player typed /basic then do the following...
            if (!(sender instanceof Player)) {
                sender.sendMessage("這個指令只能由玩家使用.");
            } else {
                Player player = (Player) sender;
                Player target = (Bukkit.getServer().getPlayer(args[1]));
                if(args[0].equalsIgnoreCase("Invite")){//Invite
                    if (target == null) {
                        sender.sendMessage("目標玩家 "+args[1] + " 不在線上!");
                        return false;
                    }
                    String chooseCommand = pvpInvite.chooseCommand.replaceAll("%player%",player.getDisplayName());
                    pvpInvite.send(target, pvpInvite.invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                    pvpInvite.sendChoose(target,chooseCommand.split(","),new String[]{pvpInvite.choose_accept,pvpInvite.choose_deny});

                    addPVP(player, target);
                }else if(args[0].equalsIgnoreCase("Accept")){//Accept
                    if(pvpInvite.invites.get(target.getUniqueId())!=null){
                        String accept = pvpInvite.accept.replaceAll("%player%",target.getDisplayName());
                        String acceptTo = pvpInvite.acceptTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(acceptTo);
                        sender.sendMessage(accept);
                        //同意則開始倒數並把接受決鬥的玩家加入
                        sendStartPVP(player,target);
                        acceptPVP(player, target);
                    }
                }else if(args[0].equalsIgnoreCase("Deny")){//Deny
                    if(pvpInvite.invites.get(target.getUniqueId())!=null){
                        String deny = pvpInvite.deny.replaceAll("%player%",target.getDisplayName());
                        String denyTo = pvpInvite.denyTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(denyTo);//給邀請者回覆
                        sender.sendMessage(deny);
                        //拒絕則把邀請決鬥的玩家移除
                        removePVP(player, target);
                    }
                }
            }
            return true;
        }
        return false;
    }

    private void sendStartPVP(Player sender, Player target) {
        Integer delay = 0;
        String pvpStart = PvPInvite.pvpStart;
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

    public void addPVP(Player player, Player target){
        pvpPlayer = new PvPPlayer(player.getUniqueId());
        pvpInvite.invites.put(player.getUniqueId(),pvpPlayer);
        Integer delay = 15;
        Bukkit.getScheduler().runTaskLater(pvpInvite, new Runnable() {
            @Override
            public void run() {
                if(!pvpInvite.invites.get(player.getUniqueId()).pvping){
                    player.sendMessage(pvpInvite.invite_OverTime);
                    removePVP(player, target);
                }
            }
        }, delay*20L);
    }

    public void acceptPVP(Player sender, Player target){
        PvPPlayer pvpPlayer = new PvPPlayer(sender.getUniqueId());
        pvpPlayer.addOpponent(target.getUniqueId());
        pvpInvite.invites.put(sender.getUniqueId(),pvpPlayer);

        pvpPlayer = new PvPPlayer(target.getUniqueId());
        pvpPlayer.addOpponent(sender.getUniqueId());
        pvpInvite.invites.put(target.getUniqueId(),pvpPlayer);
        sender.sendMessage("pvping:"+pvpInvite.invites.get(sender.getUniqueId()).pvping+"");
    }

    public void removePVP(Player sender, Player target){
        pvpInvite.invites.remove(sender.getUniqueId());
        pvpInvite.invites.remove(target.getUniqueId());
    }
}