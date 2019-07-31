package me.slimelab;

import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.UUID;

public class PvPPlayer {

    UUID uuid;
    public ArrayList<UUID> opponents = new ArrayList<>();
    public ArrayList<UUID> invites = new ArrayList<>();

    public PvPPlayer(UUID uuid){
        this.uuid = uuid;
    }

    public void addOpponent(UUID uuid){
        this.opponents.add(uuid);
    }

    public void removeOpponent(UUID uuid){
        this.opponents.remove(uuid);
    }

    public void addInvites(UUID uuid){
        this.invites.add(uuid);
        Bukkit.getScheduler().runTaskLater(PvPInvite.pvpInvite, new Runnable() {
            @Override
            public void run() {
                removeInvites(uuid);
            }
        }, 10*20L);
    }

    public void removeInvites(UUID uuid){
        this.invites.remove(uuid);
    }
    public void setUUID(UUID uuid){
        this.uuid = uuid;
    }

}
