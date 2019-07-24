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
    private PvPPlayer pvpPlayer;
    private final PvPInvite pvpInvite;
    public Commands(PvPInvite pvpInvite ) {
        this.pvpInvite = pvpInvite;
    }
    Player target;
    @Override
    public final boolean onCommand(CommandSender sender, Command cmd,String label, String[] args) {

        if (cmd.getName().equalsIgnoreCase("pvp")) { // If the player typed /basic then do the following...
            if (!(sender instanceof Player)) {
                sender.sendMessage("這個指令只能由玩家使用.");
            } else {
                Player player = (Player) sender;
                if(args[0].equalsIgnoreCase("Invite")){//Invite
                    target = (Bukkit.getServer().getPlayer(args[1]));
                    if (target == null) {
                        sender.sendMessage("目標玩家 "+args[1] + " 不在線上!");
                        return false;
                    }
                    String chooseCommand = pvpInvite.chooseCommand.replaceAll("%player%",player.getDisplayName());
                    pvpInvite.send(target, pvpInvite.invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                    pvpInvite.sendChoose(target,chooseCommand.split(","),pvpInvite.chooseString.split(","));
                    pvpPlayer = new PvPPlayer(player.getUniqueId());
                    //pvpInvite.pvpPlayer = new PvPPlayer(player.getUniqueId());
                }else if(args[0].equalsIgnoreCase("Accept")){//Accept
                    if(pvpPlayer.opponents.contains(player.getUniqueId())){
                        String accept = pvpInvite.accept.replaceAll("%player%",player.getDisplayName());
                        String acceptTo = pvpInvite.acceptTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(accept);
                        sender.sendMessage(acceptTo);//給邀請者回覆
                        //同意則開始倒數並把接受決鬥的玩家加入
                        sendStartPVP((Player)sender,target);
                        pvpPlayer.addOpponent(target.getUniqueId());
                    }
                }else if(args[0].equalsIgnoreCase("Deny")){//Deny
                    if(pvpPlayer.opponents.contains(player.getUniqueId())){
                        String deny = pvpInvite.deny.replaceAll("%player%",player.getDisplayName());
                        String denyTo = pvpInvite.denyTo.replaceAll("%player%",player.getDisplayName());
                        target.sendMessage(deny);
                        sender.sendMessage(denyTo);//給邀請者回覆
                        //拒絕則把邀請決鬥的玩家移除
                        pvpPlayer.removeOpponent(player.getUniqueId());
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
                    String title = pvpStart.replaceAll("%sec%",I.toString());
                    sender.sendTitle(title,"",0,20,0);
                    target.sendTitle(title,"",0,20,0);
                }
            }, delay*20L);
            delay++;
        }
    }

    private void sendEndPVP(Player sender, Player target, String winner) {
        String title = PvPInvite.pvpEnd.replaceAll("%player%",winner);
        sender.sendTitle(title,"",0,60,0);
        target.sendTitle(title,"",0,60,0);
    }
}