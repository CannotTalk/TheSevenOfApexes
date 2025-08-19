package net.ardcameg.thesevenofapexes.event;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.abilities.epic.*;
import net.ardcameg.thesevenofapexes.abilities.legendary.*;
import net.ardcameg.thesevenofapexes.abilities.rare.*;
import net.ardcameg.thesevenofapexes.abilities.util.StunAbility;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.PhoenixDebuffSyncS2CPacket;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = TheSevenOfApexes.MOD_ID)
public class ModEvents {

    // --- 記録簿 (State Management) ---
    private static final Map<UUID, Set<UUID>> ENVY_COPIED_ENTITIES = new HashMap<>();
    private static final Map<UUID, Vec3> PLAYER_LAST_POSITION = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_STANDING_TICKS = new HashMap<>();
    private static final Set<LivingEntity> STUNNED_ENTITIES = new HashSet<>();
    private static final Set<UUID> GRAIL_DAMAGE_FLAG = new HashSet<>();

    /**
     * 任務1：プレイヤーがダメージを受けた"後"の処理 (憤怒)
     */
    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;

        DamageSource damageSource = event.getSource();
        Entity attacker = event.getSource().getEntity();
        //if (attacker == null) return;

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if (baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int wrathCount = baseCounts.getOrDefault(ModItems.LEGENDARY_WRATH.get(), 0);
        if (wrathCount > 0 && attacker != null) {
            WrathAbility.apply(player, attacker, wrathCount, prideMultiplier);
        }

        // --- 「反転の砂時計」の処理 ---
        int hourglassCount = baseCounts.getOrDefault(ModItems.EPIC_REVERSAL_HOURGLASS.get(), 0);
        if (hourglassCount > 0 && attacker != null) {
            ReversalHourglassAbility.onPlayerDamaged(event, player, attacker, hourglassCount, prideMultiplier);
        }

        int fistCount = baseCounts.getOrDefault(ModItems.EPIC_LIGHTNING_FIST.get(), 0);
        if (fistCount > 0) {
            // NBTから最後に記録された連鎖回数を読み込む
            int lastChainCount = player.getPersistentData().getInt(LightningFistAbility.CHAIN_COUNT_TAG);
            System.out.println(lastChainCount);
            if (lastChainCount > 0) {
                // 追加ダメージを与える
                // hurt() を使うと他の効果を誘発してしまうので、直接体力を減らす
                player.setHealth(player.getHealth() - lastChainCount);
            }
        }
    }

    /**
     * 任務2：エンティティがダメージを受ける"前"の処理 (嫉妬)
     */
    @SubscribeEvent
    public static void onPreLivingDamage(LivingDamageEvent.Pre event) {
        Entity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();

        // --- プレイヤーが攻撃した時 ---
        if (attacker instanceof Player player && target instanceof LivingEntity livingTarget) {
            if (player.level().isClientSide) return;

            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
            int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

            int envyCount = baseCounts.getOrDefault(ModItems.LEGENDARY_ENVY.get(), 0);
            if (envyCount > 0) {
                EnvyAbility.apply(player, livingTarget, ENVY_COPIED_ENTITIES, envyCount, prideMultiplier);
            }

            int bargainCount = baseCounts.getOrDefault(ModItems.EPIC_FIENDS_BARGAIN.get(), 0);
            if (bargainCount > 0) {
                FiendsBargainAbility.apply(event, player, livingTarget, bargainCount, prideMultiplier);
            }
        }

        // --- プレイヤーが攻撃された時 ---
        if (target instanceof Player player) {
            if (player.level().isClientSide) return;

            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            int dragCount = baseCounts.getOrDefault(ModItems.EPIC_BERSERKERS_DRAG.get(), 0);

            int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
            int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

            // --- 「傷ついた聖杯」の処理 ---
            if (baseCounts.containsKey(ModItems.EPIC_SCARRED_GRAIL.get())) {
                ScarredGrailAbility.onPlayerDamaged(event, player);
                // 聖杯がダメージを無効化した場合、この後の処理は行わない
                if (event.getNewDamage() == 0) return;
            }

            if (dragCount > 0) {
                BerserkersDragAbility.onPlayerDamaged(event, player, dragCount, prideMultiplier);
            }
        }
    }

    /**
     * 任務3：プレイヤーが存在する間、毎tick呼び出される処理 (怠惰)
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        handlePhoenixFeatherDebuff(player);

        // --- 棚卸し：まず、現在有効なアイテムをすべて数える ---
        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        // --- 各担当部署への指示出し ---
        // アイテムを持っていなくても、0を渡して「解除処理」をさせるのが重要

        // 「怠惰」担当
        int slothCount = baseCounts.getOrDefault(ModItems.LEGENDARY_SLOTH.get(), 0);
        if (slothCount > 0) {
            handleSloth(player, slothCount, prideMultiplier);
        } else {
            PLAYER_STANDING_TICKS.remove(player.getUUID());
        }

        // 「色欲」担当
        int lustCount = baseCounts.getOrDefault(ModItems.LEGENDARY_LUST.get(), 0);
        LustAbility.applyPassiveEffect(player, lustCount, prideMultiplier); // applyPassiveEffectは0を渡せば何もしない賢いメソッド

        // 「日輪」と「月光」担当
        int sunSealCount = baseCounts.getOrDefault(ModItems.LEGENDARY_SUNLIGHT_SACRED_SEAL.get(), 0);
        int moonSealCount = baseCounts.getOrDefault(ModItems.LEGENDARY_MOONLIGHT_SACRED_SEAL.get(), 0);
        boolean hasSunSeal = sunSealCount > 0;
        boolean hasMoonSeal = moonSealCount > 0;
        SunlightSacredSealAbility.updateEffect(player, sunSealCount, prideMultiplier, hasSunSeal);
        MoonlightSacredSealAbility.updateEffect(player, moonSealCount, prideMultiplier, hasSunSeal, PLAYER_LAST_POSITION);

        // 「生命吸収の杖」担当
        int stickCount = baseCounts.getOrDefault(ModItems.EPIC_LIFE_STEEL_STICK.get(), 0);
        LifeSteelStickAbility.updatePassiveDebuff(player, stickCount);

        int dragCount = baseCounts.getOrDefault(ModItems.EPIC_BERSERKERS_DRAG.get(), 0);
        BerserkersDragAbility.updateEffect(player, dragCount, prideMultiplier);

        int anathemaCount = baseCounts.getOrDefault(ModItems.EPIC_WALKING_ANATHEMA.get(), 0);
        WalkingAnathemaAbility.applyAura(player, anathemaCount, prideMultiplier);

        int mantleCount = baseCounts.getOrDefault(ModItems.EPIC_VOID_MANTLE.get(), 0);
        VoidMantleAbility.updateEffect(player, mantleCount, prideMultiplier);

        int hourglassCount = baseCounts.getOrDefault(ModItems.EPIC_REVERSAL_HOURGLASS.get(), 0);
        ReversalHourglassAbility.updatePassiveDebuff(player, hourglassCount);

        int gavelCount = baseCounts.getOrDefault(ModItems.EPIC_GOLIATHS_GAVEL.get(), 0);
        GoliathsGavelAbility.updatePassiveEffect(player, gavelCount, prideMultiplier);

        int eyeCount = baseCounts.getOrDefault(ModItems.RARE_NIGHT_OWL_EYES.get(), 0);
        NightOwlEyesAbility.updateEffect(player, eyeCount, prideMultiplier);

        int crestCount = baseCounts.getOrDefault(ModItems.RARE_GUARDIANS_CREST.get(), 0);
        GuardiansCrestAbility.updateEffect(player, crestCount, prideMultiplier);

        int charmCount = baseCounts.getOrDefault(ModItems.RARE_GILLS_CHARM.get(), 0);
        GillsCharmAbility.updateEffect(player, charmCount, prideMultiplier);

        int monocleCount = baseCounts.getOrDefault(ModItems.RARE_SNIPERS_MONOCLE.get(), 0);
        SnipersMonocleAbility.updatePassiveDebuff(player, monocleCount, prideMultiplier);

        int standCount = baseCounts.getOrDefault(ModItems.RARE_LAST_STAND.get(), 0);
        LastStandAbility.updatePassiveBuffs(player, standCount, prideMultiplier);

        // --- 「守護者の紋章」の消耗度増加処理 ---
        if (crestCount > 0) {
            // プレイヤーが地面に足がついておらず、かつY軸方向に上昇している場合 ＝ ジャンプ中と見なす
            boolean isJumpingOrFalling = !player.onGround() && player.getDeltaMovement().y > 0;

            if (player.getDeltaMovement().horizontalDistanceSqr() > 0.001 || isJumpingOrFalling) {
                float extraExhaustion = 0.005f + 0.001f * crestCount * prideMultiplier;
                player.getFoodData().addExhaustion(extraExhaustion);
            }
        }

        // --- 最後の後処理 ---
        // 怠惰と月光のために、最後にプレイヤーの座標を記録
        // もしこれらのアイテムがなければ、記録は不要
        if (slothCount > 0 || moonSealCount > 0) {
            PLAYER_LAST_POSITION.put(player.getUUID(), player.position());
        } else {
            PLAYER_LAST_POSITION.remove(player.getUUID());
        }
    }

    /**
     * 任務4：Mobが倒された時の処理 (強欲)
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        if (baseCounts.containsKey(ModItems.EPIC_WALKING_ANATHEMA.get())) {
            // ターゲットが敵性Mobまたは中立Mobの場合
            if (event.getEntity() instanceof Enemy || event.getEntity() instanceof net.minecraft.world.entity.NeutralMob) {
                // ドロップリストを完全に空にする
                event.getDrops().clear();
                // 経験値オーブも落とさなくする
                event.setCanceled(true); // ★LivingDropsEventをキャンセルすると経験値も消える
            }
        }

        int greedCount = baseCounts.getOrDefault(ModItems.LEGENDARY_GREED.get(), 0);
        if (greedCount > 0) {
            GreedAbility.applyToMob(event, greedCount, prideMultiplier);
        }
    }

    /**
     * 任務5：ブロックが破壊された瞬間の処理 (強欲)
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (event.getLevel().isClientSide() || player.isCreative() || !player.getMainHandItem().isCorrectToolForDrops(event.getState())) {
            return;
        }

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int greedCount = baseCounts.getOrDefault(ModItems.LEGENDARY_GREED.get(), 0);
        if (greedCount > 0) {
            GreedAbility.applyToBlock(event, greedCount, prideMultiplier);
        }
    }

    /**
     * 任務6：プレイヤーがリスポーンした瞬間の処理
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered()) return;

        Player player = event.getEntity();

        // 暴食のデータリセット
        player.getPersistentData().remove("GluttonyGauge");
        player.getPersistentData().remove("GluttonyLevel");
        // 稲妻の拳のデータリセット
        player.getPersistentData().remove(LightningFistAbility.CHAIN_COUNT_TAG);
    }

    /**
     * 任務7：プレイヤーが死亡する直前の処理 (色欲の蘇生)
     * EventPriority.HIGHEST: 他のどのModよりも先に、この処理を実行する
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) {
            return;
        }

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if(baseCounts.isEmpty()) return;

        // --- 聖杯の暴発ダメージ中は、一度しか蘇生させない ---
        if (player.getPersistentData().getBoolean("TAKING_GRAIL_DAMAGE")) {
            // このtickで、既にこのプレイヤーが聖杯ダメージで蘇生を試みたかチェック
            if (GRAIL_DAMAGE_FLAG.contains(player.getUUID())) {
                return; // 既に試みたので、2回目以降の蘇生は行わない
            }
            // まだ試みていないので、フラグを立てる
            GRAIL_DAMAGE_FLAG.add(player.getUUID());
        }

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        // --- 蘇生アイテムの優先順位 ---
        // 1. 伝説級：色欲 (消費なし、条件付き)
        int lustCount = baseCounts.getOrDefault(ModItems.LEGENDARY_LUST.get(), 0);
        if (lustCount > 0) {
            Entity attacker = event.getSource().getEntity();
            if (LustAbility.attemptRevive(player, attacker, lustCount, prideMultiplier)) {
                event.setCanceled(true);
                return; // 蘇生成功したので、ここで処理を終了
            }
        }

        // 2. 英雄級：不死鳥の羽 (消費あり、全回復)
        int featherCount = baseCounts.getOrDefault(ModItems.EPIC_PHOENIX_FEATHER.get(), 0);
        if (featherCount > 0) {
            if (PhoenixFeatherAbility.attemptRevive(player)) {
                event.setCanceled(true);
                return;
            }
        }

        // 3. 英雄級：復活の時 (消費あり、体力1で耐える)
        int revivalCount = baseCounts.getOrDefault(ModItems.EPIC_ARRIVAL_OF_REVIVAL.get(), 0);
        if (revivalCount > 0) {
            if (ArrivalOfRevivalAbility.attemptRevive(player, revivalCount, prideMultiplier)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    /**
     * 任務8：プレイヤーが敵を攻撃し、ダメージ計算が終わった"後"の処理 (稲妻の拳)
     */
    @SubscribeEvent
    public static void onPlayerAttackPostDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof LivingEntity target) {
            if (player.level().isClientSide) return;

            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
            int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

            int fistCount = baseCounts.getOrDefault(ModItems.EPIC_LIGHTNING_FIST.get(), 0); // ModItemsに登録する必要あり
            if (fistCount > 0) {
                if(target instanceof Enemy) {
                    float initialDamage = event.getNewDamage(); // 最終的なダメージ量を取得
                    LivingEntity initialTarget = event.getEntity();

                    LightningFistAbility.applyAttackEffect(player, initialTarget, initialDamage, fistCount, prideMultiplier);
                }
            }

            int stickCount = baseCounts.getOrDefault(ModItems.EPIC_LIFE_STEEL_STICK.get(), 0);
            if (stickCount > 0) {
                LifeSteelStickAbility.applyLifeSteal(player, target, stickCount, prideMultiplier);
            }

            // --- 「影縫の手袋」の処理 ---
            int gloveCount = baseCounts.getOrDefault(ModItems.EPIC_SHADOW_BIND_GLOVES.get(), 0);
            if (gloveCount > 0) {
                ShadowBindGlovesAbility.apply(player, target, gloveCount, prideMultiplier, STUNNED_ENTITIES);
            }

            // --- 「鋼鉄の爪」の処理 ---
            int clawCount = baseCounts.getOrDefault(ModItems.EPIC_STEEL_CLAWS.get(), 0);
            if (clawCount > 0) {
                SteelClawsAbility.apply(player, target, clawCount, prideMultiplier);
            }

            int gavelCount = baseCounts.getOrDefault(ModItems.EPIC_GOLIATHS_GAVEL.get(), 0);
            if (gavelCount > 0) {
                GoliathsGavelAbility.applyAreaDamage(player, target, gavelCount, prideMultiplier);
            }
        }
    }

    /**
     * 任務9：ワールドがtickするごとの処理 (スタン解除)
     */
    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide() || STUNNED_ENTITIES.isEmpty()) return;

        // スタンしているエンティティのリストをコピーして、安全にイテレートする
        // (ループ中にリストから要素を削除するため)
        new HashSet<>(STUNNED_ENTITIES).forEach(entity -> {
            // エンティティが死んだり、ワールドからいなくなったらリストから削除
            if (!entity.isAlive() || entity.isRemoved()) {
                STUNNED_ENTITIES.remove(entity);
                return;
            }
            // スタン状態を更新する
            StunAbility.update(entity, STUNNED_ENTITIES);
        });
    }

    /**
     * 任務11：エンティティがワールドに出現した瞬間の処理 (狙撃者の隻眼)
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        // --- 1. ワールドに出現したエンティティが「矢」であるかを確認 ---
        if (event.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow) {
            // --- 2. その矢の所有者が「プレイヤー」であるかを確認 ---
            if (arrow.getOwner() instanceof Player player) {
                // --- ここから先は、あなたの元のコードと同じ ---
                if (player.level().isClientSide) return;

                Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
                int monocleCount = baseCounts.getOrDefault(ModItems.RARE_SNIPERS_MONOCLE.get(), 0);

                if (monocleCount > 0) {
                    int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
                    int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);
                    SnipersMonocleAbility.onArrowFired(event, player, monocleCount, prideMultiplier);
                }
            }
        }
    }

    /**
     * 任務12：プレイヤーが体力を回復する瞬間の処理 (最後の抵抗)
     */
    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;

        // 「最後の抵抗」が発動する条件下（体力が25%以下）でのみ、回復量を増やす
        if (player.getHealth() <= player.getMaxHealth() * 0.25f) {
            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            int standCount = baseCounts.getOrDefault(ModItems.RARE_LAST_STAND.get(), 0);

            if (standCount > 0) {
                int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
                int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);
                LastStandAbility.onPlayerHeal(event, standCount, prideMultiplier);
            }
        }
    }


    // --- onPlayerTickから分離された、怠惰専用の処理メソッド ---
    private static void handleSloth(Player player, int baseSlothCount, int prideMultiplier) {
        UUID playerUUID = player.getUUID();
        Vec3 currentPos = player.position();
        Vec3 lastPos = PLAYER_LAST_POSITION.get(playerUUID);

        if (lastPos == null) {
            PLAYER_LAST_POSITION.put(playerUUID, currentPos);
            return;
        }

        if (currentPos.distanceToSqr(lastPos) < 0.0001) {
            int ticks = PLAYER_STANDING_TICKS.getOrDefault(playerUUID, 0) + 1;
            PLAYER_STANDING_TICKS.put(playerUUID, ticks);

            int finalSlothCount = baseSlothCount * prideMultiplier;
            int requiredTicks = SlothAbility.getRequiredStandingTicks(finalSlothCount);

            if (ticks >= requiredTicks) {
                SlothAbility.apply(player, baseSlothCount, prideMultiplier);
                PLAYER_STANDING_TICKS.put(playerUUID, 0);
            }
        } else {
            PLAYER_STANDING_TICKS.put(playerUUID, 0);
        }
        PLAYER_LAST_POSITION.put(playerUUID, currentPos);
    }

    /**
     * 不死鳥の羽のデバフを管理するヘルパーメソッド
     */
    private static void handlePhoenixFeatherDebuff(Player player) {
        if (player.level().isClientSide || !(player instanceof ServerPlayer serverPlayer)) return;

        AttributeInstance healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute == null) return;

        AttributeModifier modifier = healthAttribute.getModifier(
                ResourceLocation.fromNamespaceAndPath(TheSevenOfApexes.MOD_ID, "phoenix_feather_health_debuff")
        );

        if (modifier != null) {
            int remainingTicks = player.getPersistentData().getInt("PhoenixFeatherDebuffTicks");

            if (remainingTicks > 0) {
                player.getPersistentData().putInt("PhoenixFeatherDebuffTicks", remainingTicks - 1);

                if (remainingTicks % 20 == 0) {
                    ModMessages.sendToPlayer(new PhoenixDebuffSyncS2CPacket(remainingTicks), serverPlayer);
                }
            } else {
                healthAttribute.removeModifier(modifier);
                player.getPersistentData().remove("PhoenixFeatherDebuffTicks");
                if (player.getHealth() > player.getMaxHealth()) {
                    player.setHealth(player.getMaxHealth());
                }
                ModMessages.sendToPlayer(new PhoenixDebuffSyncS2CPacket(0), serverPlayer);
            }
        }
    }
}