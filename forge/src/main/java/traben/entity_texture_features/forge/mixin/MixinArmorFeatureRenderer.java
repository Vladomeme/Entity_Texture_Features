package traben.entity_texture_features.forge.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.Model;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.trim.ArmorTrim;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.config.screens.skin.ETFScreenOldCompat;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.ETFRenderContext;
import traben.entity_texture_features.features.texture_handlers.ETFTexture;
import traben.entity_texture_features.utils.ETFVertexConsumer;

import java.util.Objects;


@Mixin(ArmorFeatureRenderer.class)
public abstract class MixinArmorFeatureRenderer<T extends LivingEntity, M extends BipedEntityModel<T>> extends FeatureRenderer<T, M> {
    @Unique
    private ETFTexture thisETF$Texture = null;

    @Unique
    private ETFTexture thisETF$TrimTexture = null;

    @SuppressWarnings("unused")
    public MixinArmorFeatureRenderer(FeatureRendererContext<T, M> context) {
        super(context);
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "HEAD"))
    private void etf$markNotToChange(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        ETFRenderContext.preventRenderLayerTextureModify();
        ETFRenderContext.allowTexturePatching();
    }

    @Inject(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "RETURN"))
    private void etf$markAllowedToChange(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        ETFRenderContext.allowRenderLayerTextureModify();
        ETFRenderContext.preventTexturePatching();
    }

    @ModifyArg(method = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderModel(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/util/Identifier;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/RenderLayer;getArmorCutoutNoCull(Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/RenderLayer;"))
    private Identifier etf$changeTexture(Identifier texture) {
        if(ETF.config().getConfig().enableArmorAndTrims) {
            thisETF$Texture = ETFManager.getInstance().getETFTextureNoVariation(texture);
            //noinspection ConstantConditions
            if (thisETF$Texture != null) {
                thisETF$Texture.reRegisterBaseTexture();
                return thisETF$Texture.getTextureIdentifier(null);
            }
        }
        return texture;
    }

    @Inject(method = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderModel(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/ArmorItem;Lnet/minecraft/client/model/Model;ZFFFLnet/minecraft/util/Identifier;)V",
            at = @At(value = "TAIL"))
    private void etf$applyEmissive(MatrixStack arg, VertexConsumerProvider arg2, int i, ArmorItem arg3, Model arg4, boolean bl, float f, float g, float h, Identifier armorResource, CallbackInfo ci) {
        //UUID id = livingEntity.getUuid();
        if (thisETF$Texture != null && ETF.config().getConfig().canDoEmissiveTextures()) {
            Identifier emissive = thisETF$Texture.getEmissiveIdentifierOfCurrentState();
            if (emissive != null) {
                VertexConsumer textureVert;// = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getBeaconBeam(PATH_EMISSIVE_TEXTURE_IDENTIFIER.get(fileString), true), false, usesSecondLayer);
                //if (ETFManager.getEmissiveMode() == ETFManager.EmissiveRenderModes.BRIGHT) {
                //    textureVert = ItemRenderer.getArmorGlintConsumer(vertexConsumers, RenderLayer.getBeaconBeam(emissive, true), false, usesSecondLayer);
                //} else {
                textureVert = arg2.getBuffer(RenderLayer.getArmorCutoutNoCull(emissive));
                //}
                ETFRenderContext.startSpecialRenderOverlayPhase();
                arg4.render(arg, textureVert, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.DEFAULT_UV, f, g, h, 1.0F);
                ETFRenderContext.endSpecialRenderOverlayPhase();
            }
        }

    }

    @Inject(method = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/entity/LivingEntity;FFFFFF)V",
            at = @At(value = "HEAD"), cancellable = true)
    private void etf$cancelIfUi(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, T livingEntity, float f, float g, float h, float j, float k, float l, CallbackInfo ci) {
        if (MinecraftClient.getInstance() != null) {
            if (MinecraftClient.getInstance().currentScreen instanceof ETFScreenOldCompat) {
                //cancel armour rendering
                ci.cancel();
            }
        }
    }



    @Inject(method = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderTrim(Lnet/minecraft/item/ArmorMaterial;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/trim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(value = "HEAD"))
    private void etf$trimGet(ArmorMaterial material, MatrixStack arg2, VertexConsumerProvider vertexConsumers, int i, ArmorTrim trim, Model arg5, boolean leggings, CallbackInfo ci) {

        if(ETF.config().getConfig().enableArmorAndTrims) {
            Identifier trimBaseId = leggings ? trim.getLeggingsModelId(material) : trim.getGenericModelId(material);
            //support modded trims with namespace
            Identifier trimMaterialIdentifier = new Identifier(trimBaseId.getNamespace(), "textures/" + trimBaseId.getPath() + ".png");
            thisETF$TrimTexture = ETFManager.getInstance().getETFTextureNoVariation(trimMaterialIdentifier);


            //if it is emmissive we need to create an identifier of the trim to render separately in iris
            if (!thisETF$TrimTexture.exists()
                    && ETF.config().getConfig().canDoEmissiveTextures()
                    && thisETF$TrimTexture.isEmissive()
                    && ETF.IRIS_DETECTED) {
                thisETF$TrimTexture.buildTrimTexture(trim, leggings);
            }
        }

    }

    @ModifyArg(method = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderTrim(Lnet/minecraft/item/ArmorMaterial;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/trim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/client/model/Model;render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"),
            index = 1)
    private VertexConsumer etf$changeTrim(VertexConsumer par2) {
        //allow a specified override trim texture if you dont want to be confined by a pallette
        if(thisETF$TrimTexture!= null
                && par2 instanceof ETFVertexConsumer etfVertexConsumer
                && etfVertexConsumer.etf$getProvider() != null){
            if(thisETF$TrimTexture.exists()){
                return Objects.requireNonNull(etfVertexConsumer.etf$getProvider()).getBuffer(RenderLayer.getArmorCutoutNoCull(thisETF$TrimTexture.getTextureIdentifier(null)));
            }else if (ETF.config().getConfig().canDoEmissiveTextures() && thisETF$TrimTexture.isEmissive() && ETF.IRIS_DETECTED){
                //iris is weird and will always render the armor trim atlas over everything else
                // if for some reason no trim texture is present then just dont render it at all
                // this is to favour packs with fully emissive trims :/
                return Objects.requireNonNull(etfVertexConsumer.etf$getProvider()).getBuffer(RenderLayer.getArmorCutoutNoCull(ETFManager.getErrorETFTexture().thisIdentifier));
            }
        }
        return par2;
    }

    @Inject(method = "Lnet/minecraft/client/render/entity/feature/ArmorFeatureRenderer;renderTrim(Lnet/minecraft/item/ArmorMaterial;Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumerProvider;ILnet/minecraft/item/trim/ArmorTrim;Lnet/minecraft/client/model/Model;Z)V",
            at = @At(value = "TAIL"))
    private void etf$trimEmissive(ArmorMaterial arg, MatrixStack arg2, VertexConsumerProvider arg3, int i, ArmorTrim arg4, Model arg5, boolean bl, CallbackInfo ci) {
        if(ETF.config().getConfig().canDoEmissiveTextures() && thisETF$TrimTexture != null){
            //trimTexture.renderEmissive(matrices,vertexConsumers,model);
            Identifier emissive = thisETF$TrimTexture.getEmissiveIdentifierOfCurrentState();
            if (emissive != null) {
                VertexConsumer textureVert= arg3.getBuffer(RenderLayer.getArmorCutoutNoCull(emissive));
                ETFRenderContext.startSpecialRenderOverlayPhase();
                arg5.render(arg2, textureVert, ETF.EMISSIVE_FEATURE_LIGHT_VALUE, OverlayTexture.DEFAULT_UV, 1, 1, 1, 1.0F);
                ETFRenderContext.endSpecialRenderOverlayPhase();
            }
        }
    }

}


