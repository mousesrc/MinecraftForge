package org.bukkit.craftbukkit.block;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityEnchantmentTable;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.block.EnchantingTable;

public class CraftEnchantingTable extends CraftBlockState implements EnchantingTable {

    private final CraftWorld world;
    private final TileEntityEnchantmentTable enchant;

    public CraftEnchantingTable(final Block block) {
        super(block);

        world = (CraftWorld) block.getWorld();
        enchant = (TileEntityEnchantmentTable) world.getTileEntityAt(getX(), getY(), getZ());
    }

    public CraftEnchantingTable(final Material material, final TileEntityEnchantmentTable te) {
        super(material);

        enchant = te;
        world = null;
    }

    @Override
    public TileEntity getTileEntity() {
        return enchant;
    }

    @Override
    public String getCustomName() {
        return enchant.hasCustomName() ? enchant.getName() : null;
    }

    @Override
    public void setCustomName(String name) {
        enchant.setCustomName(name); // PAIL: setCustomName
    }
}
