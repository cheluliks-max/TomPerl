package com.tompearl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Способности игрока в «Режиме Тома Пёрла». 0 – рывок, 1 – ударная волна, 2 – вонючее облако. */
public class Abilities {
    private static final int[] COOLDOWN_TICKS = {60, 140, 220};
    private static final Map<UUID, long[]> NEXT_USE = new HashMap<>();

    public static void use(ServerPlayerEntity player, int id) {
        if (id < 0 || id > 2) return;
        if (!player.hasStatusEffect(TomPearlMod.TOM_MODE)) {
            player.sendMessage(Text.translatable("msg.tom_pearl.need_mode"), true);
            return;
        }
        ServerWorld world = player.getServerWorld();
        long now = world.getTime();
        long[] next = NEXT_USE.computeIfAbsent(player.getUuid(), k -> new long[3]);
        if (now < next[id]) {
            player.sendMessage(Text.translatable("msg.tom_pearl.cooldown"), true);
            return;
        }
        next[id] = now + COOLDOWN_TICKS[id];

        switch (id) {
            case 0 -> dash(player, world);
            case 1 -> wave(player, world);
            case 2 -> cloud(player, world);
        }
    }

    private static void dash(ServerPlayerEntity player, ServerWorld world) {
        Vec3d look = player.getRotationVec(1.0f);
        player.setVelocity(look.x * 2.2, Math.max(look.y * 1.2 + 0.2, 0.3), look.z * 2.2);
        player.velocityModified = true;
        player.fallDistance = 0;
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_KNOCKBACK, SoundCategory.PLAYERS, 1.0f, 0.8f);
        world.spawnParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.5, player.getZ(), 15, 0.3, 0.3, 0.3, 0.05);
    }

    private static void wave(ServerPlayerEntity player, ServerWorld world) {
        Box box = player.getBoundingBox().expand(6.0);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !(e instanceof TomPearlEntity));
        for (LivingEntity e : targets) {
            e.damage(world.getDamageSources().playerAttack(player), 10.0f);
            Vec3d dir = e.getPos().subtract(player.getPos()).normalize();
            e.addVelocity(dir.x * 1.5, 0.6, dir.z * 1.5);
            e.velocityModified = true;
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_ATTACK_STRONG, SoundCategory.PLAYERS, 1.5f, 0.5f);
        world.spawnParticles(ParticleTypes.EXPLOSION, player.getX(), player.getY() + 1.0, player.getZ(), 6, 2.0, 0.5, 2.0, 0.0);
    }

    private static void cloud(ServerPlayerEntity player, ServerWorld world) {
        Box box = player.getBoundingBox().expand(8.0);
        List<LivingEntity> targets = world.getEntitiesByClass(LivingEntity.class, box,
                e -> e != player && e.isAlive() && !(e instanceof TomPearlEntity));
        for (LivingEntity e : targets) {
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.NAUSEA, 200, 0));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.POISON, 100, 1));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.WEAKNESS, 200, 1));
            e.addStatusEffect(new StatusEffectInstance(StatusEffects.SLOWNESS, 100, 1));
        }
        world.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENTITY_PLAYER_BURP, SoundCategory.PLAYERS, 1.5f, 0.6f);
        world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, player.getX(), player.getY() + 1.0, player.getZ(), 40, 3.0, 0.5, 3.0, 0.02);
    }
}
