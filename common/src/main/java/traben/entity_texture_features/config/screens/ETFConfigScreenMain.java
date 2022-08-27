package traben.entity_texture_features.config.screens;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import traben.entity_texture_features.ETFClientCommon;
import traben.entity_texture_features.ETFVersionDifferenceHandler;
import traben.entity_texture_features.config.ETFConfig;
import traben.entity_texture_features.texture_handlers.ETFManager;
import traben.entity_texture_features.utils.ETFUtils2;

import java.util.Objects;

import static traben.entity_texture_features.ETFClientCommon.ETFConfigData;
import static traben.entity_texture_features.ETFClientCommon.MOD_ID;

//inspired by puzzles custom gui code
public class ETFConfigScreenMain extends ETFConfigScreen {

    static ETFConfig temporaryETFConfig = null;
    boolean shownWarning = false;
    int warningCount = 0;
    ObjectOpenHashSet<ETFConfigScreenWarnings.ConfigWarning> warningsFound = new ObjectOpenHashSet<>();


    //todo translatable text for menus
    ETFConfigScreenWarnings warningsScreen;
    ETFConfigScreenSkinSettings playerSkinSettingsScreen = new ETFConfigScreenSkinSettings(this);
    ETFConfigScreenRandomSettings randomSettingsScreen = new ETFConfigScreenRandomSettings(this);
    ETFConfigScreenEmissiveSettings emissiveSettingsScreen = new ETFConfigScreenEmissiveSettings(this);
    ETFConfigScreenBlinkSettings blinkSettingsScreen = new ETFConfigScreenBlinkSettings(this);
    ETFConfigScreenGeneralSettings generalSettingsScreen = new ETFConfigScreenGeneralSettings(this);
    public ETFConfigScreenMain(Screen parent) {
        super(ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".title"), parent);
        temporaryETFConfig = ETFConfig.copyFrom(ETFConfigData);


        //this warning disables skin features with figura present
        if (FabricLoader.getInstance().isModLoaded(ETFConfigScreenWarnings.ConfigWarning.FIGURA.getMod_id())) {
            shownWarning = true;
            warningCount++;
            warningsFound.add(ETFConfigScreenWarnings.ConfigWarning.FIGURA);
        }
        if (FabricLoader.getInstance().isModLoaded(ETFConfigScreenWarnings.ConfigWarning.SKINLAYERS.getMod_id())) {
            shownWarning = true;
            warningCount++;
            warningsFound.add(ETFConfigScreenWarnings.ConfigWarning.SKINLAYERS);
        }
        if (FabricLoader.getInstance().isModLoaded(ETFConfigScreenWarnings.ConfigWarning.QUARK.getMod_id())) {
            shownWarning = true;
            warningCount++;
            warningsFound.add(ETFConfigScreenWarnings.ConfigWarning.QUARK);
        }
        if (FabricLoader.getInstance().isModLoaded(ETFConfigScreenWarnings.ConfigWarning.ENHANCED_BLOCK_ENTITIES.getMod_id())) {
            shownWarning = true;
            warningCount++;
            warningsFound.add(ETFConfigScreenWarnings.ConfigWarning.ENHANCED_BLOCK_ENTITIES);
        }

        warningsScreen = new ETFConfigScreenWarnings(this, warningsFound);
    }

    @Override
    protected void init() {
        super.init();


        if (shownWarning) {
            this.addDrawableChild(new ButtonWidget((int) (this.width * 0.1), (int) (this.height * 0.1) - 15, (int) (this.width * 0.2), 20,
                    Text.of(""),
                    (button) -> {
                        Objects.requireNonNull(client).setScreen(warningsScreen);
                    }));
        }

        this.addDrawableChild(new ButtonWidget((int) (this.width * 0.7), (int) (this.height * 0.9), (int) (this.width * 0.2), 20,
                ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETFClientCommon.MOD_ID + ".save_and_exit"),
                (button) -> {
                    ETFConfigData = temporaryETFConfig;
                    ETFUtils2.saveConfig();
                    ETFUtils2.checkModCompatabilities();
                    ETFManager.reset();
                    Objects.requireNonNull(client).setScreen(parent);
                }));
        this.addDrawableChild(new ButtonWidget((int) (this.width * 0.4), (int) (this.height * 0.9), (int) (this.width * 0.2), 20,
                ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETFClientCommon.MOD_ID + ".reset_defaults"),
                (button) -> {
                    temporaryETFConfig = new ETFConfig();
                    this.clearAndInit();
                    //Objects.requireNonNull(client).setScreen(parent);
                }));
        this.addDrawableChild(new ButtonWidget((int) (this.width * 0.1), (int) (this.height * 0.9), (int) (this.width * 0.2), 20,
                ScreenTexts.CANCEL,
                (button) -> {
                    temporaryETFConfig = null;
                    Objects.requireNonNull(client).setScreen(parent);
                }));


        this.addDrawableChild(new ButtonWidget((int) (this.width * 0.3) + 75, (int) (this.height * 0.5) + 17, 140, 20,
                ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETFClientCommon.MOD_ID + ".blinking_mob_settings_sub.title"),
                (button) -> {
                    Objects.requireNonNull(client).setScreen(blinkSettingsScreen);
                }));

        this.addDrawableChild(new ButtonWidget((int) (this.width * 0.3) + 75, (int) (this.height * 0.5) - 10, 120, 20,
                ETFVersionDifferenceHandler.getTextFromTranslation("config." + MOD_ID + ".player_skin_settings.title"),
                (button) -> {
                    Objects.requireNonNull(client).setScreen(playerSkinSettingsScreen);
                }));

        this.addDrawableChild(new ButtonWidget((int) (this.width * 0.3) + 75, (int) (this.height * 0.5) - 64, 165, 20,
                ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETFClientCommon.MOD_ID + ".random_settings.title"),
                (button) -> {
                    Objects.requireNonNull(client).setScreen(randomSettingsScreen);
                }));

        this.addDrawableChild(new ButtonWidget((int) (this.width * 0.3) + 75, (int) (this.height * 0.5) - 37, 145, 20,
                ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETFClientCommon.MOD_ID + ".emissive_settings.title"),
                (button) -> {
                    Objects.requireNonNull(client).setScreen(emissiveSettingsScreen);
                }));

        this.addDrawableChild(new ButtonWidget((int) (this.width * 0.3) + 75, (int) (this.height * 0.5) + 44, 120, 20,
                ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETFClientCommon.MOD_ID + ".general_settings.title"),
                (button) -> {
                    Objects.requireNonNull(client).setScreen(generalSettingsScreen);
                }));

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        super.render(matrices, mouseX, mouseY, delta);

        renderGUITexture(new Identifier(MOD_ID + ":textures/gui/icon.png"), (this.width * 0.3) - 64, (this.height * 0.5) - 64, (this.width * 0.3) + 64, (this.height * 0.5) + 64);
        if (shownWarning) {
            drawCenteredText(matrices, textRenderer,
                    Text.of(ETFVersionDifferenceHandler.getTextFromTranslation("config." + ETFClientCommon.MOD_ID + ".warnings_main").getString() + warningCount),
                    (int) (width * 0.2), (int) (height * 0.1) - 9, 0xFF1111);
        }

    }


}
