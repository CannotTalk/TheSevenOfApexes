// net/ardcameg/thesevenofapexes/abilities/uncommon/SecretArtOfSewingAbility.java

package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

public final class SecretArtOfSewingAbility {
    private SecretArtOfSewingAbility() {}

    /**
     * "裁縫の秘技"の効果を適用する
     * @param event 右クリックイベント
     * @param sewingCount "裁縫の秘技"の所持数
     * @param prideMultiplier "傲慢"の倍率 (このアビリティでは固定ドロップのため未使用)
     */
    public static void apply(PlayerInteractEvent.RightClickBlock event, int sewingCount, int prideMultiplier) {
        if (sewingCount <= 0) return;

        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        // --- 条件判定 ---
        if (!event.getItemStack().is(Items.SHEARS)) return;
        if (!level.getBlockState(pos).is(BlockTags.WOOL)) return;

        // --- 処理実行 ---
        if (!level.isClientSide) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
            Block.popResource(level, pos, new ItemStack(Items.STRING, 4));

            event.getItemStack().hurtAndBreak(1, player, EquipmentSlot.MAINHAND);

            level.playSound(null, pos, SoundEvents.SHEEP_SHEAR, SoundSource.BLOCKS, 1.0f, 1.0f);
        }

        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}