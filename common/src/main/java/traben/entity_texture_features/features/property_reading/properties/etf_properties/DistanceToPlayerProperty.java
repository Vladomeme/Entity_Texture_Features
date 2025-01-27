package traben.entity_texture_features.features.property_reading.properties.etf_properties;

import net.minecraft.client.MinecraftClient;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.features.property_reading.properties.generic_properties.FloatRangeFromStringArrayProperty;
import traben.entity_texture_features.utils.ETFEntity;

import java.util.Properties;

public class DistanceToPlayerProperty extends FloatRangeFromStringArrayProperty {


    protected DistanceToPlayerProperty(Properties properties, int propertyNum) throws RandomPropertyException {
        super(readPropertiesOrThrow(properties, propertyNum, "distance", "distanceFromPlayer"));
    }

    public static DistanceToPlayerProperty getPropertyOrNull(Properties properties, int propertyNum) {
        try {
            return new DistanceToPlayerProperty(properties, propertyNum);
        } catch (RandomPropertyException e) {
            return null;
        }
    }


    @Nullable
    @Override
    protected Float getRangeValueFromEntity(ETFEntity entity) {
        if (MinecraftClient.getInstance().player == null)
            return null;
        return entity.etf$distanceTo(MinecraftClient.getInstance().player);
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"distance", "distanceFromPlayer"};
    }

}
