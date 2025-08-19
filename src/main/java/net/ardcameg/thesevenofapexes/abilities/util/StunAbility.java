package net.ardcameg.thesevenofapexes.abilities.util;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import java.util.Set;

public final class StunAbility {
    private StunAbility() {}

    // --- このアビリティが管理するAttribute ModifierのID ---
    private static final ResourceLocation STUN_MOVEMENT_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "stun_movement");
    private static final ResourceLocation STUN_ATTACK_ID = ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "stun_attack");

    // NBTにスタン残り時間を保存するためのタグ名
    private static final String STUN_TICKS_TAG = "StunTicks";

    /**
     * 指定されたエンティティをスタンさせる
     * @param entity スタンさせる対象
     * @param durationTicks スタンの持続時間 (tick)
     */
    public static void apply(LivingEntity entity, int durationTicks, Set<LivingEntity> stunnedEntities) {
        // --- 1. Attributeを0にするModifierを適用 ---
        // 移動速度を-100%にする
        addModifier(entity, Attributes.MOVEMENT_SPEED, STUN_MOVEMENT_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        // 攻撃速度を-100%にする (攻撃できなくする)
        addModifier(entity, Attributes.ATTACK_SPEED, STUN_ATTACK_ID, -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);

        // --- 2. NBTにスタン時間を記録 ---
        // 既にスタンしている場合でも、新しい時間で上書きする
        entity.getPersistentData().putInt(STUN_TICKS_TAG, durationTicks);
        stunnedEntities.add(entity);
    }

    /**
     * 指定されたエンティティのスタン状態を毎tick更新し、必要であれば解除する
     * このメソッドは、ModEventsの onLivingTick で呼び出す必要がある
     * @param entity チェックする対象
     */
    public static void update(LivingEntity entity, Set<LivingEntity> stunnedEntities) {
        // NBTにスタン時間が記録されているかチェック
        if (entity.getPersistentData().contains(STUN_TICKS_TAG)) {
            int remainingTicks = entity.getPersistentData().getInt(STUN_TICKS_TAG);

            if (remainingTicks > 0) {
                // 残り時間があれば1減らす
                entity.getPersistentData().putInt(STUN_TICKS_TAG, remainingTicks - 1);
            } else {
                // 残り時間が0になったら、スタンを解除する
                removeStun(entity, stunnedEntities);
            }
        }
    }

    private static void removeStun(LivingEntity entity, Set<LivingEntity> stunnedEntities) {
        // Attribute Modifierを削除
        removeAttributeModifier(entity, Attributes.MOVEMENT_SPEED, STUN_MOVEMENT_ID);
        removeAttributeModifier(entity, Attributes.ATTACK_SPEED, STUN_ATTACK_ID);
        // NBTタグを削除
        entity.getPersistentData().remove(STUN_TICKS_TAG);
        stunnedEntities.remove(entity);
    }

    private static void addModifier(LivingEntity entity, net.minecraft.core.Holder<Attribute> attribute, ResourceLocation id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id); // 念のため削除
            instance.addPermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }

    private static void removeAttributeModifier(LivingEntity entity, net.minecraft.core.Holder<Attribute> attribute, ResourceLocation id) {
        AttributeInstance instance = entity.getAttribute(attribute);
        if (instance != null) {
            instance.removeModifier(id);
        }
    }
}