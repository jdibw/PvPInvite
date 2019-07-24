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

    private final PvPInvite pvpInvite;
    public Commands(PvPInvite pvpInvite ) {
        this.pvpInvite = pvpInvite;
    }

    @Override
    public final boolean onCommand(final CommandSender sender, final Command cmd, final String label, final String[] args) {
        if (args.length > 3 || args.length < 2) {
            sender.sendMessage("錯誤的參數數目!");
            return false;
        }

        Player target = (Bukkit.getServer().getPlayer(args[1]));
        if (target == null) {
            sender.sendMessage("目標玩家 "+args[1] + " 不在線上!");
            return false;
        }

        if (cmd.getName().equalsIgnoreCase("pvp")) { // If the player typed /basic then do the following...
            if (!(sender instanceof Player)) {
                sender.sendMessage("這個指令只能由玩家使用.");
            } else {
                Player player = (Player) sender;
                // do something
                if(args[0].equalsIgnoreCase("Invite")){//Invite
                    String chooseString = pvpInvite.chooseString.replaceAll("%player%",player.getDisplayName());
                    pvpInvite.send(target, pvpInvite.invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                    pvpInvite.sendChoose(target,chooseString.split(","),"[同意],[拒絕]".split(","));
                    pvpInvite.pvpPlayer = new PvPPlayer(player.getUniqueId());
                }else if(args[0].equalsIgnoreCase("Accept")){//Accept
                    pvpInvite.pvpPlayer.addOpponent(target.getUniqueId());
                    try {
                        sendStartPVP((Player)sender,target);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }else if(args[0].equalsIgnoreCase("Deny")){//Deny
                    sender.sendMessage("Deny Invite");
                    target.sendMessage("Deny Invite");
                    pvpInvite.pvpPlayer.removeOpponent(player.getUniqueId());
                }
            }
            return true;
        }
        return false;
    }

    private void sendStartPVP(final Player sender, final Player target) throws InterruptedException {

        for(int i = 3 ; i > 0 ; i--){
            String title = "決鬥將在" + i + "秒後開始！！！";
            sender.sendTitle(title,"",0,20,0);
            target.sendTitle(title,"",0,20,0);
            Thread.sleep(1000);
        }
        sender.sendTitle("","",0,20,0);
        target.sendTitle("","",0,20,0);
    }

    private void sendEndPVP(final Player sender, final Player target, final String name) {
        String title = "決鬥結束，此次決鬥由" + name + "獲勝";
        sender.sendTitle(title,"",0,60,0);
        target.sendTitle(title,"",0,60,0);
    }
}