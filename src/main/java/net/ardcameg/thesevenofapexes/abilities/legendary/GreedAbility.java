package net.ardcameg.thesevenofapexes.abilities.legendary;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

import java.util.ArrayList;
import java.util.List;

public final class GreedAbility {
    private GreedAbility() {}

    /**
     * Mobのドロップを指定された倍率にする
     * @param event Mobがドロップする瞬間のイベント
     * @param greedCount "強欲"の素の所持数
     * @param prideMultiplier "傲慢"による効果倍率(強欲は"傲慢"によってコピーされない)
     */
    public static void applyToMob(LivingDropsEvent event, int greedCount, int prideMultiplier) {
        if (greedCount <= 0) return;
        //int finalCount = greedCount * prideMultiplier;
        int actualMultiplier = 1 + greedCount; // ドロップ倍率を計算 (1個なら2倍, 2個なら3倍...)

        List<ItemEntity> originalDrops = new ArrayList<>(event.getDrops());
        event.getDrops().clear();

        for (ItemEntity dropEntity : originalDrops) {
            ItemStack stack = dropEntity.getItem();
            if (stack.isStackable()) {
                stack.setCount(stack.getCount() * actualMultiplier);
                event.getDrops().add(dropEntity);
            } else {
                for (int i = 0; i < actualMultiplier; i++) {
                    ItemEntity newDrop = new ItemEntity(dropEntity.level(), dropEntity.getX(), dropEntity.getY(), dropEntity.getZ(), stack.copy());
                    event.getDrops().add(newDrop);
                }
            }
        }
    }

    /**
     * ブロックのドロップを指定された倍率にする
     * @param event ブロックが破壊された瞬間のイベント
     * @param finalCount "強欲"の素の所持数
     * @param multiplier "強欲"には使われない
     */
    public static void applyToBlock(BlockEvent.BreakEvent event, int finalCount, int multiplier) {
        if (finalCount <= 0) return;
        int actualMultiplier = 1 + finalCount; // ドロップ倍率を計算

        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockState state = event.getState();
        var pos = event.getPos();
        var player = event.getPlayer();

        event.setCanceled(true);

        List<ItemStack> originalDrops = Block.getDrops(state, level, pos, null, player, player.getMainHandItem());

        for (ItemStack stack : originalDrops) {
            if (stack.isStackable()) {
                stack.setCount(stack.getCount() * actualMultiplier);
                Block.popResource(level, pos, stack);
            } else {
                for (int i = 0; i < actualMultiplier; i++) {
                    Block.popResource(level, pos, stack.copy());
                }
            }
        }
        level.setBlock(pos, state.getFluidState().createLegacyBlock(), 3);
    }
}