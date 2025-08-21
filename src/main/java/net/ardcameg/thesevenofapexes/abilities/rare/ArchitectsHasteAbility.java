package net.ardcameg.thesevenofapexes.abilities.rare;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.Random;

public final class ArchitectsHasteAbility {
    private ArchitectsHasteAbility() {}
    private static final Random RANDOM = new Random();

    private static final ResourceLocation BLOCK_BREAK_SPEED_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "architects_haste_break_speed");
    private static final ResourceLocation BLOCK_INTERACTION_RANGE_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "architects_haste_interaction_range");

    /**
     * 任務1：継続的なバフを適用する
     */
    public static void updatePassiveBuffs(Player player, int hasteCount, int prideMultiplier) {
        AttributeInstance breakSpeed = player.getAttribute(Attributes.BLOCK_BREAK_SPEED);
        AttributeInstance interactionRange = player.getAttribute(Attributes.BLOCK_INTERACTION_RANGE);
        if (breakSpeed == null || interactionRange == null) return;

        // まず既存の効果をクリア
        breakSpeed.removeModifier(BLOCK_BREAK_SPEED_ID);
        interactionRange.removeModifier(BLOCK_INTERACTION_RANGE_ID);

        if (hasteCount > 0) {
            int finalCount = hasteCount * prideMultiplier;
            // ブロック破壊速度: +25%
            addModifier(breakSpeed, BLOCK_BREAK_SPEED_ID, finalCount * 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            addModifier(interactionRange, BLOCK_INTERACTION_RANGE_ID, finalCount * 0.25, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        }
    }

    /**
     * 任務2：ツールの耐久値を余分に消費させる
     */
    public static void applyDurabilityPenalty(Player player, int hasteCount, int prideMultiplier) {
        if (hasteCount <= 0 || player.level().isClientSide) return;

        ItemStack tool = player.getMainHandItem();
        // ツールが耐久値を持つアイテムかチェック
        if (tool.isDamageableItem()) {
            int finalCount = hasteCount * prideMultiplier;
            // 25%の確率で、追加で1ダメージを与える
            float penaltyChance = 0.25f * finalCount;

            // 確率を整数回と小数部に分ける
            int guaranteedDamage = (int) penaltyChance;
            float fractionalChance = penaltyChance - guaranteedDamage;

            // 確定分の追加ダメージ
            if (guaranteedDamage > 0) {
                tool.hurtAndBreak(guaranteedDamage, (ServerLevel) player.level(), player, (item) -> {});
            }
            // 確率分の追加ダメージ
            if (RANDOM.nextFloat() < fractionalChance) {
                tool.hurtAndBreak(1, (ServerLevel) player.level(), player, (item) -> {});
            }
        }
    }

    private static void addModifier(AttributeInstance attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        attribute.addPermanentModifier(new AttributeModifier(id, amount, operation));
    }
}