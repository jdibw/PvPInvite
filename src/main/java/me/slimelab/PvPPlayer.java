package me.slimelab;

import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.UUID;

public class PvPPlayer {

    UUID uuid;
    public boolean pvping = false;
    public ArrayList<UUID> opponents = new ArrayList<>();

    public PvPPlayer(UUID uuid){
        this.uuid = uuid;
        PvPInvite.pvpers.add(uuid);
    }

    public void addOpponent(UUID uuid){
        this.opponents.add(uuid);
    }

    public void removeOpponent(UUID uuid){
        this.opponents.remove(uuid);
    }

    public void setUUID(UUID uuid){
        this.uuid = uuid;
    }

    public void setPVPing(boolean pvping){
        this.pvping = pvping;
    }

}
