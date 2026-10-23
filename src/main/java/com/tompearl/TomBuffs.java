package com.tompearl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;

/** Супер-баффы, которые получает тот, кто съел фикалию (Том или игрок). */
public class TomBuffs {
    public static final int DURATION = 20 * 60; // 60 секунд

    public static void apply(LivingEntity e) {
        e.addStatusEffect(new StatusEffectInstance(TomPearlMod.TOM_MODE, DURATION, 0));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.STRENGTH, DURATION, 2));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.SPEED, DURATION, 2));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.RESISTANCE, DURATION, 1));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.REGENERATION, DURATION, 1));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.JUMP_BOOST, DURATION, 2));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.HASTE, DURATION, 2));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.FIRE_RESISTANCE, DURATION, 0));
        e.addStatusEffect(new StatusEffectInstance(StatusEffects.ABSORPTION, DURATION, 3));
    }
}
