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

    @Override
    public final boolean onCommand(CommandSender sender, Command cmd,String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("pvp")) { // If the player typed /basic then do the following...
            if (!(sender instanceof Player)) {
                sender.sendMessage("這個指令只能由玩家使用.");
            } else {
                Player player = (Player) sender;
                if(args[0].equalsIgnoreCase("Invite")){//Invite
                    Player target = (Bukkit.getServer().getPlayer(args[1]));
                    if (target == null) {
                        sender.sendMessage("目標玩家 "+args[1] + " 不在線上!");
                        return false;
                    }
                    String chooseString = pvpInvite.chooseString.replaceAll("%player%",player.getDisplayName());
                    pvpInvite.send(target, pvpInvite.invite.replaceAll("%player%", player.getDisplayName()).split("%NEWLINE%"));
                    pvpInvite.sendChoose(target,chooseString.split(","),"[同意],[拒絕]".split(","));
                    pvpPlayer = new PvPPlayer(player.getUniqueId());
                }else if(args[0].equalsIgnoreCase("Accept")){//Accept
                    sender.sendMessage("Accept Invite");
                }else if(args[0].equalsIgnoreCase("Deny")){//Deny
                    sender.sendMessage("Deny Invite");
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
        String title =
        sender.sendTitle(title,"",0,60,0);
        target.sendTitle(title,"",0,60,0);
    }
}