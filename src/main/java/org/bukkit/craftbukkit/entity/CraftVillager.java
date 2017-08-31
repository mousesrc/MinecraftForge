package org.bukkit.craftbukkit.entity;

import java.util.List;
import net.minecraft.entity.passive.EntityVillager;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Villager;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.MerchantRecipe;

import org.bukkit.inventory.PlayerInventory;

public class CraftVillager extends CraftAgeable implements Villager, InventoryHolder {

    private CraftMerchant merchant;

    public CraftVillager(CraftServer server, EntityVillager entity) {
        super(server, entity);
    }

    @Override
    public EntityVillager getHandle() {
        return (EntityVillager) entity;
    }

    @Override
    public String toString() {
        return "CraftVillager";
    }

    public EntityType getType() {
        return EntityType.VILLAGER;
    }

    public Profession getProfession() {
        return Profession.values()[getHandle().getProfession() + 1]; // Offset by 1 from the zombie types
    }

    public void setProfession(Profession profession) {
        Validate.notNull(profession);
        Validate.isTrue(!profession.isZombie(), "Profession is reserved for Zombies: ", profession);
        getHandle().setProfession(profession.ordinal() - 1);
    }

    @Override
    public PlayerInventory getInventory() {
        return (PlayerInventory) new CraftInventory(getHandle().villagerInventory);
    }

    private CraftMerchant getMerchant() {
        return (merchant == null) ? merchant = new CraftMerchant(getHandle()) : merchant;
    }

    @Override
    public List<MerchantRecipe> getRecipes() {
        return getMerchant().getRecipes();
    }

    @Override
    public void setRecipes(List<MerchantRecipe> recipes) {
        this.getMerchant().setRecipes(recipes);
    }

    @Override
    public MerchantRecipe getRecipe(int i) {
        return getMerchant().getRecipe(i);
    }

    @Override
    public void setRecipe(int i, MerchantRecipe merchantRecipe) {
        getMerchant().setRecipe(i, merchantRecipe);
    }

    @Override
    public int getRecipeCount() {
        return getMerchant().getRecipeCount();
    }

    @Override
    public boolean isTrading() {
        return getTrader() != null;
    }

    @Override
    public HumanEntity getTrader() {
        return getMerchant().getTrader();
    }

    @Override
    public int getRiches() {
        return getHandle().wealth;
    }

    @Override
    public void setRiches(int riches) {
        getHandle().wealth = riches;
    }
}
