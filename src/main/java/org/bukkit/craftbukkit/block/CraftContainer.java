package org.bukkit.craftbukkit.block;

import net.minecraft.world.LockCode;
import net.minecraft.world.ILockableContainer;
import net.minecraft.tileentity.TileEntityLockable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Lockable;
import org.bukkit.craftbukkit.CraftWorld;

public class CraftContainer extends CraftBlockState implements Lockable {

    private final ILockableContainer container;

    public CraftContainer(Block block) {
        super(block);

        container = (TileEntityLockable) ((CraftWorld) block.getWorld()).getTileEntityAt(block.getX(), block.getY(), block.getZ());
    }

    public CraftContainer(final Material material, ILockableContainer tileEntity) {
        super(material);

        container = tileEntity;
    }

    @Override
    public boolean isLocked() {
        return container.isLocked();
    }

    @Override
    public String getLock() {
        return container.getLockCode().getLock(); // PAIL: getKey
    }

    @Override
    public void setLock(String key) {
        container.setLockCode(key == null ? LockCode.EMPTY_CODE : new LockCode(key)); // PAIL: setLock
    }
}
