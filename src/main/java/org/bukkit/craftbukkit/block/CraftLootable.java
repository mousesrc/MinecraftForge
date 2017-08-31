package org.bukkit.craftbukkit.block;

import net.minecraft.tileentity.TileEntityLockableLoot;
import org.bukkit.Material;
import org.bukkit.Nameable;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;

public class CraftLootable extends CraftContainer implements Nameable {

    private final TileEntityLockableLoot te;

    public CraftLootable(Block block) {
        super(block);

        te = (TileEntityLockableLoot) ((CraftWorld) block.getWorld()).getTileEntityAt(getX(), getY(), getZ());
    }

    public CraftLootable(Material material, TileEntityLockableLoot tileEntity) {
        super(material, tileEntity);

        te = tileEntity;
    }

    @Override
    public String getCustomName() {
        return te.hasCustomName() ? te.getName() : null;
    }

    @Override
    public void setCustomName(String name) {
        te.setCustomName(name); // PAIL: setCustomName
    }
}
