package net.hellomouse.kontrol.util;

import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class WorldUtil {
    public static void spawnItemStack(World world, BlockPos pos, ItemStack itemStack) {
        ItemEntity itemEntity = new ItemEntity(world, (double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5, itemStack);
        itemEntity.setToDefaultPickupDelay();
        world.spawnEntity(itemEntity);
    }
}
