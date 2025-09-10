package net.ardcameg.thesevenofapexes.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public class KeyBinding {
    public static final String KEY_CATEGORY_SEVEN_APEXES = "key.category.seven_apexes";
    public static final String KEY_EAT_BLOCK = "key.seven_apexes.eat_block";
    public static final String KEY_SOUL_RELEASE = "key.seven_apexes.soul_release";

    public static final KeyMapping EAT_BLOCK_KEY = new KeyMapping(
            KEY_EAT_BLOCK,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_G, // デフォルトのキーを 'G' キーに設定
            KEY_CATEGORY_SEVEN_APEXES
    );

    public static final KeyMapping SOUL_RELEASE_KEY = new KeyMapping(
            KEY_SOUL_RELEASE,
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_K, // デフォルトのキーを 'K' キーに設定
            KEY_CATEGORY_SEVEN_APEXES
    );
}