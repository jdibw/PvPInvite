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

import static me.slimelab.PvPInvite.pvpInvite;

public class Commands implements CommandExecutor {

    private PvPPlayer pvpPlayer;

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd,String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pvp")) { // If the player typed /basic then do the following...
            if (!(sender instanceof Player)) {
                sender.sendMessage(pvpInvite.command_Permission);
            }else if(args.length != 2 || !args[0].equalsIgnoreCase("Invite") ||
                    !args[0].equalsIgnoreCase("Accept") ||
                    !args[0].equalsIgnoreCase("Deny")){
                sender.sendMessage(pvpInvite.command);
            }else{
                Player player = (Player) sender;
                Player target = (Bukkit.getServer().getPlayer(args[1]));
                if(args[0].equalsIgnoreCase("Invite")){//Invite
                    if (target == null) {
                        sender.sendMessage(pvpInvite.target_Offline.replaceAll("%player%",args[1]));
                        return false;
                    }else if(pvpInvite.invites.get(target.getUniqueId())!=null &&
                            pvpInvite.invites.get(target.getUniqueId()).pvping){
                        //對方已在對戰無法邀請
                        player.sendMessage(pvpInvite.pvping_invite);
                    }else{
                        pvpInvite.send(player,pvpInvite.wait_for_accept.replaceAll("%player%", target.getDisplayName()));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(pvpInvite.wait_for_accept.replaceAll("%player%", target.getDisplayName())));
                        String chooseCommand = pvpInvite.chooseCommand.replaceAll("%player%",player.getDisplayName());
                        pvpInvite.send(target, pvpInvite.invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                        pvpInvite.sendChoose(target,chooseCommand.split(","),new String[]{pvpInvite.choose_accept,pvpInvite.choose_deny});

                        pvpInvite.addPVP(player, target);
                    }

                }else if(args[0].equalsIgnoreCase("Accept")){//Accept
                    if(pvpInvite.invites.get(player.getUniqueId())!=null &&
                            pvpInvite.invites.get(player.getUniqueId()).pvping){
                        //已在對戰中無法接受邀請
                        player.sendMessage(pvpInvite.pvping_Accept);
                    }else if(pvpInvite.invites.get(target.getUniqueId())!=null){
                        //對方有邀請並且自己目前沒在對戰中
                        String accept = pvpInvite.accept.replaceAll("%player%",target.getDisplayName());
                        String acceptTo = pvpInvite.acceptTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(acceptTo);
                        sender.sendMessage(accept);
                        //同意則開始倒數並把接受決鬥的玩家加入
                        sendStartPVP(player,target);
                        pvpInvite.acceptPVP(player, target);
                    }
                }else if(args[0].equalsIgnoreCase("Deny")){//Deny
                    if(pvpInvite.invites.get(target.getUniqueId())!=null){
                        String deny = pvpInvite.deny.replaceAll("%player%",target.getDisplayName());
                        String denyTo = pvpInvite.denyTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(denyTo);//給邀請者回覆
                        sender.sendMessage(deny);
                        //拒絕則把邀請決鬥的玩家移除
                        pvpInvite.removePVP(player, target);
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

}