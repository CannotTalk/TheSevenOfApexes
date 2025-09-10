package net.ardcameg.thesevenofapexes.abilities.forbidden;

import net.ardcameg.thesevenofapexes.item.ModItems;
import net.ardcameg.thesevenofapexes.util.BuffItemUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    public static void update(ServerPlayer player) {
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
                startCountdown(player);
            }
            // タイマーが既に作動中の場合
            else {
                tickCountdown(player);
            }
        }
        // プレイヤーが死んでいるか、アイテムを持っていない場合
        else {
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

    private static void startCountdown(ServerPlayer player) {
        player.getPersistentData().putInt(TIMER_TAG, DURATION_TICKS);

        CommandSourceStack source = player.getServer().createCommandSourceStack().withSuppressedOutput();
        player.getServer().getCommands().performPrefixedCommand(source, "title " + player.getName().getString() + " times 0 40 10");

        showTitle(player, 10);
        player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private static void tickCountdown(ServerPlayer player) {
        int ticksLeft = player.getPersistentData().getInt(TIMER_TAG);
        if (ticksLeft > 0) {
            if (ticksLeft % 20 == 0) {
                showTitle(player, ticksLeft / 20);
                player.level().playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            player.getPersistentData().putInt(TIMER_TAG, ticksLeft - 1);
        } else {
            player.getPersistentData().remove(TIMER_TAG);
            player.getPersistentData().putBoolean(DYING_TAG, true);

            BuffItemUtils.consumeItemFromInventory(player, ModItems.FORBIDDEN_DEATHS_PREMONITION.get());
            player.level().playSound(null, player.blockPosition(), SoundEvents.GOAT_HORN_SOUND_VARIANTS.get(5).value(), SoundSource.PLAYERS, 1.0f, 2.0f);

            player.kill();
        }
    }

    private static void showTitle(ServerPlayer player, int seconds) {
        Component titleText = Component.literal(String.valueOf(seconds)).withStyle(ChatFormatting.RED, ChatFormatting.BOLD);
        CommandSourceStack source = player.getServer().createCommandSourceStack().withSuppressedOutput();
        String jsonText = Component.Serializer.toJson(titleText, source.registryAccess());
        player.getServer().getCommands().performPrefixedCommand(source, "title " + player.getName().getString() + " title " + jsonText);
    }
}