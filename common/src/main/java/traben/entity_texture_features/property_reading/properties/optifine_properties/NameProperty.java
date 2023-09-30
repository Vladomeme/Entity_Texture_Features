package traben.entity_texture_features.property_reading.properties.optifine_properties;

import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import traben.entity_texture_features.entity_handlers.ETFEntity;
import traben.entity_texture_features.property_reading.properties.generic_properties.StringArrayOrRegexProperty;

import java.util.ArrayList;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NameProperty extends StringArrayOrRegexProperty {


    protected NameProperty(String data) throws RandomPropertyException {
        super(data);


    }

    @Override
    protected boolean shouldForceLowerCaseCheck() {
        return false;
    }


    public static NameProperty getPropertyOrNull(Properties properties, int propertyNum){
        try {
            String dataFromProperty = readPropertiesOrThrow(properties, propertyNum,"name","names");

            ArrayList<String> names = new ArrayList<>();

            if(dataFromProperty.isBlank())
                throw new RandomPropertyException("Name failed");

            if (dataFromProperty.startsWith("regex:") || dataFromProperty.startsWith("pattern:")) {
                //add entire line as a test also
                names.add(dataFromProperty);
            } else {
                //names = dataFromProps.split("\s+");
                //allow    "multiple names" among "other"
                //List<String> list = new ArrayList<>();
                //add the full line as the first name option to allow for simple multiple names
                //in case someone just writes   names.1=john smith
                //instead of                   names.1="john smith"
                Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(dataFromProperty);
                while (m.find()) {
                    names.add(m.group(1).replace("\"", "").trim());
                }
            }

            StringBuilder builder = new StringBuilder();
            for (String str :
                    names) {
                builder.append(str).append(" ");
            }

            return new NameProperty(builder.toString().trim());
        }catch(RandomPropertyException e){
            return null;
        }
    }

    @Override
    public @Nullable String getValueFromEntity(ETFEntity etfEntity) {
        if (etfEntity.hasCustomName()) {
            Text entityNameText = etfEntity.getCustomName();
            if(entityNameText != null) {
                return entityNameText.getString();
            }
        }
        return null;
    }

    @Override
    public boolean isPropertyUpdatable(){
        return true;
    }

    @Override
    public @NotNull String[] getPropertyIds() {
        return new String[]{"name","names"};
    }

}
