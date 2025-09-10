package net.ardcameg.thesevenofapexes.abilities.common;

import net.ardcameg.thesevenofapexes.Config;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;

import java.util.Random;

public final class OldAnglersDiaryAbility {
    private OldAnglersDiaryAbility() {}
    private static final Random RANDOM = new Random();

    /**
     * "古い釣り人の日誌"の効果を適用する
     */
    public static void apply(Player player, int diaryCount, int prideMultiplier) {
        if (diaryCount <= 0) return;

        boolean isHoldingRod = player.getMainHandItem().getItem() instanceof FishingRodItem ||
                player.getOffhandItem().getItem() instanceof FishingRodItem;

        if (isHoldingRod) {
            int finalCount = diaryCount * prideMultiplier;

            // 約10秒に1回
            if (RANDOM.nextInt(2000) < finalCount) {
                // 1. Configからメッセージの総数を取得
                int totalMessages = Config.oldAnglersDiaryMessageCount.get();
                if (totalMessages <= 0) return; //念のためチェック

                // 2. 1から総数までの間でランダムなIDを生成
                int messageId = RANDOM.nextInt(totalMessages) + 1;

                // 3. IDを使って動的にメッセージキーを組み立てる
                String messageKey = "message.seven_apexes.diary." + messageId;

                // 4. 組み立てたキーでメッセージを送信
                player.sendSystemMessage(Component.translatable(messageKey));
            }
        }
    }
}