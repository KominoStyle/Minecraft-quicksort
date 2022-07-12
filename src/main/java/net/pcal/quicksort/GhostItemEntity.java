package net.pcal.quicksort;

import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class GhostItemEntity extends ItemEntity {

    public GhostItemEntity(World world, double d, double e, double f, ItemStack stack) {
        super(world, d, e, f, stack);
    }


    @Override
    protected void checkBlockCollision() {
    }


    public void onPlayerCollision(PlayerEntity player) {
        // never collide with a player
    }

    public boolean isInsideWall() {
        return false;
    }

    public boolean isCollidable() {
        return false;
    }

    public boolean doesNotCollide(double offsetX, double offsetY, double offsetZ) {
        return true;
    }

}


