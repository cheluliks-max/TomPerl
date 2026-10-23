package com.tompearl;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.minecraft.component.type.FoodComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpawnEggItem;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class TomPearlMod implements ModInitializer {
    public static final String MOD_ID = "tom_pearl";

    public static Item FIKALIYA;
    public static Item TOM_SPAWN_EGG;
    public static EntityType<TomPearlEntity> TOM_PEARL;
    public static RegistryEntry<StatusEffect> TOM_MODE;

    public static Identifier id(String path) {
        return Identifier.of(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Эффект «Режим Тома Пёрла»
        TOM_MODE = Registry.registerReference(Registries.STATUS_EFFECT, id("tom_mode"), new TomModeEffect());

        // Фикалия: еда, которую можно есть всегда
        FoodComponent food = new FoodComponent.Builder()
                .nutrition(4)
                .saturationModifier(0.6f)
                .alwaysEdible()
                .build();
        FIKALIYA = Registry.register(Registries.ITEM, id("fikaliya"),
                new FikaliyaItem(new Item.Settings().maxCount(64).food(food)));

        // Том Пёрл (моб)
        Identifier tomId = id("tom_pearl");
        TOM_PEARL = Registry.register(Registries.ENTITY_TYPE, tomId,
                EntityType.Builder.create(TomPearlEntity::new, SpawnGroup.CREATURE)
                        .dimensions(0.6f, 1.8f)
                        .build(tomId.toString()));
        FabricDefaultAttributeRegistry.register(TOM_PEARL, TomPearlEntity.createAttributes());

        TOM_SPAWN_EGG = Registry.register(Registries.ITEM, id("tom_pearl_spawn_egg"),
                new SpawnEggItem(TOM_PEARL, 0x6B4423, 0x3C7A3C, new Item.Settings()));

        // Вкладка в креативе
        ItemGroup group = FabricItemGroup.builder()
                .icon(() -> new ItemStack(FIKALIYA))
                .displayName(Text.translatable("itemGroup.tom_pearl"))
                .entries((ctx, entries) -> {
                    entries.add(FIKALIYA);
                    entries.add(TOM_SPAWN_EGG);
                })
                .build();
        Registry.register(Registries.ITEM_GROUP, id("main"), group);

        // Сеть: клиент нажал клавишу способности -> сервер выполняет
        PayloadTypeRegistry.playC2S().register(AbilityPayload.ID, AbilityPayload.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(AbilityPayload.ID,
                (payload, context) -> Abilities.use(context.player(), payload.abilityId()));
    }
}
