package traben.entity_texture_features.mixin.entity.misc;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.GlowSquidEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.features.ETFManager;

@Mixin(GlowSquidEntity.class)
public abstract class MixinGlowSquidEntity extends SquidEntity {


    @SuppressWarnings("unused")
    public MixinGlowSquidEntity(EntityType<? extends SquidEntity> entityType, World world) {
        super(entityType, world);
    }

    @ModifyArg(
            method = "tickMovement",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;addParticle(Lnet/minecraft/particle/ParticleEffect;DDDDDD)V"),
            index = 2
    )
    private double mixin(double x) {
        if (ETF.config().getConfig().canDoCustomTextures()
                && ETFManager.getInstance().ENTITY_TYPE_IGNORE_PARTICLES.contains(this.getType())) {
            return -500;
        }
        return x;
    }


}


