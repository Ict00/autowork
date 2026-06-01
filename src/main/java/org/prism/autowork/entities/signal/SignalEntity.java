package org.prism.autowork.entities.signal;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.prism.autowork.CommonConfig;
import org.prism.autowork.block.ModBlocks;
import org.prism.autowork.block.transmitter.TransmitterBlock;
import org.prism.autowork.entities.ModEntities;
import org.prism.autowork.other.ModOther;
import org.prism.autowork.particles.ModParticles;

public class SignalEntity extends AbstractHurtingProjectile {
    public static final EntityDataAccessor<Integer> LIVED =
            SynchedEntityData.defineId(SignalEntity.class, EntityDataSerializers.INT);

    public final AnimationState spinAnimationState = new AnimationState();

    public SignalEntity(double x, double y, double z, Vec3 movement, Level level) {
        super(ModEntities.SIGNAL_ENTITY.get(), x, y, z, movement, level);
        super.accelerationPower = 0.01f;
    }

    public SignalEntity(EntityType<? extends AbstractHurtingProjectile> entityType, double x, double y, double z, Vec3 movement, Level level) {
        super(ModEntities.SIGNAL_ENTITY.get(), x, y, z, movement, level);
        super.accelerationPower = 0.01f;
    }

    public SignalEntity(EntityType<SignalEntity> signalEntityEntityType, Level level) {
        super(signalEntityEntityType, level);
        super.accelerationPower = 0.01f;
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return true;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        doDiscard();
    }

    public void doDiscard() {
        if (!this.level().isClientSide()) {
            ((ServerLevel)level()).sendParticles(ModParticles.SIGNAL_PARTICLES.get(), getX(), getY(), getZ(), 10, 0.5, 0.5, 0.5, 0.5);
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        var l = level();
        var state = l.getBlockState(result.getBlockPos());

        if (state.is(ModOther.GLASSES) ||
            state.canBeReplaced() || state.isAir()) {
            return;
        }
        else if (state.is(ModBlocks.TRANSMITTER)) {
            if (state.getValue(TransmitterBlock.FACING).equals(result.getDirection())) {
                this.level().playSound(null, result.getBlockPos(), SoundEvents.VAULT_CLOSE_SHUTTER, SoundSource.BLOCKS, 1, 0.5f);

                l.setBlockAndUpdate(result.getBlockPos(), state.setValue(TransmitterBlock.POWER_STATE, 1));
            }
        }
        else {
            super.onHitBlock(result);
        }
        doDiscard();
    }

    @Override
    protected @Nullable ParticleOptions getTrailParticle() {
        return ModParticles.SIGNAL_PARTICLES.get();
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public void onAddedToLevel() {
        super.onAddedToLevel();

        this.spinAnimationState.startIfStopped(this.tickCount);
    }

    @Override
    public void tick() {
        super.tick();

        var lv = getEntityData().get(LIVED);
        lv++;

        if (this.level().isClientSide()) {
            this.spinAnimationState.startIfStopped(this.tickCount);
        }

        if (lv > CommonConfig.TRANSMITTER_SIGNAL_LIFETIME.getAsInt()) {
            doDiscard();
        }

        getEntityData().set(LIVED, lv);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(LIVED, 0);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("lived", getEntityData().get(LIVED));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        getEntityData().set(LIVED, compound.getInt("lived"));
    }
}
