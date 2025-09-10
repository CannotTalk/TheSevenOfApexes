package net.ardcameg.thesevenofapexes.item.component;

import com.mojang.serialization.Codec;
import net.ardcameg.thesevenofapexes.TheSevenOfApexes;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.UnaryOperator;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, TheSevenOfApexes.MOD_ID);

    public static final java.util.function.Supplier<DataComponentType<List<Float>>> STORED_DAMAGE = DATA_COMPONENTS.register("stored_damage", () ->
            DataComponentType.<List<Float>>builder()
                    .persistent(Codec.FLOAT.listOf())
                    .networkSynchronized(ByteBufCodecs.FLOAT.apply(ByteBufCodecs.list()))
                    .build()
    );

    // データ部品を不変にするためのヘルパー
    public static <T> UnaryOperator<T> copyAndCreate(java.util.function.Supplier<T> supplier) {
        return value -> supplier.get();
    }
}