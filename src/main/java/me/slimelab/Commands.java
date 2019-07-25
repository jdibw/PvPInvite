package me.slimelab;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.lang.*;

import java.util.UUID;

public class Commands implements CommandExecutor {
    
    private PvPPlayer pvpPlayer;

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
                    }else if(PvPInvite.invites.get(target.getUniqueId())!=null &&
                            PvPInvite.invites.get(target.getUniqueId()).pvping){
                        //對方已在對戰無法邀請
                        player.sendMessage(PvPInvite.pvping_invite);
                    }else{
                        PvPInvite.send(player,PvPInvite.wait_for_accept.replaceAll("%player%", target.getDisplayName()));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PvPInvite.wait_for_accept.replaceAll("%player%", target.getDisplayName())));
                        String chooseCommand = PvPInvite.chooseCommand.replaceAll("%player%",player.getDisplayName());
                        PvPInvite.send(target, PvPInvite.invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                        PvPInvite.sendChoose(target,chooseCommand.split(","),new String[]{PvPInvite.choose_accept,PvPInvite.choose_deny});

                        PvPInvite.addPVP(player, target);
                    }

                }else if(args[0].equalsIgnoreCase("Accept")){//Accept
                    if(PvPInvite.invites.get(player.getUniqueId())!=null &&
                            PvPInvite.invites.get(player.getUniqueId()).pvping){
                        //已在對戰中無法接受邀請
                        player.sendMessage(PvPInvite.pvping_Accept);
                    }else if(PvPInvite.invites.get(target.getUniqueId())!=null){
                        //對方有邀請並且自己目前沒在對戰中
                        String accept = PvPInvite.accept.replaceAll("%player%",target.getDisplayName());
                        String acceptTo = PvPInvite.acceptTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(acceptTo);
                        sender.sendMessage(accept);
                        //同意則開始倒數並把接受決鬥的玩家加入
                        sendStartPVP(player,target);
                        PvPInvite.acceptPVP(player, target);
                    }
                }else if(args[0].equalsIgnoreCase("Deny")){//Deny
                    if(PvPInvite.invites.get(target.getUniqueId())!=null){
                        String deny = PvPInvite.deny.replaceAll("%player%",target.getDisplayName());
                        String denyTo = PvPInvite.denyTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(denyTo);//給邀請者回覆
                        sender.sendMessage(deny);
                        //拒絕則把邀請決鬥的玩家移除
                        PvPInvite.removePVP(player, target);
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
            Bukkit.getScheduler().runTaskLater(PvPInvite.pvpInvite, new Runnable() {
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

}