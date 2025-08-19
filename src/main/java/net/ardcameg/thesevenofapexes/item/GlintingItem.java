package net.ardcameg.thesevenofapexes.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class GlintingItem extends Item {

    public GlintingItem(Properties properties) {
        super(properties);
    }

    /**
     * このアイテムがエンチャントされているかのように光るかどうかを返す。
     * @return 常にtrueを返すことで、常にキラキラ光るように
     */
    @Override
    public boolean isFoil(ItemStack pStack) {
        return true;
    }
}