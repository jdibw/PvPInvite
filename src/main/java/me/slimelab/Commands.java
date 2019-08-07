package me.slimelab;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.lang.*;

public class Commands implements CommandExecutor {


    @Override
    public final boolean onCommand(CommandSender sender, Command cmd,String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pvp")) { // If the player typed /basic then do the following...
            if (!(sender instanceof Player)) {
                sender.sendMessage(PvPInvite.command_Permission);
            }else if(args.length != 2){
                sender.sendMessage(PvPInvite.command);
            }else if(sender.hasPermission("PvPInvite.pvp")){
                Player player = (Player) sender;
                Player target = (Bukkit.getServer().getPlayer(args[1]));
                if(args[0].equalsIgnoreCase("Invite")){//Invite
                    if (target == null) {
                        sender.sendMessage(PvPInvite.target_Offline.replaceAll("%player%", args[1]));
                        return false;
                    }else{
                        PvPInvite.send(player, PvPInvite.wait_for_accept.replaceAll("%player%", target.getDisplayName()));
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent.fromLegacyText(PvPInvite.wait_for_accept.replaceAll("%player%", target.getDisplayName())));
                        String chooseCommand = PvPInvite.chooseCommand.replaceAll("%player%",player.getDisplayName());
                        PvPInvite.send(target, PvPInvite.invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                        PvPInvite.sendChoose(target,chooseCommand.split(","),new String[]{PvPInvite.choose_accept,PvPInvite.choose_deny});

                        PvPInvite.invitesPVP(player, target);
                    }

                }else if(args[0].equalsIgnoreCase("Accept")){//Accept
                    if(PvPInvite.players.get(player.getUniqueId()).invites.contains(target.getUniqueId())){
                        //對方有邀請並且自己目前沒在對戰中
                        String accept = PvPInvite.accept.replaceAll("%player%",target.getDisplayName());
                        String acceptTo = PvPInvite.acceptTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(acceptTo);
                        sender.sendMessage(accept);
                        //同意則開始倒數並把接受決鬥的玩家加入
                        PvPInvite.sendStartPVP(player,target);
                        PvPInvite.acceptPVP(player, target);
                    }
                }else if(args[0].equalsIgnoreCase("Deny")){//Deny
                    if(PvPInvite.players.get(player.getUniqueId()).invites.contains(target.getUniqueId())){
                        String deny = PvPInvite.deny.replaceAll("%player%",target.getDisplayName());
                        String denyTo = PvPInvite.denyTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(denyTo);//給邀請者回覆
                        sender.sendMessage(deny);
                        //拒絕則把邀請決鬥的玩家移除
                        PvPInvite.removeInvites(target, player);
                    }
                }else{
                    sender.sendMessage(PvPInvite.command);
                }
            }else{
                sender.sendMessage(PvPInvite.No_Permission);
            }
            return true;
        }
        return false;
    }
}