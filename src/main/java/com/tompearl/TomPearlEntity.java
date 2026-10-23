package com.tompearl;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.ai.goal.ActiveTargetGoal;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.ai.goal.WanderAroundFarGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;

public class TomPearlEntity extends PathAwareEntity {
    public TomPearlEntity(EntityType<? extends PathAwareEntity> type, World world) {
        super(type, world);
    }

    public static DefaultAttributeContainer.Builder createAttributes() {
        return MobEntity.createMobAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 40.0)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 4.0)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 24.0);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(0, new SwimGoal(this));
        this.goalSelector.add(1, new EatFikaliyaGoal(this));
        this.goalSelector.add(2, new MeleeAttackGoal(this, 1.2, true));
        this.goalSelector.add(5, new WanderAroundFarGoal(this, 0.8));
        this.goalSelector.add(6, new LookAtEntityGoal(this, PlayerEntity.class, 8.0f));
        this.goalSelector.add(7, new LookAroundGoal(this));
        this.targetSelector.add(1, new ActiveTargetGoal<>(this, HostileEntity.class, true));
    }

    /** Накормить Тома: правый клик по нему фикалией. */
    @Override
    public ActionResult interactMob(PlayerEntity player, Hand hand) {
        ItemStack stack = player.getStackInHand(hand);
        if (stack.isOf(TomPearlMod.FIKALIYA)) {
            if (!this.getWorld().isClient) {
                if (!player.isCreative()) stack.decrement(1);
                eatAndPowerUp();
            }
            return ActionResult.success(this.getWorld().isClient);
        }
        return super.interactMob(player, hand);
    }

    public void eatAndPowerUp() {
        if (this.getWorld() instanceof ServerWorld sw) {
            this.playSound(SoundEvents.ENTITY_GENERIC_EAT, 1.0f, 0.7f);
            sw.spawnParticles(ParticleTypes.ANGRY_VILLAGER, getX(), getEyeY(), getZ(), 8, 0.3, 0.3, 0.3, 0.02);
            TomBuffs.apply(this);
        }
    }

    @Override
    public boolean canImmediatelyDespawn(double distanceSquared) {
        return false;
    }

    /** Том сам ищет выброшенные фикалии рядом и съедает их. */
    static class EatFikaliyaGoal extends Goal {
        private final TomPearlEntity tom;
        private ItemEntity target;

        EatFikaliyaGoal(TomPearlEntity tom) {
            this.tom = tom;
            this.setControls(EnumSet.of(Control.MOVE, Control.LOOK));
        }

        private ItemEntity findNearest() {
            List<ItemEntity> list = tom.getWorld().getEntitiesByClass(ItemEntity.class,
                    tom.getBoundingBox().expand(16.0),
                    e -> e.isAlive() && e.getStack().isOf(TomPearlMod.FIKALIYA));
            return list.stream().min(Comparator.comparingDouble(tom::squaredDistanceTo)).orElse(null);
        }

        @Override
        public boolean canStart() {
            target = findNearest();
            return target != null;
        }

        @Override
        public boolean shouldContinue() {
            return target != null && target.isAlive();
        }

        @Override
        public void start() {
            tom.getNavigation().startMovingTo(target, 1.2);
        }

        @Override
        public void stop() {
            target = null;
            tom.getNavigation().stop();
        }

        @Override
        public void tick() {
            if (target == null || !target.isAlive()) return;
            tom.getLookControl().lookAt(target, 30.0f, 30.0f);
            if (tom.squaredDistanceTo(target) < 2.5) {
                ItemStack stack = target.getStack();
                stack.decrement(1);
                if (stack.isEmpty()) target.discard();
                tom.eatAndPowerUp();
                target = null;
            } else if (tom.age % 10 == 0) {
                tom.getNavigation().startMovingTo(target, 1.2);
            }
        }
    }
}
