package net.ardcameg.thesevenofapexes.util;

import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.PlayTotemAnimationS2CPacket;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// このクラスは便利な道具箱であることを示す final
public final class BuffItemUtils {

    // このクラスがインスタンス化されるのを防ぐためのプライベートコンストラクタ
    private BuffItemUtils() {}

    /**
     * バフ列にあるすべてのアイテムとその数を数え上げ、マップとして返す
     * どこからでも呼び出せるように public static に変更
     * @param player 調べるプレイヤー
     * @return <アイテムの種類, その数> のマップ
     */
    public static Map<Item, Integer> countAllItemsInBuffRow(Player player) {
        Map<Item, Integer> itemCounts = new HashMap<>();
        for (int i = 9; i <= 17; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty()) {
                itemCounts.put(stack.getItem(), itemCounts.getOrDefault(stack.getItem(), 0) + 1);
            }
        }
        return itemCounts;
    }

    /**
     * 指定したバフアイテムがいくつ有効化されているのかを返す
     * どこからでも呼び出せるように public static に変更
     * @param player 調べるプレイヤー
     * @param itemToCheck 調べるアイテム
     * @return 有効化されているバフアイテムの数
     */
    public static int countItemInBuffRow(Player player, Item itemToCheck) {
        int amount = 0;
        for (int i = 9; i <= 17; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == itemToCheck) {
                amount++;
            }
        }
        return amount;
    }

    /**
     * バフ列にある指定されたアイテムを1つ消費する
     * @param player 対象のプレイヤー
     * @param itemToConsume 消費させたいアイテム
     * @return 消費に成功すればtrue、アイテムが見つからなければfalse
     */
    public static boolean consumeItemFromBuffRow(Player player, Item itemToConsume) {
        // インベントリの下一列 (スロット番号 9 から 17) を探す
        for (int i = 9; i <= 17; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() == itemToConsume) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    /**
     * 指定されたプレイヤーのクライアントで、トーテムアニメーションを再生するよう命令する
     * @param player アニメーションを表示させたいプレイヤー (サーバーサイドである必要がある)
     * @param itemToAnimate アニメーションさせたいアイテム
     */
    public static void playTotemAnimation(Player player, Item itemToAnimate) {
        if (player instanceof ServerPlayer serverPlayer) {
            ModMessages.sendToPlayer(new PlayTotemAnimationS2CPacket(itemToAnimate), serverPlayer);
        }
    }

    /**
     * プレイヤーに付与されている全てのデバフ（悪い効果）を解除する
     * @param player 対象のプレイヤー
     */
    public static void clearAllDebuffs(Player player) {
        // 現在かかっているエフェクトのリストをコピーする
        ArrayList<MobEffectInstance> effects = new ArrayList<>(player.getActiveEffects());

        for (MobEffectInstance effectInstance : effects) {
            // isBeneficial() が false のエフェクト（＝デバフ）を全て探し出す
            if (!effectInstance.getEffect().value().isBeneficial()) {
                // そのデバフを解除する
                player.removeEffect(effectInstance.getEffect());
            }
        }
    }

    /**
     * 二つのエンティティ間に、指定されたパーティクルの直線を描画する
     * @param level ワールド
     * @param from 開始エンティティ
     * @param to 終了エンティティ
     * @param particleOptions 使用するパーティクルの種類
     */
    public static void drawParticleLine(ServerLevel level, Entity from, Entity to, DustParticleOptions particleOptions) {
        Vec3 startPos = from.getEyePosition();
        Vec3 endPos = to.getEyePosition();
        Vec3 vector = endPos.subtract(startPos);
        double distance = vector.length();

        // 0.2ブロックごとにパーティクルを1つ生成する
        for (double i = 0; i < distance; i += 0.2) {
            Vec3 point = startPos.add(vector.normalize().scale(i));
            level.sendParticles(
                    particleOptions,
                    point.x,
                    point.y,
                    point.z,
                    1, // パーティクルの数
                    0, 0, 0, // 散開範囲 (0でピンポイント)
                    0  // 速度
            );
        }
    }

    /**
     * バフ列にある指定されたアイテムのItemStackリストを返す
     * @param player 対象のプレイヤー
     * @param itemToFind 探したいアイテム
     * @return ItemStackのリスト
     */
    public static List<ItemStack> findItemsInBuffRow(Player player, Item itemToFind) {
        List<ItemStack> foundItems = new ArrayList<>();
        for (int i = 9; i <= 17; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(itemToFind)) {
                foundItems.add(stack);
            }
        }
        return foundItems;
    }
}