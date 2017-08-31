package org.bukkit.craftbukkit.entity;

import net.minecraft.entity.Entity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Explosive;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.inventory.ItemStack;

/**
 * Created by lyt on 2017/8/30.
 */
public class CraftCustomExplosive extends CraftEntity implements Explosive {
    private Class explosivetype;
    float power;
    public CraftCustomExplosive(CraftServer server, Entity entity, Class<? extends Entity> type, float power)
    {
        this(server,entity);
        explosivetype = type;
        this.power = power;
    }
    private CraftCustomExplosive(CraftServer server, Entity entity) {
        super(server, entity);
    }

    @Override
    public void setYield(float yield) {
        throw new UnsupportedOperationException("You cannot do this!");
    }

    @Override
    public float getYield() {
        return power;
    }

    @Override
    public void setIsIncendiary(boolean isIncendiary) {
        throw new UnsupportedOperationException("You cannot do this!");
    }

    @Override
    public boolean isIncendiary() {
        return false;
    }

    @Override
    public EntityType getType() {
        return EntityType.UNKNOWN;
    }
}
