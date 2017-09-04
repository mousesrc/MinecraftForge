package org.bukkit.enchantments;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.function.Function;

/**
 * Represents the applicable target for a {@link Enchantment}
 */
public enum EnchantmentTarget {
    /**
     * Allows the Enchantment to be placed on all items
     */
    ALL(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return true;
        }
    }),

    /**
     * Allows the Enchantment to be placed on armor
     */
    ARMOR(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return ARMOR_FEET.delegate.apply(material)
                    || ARMOR_LEGS.delegate.apply(material)
                    || ARMOR_HEAD.delegate.apply(material)
                    || ARMOR_TORSO.delegate.apply(material);
        }
    }),

    /**
     * Allows the Enchantment to be placed on feet slot armor
     */
    ARMOR_FEET(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return material.equals(Material.LEATHER_BOOTS)
                    || material.equals(Material.CHAINMAIL_BOOTS)
                    || material.equals(Material.IRON_BOOTS)
                    || material.equals(Material.DIAMOND_BOOTS)
                    || material.equals(Material.GOLD_BOOTS);
        }
    }),

    /**
     * Allows the Enchantment to be placed on leg slot armor
     */
    ARMOR_LEGS(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return material.equals(Material.LEATHER_LEGGINGS)
                    || material.equals(Material.CHAINMAIL_LEGGINGS)
                    || material.equals(Material.IRON_LEGGINGS)
                    || material.equals(Material.DIAMOND_LEGGINGS)
                    || material.equals(Material.GOLD_LEGGINGS);
        }
    }),

    /**
     * Allows the Enchantment to be placed on torso slot armor
     */
    ARMOR_TORSO(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return material.equals(Material.LEATHER_CHESTPLATE)
                    || material.equals(Material.CHAINMAIL_CHESTPLATE)
                    || material.equals(Material.IRON_CHESTPLATE)
                    || material.equals(Material.DIAMOND_CHESTPLATE)
                    || material.equals(Material.GOLD_CHESTPLATE);
        }
    }),
    /**
     * Allows the Enchantment to be placed on head slot armor
     */
    ARMOR_HEAD(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return material.equals(Material.LEATHER_HELMET)
                    || material.equals(Material.CHAINMAIL_HELMET)
                    || material.equals(Material.DIAMOND_HELMET)
                    || material.equals(Material.IRON_HELMET)
                    || material.equals(Material.GOLD_HELMET);
        }
    }),

    /**
     * Allows the Enchantment to be placed on weapons (swords)
     */
    WEAPON(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return material.equals(Material.WOOD_SWORD)
                    || material.equals(Material.STONE_SWORD)
                    || material.equals(Material.IRON_SWORD)
                    || material.equals(Material.DIAMOND_SWORD)
                    || material.equals(Material.GOLD_SWORD);
        }
    }),

    /**
     * Allows the Enchantment to be placed on tools (spades, pickaxe, hoes,
     * axes)
     */
    TOOL(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return material.equals(Material.WOOD_SPADE)
                    || material.equals(Material.STONE_SPADE)
                    || material.equals(Material.IRON_SPADE)
                    || material.equals(Material.DIAMOND_SPADE)
                    || material.equals(Material.GOLD_SPADE)
                    || material.equals(Material.WOOD_PICKAXE)
                    || material.equals(Material.STONE_PICKAXE)
                    || material.equals(Material.IRON_PICKAXE)
                    || material.equals(Material.DIAMOND_PICKAXE)
                    || material.equals(Material.GOLD_PICKAXE)
                    || material.equals(Material.WOOD_HOE)         // NOTE: No vanilla enchantments for this
                    || material.equals(Material.STONE_HOE)        // NOTE: No vanilla enchantments for this
                    || material.equals(Material.IRON_HOE)         // NOTE: No vanilla enchantments for this
                    || material.equals(Material.DIAMOND_HOE)      // NOTE: No vanilla enchantments for this
                    || material.equals(Material.GOLD_HOE)         // NOTE: No vanilla enchantments for this
                    || material.equals(Material.WOOD_AXE)
                    || material.equals(Material.STONE_AXE)
                    || material.equals(Material.IRON_AXE)
                    || material.equals(Material.DIAMOND_AXE)
                    || material.equals(Material.GOLD_AXE)
                    || material.equals(Material.SHEARS)           // NOTE: No vanilla enchantments for this
                    || material.equals(Material.FLINT_AND_STEEL); // NOTE: No vanilla enchantments for this
        }
    }),

    /**
     * Allows the Enchantment to be placed on bows.
     */
    BOW(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return material.equals(Material.BOW);
        }
    }),
    /**
     * Allows the Enchantment to be placed on fishing rods.
     */
    FISHING_ROD(new Function<Material,Boolean>(){
        @Override
        public Boolean apply(Material item) {
            return item.equals(Material.FISHING_ROD);
        }
    }),

    /**
     * Allows the enchantment to be placed on items with durability.
     */
    BREAKABLE(new Function<Material, Boolean>() {
        @Override
        public Boolean apply(Material material) {
            return material.getMaxDurability() > 0 && material.getMaxStackSize() == 1;
        }
    });

    /**
     * Check whether this target includes the specified item.
     *
     * @param material The item to check
     * @return True if the target includes the item
     */
    private Function<Material,Boolean> delegate;
    private EnchantmentTarget(Function<Material,Boolean> delegate)
    {
        this.delegate = delegate;
    }
    /**
     * Check whether this target includes the specified item.
     *
     * @param item The item to check
     * @return True if the target includes the item
     */
    public boolean includes(ItemStack item) {
        return delegate.apply(item.getType());
    }
}
