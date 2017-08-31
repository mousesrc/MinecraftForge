package org.bukkit.craftbukkit.block;

import java.util.ArrayList;
import java.util.Collection;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.tileentity.TileEntityBeacon;
import org.bukkit.Material;
import org.bukkit.block.Beacon;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventoryBeacon;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CraftBeacon extends CraftContainer implements Beacon {
    private final CraftWorld world;
    private final TileEntityBeacon beacon;

    public CraftBeacon(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        beacon = (TileEntityBeacon) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public CraftBeacon(final Material material, final TileEntityBeacon te) {
        super(material, te);
        world = null;
        beacon = te;
    }

    public Inventory getInventory() {
        return new CraftInventoryBeacon(beacon);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result) {
            beacon.markDirty();
        }

        return result;
    }

    @Override
    public TileEntityBeacon getTileEntity() {
        return beacon;
    }

    @Override
    public Collection<LivingEntity> getEntitiesInRange() {
        Collection<EntityPlayer> nms = beacon.getHumansInRange();
        Collection<LivingEntity> bukkit = new ArrayList<LivingEntity>(nms.size());

        for (EntityPlayer human : nms) {
            bukkit.add(human.getBukkitEntity());
        }

        return bukkit;
    }

    @Override
    public int getTier() {
        return beacon.levels;
    }

    @Override
    public PotionEffect getPrimaryEffect() {
        return beacon.getPrimaryEffect();
    }

    @Override
    public void setPrimaryEffect(PotionEffectType effect) {
        beacon.primaryEffect = (effect != null) ? Potion.getPotionById(effect.getId()) : null;
    }

    @Override
    public PotionEffect getSecondaryEffect() {
        return beacon.getSecondaryEffect();
    }

    @Override
    public void setSecondaryEffect(PotionEffectType effect) {
        beacon.secondaryEffect = (effect != null) ? Potion.getPotionById(effect.getId()) : null;
    }

    @Override
    public String getCustomName() {
        return beacon.hasCustomName() ? beacon.getName() : null;
    }

    @Override
    public void setCustomName(String name) {
        beacon.setName(name); // PAIL: setCustomName
    }
}
