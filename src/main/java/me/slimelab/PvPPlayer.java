package me.slimelab;

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
    }

    public void removeInvites(UUID uuid){
        this.invites.remove(uuid);
    }

    public void setUUID(UUID uuid){
        this.uuid = uuid;
    }

}
