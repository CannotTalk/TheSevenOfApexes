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

public class RarePackItem extends Item {

    public RarePackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PackLootManager.openRarePack(player);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        // 実績を達成
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementTriggers.grantCriterion(serverPlayer, "collector_of_possibilities", "opened_rare_pack");
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}