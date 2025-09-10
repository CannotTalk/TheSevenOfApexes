package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.player.CanPlayerSleepEvent; // 【修正】正しいイベントをインポート

public final class EndlessInsomniaAbility {
    private EndlessInsomniaAbility() {}

    /**
     * プレイヤーの睡眠を妨げる。
     * リスポーン地点は設定されるが、時間のスキップは行われない。
     * @param event 睡眠可否判定イベント
     * @param player 対象のプレイヤー
     */
    public static void preventSleep(CanPlayerSleepEvent event, Player player) {
        // これにより、時間はスキップされないが、リスポーン地点は正しく設定される。
        event.setProblem(Player.BedSleepingProblem.OTHER_PROBLEM);

        // プレイヤーに、眠れない理由を伝えるフレーバーテキストを送信する
        player.sendSystemMessage(Component.translatable("message.seven_apexes.endless_insomnia_cant_sleep"));
    }
}