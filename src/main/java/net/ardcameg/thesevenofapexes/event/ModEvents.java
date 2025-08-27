package net.ardcameg.thesevenofapexes.event;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.abilities.epic.*;
import net.ardcameg.thesevenofapexes.abilities.legendary.*;
import net.ardcameg.thesevenofapexes.abilities.rare.*;
import net.ardcameg.thesevenofapexes.abilities.uncommon.*;
import net.ardcameg.thesevenofapexes.abilities.common.*;
import net.ardcameg.thesevenofapexes.abilities.util.StunAbility;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.PhoenixDebuffSyncS2CPacket;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.ardcameg.thesevenofapexes.util.PackLootTableReloadListener;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerWakeUpEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.*;

@EventBusSubscriber(modid = TheSevenOfApexes.MOD_ID)
public class ModEvents {

    // --- 記録簿 (State Management) ---
    private static final Map<UUID, Set<UUID>> ENVY_COPIED_ENTITIES = new HashMap<>();
    private static final Map<UUID, Vec3> PLAYER_LAST_POSITION = new HashMap<>();
    private static final Map<UUID, Integer> PLAYER_STANDING_TICKS = new HashMap<>();
    private static final Set<LivingEntity> STUNNED_ENTITIES = new HashSet<>();
    private static final Set<UUID> GRAIL_DAMAGE_FLAG = new HashSet<>();
    private static final Set<UUID> CRITICAL_HIT_FLAG = new HashSet<>();

    /**
     * 任務1：プレイヤーがダメージを受けた"後"の処理
     */
    @SubscribeEvent
    public static void onPlayerDamaged(LivingDamageEvent.Post event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;

        DamageSource damageSource = event.getSource();
        Entity attacker = damageSource.getEntity();

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if (baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int wrathCount = baseCounts.getOrDefault(ModItems.LEGENDARY_WRATH.get(), 0);
        if (wrathCount > 0 && attacker != null) {
            WrathAbility.apply(player, attacker, wrathCount, prideMultiplier);
        }

        int heartOfStormCount = baseCounts.getOrDefault(ModItems.EPIC_HEART_OF_STORM.get(), 0);
        if (heartOfStormCount > 0 && attacker != null) {
            HeartOfStormAbility.apply(player, attacker, heartOfStormCount, prideMultiplier, STUNNED_ENTITIES);
        }

        int reversalHourglassCount = baseCounts.getOrDefault(ModItems.EPIC_REVERSAL_HOURGLASS.get(), 0);
        if (reversalHourglassCount > 0 && attacker != null) {
            ReversalHourglassAbility.onPlayerDamaged(event, player, attacker, reversalHourglassCount, prideMultiplier);
        }

        int lightningFistCount = baseCounts.getOrDefault(ModItems.EPIC_LIGHTNING_FIST.get(), 0);
        if (lightningFistCount > 0) {
            int lastChainCount = player.getPersistentData().getInt(LightningFistAbility.CHAIN_COUNT_TAG);
            if (lastChainCount > 0) {
                player.setHealth(player.getHealth() - lastChainCount);
            }
        }

        int scytheCount = baseCounts.getOrDefault(ModItems.RARE_REAPERS_SCYTHE.get(), 0);
        if (scytheCount > 0) {
            if (attacker != null && attacker != player) {
                ReapersScytheAbility.applyWitherOnDamaged(player, scytheCount, prideMultiplier);
            }
        }
    }

    /**
     * 任務2：エンティティがダメージを受ける"前"の処理
     */
    @SubscribeEvent
    public static void onPreLivingDamage(LivingDamageEvent.Pre event) {
        Entity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();

        if (attacker instanceof Player player && target instanceof LivingEntity livingTarget) {

            if (player.level().isClientSide) return;

            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            if (baseCounts.isEmpty()) return;

            int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
            int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

            int envyCount = baseCounts.getOrDefault(ModItems.LEGENDARY_ENVY, 0);
            if (envyCount > 0) {
                EnvyAbility.apply(player, livingTarget, ENVY_COPIED_ENTITIES, envyCount, prideMultiplier);
            }

            int fiendsBargainCount = baseCounts.getOrDefault(ModItems.EPIC_FIENDS_BARGAIN.get(), 0);
            if (fiendsBargainCount > 0) {
                FiendsBargainAbility.apply(event, player, livingTarget, fiendsBargainCount, prideMultiplier);
            }

            int deadeyeGlassCount = baseCounts.getOrDefault(ModItems.RARE_DEADEYE_GLASS.get(), 0);
            if (deadeyeGlassCount > 0) {
                if (!CRITICAL_HIT_FLAG.contains(player.getUUID())) {
                    DeadeyeGlassAbility.onNormalAttack(event);
                }
            }
        }

        if (target instanceof Player player) {
            if (player.level().isClientSide) return;
            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            if (baseCounts.isEmpty()) return;
            int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
            int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

            int pearlEyeCount = baseCounts.getOrDefault(ModItems.UNCOMMON_PEARL_EYE, 0);
            if (pearlEyeCount > 0) {
                PearlEyeAbility.reduceEnderPearlDamage(event, pearlEyeCount, prideMultiplier);
            }

            int hungryBanquetCount = baseCounts.getOrDefault(ModItems.RARE_HUNGRY_BANQUET.get(), 0);
            if (hungryBanquetCount > 0) {
                HungryBanquetAbility.convertDamageToHunger(event, player, hungryBanquetCount, prideMultiplier);
                if (event.getNewDamage() <= 0) return;
            }

            int scarredGrailCount = baseCounts.getOrDefault(ModItems.EPIC_SCARRED_GRAIL.get(), 0);
            if (scarredGrailCount > 0) {
                ScarredGrailAbility.onPlayerDamaged(event, player);
                if (event.getNewDamage() == 0) return;
            }

            int berserkersDragCount = baseCounts.getOrDefault(ModItems.EPIC_BERSERKERS_DRAG.get(), 0);
            if (berserkersDragCount > 0) {
                BerserkersDragAbility.onPlayerDamaged(event, player, berserkersDragCount, prideMultiplier);
            }
        }
    }

    /**
     * 任務3：プレイヤーが存在する間、毎tick呼び出される処理
     */
    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        handlePhoenixFeatherDebuff(player);

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int slothCount = baseCounts.getOrDefault(ModItems.LEGENDARY_SLOTH.get(), 0);
        if (slothCount > 0) {
            handleSloth(player, slothCount, prideMultiplier);
        } else {
            PLAYER_STANDING_TICKS.remove(player.getUUID());
        }

        LustAbility.applyPassiveEffect(player, baseCounts.getOrDefault(ModItems.LEGENDARY_LUST.get(), 0), prideMultiplier);

        int sunSealCount = baseCounts.getOrDefault(ModItems.LEGENDARY_SUNLIGHT_SACRED_SEAL.get(), 0);
        int moonSealCount = baseCounts.getOrDefault(ModItems.LEGENDARY_MOONLIGHT_SACRED_SEAL.get(), 0);
        boolean hasSunSeal = sunSealCount > 0;
        boolean hasMoonSeal = moonSealCount > 0;
        SunlightSacredSealAbility.updateEffect(player, sunSealCount, prideMultiplier, hasMoonSeal);
        MoonlightSacredSealAbility.updateEffect(player, moonSealCount, prideMultiplier, hasSunSeal, PLAYER_LAST_POSITION);

        int lifeSteelCount = baseCounts.getOrDefault(ModItems.EPIC_LIFE_STEAL_STICK.get(), 0);
        LifeStealStickAbility.updatePassiveDebuff(player, lifeSteelCount);

        int berserkersDragCount = baseCounts.getOrDefault(ModItems.EPIC_BERSERKERS_DRAG.get(), 0);
        BerserkersDragAbility.updateEffect(player, berserkersDragCount, prideMultiplier);

        int walkingAnathemaCount = baseCounts.getOrDefault(ModItems.EPIC_WALKING_ANATHEMA.get(), 0);
        WalkingAnathemaAbility.applyAura(player, walkingAnathemaCount, prideMultiplier);

        int voidMantleCount = baseCounts.getOrDefault(ModItems.EPIC_VOID_MANTLE.get(), 0);
        VoidMantleAbility.updateEffect(player, voidMantleCount, prideMultiplier);

        int reversalHourGlassCount = baseCounts.getOrDefault(ModItems.EPIC_REVERSAL_HOURGLASS.get(), 0);
        ReversalHourglassAbility.updatePassiveDebuff(player, reversalHourGlassCount);

        int goliathsGavelCount = baseCounts.getOrDefault(ModItems.EPIC_GOLIATHS_GAVEL.get(), 0);
        GoliathsGavelAbility.updatePassiveEffect(player, goliathsGavelCount, prideMultiplier);


        int nightOwlEyesCount = baseCounts.getOrDefault(ModItems.RARE_NIGHT_OWL_EYES.get(), 0);
        NightOwlEyesAbility.updateEffect(player, nightOwlEyesCount, prideMultiplier);

        int guardiansCrestCount = baseCounts.getOrDefault(ModItems.RARE_GUARDIANS_CREST.get(), 0);
        GuardiansCrestAbility.updateEffect(player, guardiansCrestCount, prideMultiplier);

        int gillsCharmCount = baseCounts.getOrDefault(ModItems.RARE_GILLS_CHARM.get(), 0);
        GillsCharmAbility.updateEffect(player, gillsCharmCount, prideMultiplier);

        int snipersMonocleCount = baseCounts.getOrDefault(ModItems.RARE_SNIPERS_MONOCLE.get(), 0);
        SnipersMonocleAbility.updatePassiveDebuff(player, snipersMonocleCount, prideMultiplier);

        int lastStandCount = baseCounts.getOrDefault(ModItems.RARE_LAST_STAND.get(), 0);
        LastStandAbility.updatePassiveBuffs(player, lastStandCount, prideMultiplier);

        int blademastersProwessCount = baseCounts.getOrDefault(ModItems.RARE_BLADEMASTERS_PROWESS.get(), 0);
        BlademastersProwessAbility.updatePassiveBuffs(player, blademastersProwessCount, prideMultiplier);

        int deadeyeGlassCount = baseCounts.getOrDefault(ModItems.RARE_DEADEYE_GLASS.get(), 0);
        DeadeyeGlassAbility.updatePassiveBuffs(player, deadeyeGlassCount, prideMultiplier);

        int architectsHasteCount = baseCounts.getOrDefault(ModItems.RARE_ARCHITECTS_HASTE.get(), 0);
        ArchitectsHasteAbility.updatePassiveBuffs(player, architectsHasteCount, prideMultiplier);

        int ringCount = baseCounts.getOrDefault(ModItems.RARE_VITAL_CONVERSION_RING.get(), 0);
        if (ringCount > 0) {
            VitalConversionRingAbility.applyHealthRecovery(player, ringCount, prideMultiplier);
            VitalConversionRingAbility.rejectNonMeatFood(player);
        }

        int totemCount = baseCounts.getOrDefault(ModItems.RARE_BOUNTY_TOTEM.get(), 0);
        if (totemCount > 0) {
            handleBountyTotemAura(player, totemCount, prideMultiplier);
        }

        int spiderWarpCount = baseCounts.getOrDefault(ModItems.UNCOMMON_SPIDERS_WARP.get(), 0);
        if (spiderWarpCount > 0) {
            SpiderWarpAbility.apply(player, spiderWarpCount, prideMultiplier);
        }

        int shiningAuraCount = baseCounts.getOrDefault(ModItems.COMMON_SHINING_AURA.get(), 0);
        // このアイテムは効果を更新する必要があるので、if文ではなく直接呼び出す
        ShiningAuraAbility.updateEffect(player, shiningAuraCount, prideMultiplier);

        int clothesCount = baseCounts.getOrDefault(ModItems.COMMON_EMPERORS_NEW_CLOTHES.get(), 0);
        if (clothesCount > 0) {
            EmperorsNewClothesAbility.apply(player, clothesCount, prideMultiplier);
        }

        int crestCount = baseCounts.getOrDefault(ModItems.RARE_GUARDIANS_CREST.get(), 0);
        if (crestCount > 0) {
            boolean isJumpingOrFalling = !player.onGround() && player.getDeltaMovement().y > 0;
            if (player.getDeltaMovement().horizontalDistanceSqr() > 0.001 || isJumpingOrFalling) {
                float extraExhaustion = 0.005f + 0.001f * crestCount * prideMultiplier;
                player.getFoodData().addExhaustion(extraExhaustion);
            }
        }

        if (slothCount > 0 || moonSealCount > 0) {
            PLAYER_LAST_POSITION.put(player.getUUID(), player.position());
        } else {
            PLAYER_LAST_POSITION.remove(player.getUUID());
        }
    }

    /**
     * 任務4：Mobが倒された時の処理
     */
    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player)) return;

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if (baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int walkingAnathemaCount = baseCounts.getOrDefault(ModItems.EPIC_WALKING_ANATHEMA.get(), 0);
        if (walkingAnathemaCount > 0) {
            if (event.getEntity() instanceof Enemy || event.getEntity() instanceof NeutralMob) {
                event.getDrops().clear();
                event.setCanceled(true);
            }
        }

        int greedCount = baseCounts.getOrDefault(ModItems.LEGENDARY_GREED.get(), 0);
        if (greedCount > 0) {
            GreedAbility.applyToMob(event, greedCount, prideMultiplier);
        }
    }

    /**
     * 任務5：ブロックが破壊された瞬間の処理
     */
    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (event.getLevel().isClientSide() || player.isCreative()) return;
        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if (baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int greedCount = baseCounts.getOrDefault(ModItems.LEGENDARY_GREED.get(), 0);
        if (greedCount > 0) {
            GreedAbility.applyToBlock(event, greedCount, prideMultiplier);
        }

        int architectsHasteCount = baseCounts.getOrDefault(ModItems.RARE_ARCHITECTS_HASTE.get(), 0);
        if (architectsHasteCount > 0) {
            ArchitectsHasteAbility.applyDurabilityPenalty(player, architectsHasteCount, prideMultiplier);
        }

        int bountyTotem = baseCounts.getOrDefault(ModItems.RARE_BOUNTY_TOTEM.get(), 0);
        if (event.getState().getBlock() instanceof CropBlock) {
            if (bountyTotem > 0) {
                BountyTotemAbility.degradeFarmland(event, bountyTotem, prideMultiplier);
            }
        }

        int luckyFlintCount = baseCounts.getOrDefault(ModItems.UNCOMMON_LUCKY_FLINT.get(), 0);
        if (luckyFlintCount > 0) {
            LuckyFlintAbility.apply(event, luckyFlintCount, prideMultiplier);
        }

        int redundantFlintCount = baseCounts.getOrDefault(ModItems.UNCOMMON_REDUNDANT_FLINT.get(), 0);
        if (redundantFlintCount > 0) {
            RedundantFlintAbility.apply(event, redundantFlintCount, prideMultiplier);
            return;
        }
    }

    /**
     * 任務6：プレイヤーがリスポーンした瞬間の処理
     */
    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered()) return;
        Player player = event.getEntity();
        player.getPersistentData().remove("GluttonyGauge");
        player.getPersistentData().remove("GluttonyLevel");
        player.getPersistentData().remove(LightningFistAbility.CHAIN_COUNT_TAG);
    }

    /**
     * 任務7：プレイヤーが死亡する直前の処理
     */
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if(baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        if (player.getPersistentData().getBoolean("TAKING_GRAIL_DAMAGE")) {
            if (GRAIL_DAMAGE_FLAG.contains(player.getUUID())) return;
            GRAIL_DAMAGE_FLAG.add(player.getUUID());
        }

        int lustCount = baseCounts.getOrDefault(ModItems.LEGENDARY_LUST.get(), 0);
        if (lustCount > 0) {
            if (LustAbility.attemptRevive(player, event.getSource().getEntity(), lustCount, prideMultiplier)) {
                event.setCanceled(true);
                return;
            }
        }

        int phoenixFeatherCount = baseCounts.getOrDefault(ModItems.EPIC_PHOENIX_FEATHER.get(), 0);
        if (phoenixFeatherCount > 0) {
            if (PhoenixFeatherAbility.attemptRevive(player)) {
                event.setCanceled(true);
                return;
            }
        }

        int arrivalOfRevivalCount = baseCounts.getOrDefault(ModItems.EPIC_ARRIVAL_OF_REVIVAL.get(), 0);
        if (arrivalOfRevivalCount > 0) {
            if (ArrivalOfRevivalAbility.attemptRevive(player, arrivalOfRevivalCount, prideMultiplier)) {
                event.setCanceled(true);
                return;
            }
        }
    }

    /**
     * 任務8：プレイヤーが敵を攻撃し、ダメージ計算が終わった"後"の処理
     */
    @SubscribeEvent
    public static void onPlayerAttackPostDamage(LivingDamageEvent.Post event) {
        if (event.getSource().getEntity() instanceof Player player && event.getEntity() instanceof LivingEntity target) {

            if (player.level().isClientSide) return;

            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            if(baseCounts.isEmpty()) return;

            int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
            int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

            int lightningFistCount = baseCounts.getOrDefault(ModItems.EPIC_LIGHTNING_FIST.get(), 0);
            if (lightningFistCount > 0 && target instanceof Enemy) {
                LightningFistAbility.applyAttackEffect(player, target, event.getNewDamage(), lightningFistCount, prideMultiplier);
            }

            int lifeSteelStickCount = baseCounts.getOrDefault(ModItems.EPIC_LIFE_STEAL_STICK.get(), 0);
            if (lifeSteelStickCount > 0) {
                // 旧: LifeSteelStickAbility.applyLifeSteal(player, target, lifeSteelStickCount, prideMultiplier);
                // 新: eventから実際に与えたダメージを取得して渡す
                LifeStealStickAbility.applyLifeSteal(player, event.getNewDamage(), lifeSteelStickCount, prideMultiplier);
            }

            int shadowBindGlovesCount = baseCounts.getOrDefault(ModItems.EPIC_SHADOW_BIND_GLOVES.get(), 0);
            if (shadowBindGlovesCount > 0) {
                ShadowBindGlovesAbility.apply(player, target, shadowBindGlovesCount, prideMultiplier, STUNNED_ENTITIES);
            }

            int steelClawsCount = baseCounts.getOrDefault(ModItems.EPIC_STEEL_CLAWS.get(), 0);
            if (steelClawsCount > 0) {
                SteelClawsAbility.apply(player, target, steelClawsCount, prideMultiplier);
            }

            int goliathsGavelCount = baseCounts.getOrDefault(ModItems.EPIC_GOLIATHS_GAVEL.get(), 0);
            if (goliathsGavelCount > 0) {
                GoliathsGavelAbility.applyAreaDamage(player, target, goliathsGavelCount, prideMultiplier);
            }

            int reapersScytheCount = baseCounts.getOrDefault(ModItems.RARE_REAPERS_SCYTHE.get(), 0);
            if (reapersScytheCount > 0) {
                ReapersScytheAbility.applyWitherOnAttack(target, reapersScytheCount, prideMultiplier);
            }

            int architectsHasteCount = baseCounts.getOrDefault(ModItems.RARE_ARCHITECTS_HASTE.get(), 0);
            if (architectsHasteCount > 0) {
                ArchitectsHasteAbility.applyDurabilityPenalty(player, architectsHasteCount, prideMultiplier);
            }
        }
    }

    /**
     * 任務9：ワールドがtickするごとの処理 (スタン解除)
     */
    @SubscribeEvent
    public static void onWorldTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide() || STUNNED_ENTITIES.isEmpty()) return;
        new HashSet<>(STUNNED_ENTITIES).forEach(entity -> {
            if (!entity.isAlive() || entity.isRemoved()) {
                STUNNED_ENTITIES.remove(entity);
                return;
            }
            StunAbility.update(entity, STUNNED_ENTITIES);
        });
    }

    /**
     * 任務11：エンティティがワールドに出現した瞬間の処理
     */
    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof net.minecraft.world.entity.projectile.AbstractArrow arrow && arrow.getOwner() instanceof Player player) {
            if (player.level().isClientSide) return;

            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            if (baseCounts.isEmpty()) return;

            int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
            int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

            int snipersMonocleCount = baseCounts.getOrDefault(ModItems.RARE_SNIPERS_MONOCLE.get(), 0);
            if (snipersMonocleCount > 0) {
                SnipersMonocleAbility.onArrowFired(event, player, snipersMonocleCount, prideMultiplier);
            }

            int blademastersProwessCount = baseCounts.getOrDefault(ModItems.RARE_BLADEMASTERS_PROWESS.get(), 0);
            if (blademastersProwessCount > 0) {
                BlademastersProwessAbility.onArrowFired(event, blademastersProwessCount, prideMultiplier);
            }
        }
    }

    /**
     * 任務12：プレイヤーが体力を回復する瞬間の処理
     */
    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;


        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if (baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        if (player.getHealth() <= player.getMaxHealth() * 0.25f) {
            int lastStandCount = baseCounts.getOrDefault(ModItems.RARE_LAST_STAND.get(), 0);
            if (lastStandCount > 0) {
                LastStandAbility.onPlayerHeal(event, lastStandCount, prideMultiplier);
            }
        }
    }

    /**
     * 任務13：プレイヤーの攻撃がクリティカルヒットか判定される瞬間の処理
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onPlayerCriticalHit(CriticalHitEvent event) {
        Player player = event.getEntity();

        if (player.level().isClientSide) return;

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if (baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int deadeyeGlassCount = baseCounts.getOrDefault(ModItems.RARE_DEADEYE_GLASS.get(), 0);
        if (deadeyeGlassCount > 0) {
            DeadeyeGlassAbility.onCriticalHit(event, deadeyeGlassCount, prideMultiplier);

            if (event.isCriticalHit()) {
                CRITICAL_HIT_FLAG.add(player.getUUID());
                net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                        EventPriority.NORMAL, false, ServerTickEvent.Post.class,
                        serverTickEvent -> CRITICAL_HIT_FLAG.remove(player.getUUID())
                );
            }
        }
    }

    /**
     * 任務16：プレイヤーがブロックを右クリックした瞬間の処理
     */
    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos pos = event.getPos();

        if (level.isClientSide) return;

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if (baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int sewingCount = baseCounts.getOrDefault(ModItems.UNCOMMON_SECRET_ART_OF_SEWING.get(), 0);
        if (sewingCount > 0) {
            SecretArtOfSewingAbility.apply(event, sewingCount, prideMultiplier);
            if (event.isCanceled()) return;
        }

        if (level.getBlockState(pos).getBlock() instanceof ComposterBlock) {
            if (!ComposterBlock.COMPOSTABLES.containsKey(event.getItemStack().getItem())) return;

            int scentOfCompostCount = baseCounts.getOrDefault(ModItems.UNCOMMON_SCENT_OF_COMPOST.get(), 0);
            if (scentOfCompostCount > 0) {
                player.getServer().tell(new net.minecraft.server.TickTask(player.getServer().getTickCount() + 1, () ->
                        ScentOfCompostAbility.tryBoostComposter(
                                (ServerLevel) level, pos, scentOfCompostCount, prideMultiplier
                        )
                ));
            }
            return;
        }

        if (event.getItemStack().is(Items.BONE_MEAL)) {
            int fertileClodCount = baseCounts.getOrDefault(ModItems.UNCOMMON_FERTILE_CLOD.get(), 0);
            if (fertileClodCount > 0) {
                FertileClodAbility.tryExtraBonemeal((ServerLevel) level, pos, fertileClodCount, prideMultiplier);
            }
        }
    }

    /**
     * 任務18：プレイヤーがベッドから起きた瞬間の処理
     */
    @SubscribeEvent
    public static void onPlayerWakeUp(PlayerWakeUpEvent event) {
        Player player = event.getEntity();
        // サーバーサイドであり、かつワールドの時刻が朝になっている（＝夜を正常に過ごした）場合のみ処理
        if (player.level().isClientSide() || !player.level().isDay()) {
            return;
        }

        Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
        if (baseCounts.isEmpty()) return;

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        int healingLinensCount = baseCounts.getOrDefault(ModItems.UNCOMMON_HEALING_LINENS.get(), 0);
        if (healingLinensCount > 0) {
            HealingLinensAbility.apply(player, healingLinensCount, prideMultiplier);
        }
    }

    /**
     * 任務19(改)：サーバーのリソースがリロードされる際に、我々のリスナーを登録する
     */
    @SubscribeEvent
    public static void onAddReloadListener(AddReloadListenerEvent event) {
        event.addListener(new PackLootTableReloadListener());
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

    private static void handleBountyTotemAura(Player player, int totemCount, int prideMultiplier) {
        if (player.level().getGameTime() % 20 != 0) return; // 1秒に1回だけ処理

        int finalCount = totemCount * prideMultiplier;
        int radius = 5 + (finalCount - 1);

        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // 範囲内の全てのブロックをチェック
        BlockPos.betweenClosedStream(player.blockPosition().offset(-radius, -2, -radius), player.blockPosition().offset(radius, 2, radius))
                .forEach(pos -> {
                    BlockState state = serverLevel.getBlockState(pos);
                    // 対象が作物であり、かつ成長しきっていない場合
                    if (state.getBlock() instanceof CropBlock crop && !crop.isMaxAge(state)) {
                        // 50%の確率で追加の成長tickを発生させる
                        if (serverLevel.random.nextFloat() < (0.5f * finalCount)) {
                            // これはバニラの骨粉と同じ成長ロジック
                            crop.growCrops(serverLevel, pos, state);
                        }
                    }
                });
    }
}