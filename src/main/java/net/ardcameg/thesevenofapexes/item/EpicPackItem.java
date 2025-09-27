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

public class EpicPackItem extends Item {

    public EpicPackItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            PackLootManager.openEpicPack(player);

            if (!player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }

        //実績を達成
        if (player instanceof ServerPlayer serverPlayer) {
            AdvancementTriggers.grantAdvancement(serverPlayer, "high_rarity");

            AdvancementTriggers.grantCriterion(serverPlayer, "collector_of_possibilities", "opened_epic_pack");
        }

        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResultHolder.sidedSuccess(itemStack, level.isClientSide());
    }
}