package traben.entity_texture_features.mixin.mods.sodium;

import me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer;
import me.jellysquid.mods.sodium.client.render.vertex.VertexConsumerUtils;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.mixin.MixinModelPart;
import traben.entity_texture_features.utils.ETFUtils2;
import traben.entity_texture_features.utils.ETFVertexConsumer;

/**
 * this is a copy of {@link MixinModelPart} but for sodium's alternative model part render method
 * <p>
 * this should have no negative impact on sodium's render process, other than of course adding more code that needs to run
 */
@Pseudo
@Mixin(value = EntityRenderer.class)
public abstract class MixinModelPartSodium {

    @SuppressWarnings("EmptyMethod")
    @Shadow
    public static void render(MatrixStack matrixStack, VertexBufferWriter writer, ModelPart part, int light, int overlay, int color) {
    }

    @Inject(method = "render",
            at = @At(value = "HEAD"))
    private static void etf$findOutIfInitialModelPart(MatrixStack matrixStack, VertexBufferWriter writer, ModelPart part, int light, int overlay, int color, CallbackInfo ci) {
        ETFRenderContext.incrementCurrentModelPartDepth();
    }

    @Inject(method = "render",
            at = @At(value = "RETURN"))
    private static void etf$doEmissiveIfInitialPart(MatrixStack matrixStack, VertexBufferWriter writer, ModelPart part, int light, int overlay, int color, CallbackInfo ci) {
        //run code if this is the initial topmost rendered part
        if (ETFRenderContext.getCurrentModelPartDepth() != 1) {
            ETFRenderContext.decrementCurrentModelPartDepth();
        } else {
            if (ETFRenderContext.isCurrentlyRenderingEntity()
                    && writer instanceof ETFVertexConsumer etfVertexConsumer) {
                ETFTexture texture = etfVertexConsumer.etf$getETFTexture();
                if (texture != null && (texture.isEmissive() || texture.isEnchanted())) {
                    VertexConsumerProvider provider = etfVertexConsumer.etf$getProvider();
                    RenderLayer layer = etfVertexConsumer.etf$getRenderLayer();
                    if (provider != null && layer != null) {
                        //attempt special renders as eager OR checks
                        ETFUtils2.RenderMethodForOverlay renderer = (a, b) -> {
                            VertexBufferWriter a2 = VertexConsumerUtils.convertOrLog(a);
                            if (a2 == null) {
                                return;
                            }
                            render(matrixStack, a2, part, b, overlay, color);
                        };
                        if (ETFUtils2.renderEmissive(texture, provider, renderer) |
                                ETFUtils2.renderEnchanted(texture, provider, light, renderer)) {
                            //reset render layer stuff behind the scenes if special renders occurred
                            provider.getBuffer(layer);
                        }
                    }
                }
            }
            //ensure model count is reset
            ETFRenderContext.resetCurrentModelPartDepth();
        }
    }


}
