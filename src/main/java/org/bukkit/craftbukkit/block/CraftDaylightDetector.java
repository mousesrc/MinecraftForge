package org.bukkit.craftbukkit.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDaylightDetector;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.DaylightDetector;
import org.bukkit.craftbukkit.CraftWorld;

public class CraftDaylightDetector extends CraftBlockState implements DaylightDetector {

    private final CraftWorld world;
    private final TileEntityDaylightDetector detector;

    public CraftDaylightDetector(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        detector = (TileEntityDaylightDetector) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public CraftDaylightDetector(final Material material, final TileEntityDaylightDetector te) {
        super(material);

        detector = te;
        world = null;
    }

    @Override
    public TileEntity getTileEntity() {
        return detector;
    }
}
