package traben.tconfig.gui.entries;

import com.demonwav.mcdev.annotations.Translatable;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class TConfigEntryBoolean extends TConfigEntryValue<Boolean> {

    private final BooleanButtonWidget widget;


    public TConfigEntryBoolean(@Translatable String translationKey, @Translatable @Nullable String tooltip, Supplier<Boolean> getter, Consumer<Boolean> setter, boolean defaultValue) {
        super(translationKey, tooltip, getter, setter, defaultValue);
        widget = new BooleanButtonWidget(0, 0, 20, 20, getText().getString(), getter.get(), getTooltip());
    }

    @SuppressWarnings("unused")
    public TConfigEntryBoolean(@Translatable String translationKey, Supplier<Boolean> getter, Consumer<Boolean> setter, boolean defaultValue) {
        this(translationKey, null, getter, setter, defaultValue);
    }

    @SuppressWarnings("unused")
    public TConfigEntryBoolean setType(Type type) {
        widget.type = type;
        return this;
    }


    @Override
    protected Boolean getValueFromWidget() {
        return widget.value;
    }

    @Override
    public ClickableWidget getWidget(final int x, final int y, final int width, final int height) {
        widget.setDimensionsAndPosition(width, height, x, y);
        return widget;
    }


    @Override
    void setWidgetToDefaultValue() {
        widget.value = defaultValue;
        widget.updateMessage();
    }

    @Override
    void resetWidgetToInitialValue() {
        widget.value = getter.get();
        widget.updateMessage();
    }

    public enum Type {
        ON_OFF(ScreenTexts.ON, ScreenTexts.OFF),
        @SuppressWarnings("unused") YES_NO(ScreenTexts.YES, ScreenTexts.NO);

        private final String t;
        private final String f;

        Type(Text t, Text f) {
            this.t = t.getString();
            this.f = f.getString();
        }

        public String get(boolean value) {
            return value ? t : f;
        }
    }

    private class BooleanButtonWidget extends ButtonWidget {
        private final String title;
        private boolean value;


        private Type type = Type.ON_OFF;

        public BooleanButtonWidget(final int x, final int y, final int width, final int height, final String text,
                                   final boolean initialValue, final Tooltip tooltip) {
            super(x, y, width, height, Text.of(""), null, DEFAULT_NARRATION_SUPPLIER);
            this.value = initialValue;
            this.title = text + ": ";
            updateMessage();
            setTooltip(tooltip);
        }

        private void updateMessage() {
            setMessage(Text.of(title + (value != getter.get() ? TConfigEntry.CHANGED_COLOR : "") + type.get(value)));
        }

        @Override
        public void onPress() {
            value = !value;
            updateMessage();
        }
    }
}
