package net.ardcameg.thesevenofapexes.event;

import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.ardcameg.thesevenofapexes.abilities.epic.*;
import net.ardcameg.thesevenofapexes.abilities.legendary.*;
import net.ardcameg.thesevenofapexes.abilities.rare.*;
import net.ardcameg.thesevenofapexes.abilities.uncommon.*;
import net.ardcameg.thesevenofapexes.abilities.util.StunAbility;
import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.networking.ModMessages;
import net.ardcameg.thesevenofapexes.networking.packet.PhoenixDebuffSyncS2CPacket;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
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

        if (baseCounts.containsKey(ModItems.LEGENDARY_WRATH.get()) && attacker != null) {
            WrathAbility.apply(player, attacker, baseCounts.get(ModItems.LEGENDARY_WRATH.get()), prideMultiplier);
        }
        if (baseCounts.containsKey(ModItems.EPIC_REVERSAL_HOURGLASS.get()) && attacker != null) {
            ReversalHourglassAbility.onPlayerDamaged(event, player, attacker, baseCounts.get(ModItems.EPIC_REVERSAL_HOURGLASS.get()), prideMultiplier);
        }
        if (baseCounts.containsKey(ModItems.EPIC_LIGHTNING_FIST.get())) {
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

            if (baseCounts.containsKey(ModItems.LEGENDARY_ENVY.get())) {
                EnvyAbility.apply(player, livingTarget, ENVY_COPIED_ENTITIES, baseCounts.get(ModItems.LEGENDARY_ENVY.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.EPIC_FIENDS_BARGAIN.get())) {
                FiendsBargainAbility.apply(event, player, livingTarget, baseCounts.get(ModItems.EPIC_FIENDS_BARGAIN.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.RARE_DEADEYE_GLASS.get())) {
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

            if (baseCounts.containsKey(ModItems.UNCOMMON_PEARL_EYE.get())) {
                PearlEyeAbility.reduceEnderPearlDamage(event, baseCounts.get(ModItems.UNCOMMON_PEARL_EYE.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.RARE_HUNGRY_BANQUET.get())) {
                HungryBanquetAbility.convertDamageToHunger(event, player, baseCounts.get(ModItems.RARE_HUNGRY_BANQUET.get()), prideMultiplier);
                if (event.getNewDamage() <= 0) return;
            }
            if (baseCounts.containsKey(ModItems.EPIC_SCARRED_GRAIL.get())) {
                ScarredGrailAbility.onPlayerDamaged(event, player);
                if (event.getNewDamage() == 0) return;
            }
            if (baseCounts.containsKey(ModItems.EPIC_BERSERKERS_DRAG.get())) {
                BerserkersDragAbility.onPlayerDamaged(event, player, baseCounts.get(ModItems.EPIC_BERSERKERS_DRAG.get()), prideMultiplier);
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
        SunlightSacredSealAbility.updateEffect(player, sunSealCount, prideMultiplier, sunSealCount > 0);
        MoonlightSacredSealAbility.updateEffect(player, moonSealCount, prideMultiplier, moonSealCount > 0, PLAYER_LAST_POSITION);

        LifeSteelStickAbility.updatePassiveDebuff(player, baseCounts.getOrDefault(ModItems.EPIC_LIFE_STEEL_STICK.get(), 0));
        BerserkersDragAbility.updateEffect(player, baseCounts.getOrDefault(ModItems.EPIC_BERSERKERS_DRAG.get(), 0), prideMultiplier);
        WalkingAnathemaAbility.applyAura(player, baseCounts.getOrDefault(ModItems.EPIC_WALKING_ANATHEMA.get(), 0), prideMultiplier);
        VoidMantleAbility.updateEffect(player, baseCounts.getOrDefault(ModItems.EPIC_VOID_MANTLE.get(), 0), prideMultiplier);
        ReversalHourglassAbility.updatePassiveDebuff(player, baseCounts.getOrDefault(ModItems.EPIC_REVERSAL_HOURGLASS.get(), 0));
        GoliathsGavelAbility.updatePassiveEffect(player, baseCounts.getOrDefault(ModItems.EPIC_GOLIATHS_GAVEL.get(), 0), prideMultiplier);

        NightOwlEyesAbility.updateEffect(player, baseCounts.getOrDefault(ModItems.RARE_NIGHT_OWL_EYES.get(), 0), prideMultiplier);
        GuardiansCrestAbility.updateEffect(player, baseCounts.getOrDefault(ModItems.RARE_GUARDIANS_CREST.get(), 0), prideMultiplier);
        GillsCharmAbility.updateEffect(player, baseCounts.getOrDefault(ModItems.RARE_GILLS_CHARM.get(), 0), prideMultiplier);
        SnipersMonocleAbility.updatePassiveDebuff(player, baseCounts.getOrDefault(ModItems.RARE_SNIPERS_MONOCLE.get(), 0), prideMultiplier);
        LastStandAbility.updatePassiveBuffs(player, baseCounts.getOrDefault(ModItems.RARE_LAST_STAND.get(), 0), prideMultiplier);
        BlademastersProwessAbility.updatePassiveBuffs(player, baseCounts.getOrDefault(ModItems.RARE_BLADEMASTERS_PROWESS.get(), 0), prideMultiplier);
        DeadeyeGlassAbility.updatePassiveBuffs(player, baseCounts.getOrDefault(ModItems.RARE_DEADEYE_GLASS.get(), 0), prideMultiplier);
        ArchitectsHasteAbility.updatePassiveBuffs(player, baseCounts.getOrDefault(ModItems.RARE_ARCHITECTS_HASTE.get(), 0), prideMultiplier);

        int ringCount = baseCounts.getOrDefault(ModItems.RARE_VITAL_CONVERSION_RING.get(), 0);
        if (ringCount > 0) {
            VitalConversionRingAbility.applyHealthRecovery(player, ringCount, prideMultiplier);
            VitalConversionRingAbility.rejectNonMeatFood(player);
        }

        int totemCount = baseCounts.getOrDefault(ModItems.RARE_BOUNTY_TOTEM.get(), 0);
        if (totemCount > 0) {
            handleBountyTotemAura(player, totemCount, prideMultiplier);
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

        if (baseCounts.containsKey(ModItems.EPIC_WALKING_ANATHEMA.get())) {
            if (event.getEntity() instanceof Enemy || event.getEntity() instanceof net.minecraft.world.entity.NeutralMob) {
                event.getDrops().clear();
                event.setCanceled(true);
            }
        }

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);
        if (baseCounts.containsKey(ModItems.LEGENDARY_GREED.get())) {
            GreedAbility.applyToMob(event, baseCounts.get(ModItems.LEGENDARY_GREED.get()), prideMultiplier);
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

        if (baseCounts.containsKey(ModItems.LEGENDARY_GREED.get())) {
            GreedAbility.applyToBlock(event, baseCounts.get(ModItems.LEGENDARY_GREED.get()), prideMultiplier);
        }
        if (baseCounts.containsKey(ModItems.RARE_ARCHITECTS_HASTE.get())) {
            ArchitectsHasteAbility.applyDurabilityPenalty(player, baseCounts.get(ModItems.RARE_ARCHITECTS_HASTE.get()), prideMultiplier);
        }
        if (event.getState().getBlock() instanceof CropBlock) {
            if (baseCounts.containsKey(ModItems.RARE_BOUNTY_TOTEM.get())) {
                BountyTotemAbility.degradeFarmland(event, baseCounts.get(ModItems.RARE_BOUNTY_TOTEM.get()), prideMultiplier);
            }
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

        if (player.getPersistentData().getBoolean("TAKING_GRAIL_DAMAGE")) {
            if (GRAIL_DAMAGE_FLAG.contains(player.getUUID())) return;
            GRAIL_DAMAGE_FLAG.add(player.getUUID());
        }

        int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
        int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);

        if (baseCounts.containsKey(ModItems.LEGENDARY_LUST.get())) {
            if (LustAbility.attemptRevive(player, event.getSource().getEntity(), baseCounts.get(ModItems.LEGENDARY_LUST.get()), prideMultiplier)) {
                event.setCanceled(true);
                return;
            }
        }
        if (baseCounts.containsKey(ModItems.EPIC_PHOENIX_FEATHER.get())) {
            if (PhoenixFeatherAbility.attemptRevive(player)) {
                event.setCanceled(true);
                return;
            }
        }
        if (baseCounts.containsKey(ModItems.EPIC_ARRIVAL_OF_REVIVAL.get())) {
            if (ArrivalOfRevivalAbility.attemptRevive(player, baseCounts.get(ModItems.EPIC_ARRIVAL_OF_REVIVAL.get()), prideMultiplier)) {
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

            if (baseCounts.containsKey(ModItems.EPIC_LIGHTNING_FIST.get()) && target instanceof Enemy) {
                LightningFistAbility.applyAttackEffect(player, target, event.getNewDamage(), baseCounts.get(ModItems.EPIC_LIGHTNING_FIST.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.EPIC_LIFE_STEEL_STICK.get())) {
                LifeSteelStickAbility.applyLifeSteal(player, target, baseCounts.get(ModItems.EPIC_LIFE_STEEL_STICK.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.EPIC_SHADOW_BIND_GLOVES.get())) {
                ShadowBindGlovesAbility.apply(player, target, baseCounts.get(ModItems.EPIC_SHADOW_BIND_GLOVES.get()), prideMultiplier, STUNNED_ENTITIES);
            }
            if (baseCounts.containsKey(ModItems.EPIC_STEEL_CLAWS.get())) {
                SteelClawsAbility.apply(player, target, baseCounts.get(ModItems.EPIC_STEEL_CLAWS.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.EPIC_GOLIATHS_GAVEL.get())) {
                GoliathsGavelAbility.applyAreaDamage(player, target, baseCounts.get(ModItems.EPIC_GOLIATHS_GAVEL.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.RARE_REAPERS_SCYTHE.get())) {
                ReapersScytheAbility.applyWitherOnAttack(target, baseCounts.get(ModItems.RARE_REAPERS_SCYTHE.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.RARE_ARCHITECTS_HASTE.get())) {
                ArchitectsHasteAbility.applyDurabilityPenalty(player, baseCounts.get(ModItems.RARE_ARCHITECTS_HASTE.get()), prideMultiplier);
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

            if (baseCounts.containsKey(ModItems.RARE_SNIPERS_MONOCLE.get())) {
                SnipersMonocleAbility.onArrowFired(event, player, baseCounts.get(ModItems.RARE_SNIPERS_MONOCLE.get()), prideMultiplier);
            }
            if (baseCounts.containsKey(ModItems.RARE_BLADEMASTERS_PROWESS.get())) {
                BlademastersProwessAbility.onArrowFired(event, baseCounts.get(ModItems.RARE_BLADEMASTERS_PROWESS.get()), prideMultiplier);
            }
        }
    }

    /**
     * 任務12：プレイヤーが体力を回復する瞬間の処理
     */
    @SubscribeEvent
    public static void onPlayerHeal(LivingHealEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        if (player.getHealth() <= player.getMaxHealth() * 0.25f) {
            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            if (baseCounts.containsKey(ModItems.RARE_LAST_STAND.get())) {
                int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
                int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);
                LastStandAbility.onPlayerHeal(event, baseCounts.get(ModItems.RARE_LAST_STAND.get()), prideMultiplier);
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
        if (baseCounts.containsKey(ModItems.RARE_DEADEYE_GLASS.get())) {
            int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
            int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);
            DeadeyeGlassAbility.onCriticalHit(event, baseCounts.get(ModItems.RARE_DEADEYE_GLASS.get()), prideMultiplier);

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

        if (level.getBlockState(pos).getBlock() instanceof ComposterBlock) {
            if (!ComposterBlock.COMPOSTABLES.containsKey(event.getItemStack().getItem())) return;
            if (player.level().isClientSide) return;
            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            if (baseCounts.containsKey(ModItems.UNCOMMON_SCENT_OF_COMPOST.get())) {
                int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
                int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);
                player.getServer().tell(new net.minecraft.server.TickTask(player.getServer().getTickCount() + 1, () ->
                        ScentOfCompostAbility.tryBoostComposter(
                                (ServerLevel) level, pos, baseCounts.get(ModItems.UNCOMMON_SCENT_OF_COMPOST.get()), prideMultiplier
                        )
                ));
            }
            return;
        }

        if (event.getItemStack().is(Items.BONE_MEAL)) {
            if (level.isClientSide) return;
            Map<Item, Integer> baseCounts = BuffItemUtils.countAllItemsInBuffRow(player);
            if (baseCounts.containsKey(ModItems.UNCOMMON_FERTILE_CLOD.get())) {
                int prideCount = baseCounts.getOrDefault(ModItems.LEGENDARY_PRIDE.get(), 0);
                int prideMultiplier = PrideAbility.calculateEffectMultiplier(prideCount);
                FertileClodAbility.tryExtraBonemeal((ServerLevel) level, pos, baseCounts.get(ModItems.UNCOMMON_FERTILE_CLOD.get()), prideMultiplier);
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