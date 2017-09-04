package net.minecraftforge.MCPCRevive.entity;

import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.EntityType;

/**
 * Created by lyt on 2017/9/4.
 */
public class CraftCustomEntity extends CraftEntity{

    public Class<? extends Entity> entityClass;
    public String entityName;

    public CraftCustomEntity(CraftServer server, net.minecraft.entity.Entity entity) {
        super(server, entity);
        this.entityClass = entity.getClass();
        this.entityName = EntityRegistry.getEntry(entityClass).getName();
        if (entityName == null)
            entityName = entity.getCommandSenderEntity().getName();
    }

    @Override
    public net.minecraft.entity.Entity getHandle() {
        return (net.minecraft.entity.Entity) entity;
    }

    @Override
    public String toString() {
        return this.entityName;
    }

    public EntityType getType() {
        EntityType type = EntityType.fromName(this.entityName);
        if (type != null)
            return type;
        else return EntityType.UNKNOWN;
    }
}
