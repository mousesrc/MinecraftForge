package org.bukkit.craftbukkit.block;

import net.minecraft.tileentity.TileEntityBrewingStand;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BrewingStand;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.inventory.CraftInventoryBrewer;
import org.bukkit.inventory.BrewerInventory;

public class CraftBrewingStand extends CraftContainer implements BrewingStand {
    private final TileEntityBrewingStand brewingStand;

    public CraftBrewingStand(Block block) {
        super(block);

        brewingStand = (TileEntityBrewingStand) ((CraftWorld) block.getWorld()).getTileEntityAt(getX(), getY(), getZ());
    }

    public CraftBrewingStand(final Material material, final TileEntityBrewingStand te) {
        super(material, te);
        brewingStand = te;
    }

    public BrewerInventory getInventory() {
        return new CraftInventoryBrewer(brewingStand);
    }

    @Override
    public boolean update(boolean force, boolean applyPhysics) {
        boolean result = super.update(force, applyPhysics);

        if (result) {
            brewingStand.markDirty();
        }

        return result;
    }

    public int getBrewingTime() {
        return brewingStand.getField(0);
    }

    public void setBrewingTime(int brewTime) {
        brewingStand.setField(0, brewTime);
    }

    @Override
    public TileEntityBrewingStand getTileEntity() {
        return brewingStand;
    }

    @Override
    public int getFuelLevel() {
        return brewingStand.getField(1);
    }

    @Override
    public void setFuelLevel(int level) {
        brewingStand.setField(1, level);
    }

    @Override
    public String getCustomName() {
        return brewingStand.hasCustomName() ? brewingStand.getName() : null;
    }

    @Override
    public void setCustomName(String name) {
        brewingStand.setName(name); // PAIL: setCustomName
    }
}
