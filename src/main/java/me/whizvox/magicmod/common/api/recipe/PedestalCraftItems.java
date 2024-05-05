package me.whizvox.magicmod.common.api.recipe;

import net.minecraft.world.item.ItemStack;

import java.util.List;

public record PedestalCraftItems(ItemStack center, List<ItemStack> outer) {

}
