package net.minecraftforge.MCPCRevive.inventory;

import com.google.common.collect.Maps;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.common.util.EnumHelper;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.craftbukkit.block.CraftBlockState;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashSet;
import java.util.Map;

/**
 * Created by lyt on 2017/9/4.
 */
public class CraftCustomInventoryContainer extends CraftBlockState implements InventoryHolder {
    private final CraftWorld world;
    private final net.minecraft.inventory.IInventory container;
    static final HashSet<Class> registered = new HashSet<Class>();
    public CraftCustomInventoryContainer(org.bukkit.block.Block block) {
        super(block);
        world = (CraftWorld) block.getWorld();
        container = (IInventory)world.getTileEntityAt(getX(), getY(), getZ());
        boolean hasregistered = registered.contains(container.getClass());
        if(!hasregistered)
        {
            EnumHelper.addBukkitInventory(container);
            registered.add(container.getClass());
        }
    }

    @Override
    public Inventory getInventory() {
        CraftInventory inventory = new CraftInventory(container);
        return inventory;
    }
}
