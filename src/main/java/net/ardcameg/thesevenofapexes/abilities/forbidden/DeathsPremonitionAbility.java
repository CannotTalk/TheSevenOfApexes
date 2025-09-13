package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

public final class DeathsPremonitionAbility {
    private DeathsPremonitionAbility() {}

    private static final String TIMER_TAG = "DeathsPremonitionTimer";
    private static final String DYING_TAG = "DyingFromPremonition";
    private static final int DURATION_TICKS = 200; // 10 seconds

    /**
     * 「死の予感」のカウントダウンを処理する。
     * onPlayerTick から毎tick呼び出されることを想定。
     * @param player サーバーサイドのプレイヤー
     */
    public static void update(ServerPlayer player, boolean reversed) {
        boolean hasPremonition = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).is(ModItems.FORBIDDEN_DEATHS_PREMONITION.get())) {
                hasPremonition = true;
                break;
            }
        }

        // プレイヤーが生きている、かつ、死の予感を持っている場合のみ処理を続ける
        if (player.isAlive() && hasPremonition) {
            // タイマーがまだ始まっていない場合
            if (!player.getPersistentData().contains(TIMER_TAG)) {
                startCountdown(player, reversed);
            }
            // タイマーが既に作動中の場合
            else {
                tickCountdown(player, reversed);
            }
        }else { // プレイヤーが死んでいるか、アイテムを持っていない場合
            // もし何らかの理由でタイマーだけが残っていたら、ここで消去する
            if (player.getPersistentData().contains(TIMER_TAG)) {
                player.getPersistentData().remove(TIMER_TAG);
            }
        }
    }

    /**
     * 蘇生を無効化する必要があるかどうかをチェックする。
     * onPlayerDeath で呼び出される。
     * @return 蘇生を無効化すべきならtrue
     */
    public static boolean shouldCancelRevive(ServerPlayer player) {
        if (player.getPersistentData().getBoolean(DYING_TAG)) {
            player.getPersistentData().remove(DYING_TAG);
            return true;
        }
        return false;
    }

    private static void startCountdown(ServerPlayer player, boolean reversed) {
        player.getPersistentData().putInt(TIMER_TAG, DURATION_TICKS);

        CommandSourceStack source = player.getServer().createCommandSourceStack().withSuppressedOutput();
        player.getServer().getCommands().performPrefixedCommand(source, "title " + player.getName().getString() + " times 0 40 10");

        showTitle(player, 10, reversed);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void tickCountdown(ServerPlayer player, boolean reversed) {
        int ticksLeft = player.getPersistentData().getInt(TIMER_TAG);
        if (ticksLeft > 0) {
            if (ticksLeft % 20 == 0) {
                showTitle(player, ticksLeft / 20, reversed);
                if (!reversed) {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
                }else {
                    player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0f, 1.0f);
                }
            }
            player.getPersistentData().putInt(TIMER_TAG, ticksLeft - 1);
        } else {
            player.getPersistentData().remove(TIMER_TAG);
            player.getPersistentData().putBoolean(DYING_TAG, true);

            BuffItemUtils.consumeItemFromInventory(player, ModItems.FORBIDDEN_DEATHS_PREMONITION.get());

            if(!reversed) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(5).value(), SoundSource.PLAYERS, 1.0f, 2.0f);
                player.kill();
            }else {
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 0.5f);
                player.setHealth(player.getMaxHealth());
                player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 2, 127, false, false));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 2, false, true));
            }
        }
    }

    private static void showTitle(ServerPlayer player, int seconds, boolean reversed) {
        ChatFormatting textColor;
        if(!reversed){
            textColor = ChatFormatting.RED;
        }else {
            textColor = ChatFormatting.GREEN;
        }

        Component titleText = Component.literal(String.valueOf(seconds)).withStyle(textColor, ChatFormatting.BOLD);
        CommandSourceStack source = player.getServer().createCommandSourceStack().withSuppressedOutput();
        String jsonText = Component.Serializer.toJson(titleText, source.registryAccess());
        player.getServer().getCommands().performPrefixedCommand(source, "title " + player.getName().getString() + " title " + jsonText);
    }
}