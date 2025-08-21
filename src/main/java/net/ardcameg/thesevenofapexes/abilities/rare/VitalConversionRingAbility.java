package net.ardcameg.thesevenofapexes.abilities.rare;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;

public final class VitalConversionRingAbility {
    private VitalConversionRingAbility() {}

    /**
     * 任務1：満腹度が満タンでなくても、体力を回復させる (変更なし)
     */
    public static void applyHealthRecovery(Player player, int ringCount, int prideMultiplier) {
        if (player.level().getGameTime() % 20 != 0) return;
        if (player.getHealth() >= player.getMaxHealth()) return;

        var foodData = player.getFoodData();
        if (foodData.getFoodLevel() > 0) {
            int finalCount = ringCount * prideMultiplier;
            foodData.eat(-1 * finalCount, 0.0F);
            player.heal(0.5f * finalCount);
        }
    }

    /**
     * 任務2：インベントリ内の肉以外の食料を強制的に排出する
     */
    public static void rejectNonMeatFood(Player player) {
        // プレイヤーのメインインベントリ（ホットバー含む）を全てチェック
        for (int i = 0; i < player.getInventory().items.size(); i++) {
            ItemStack stack = player.getInventory().getItem(i);

            // スロットが空か、食料でなければスキップ
            if (stack.isEmpty() || stack.getFoodProperties(player) == null) continue;

            // もし、その食料が「肉」で"ない"なら...
            if (!stack.is(ItemTags.MEAT)) {
                // インベントリからそのアイテムを完全に削除
                player.getInventory().setItem(i, ItemStack.EMPTY);
                // 削除したアイテムを、プレイヤーの足元にドロップ
                player.drop(stack, false);
                // 排出した音を鳴らす
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_BURP, SoundSource.PLAYERS, 0.5f, 1.5f);
            }
        }
    }
}