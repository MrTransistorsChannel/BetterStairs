package ru.tulavcube.BetterStairs;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityDismountEvent;

public class BetterStairs extends JavaPlugin implements Listener {
    @Override
    public void onEnable() {
        super.onEnable();
        getServer().getLogger().info("BetterStairs plugin started");
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        super.onDisable();
        getServer().getLogger().info("BetterStairs plugin stopped");
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent e) {
        if (e.getAction() == Action.RIGHT_CLICK_BLOCK) { // checking if a player clicked the top face of a stair
            if (e.getHand() == EquipmentSlot.HAND && !e.hasItem()) { // with empty hand
                Block block = e.getClickedBlock();
                BlockData clicked = block.getBlockData();
                if (clicked instanceof Stairs) {
                    if (((Bisected) clicked).getHalf() == Bisected.Half.BOTTOM // checking if the stair is valid for sitting
                            && e.getBlockFace() == BlockFace.UP) {             // (bottom position)
                        Player player = e.getPlayer();
                        Location loc = block.getLocation();
                        ArmorStand chair = (ArmorStand) loc.getWorld() // "Chair" armorstand, using as a vehicle
                                .spawnEntity(adjustForStair(loc, (Stairs) clicked), EntityType.ARMOR_STAND);
                        chair.setVisible(false); // invisible
                        chair.setGravity(false); // not affected by gravity
                        chair.setInvulnerable(true); // can`t be killed without commands
                        chair.setMarker(true); // making hitbox small (it`s literally a dot)
                        chair.setSmall(true); // small model
                        chair.addScoreboardTag(player.getName()); // tagging with sitter`s name
                        chair.addPassenger(player); // making player sit
                    }
                }
            }
        }
    }

    private Location adjustForStair(Location loc, Stairs stair){ // this method adjusts the position of armor stand
                                                                 // according to stair direction
        loc.setX(loc.getX()+0.5); // moving to the center and adjusting vertical elevation so player looks like actually
        loc.setY(loc.getY()+0.3); // sitting
        loc.setZ(loc.getZ()+0.5);
        switch (stair.getFacing()) { // adjusting position and yaw so player will sit on the lower part of the stair and
            case EAST -> {           // his feet will be facing in right direction
                loc.setX(loc.getX() - 0.3);
                loc.setYaw(90);
            }
            case WEST -> {
                loc.setX(loc.getX() + 0.3);
                loc.setYaw(-90);
            }
            case SOUTH -> {
                loc.setZ(loc.getZ() - 0.3);
                loc.setYaw(180);
            }
            case NORTH -> {
                loc.setZ(loc.getZ() + 0.3);
                loc.setYaw(0);
            }
        }
        return loc;
    }

    @EventHandler
    public void onDismount(EntityDismountEvent e) {
        Entity vehicle = e.getDismounted();
        if (vehicle instanceof ArmorStand) { // if something tried to dismount an armorstand
            Entity passenger = e.getEntity();
            if (vehicle.getScoreboardTags().contains(passenger.getName())) { // and it has a tag with name of the passenger
                Location loc = passenger.getLocation();
                loc.setY(loc.getY()+0.8); // teleport player up a bit to prevent falling through stair and block underneath
                passenger.teleport(loc);
                vehicle.remove(); // deleting armorstand
            }
        }
    }
}
