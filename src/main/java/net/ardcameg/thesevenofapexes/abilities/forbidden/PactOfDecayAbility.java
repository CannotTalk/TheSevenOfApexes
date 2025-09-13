package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.ardcameg.thesevenofapexes.Config;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.client.ClientTimerData;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.TimerSyncS2CPacket;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public final class PactOfDecayAbility {
    public static final String ID = "pact_of_decay";
    private static final String TIMER_TAG = ID + "_timer";
    private static final String HEALTH_MODIFIER_TAG = ID + "_health_modifier";
    private static final ResourceLocation HEALTH_MODIFIER_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, ID);

    public static void update(ServerPlayer player, boolean reversed) {
        if (!player.isAlive()) {
            return;
        }

        int itemCount = BuffItemUtils.countAllItemsForPlayer(player, ModItems.FORBIDDEN_PACT_OF_DECAY.get());
        if (itemCount <= 0 || (player.isCreative() || player.isSpectator())) {
            if (player.getPersistentData().contains(TIMER_TAG)) {
                clear(player);
            }
            return;
        }

        CompoundTag data = player.getPersistentData();
        if (!data.contains(TIMER_TAG)) {
            data.putInt(TIMER_TAG, Config.pactOfDecayIntervalTicks.get());
            data.putInt(HEALTH_MODIFIER_TAG, 0);
        }

        int ticksLeft = data.getInt(TIMER_TAG);
        if (ticksLeft <= 0) {
            if (!reversed) {
                if (player.getMaxHealth() > 1.0f) {
                    if (player.getRandom().nextBoolean()) {
                        int healthModifier = data.getInt(HEALTH_MODIFIER_TAG) - 1;
                        data.putInt(HEALTH_MODIFIER_TAG, healthModifier);
                        updateMaxHealth(player, healthModifier);
                    } else {
                        player.hurt(player.damageSources().generic(), 1.0f);
                    }
                } else {
                    player.getFoodData().setFoodLevel(Math.max(0, player.getFoodData().getFoodLevel() - 4));
                    player.getFoodData().setExhaustion(0.0f);
                }
            }else {
                if (player.getRandom().nextBoolean()) {
                    int healthModifier = data.getInt(HEALTH_MODIFIER_TAG) + 2;
                    data.putInt(HEALTH_MODIFIER_TAG, healthModifier);
                    updateMaxHealth(player, healthModifier);
                } else {
                    // Do nothing
                }
            }
            data.putInt(TIMER_TAG, Config.pactOfDecayIntervalTicks.get());
        } else {
            data.putInt(TIMER_TAG, ticksLeft - 1);
        }
        sync(player);
    }

    private static void updateMaxHealth(ServerPlayer player, int healthModifier) {
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr == null) return;
        healthAttr.removeModifier(HEALTH_MODIFIER_ID);
        AttributeModifier modifier = new AttributeModifier(HEALTH_MODIFIER_ID, healthModifier, AttributeModifier.Operation.ADD_VALUE);
        healthAttr.addPermanentModifier(modifier);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void clear(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.remove(TIMER_TAG);
        data.remove(HEALTH_MODIFIER_TAG);
        AttributeInstance healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) healthAttr.removeModifier(HEALTH_MODIFIER_ID);
        ModMessages.sendToPlayer(new TimerSyncS2CPacket(ID, 0, ClientTimerData.TimerState.INACTIVE.ordinal(), 0, 0, 0, 0), player);
    }

    private static void sync(ServerPlayer player) {
        int ticksLeft = player.getPersistentData().getInt(TIMER_TAG);
        int maxTicks = Config.pactOfDecayIntervalTicks.getAsInt();
        ModMessages.sendToPlayer(new TimerSyncS2CPacket(ID, ticksLeft, ClientTimerData.TimerState.COUNTING.ordinal(), 0, 0, 0, maxTicks), player);
    }
}