package net.ardcameg.thesevenofapexes.item;

import net.ardcameg.thesevenofapexes.event.AdvancementTriggers;
import net.ardcameg.thesevenofapexes.util.PackLootManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class RandomPackItem extends Item {

    public RandomPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        // アイテムを与える処理は、サーバーサイドでのみ実行する
        if (!level.isClientSide) {
            // ルート管理者に開封処理を依頼
            PackLootManager.openRandomPack(player);

            // プレイヤーがクリエイティブモードでなければ、パックを1つ消費する
            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        // 実績を達成
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementTriggers.grantCriterion(serverPlayer, "collector_of_possibilities", "opened_random_pack");
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        // 成功したことをクライアントとサーバーの両方に伝える
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}