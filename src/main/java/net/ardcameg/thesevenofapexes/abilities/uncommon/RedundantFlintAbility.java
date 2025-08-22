// net/ardcameg/thesevenofapexes/abilities/uncommon/RedundantFlintAbility.java

package net.ardcameg.thesevenofapexes.abilities.uncommon;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;

public final class RedundantFlintAbility {
    private RedundantFlintAbility() {}

    /**
     * "過剰な火打石"の効果を適用する。
     * @param event ブロック破壊イベント
     * @param flintCount "過剰な火石"の所持数
     * @param prideMultiplier "傲慢"による効果倍率
     */
    public static void apply(BreakEvent event, int flintCount, int prideMultiplier) {
        if (flintCount <= 0 || event.getPlayer() == null) return;

        // 破壊されたブロックが砂利かどうかをチェック
        if (event.getState().is(Blocks.GRAVEL)) {
            // プレイヤーのワールドから、エンチャントの登録情報を取得する
            var enchantmentLookup = event.getPlayer().level().holderLookup(Registries.ENCHANTMENT);

            // 登録情報を使って、エンチャントのキーからHolderを取得する
            Holder<Enchantment> silkTouchHolder = enchantmentLookup.getOrThrow(Enchantments.SILK_TOUCH);
            Holder<Enchantment> fortuneHolder = enchantmentLookup.getOrThrow(Enchantments.FORTUNE);

            // プレイヤーの持っているアイテムから、Holderを使ってエンチャントレベルを取得
            ItemStack mainHandItem = event.getPlayer().getMainHandItem();

            // シルクタッチが付いている場合は、能力を無効化する
            if (EnchantmentHelper.getItemEnchantmentLevel(silkTouchHolder, mainHandItem) > 0) {
                return;
            }

            event.setCanceled(true);
            event.getLevel().setBlock(event.getPos(), Blocks.AIR.defaultBlockState(), 3);

            int fortuneLevel = EnchantmentHelper.getItemEnchantmentLevel(fortuneHolder, mainHandItem);
            float originalFlintChance = getVanillaFlintChance(fortuneLevel);

            float newFlintChance = 1.0f - originalFlintChance;

            int finalCount = flintCount * prideMultiplier;
            float finalSuccessChance = 1.0f - (float)Math.pow(1.0f - newFlintChance, finalCount);

            ItemStack dropStack;
            if (event.getLevel().getRandom().nextFloat() < finalSuccessChance) {
                dropStack = new ItemStack(Items.FLINT);
            } else {
                dropStack = new ItemStack(Blocks.GRAVEL);
            }
            net.minecraft.world.level.block.Block.popResource(
                    (net.minecraft.world.level.Level) event.getLevel(),
                    event.getPos(),
                    dropStack
            );
        }
    }

    /**
     * バニラの幸運レベルに応じた火打石のドロップ率を返すヘルパーメソッド
     */
    private static float getVanillaFlintChance(int fortuneLevel) {
        if (fortuneLevel == 1) return 0.14f;
        if (fortuneLevel == 2) return 0.25f;
        if (fortuneLevel >= 3) return 1.0f;
        return 0.1f; // 素手または幸運なし
    }
}