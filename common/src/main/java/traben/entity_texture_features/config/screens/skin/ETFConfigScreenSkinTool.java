package traben.entity_texture_features.config.screens.skin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.LivingEntity;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import traben.entity_texture_features.ETF;
import traben.entity_texture_features.ETFVersionDifferenceHandler;
import traben.entity_texture_features.features.ETFManager;
import traben.entity_texture_features.features.player.ETFPlayerTexture;
import traben.entity_texture_features.utils.ETFUtils2;

import java.nio.file.Path;
import java.util.Objects;

import static traben.entity_texture_features.ETF.CONFIG_DIR;
import static traben.entity_texture_features.ETF.MOD_ID;

//inspired by puzzles custom gui code
@SuppressWarnings("EnhancedSwitchMigration")
public class ETFConfigScreenSkinTool extends ETFScreenOldCompat {
    public Boolean originalEnableBlinking;
    public ETFPlayerTexture thisETFPlayerTexture = null;
    public NativeImage currentEditorSkin = null;
    public boolean flipView = false;
    ButtonWidget printSkinFileButton = null;
    ButtonWidget villagerNoseButton = null;
    ButtonWidget coatButton = null;
    ButtonWidget coatLengthButton = null;
    ButtonWidget blinkButton = null;
    ButtonWidget blinkHeightButton = null;
    ButtonWidget emissiveButton = null;
    ButtonWidget emissiveSelectButton = null;
    ButtonWidget enchantButton = null;
    ButtonWidget enchantSelectButton = null;
    //    ButtonWidget capeButton = null;
    ButtonWidget transparencyButton = null;

    public ETFConfigScreenSkinTool(Screen parent) {
        super("config." + MOD_ID + ".player_skin_features.title", parent, false);

    }

    public static int getPixelColour(int choice) {
        //no enhanced switch for back compat
        switch (choice) {
            case 1:
                return -65281;
            case 2:
                return -256;
            case 3:
                return -16776961;
            case 4:
                return -16711936;
            case 5:
                return -16760705;
            case 6:
                return -65536;
            case 7:
                return -16744449;
            case 8:
                return -14483457;
            default:
                return choice;
        }
    }

    private void onExit() {
        ETF.config().getConfig().enableBlinking = originalEnableBlinking;
        if (MinecraftClient.getInstance().player != null) {
            ETFManager.getInstance().PLAYER_TEXTURE_MAP.removeEntryOnly(MinecraftClient.getInstance().player.getUuid());
        }
        thisETFPlayerTexture = null;

    }

    @Override
    public void close() {
        onExit();
        super.close();
    }

    @Override
    protected void init() {
        super.init();

        //make blinking enabled for skin tool
        if (originalEnableBlinking == null) {
            originalEnableBlinking = ETF.config().getConfig().enableBlinking;
            ETF.config().getConfig().enableBlinking = true;
        }


        if (MinecraftClient.getInstance().player != null && thisETFPlayerTexture == null) {
            thisETFPlayerTexture = ETFManager.getInstance().PLAYER_TEXTURE_MAP.get(MinecraftClient.getInstance().player.getUuid());
            if (thisETFPlayerTexture == null) {
                ETFPlayerTexture etfPlayerTexture = new ETFPlayerTexture();
                ETFManager.getInstance().PLAYER_TEXTURE_MAP.put(MinecraftClient.getInstance().player.getUuid(), etfPlayerTexture);
                thisETFPlayerTexture = etfPlayerTexture;
            } else if (thisETFPlayerTexture.etfTextureOfFinalBaseSkin != null) {
                thisETFPlayerTexture.etfTextureOfFinalBaseSkin.setGUIBlink();
            }
        }
        if (currentEditorSkin == null) {
            currentEditorSkin = ETFUtils2.emptyNativeImage(64, 64);
            NativeImage skin = ETFPlayerTexture.clientPlayerOriginalSkinImageForTool;
            if (skin != null) {
                currentEditorSkin.copyFrom(ETFPlayerTexture.clientPlayerOriginalSkinImageForTool);
            } else {

                onExit();
                ETFUtils2.logError("could not load tool as skin could not be loaded");
                Objects.requireNonNull(client).setScreen(parent);

            }
        }

        this.addDrawableChild(getETFButton(this.width / 2 - 210, (int) (this.height * 0.9), 200, 20, ScreenTexts.CANCEL, (button) -> {
            onExit();
            Objects.requireNonNull(client).setScreen(parent);
        }));

        this.addDrawableChild(getETFButton((int) (this.width * 0.024), (int) (this.height * 0.2), 20, 20,
                Text.of("⟳"),
                (button) -> flipView = !flipView));

        printSkinFileButton = getETFButton(this.width / 2 + 10, (int) (this.height * 0.9), 200, 20,
                ETFVersionDifferenceHandler.getTextFromTranslation("selectWorld.edit.save"),
                (button) -> {
                    boolean result = false;
                    if (MinecraftClient.getInstance().player != null) {
                        result = printPlayerSkinCopy();
                    }
                    onExit();
                    Objects.requireNonNull(client).setScreen(new ETFConfigScreenSkinToolOutcome(parent, result, currentEditorSkin));
                });
        this.addDrawableChild(printSkinFileButton);


        if (MinecraftClient.getInstance().player != null) {

            //skin feature buttons

            this.addDrawableChild(getETFButton((int) (this.width * 0.25), (int) (this.height * 0.2), (int) (this.width * 0.42), 20,
                    thisETFPlayerTexture.hasFeatures ?
                            ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.remove_features") :
                            ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.add_features"),
                    (button) -> {
                        if (thisETFPlayerTexture.hasFeatures) {
                            applyExistingOverlayToSkin(new Identifier(MOD_ID + ":textures/skin_feature_remove.png"));
                        } else {
                            applyExistingOverlayToSkin(new Identifier(MOD_ID + ":textures/skin_feature_printout.png"));
                        }
                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        button.setMessage(thisETFPlayerTexture.hasFeatures ?
                                ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.remove_features") :
                                ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.add_features"));
                        updateButtons();
                    }));

            villagerNoseButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.7), (int) (this.width * 0.2), 20,
                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.button").getString() +
                            thisETFPlayerTexture.noseType.getButtonText().getString()),
                    (button) -> {
                        int colour = thisETFPlayerTexture.noseType.next().getNosePixelColour();

                        currentEditorSkin.setColor(53, 17, colour);
                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);

                        button.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.button").getString() +
                                thisETFPlayerTexture.noseType.getButtonText().getString()));
                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.tooltip"));

//            capeButton = getETFButton((int) (this.width * 0.47), (int) (this.height * 0.7), (int) (this.width * 0.2), 20,
//                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.cape.button").getString() +
//                            thisETFPlayerTexture.capeType.getButtonText().getString()),
//                    (button) -> {
//                        CapeType cape = thisETFPlayerTexture.capeType.next();
//
//                        button.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.cape.button").getString() +
//                                cape.getButtonText().getString()));
//
//                        currentEditorSkin.setColor(53, 16, cape.getCapePixelColour());
//
//                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
//                        updateButtons();
//                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.cape.tooltip")
//            );

            transparencyButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.7), (int) (this.width * 0.275), 20,
                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.transparency.button").getString() +
                            booleanAsOnOff(!thisETFPlayerTexture.wasForcedSolid)),
                    (button) -> {

                        button.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.transparency.button").getString() +
                                booleanAsOnOff(thisETFPlayerTexture.wasForcedSolid)));

                        currentEditorSkin.setColor(53, 18, getPixelColour(thisETFPlayerTexture.wasForcedSolid ? 0 : 1));

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        updateButtons();
                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.transparency.tooltip")
            );

            coatButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.3), (int) (this.width * 0.42), 20,
                    CoatStyle.get(thisETFPlayerTexture.coatStyle).getTitle(),
                    (button) -> {
                        CoatStyle coat = CoatStyle.get(thisETFPlayerTexture.coatStyle).next();

                        button.setMessage(coat.getTitle());

                        currentEditorSkin.setColor(52, 17, coat.getCoatPixelColour());

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        updateButtons();
                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.tooltip")
            );

            coatLengthButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.3), (int) (this.width * 0.275), 20,
                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_length.title").getString()
                            + thisETFPlayerTexture.coatLength),
                    (button) -> {
                        int lengthChoice;
                        if (thisETFPlayerTexture.coatLength == 8) {
                            lengthChoice = 1;
                        } else {
                            lengthChoice = thisETFPlayerTexture.coatLength + 1;
                        }
                        currentEditorSkin.setColor(52, 18, getPixelColour(lengthChoice));
                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);

                        button.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_length.title").getString()
                                + thisETFPlayerTexture.coatLength));
                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_length.tooltip")
            );

            blinkButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.4), (int) (this.width * 0.42), 20,
                    BlinkType.get(thisETFPlayerTexture.blinkType).getTitle(),
                    (button) -> {
                        BlinkType blink = BlinkType.get(thisETFPlayerTexture.blinkType).next();

                        button.setMessage(blink.getTitle());

                        if (blink != BlinkType.NONE
                                && blink != BlinkType.WHOLE_FACE_TWO
                                && blink != BlinkType.WHOLE_FACE) {
                            //set height choice to 0
                            currentEditorSkin.setColor(52, 19, getPixelColour(0));
                        } else if (currentEditorSkin.getColor(52, 19) > blink.getMaxEyePixelHeight()) {
                            //set height choice to the highest possible if too big for new type choice
                            currentEditorSkin.setColor(52, 19, getPixelColour(blink.getMaxEyePixelHeight()));
                        }

                        currentEditorSkin.setColor(52, 16, blink.getBlinkPixelColour());

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        updateButtons();
                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.tooltip")
            );

            blinkHeightButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.4), (int) (this.width * 0.275), 20,
                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_height.title").getString()
                            + thisETFPlayerTexture.blinkHeight),
                    (button) -> {
                        int heightChoice;
                        if (thisETFPlayerTexture.blinkHeight == BlinkType.get(thisETFPlayerTexture.blinkType).getMaxEyePixelHeight()) {
                            heightChoice = 1;
                        } else {
                            heightChoice = thisETFPlayerTexture.blinkHeight + 1;
                        }
                        currentEditorSkin.setColor(52, 19, getPixelColour(heightChoice));
                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);

                        button.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_height.title").getString()
                                + thisETFPlayerTexture.blinkHeight));
                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_height.tooltip")
            );

            emissiveButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.5), (int) (this.width * 0.42), 20,
                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_enable.button").getString()
                            + (currentEditorSkin.getColor(1, 17) == getPixelColour(1) ? ScreenTexts.ON : ScreenTexts.OFF).getString()),
                    (button) -> {


                        if (thisETFPlayerTexture.hasEmissives) {
                            currentEditorSkin.setColor(1, 17, 0);
                        } else {
                            currentEditorSkin.setColor(1, 17, getPixelColour(1));
                        }

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        button.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_enable.button").getString()
                                + (currentEditorSkin.getColor(1, 17) == getPixelColour(1) ? ScreenTexts.ON : ScreenTexts.OFF).getString()));
                        updateButtons();
                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_enable.tooltip")
            );

            emissiveSelectButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.5), (int) (this.width * 0.275), 20,
                    ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_select.button"),
                    (button) -> Objects.requireNonNull(client).setScreen(new ETFConfigScreenSkinToolPixelSelection(this, ETFConfigScreenSkinToolPixelSelection.SelectionMode.EMISSIVE))
            );

            enchantButton = getETFButton((int) (this.width * 0.25), (int) (this.height * 0.6), (int) (this.width * 0.42), 20,
                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_enable.button").getString()
                            + (currentEditorSkin.getColor(1, 18) == getPixelColour(2) ? ScreenTexts.ON : ScreenTexts.OFF).getString()),
                    (button) -> {


                        if (thisETFPlayerTexture.hasEnchant) {
                            currentEditorSkin.setColor(1, 18, 0);
                        } else {
                            currentEditorSkin.setColor(1, 18, getPixelColour(2));
                        }

                        thisETFPlayerTexture.changeSkinToThisForTool(currentEditorSkin);
                        button.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_enable.button").getString()
                                + (currentEditorSkin.getColor(1, 18) == getPixelColour(2) ? ScreenTexts.ON : ScreenTexts.OFF).getString()));
                        updateButtons();
                    }, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_enable.tooltip")
            );

            enchantSelectButton = getETFButton((int) (this.width * 0.695), (int) (this.height * 0.6), (int) (this.width * 0.275), 20,
                    ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_select.button"),
                    (button) -> Objects.requireNonNull(client).setScreen(new ETFConfigScreenSkinToolPixelSelection(this, ETFConfigScreenSkinToolPixelSelection.SelectionMode.ENCHANTED))
            );

            updateButtons();
            this.addDrawableChild(villagerNoseButton);
            this.addDrawableChild(coatButton);
            this.addDrawableChild(coatLengthButton);
            this.addDrawableChild(blinkButton);
            this.addDrawableChild(blinkHeightButton);
            this.addDrawableChild(emissiveButton);
            this.addDrawableChild(emissiveSelectButton);
            this.addDrawableChild(enchantButton);
            this.addDrawableChild(enchantSelectButton);
//            this.addDrawableChild(capeButton);
            this.addDrawableChild(transparencyButton);


        }

    }

    private void updateButtons() {
        boolean activeFeatures = thisETFPlayerTexture.hasFeatures;
        if (villagerNoseButton != null) {
            villagerNoseButton.active = activeFeatures;
            villagerNoseButton.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.button").getString() +
                    thisETFPlayerTexture.noseType.getButtonText().getString()));
        }
        if (coatButton != null) {
            coatButton.active = activeFeatures;
            coatButton.setMessage(CoatStyle.get(thisETFPlayerTexture.coatStyle).getTitle());
        }
        if (coatLengthButton != null) {
            coatLengthButton.active = activeFeatures && CoatStyle.get(thisETFPlayerTexture.coatStyle) != CoatStyle.NONE;
            coatLengthButton.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_length.title").getString()
                    + thisETFPlayerTexture.coatLength));
        }
        if (blinkButton != null) {
            blinkButton.active = activeFeatures;
            blinkButton.setMessage(BlinkType.get(thisETFPlayerTexture.blinkType).getTitle());
        }
        if (blinkHeightButton != null) {
            blinkHeightButton.active = activeFeatures
                    && BlinkType.get(thisETFPlayerTexture.blinkType) != BlinkType.NONE
                    && BlinkType.get(thisETFPlayerTexture.blinkType) != BlinkType.WHOLE_FACE_TWO
                    && BlinkType.get(thisETFPlayerTexture.blinkType) != BlinkType.WHOLE_FACE;
            blinkHeightButton.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_height.title").getString()
                    + thisETFPlayerTexture.blinkHeight));
        }
        if (emissiveButton != null) {
            emissiveButton.active = activeFeatures;
            emissiveButton.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.emissive_enable.button").getString()
                    + (currentEditorSkin.getColor(1, 17) == getPixelColour(1) ? ScreenTexts.ON : ScreenTexts.OFF).getString()));
        }
        if (emissiveSelectButton != null) {
            emissiveSelectButton.active = activeFeatures
                    && currentEditorSkin.getColor(1, 17) == getPixelColour(1);
        }
        if (enchantButton != null) {
            enchantButton.active = activeFeatures;
            enchantButton.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.enchant_enable.button").getString()
                    + (currentEditorSkin.getColor(1, 18) == getPixelColour(2) ? ScreenTexts.ON : ScreenTexts.OFF).getString()));
        }
        if (enchantSelectButton != null) {
            enchantSelectButton.active = activeFeatures
                    && currentEditorSkin.getColor(1, 18) == getPixelColour(2);
        }
//        if (capeButton != null) {
//            capeButton.active = activeFeatures;
//            capeButton.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.cape.button").getString() +
//                    thisETFPlayerTexture.capeType.getButtonText().getString()));
//        }
        if (transparencyButton != null) {
            transparencyButton.active = activeFeatures;
            transparencyButton.setMessage(Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.transparency.button").getString() +
                    booleanAsOnOff(!thisETFPlayerTexture.wasForcedSolid)));
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);


        if (MinecraftClient.getInstance() != null) {
            ClientPlayerEntity player = MinecraftClient.getInstance().player;
            if (player != null) {


                int height = (int) (this.height * 0.75);
                int playerX = (int) (this.width * 0.14);
                drawEntity(context,playerX, height, (int) (this.height * 0.3), (float) (-mouseX + playerX), (float) (-mouseY + (this.height * 0.3)), player);
            } else {
                context.drawTextWithShadow(textRenderer, Text.of("Player is null for some reason!"), width / 7, (int) (this.height * 0.4), 0xFFFFFF);
                context.drawTextWithShadow(textRenderer, Text.of("Cannot load player to render!"), width / 7, (int) (this.height * 0.45), 0xFFFFFF);
            }
        }

        context.drawTextWithShadow(textRenderer, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.crouch_message"), width / 40, (int) (this.height * 0.8), 0x555555);
        context.drawTextWithShadow(textRenderer, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_message"), width / 40, (int) (this.height * 0.1), 0x555555);
//        if(ETFVersionDifferenceHandler.isThisModLoaded("iris"))
//            drawTextWithShadow(matrices, textRenderer, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.iris_message"), width / 8, (int) (this.height * 0.15), 0xFF5555);
    }

    public void applyExistingOverlayToSkin(Identifier overlayTexture) {
        if ((ETFVersionDifferenceHandler.isFabric() == ETFVersionDifferenceHandler.isThisModLoaded("fabric"))) {
            NativeImage skinFeatureImage = ETFUtils2.getNativeImageElseNull(overlayTexture);
            try {
                for (int x = 0; x < currentEditorSkin.getWidth(); x++) {
                    for (int y = 0; y < currentEditorSkin.getHeight(); y++) {
                        assert skinFeatureImage != null;
                        if (skinFeatureImage.getColor(x, y) != 0) {
                            currentEditorSkin.setColor(x, y, skinFeatureImage.getColor(x, y));
                        }
                    }
                }

                //ETFUtils2.logMessage("Skin feature layout successfully applied to a copy of your skin.", true);
            } catch (Exception e) {
                ETFUtils2.logMessage("Skin feature layout could not be applied to a copy of your skin. Error written to log.", false);
                ETFUtils2.logError(e.toString(), false);
            }

        } else {
            //requires fab api to read from mod resources
            ETFUtils2.logError("Fabric API required for skin processing, cancelling.", false);
        }
        //return skinImage;
    }

    public boolean printPlayerSkinCopy() {
        if ((ETFVersionDifferenceHandler.isFabric() == ETFVersionDifferenceHandler.isThisModLoaded("fabric")) && CONFIG_DIR != null) {
            Path outputDirectory = Path.of(CONFIG_DIR.getParent(), "\\ETF_player_skin_printout.png");
            try {
                currentEditorSkin.writeTo(outputDirectory);
                ETFUtils2.logMessage(ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETF.MOD_ID + ".player_skin_editor.print_skin.result.success").getString(), false);

                return true;
            } catch (Exception e) {
                //ETFUtils2.logMessage("Skin feature layout could not be applied to a copy of your skin and has not been saved. Error written to log.", true);
                ETFUtils2.logError(e.toString(), false);
            }

        }
        return false;
    }

    public void drawEntity(DrawContext context, int x, int y, int size, float mouseX, float mouseY, LivingEntity entity) {
        float f = (float) Math.atan((mouseX / 40.0f));

        float g = (float) Math.atan((mouseY / 40.0F));
//1.20.5        MatrixStack matrixStack = RenderSystem.getModelViewStack();

//        float j2 = (float) Math.atan(((-mouseY + this.height / 2f) / 40.0F));
        Quaternionf quaternionf = (new Quaternionf()).rotateZ(3.1415927F);
        Quaternionf quaternionf2 = (new Quaternionf()).rotateX( 0);//j2 * 20.0F * 0.017453292F);
        quaternionf.mul(quaternionf2);

        context.getMatrices().push();
        context.getMatrices().translate(x, y, 150.0);
        context.getMatrices().multiplyPositionMatrix((new Matrix4f()).scaling((float) size, (float) size, (float) (-size)));
        context.getMatrices().multiply(quaternionf);
        DiffuseLighting.method_34742();
        EntityRenderDispatcher entityRenderDispatcher = MinecraftClient.getInstance().getEntityRenderDispatcher();
        if (quaternionf2 != null) {
            quaternionf2.conjugate();
            entityRenderDispatcher.setRotation(quaternionf2);
        }

        entityRenderDispatcher.setRenderShadows(false);
        float h = entity.bodyYaw;
        float i = entity.getYaw();
        float j = entity.getPitch();
        float k = entity.prevHeadYaw;
        float l = entity.headYaw;
        entity.bodyYaw = (flipView ? 0 : 180.0F) + f * 20.0F;
        entity.setYaw((flipView ? 0 : 180.0F) + f * 40.0F);
        entity.setPitch(-g * 20.0F);
        entity.headYaw = entity.getYaw();
        entity.prevHeadYaw = entity.getYaw();


//        VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
        //noinspection deprecation
        RenderSystem.runAsFancy(() -> entityRenderDispatcher.render(entity, 0.0, 0.0, 0.0, 0.0F, 1.0F, context.getMatrices(), context.getVertexConsumers(), 15728880));
//        immediate.draw();
        context.draw();
        entityRenderDispatcher.setRenderShadows(true);
        context.getMatrices().pop();
        DiffuseLighting.enableGuiDepthLighting();
//        entityRenderDispatcher.setRenderShadows(true);
        entity.bodyYaw = h;
        entity.setYaw(i);
        entity.setPitch(j);
        entity.prevHeadYaw = k;
        entity.headYaw = l;
//        matrixStack.pop();
//        RenderSystem.applyModelViewMatrix();
//        DiffuseLighting.enableGuiDepthLighting();
    }



    @SuppressWarnings("EnhancedSwitchMigration")
    public enum NoseType {
        VILLAGER(1, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.villager")),
        VILLAGER_TEXTURED(7, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.villager2")),
        VILLAGER_REMOVE(8, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.villager3")),
        VILLAGER_TEXTURED_REMOVE(9, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.villager4")),
        TEXTURED_1(2, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.1")),
        TEXTURED_2(3, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.2")),
        TEXTURED_3(4, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.3")),
        TEXTURED_4(5, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.4")),
        TEXTURED_5(6, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.textured.5")),
        NONE(0, ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.nose.none"));

        public final int id;
        private final Text buttonText;


        NoseType(int i, Text buttonText) {
            this.id = i;
            this.buttonText = buttonText;
        }

        public NoseType getByColorId(int id) {
            for (NoseType nose : NoseType.values()) {
                if (nose.id == id) return nose;
            }
            return NONE;
        }

        public Text getButtonText() {
            return buttonText;
        }

        public NoseType next() {
            switch (this) {
                case NONE:
                    return VILLAGER;
                case VILLAGER:
                    return VILLAGER_TEXTURED;
                case VILLAGER_TEXTURED:
                    return VILLAGER_REMOVE;
                case VILLAGER_REMOVE:
                    return VILLAGER_TEXTURED_REMOVE;
                case VILLAGER_TEXTURED_REMOVE:
                    return TEXTURED_1;
                case TEXTURED_1:
                    return TEXTURED_2;
                case TEXTURED_2:
                    return TEXTURED_3;
                case TEXTURED_3:
                    return TEXTURED_4;
                case TEXTURED_4:
                    return TEXTURED_5;
                default:
                    return NONE;

            }
        }

        public int getNosePixelColour() {
            return ETFPlayerTexture.getSkinNumberToPixelColour(id);
        }
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    public enum CoatStyle {
        COPIED_THIN_TOP,
        MOVED_THIN_TOP,
        COPIED_FAT_TOP,
        MOVED_FAT_TOP,
        COPIED_THIN,
        MOVED_THIN,
        COPIED_FAT,
        MOVED_FAT,
        NONE;


        public static CoatStyle get(int id) {
            //no enhanced switch for back compat
            switch (id) {
                case 1:
                    return COPIED_THIN_TOP;
                case 2:
                    return MOVED_THIN_TOP;
                case 3:
                    return COPIED_FAT_TOP;
                case 4:
                    return MOVED_FAT_TOP;
                case 5:
                    return COPIED_THIN;
                case 6:
                    return MOVED_THIN;
                case 7:
                    return COPIED_FAT;
                case 8:
                    return MOVED_FAT;
                default:
                    return NONE;
            }
        }

        public Text getTitle() {
            //no enhanced switch for back compat
            switch (this) {
                case COPIED_THIN_TOP:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.1");
                case MOVED_THIN_TOP:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.2");
                case COPIED_FAT_TOP:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.3");
                case MOVED_FAT_TOP:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.4");
                case COPIED_THIN:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.5");
                case MOVED_THIN:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.6");
                case COPIED_FAT:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.7");
                case MOVED_FAT:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.8");
                default:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.coat_style.none");
            }
        }

        public int getCoatPixelColour() {
            //no enhanced switch for back compat
            switch (this) {
                case COPIED_THIN_TOP:
                    return -65281;
                case MOVED_THIN_TOP:
                    return -256;
                case COPIED_FAT_TOP:
                    return -16776961;
                case MOVED_FAT_TOP:
                    return -16711936;
                case COPIED_THIN:
                    return -16760705;
                case MOVED_THIN:
                    return -65536;
                case COPIED_FAT:
                    return -16744449;
                case MOVED_FAT:
                    return -14483457;
                default:
                    return 0;
            }
        }

        public CoatStyle next() {
            //no enhanced switch for back compat
            switch (this) {
                case NONE:
                    return COPIED_THIN_TOP;
                case COPIED_THIN_TOP:
                    return MOVED_THIN_TOP;
                case MOVED_THIN_TOP:
                    return COPIED_FAT_TOP;
                case COPIED_FAT_TOP:
                    return MOVED_FAT_TOP;
                case MOVED_FAT_TOP:
                    return COPIED_THIN;
                case COPIED_THIN:
                    return MOVED_THIN;
                case MOVED_THIN:
                    return COPIED_FAT;
                case COPIED_FAT:
                    return MOVED_FAT;
                default:
                    return NONE;
            }
        }
    }

    @SuppressWarnings("EnhancedSwitchMigration")
    public enum BlinkType {
        ONE_PIXEL,
        TWO_PIXEL,
        FOUR_PIXEL,
        WHOLE_FACE,
        WHOLE_FACE_TWO,
        NONE;


        public static BlinkType get(int id) {
            //no enhanced switch for back compat
            switch (id) {
                case 3:
                    return ONE_PIXEL;
                case 4:
                    return TWO_PIXEL;
                case 5:
                    return FOUR_PIXEL;
                case 1:
                    return WHOLE_FACE;
                case 2:
                    return WHOLE_FACE_TWO;
                default:
                    return NONE;
            }
        }

        public Text getTitle() {
            //no enhanced switch for back compat
            switch (this) {
                case ONE_PIXEL:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.1");
                case TWO_PIXEL:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.2");
                case FOUR_PIXEL:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.4");
                case WHOLE_FACE:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.whole.1");
                case WHOLE_FACE_TWO:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.whole.2");
                default:
                    return ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_editor.blink_type.none");
            }
        }

        public int getBlinkPixelColour() {
            //no enhanced switch for back compat
            switch (this) {
                case ONE_PIXEL:
                    return getPixelColour(3);
                case TWO_PIXEL:
                    return getPixelColour(4);
                case FOUR_PIXEL:
                    return getPixelColour(5);
                case WHOLE_FACE:
                    return getPixelColour(1);
                case WHOLE_FACE_TWO:
                    return getPixelColour(2);
                default:
                    return 0;
            }
        }

        public int getMaxEyePixelHeight() {
            //no enhanced switch for back compat
            switch (this) {
                case ONE_PIXEL:
                    return 8;
                case TWO_PIXEL:
                    return 7;
                case FOUR_PIXEL:
                    return 5;
                default:
                    return 1;
            }
        }

        public BlinkType next() {
            //no enhanced switch for back compat
            switch (this) {
                case NONE:
                    return ONE_PIXEL;
                case ONE_PIXEL:
                    return TWO_PIXEL;
                case TWO_PIXEL:
                    return FOUR_PIXEL;
                case FOUR_PIXEL:
                    return WHOLE_FACE;
                case WHOLE_FACE:
                    return WHOLE_FACE_TWO;
                default:
                    return NONE;
            }
        }
    }


}
