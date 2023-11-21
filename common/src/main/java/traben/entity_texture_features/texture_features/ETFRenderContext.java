package traben.entity_texture_features.texture_features;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.SpriteTexturedVertexConsumer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.Identifier;
import traben.entity_texture_features.texture_features.texture_handlers.ETFTexture;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Optional;
import java.util.function.Function;

public class ETFRenderContext {

//    private static Object currentTopPart = null;

    private static VertexConsumerProvider currentProvider = null;

    public static boolean isRenderingFeatures() {
        return renderingFeatures;
    }

    public static void setRenderingFeatures(boolean renderingFeatures) {
        ETFRenderContext.renderingFeatures = renderingFeatures;
    }

    public static boolean renderingFeatures = false;

    public static boolean isAllowedToRenderLayerTextureModify() {
        return allowRenderLayerTextureModify;
    }

    public static void preventRenderLayerTextureModify() {
        ETFRenderContext.allowRenderLayerTextureModify = false;
    }
    public static void allowRenderLayerTextureModify() {
        ETFRenderContext.allowRenderLayerTextureModify = true;
    }
    private static boolean allowRenderLayerTextureModify = true;

    public static void setCurrentRenderLayer(RenderLayer currentRenderLayer) {
        ETFRenderContext.currentRenderLayer = currentRenderLayer;
    }

    private static RenderLayer currentRenderLayer = null;

    public static ETFTexture getCurrentETFTexture() {
        return currentETFTexture;
    }

    public static void setCurrentETFTexture(ETFTexture currentETFTexture) {
        ETFRenderContext.currentETFTexture = currentETFTexture;
    }

    private static ETFTexture currentETFTexture = null;

//    public static void setCurrentTopPart(Object currentTopPart) {
//        ETFRenderContext.currentTopPart = currentTopPart;
//    }

    public static void setCurrentProvider(VertexConsumerProvider currentProvider) {
        ETFRenderContext.currentProvider = currentProvider;
    }

    public static VertexConsumer processVertexConsumer(VertexConsumerProvider provider, RenderLayer renderLayer) {
        currentETFTexture = null;
        ETFRenderContext.currentRenderLayer = renderLayer;
        //sprites will give the atlas id if not handled separately, and the only hook in seems to be the consumer
        if(currentRenderLayer instanceof RenderLayer.MultiPhase multiPhase){//
//            RenderLayer.MultiPhaseParameters params = multiPhase.phases;
//            RenderPhase.TextureBase base = params.texture;
            Optional<Identifier> possibleId = multiPhase.phases.texture.getId();
            // ETFManager.getInstance().getETFTexture(texture,ETFRenderContext.getCurrentEntity(), ETFManager.TextureSource.ENTITY,false);
            possibleId.ifPresent(identifier -> currentETFTexture = ETFManager.getInstance().getETFDefaultTexture(identifier, false));

            //modify render layer if needed
            if(!multiPhase.isOutline() && getCurrentEntity() != null && ETFManager.getInstance().ENTITY_TYPE_RENDER_LAYER.containsKey(getCurrentEntity().etf$getType())){
                preventRenderLayerTextureModify();
                switch (ETFManager.getInstance().ENTITY_TYPE_RENDER_LAYER.getInt(getCurrentEntity().etf$getType())) {
                    case 1 -> currentRenderLayer = RenderLayer.getEntityTranslucent(currentETFTexture.getTextureIdentifier(getCurrentEntity()));
                    case 2 -> currentRenderLayer = RenderLayer.getEntityTranslucentCull(currentETFTexture.getTextureIdentifier(getCurrentEntity()));
                    case 3 -> currentRenderLayer = RenderLayer.getEndGateway();
                    case 4 -> currentRenderLayer = RenderLayer.getOutline(currentETFTexture.getTextureIdentifier(getCurrentEntity()));
                    default -> {}
                }
                allowRenderLayerTextureModify();
            }
        }else{
            System.out.println("failed 3565683856");
        }
        return provider.getBuffer(currentRenderLayer);
    }

    public static VertexConsumer processSpriteVertexConsumer(Function<Identifier, RenderLayer> layerFactory, VertexConsumer consumer) {
        currentETFTexture = null;

        //sprites have a special vertex consumer with their atlas texture
        //see if we need to break that
        //note this will prevent sprite animations, but only if ETF features are found
        if(consumer instanceof SpriteTexturedVertexConsumer spriteTexturedVertexConsumer){
            Identifier rawId = spriteTexturedVertexConsumer.sprite.getContents().getId();

            //infer actual texture
            Identifier actualTexture;
            if(rawId.toString().endsWith(".png")){
                actualTexture = rawId;
            }else{
                //todo check all block entities follow this logic? i know chests, shulker boxes, and beds do
                actualTexture = new Identifier(rawId.getNamespace(),"textures/"+rawId.getPath()+".png");
            }

            //System.out.println("raw="+rawId+"\nactual="+actualTexture);

            currentETFTexture = ETFManager.getInstance().getETFTexture(actualTexture, ETFRenderContext.getCurrentEntity(), ETFManager.TextureSource.BLOCK_ENTITY,false);

            //if texture is emissive or a variant send in as a non sprite vertex consumer
            if(currentETFTexture.getVariantNumber() != 0 || currentETFTexture.isEmissive()){
                preventRenderLayerTextureModify();
                currentRenderLayer = layerFactory.apply(currentETFTexture.thisIdentifier);
                allowRenderLayerTextureModify();
                return ETFRenderContext.getCurrentProvider().getBuffer(currentRenderLayer);
            }
        }
        return consumer;
    }

    public static void setCurrentEntity(ETFEntity currentEntity) {
        ETFRenderContext.currentEntity = currentEntity;
    }

//    public static Object getCurrentTopPart() {
//        return currentTopPart;
//    }

    public static VertexConsumerProvider getCurrentProvider() {
        return currentProvider;
    }

    public static RenderLayer getCurrentRenderLayer() {
        return currentRenderLayer;
    }

    public static ETFEntity getCurrentEntity() {
        return currentEntity;
    }

    private static ETFEntity currentEntity = null;


    public static boolean isRenderReady(){
        return currentRenderLayer != null && currentProvider != null && currentEntity != null && currentETFTexture != null;
    }

    public static int getCurrentModelPartDepth() {
        return currentModelPartDepth;
    }

    public static void incrementCurrentModelPartDepth() {
        currentModelPartDepth++;
    }
    public static void decrementCurrentModelPartDepth() {
        currentModelPartDepth--;
    }

    public static void resetCurrentModelPartDepth() {
        currentModelPartDepth = 0;
    }
    private static int currentModelPartDepth = 0;

    public static void reset(){
        currentETFTexture = null;
        currentProvider = null;

//        currentTopPart = null;
        currentModelPartDepth = 0;

        currentRenderLayer = null;
        currentEntity = null;
    }
}
