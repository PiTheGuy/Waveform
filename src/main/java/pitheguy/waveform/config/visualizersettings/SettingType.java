package pitheguy.waveform.config.visualizersettings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import java.awt.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class SettingType<T> {
    private final Class<T> clazz;
    private final Function<T, JsonElement> serializer;
    private final Function<JsonElement, T> deserializer;
    private final Predicate<T> validator;

    public static final SettingType<Boolean> BOOLEAN = new SettingType<>(Boolean.class, JsonPrimitive::new, JsonElement::getAsBoolean);

    public SettingType(Class<T> clazz, Function<T, JsonElement> serializer, Function<JsonElement, T> deserializer, Predicate<T> validator) {
        this.clazz = clazz;
        this.serializer = serializer;
        this.deserializer = deserializer;
        this.validator = validator;
    }

    public SettingType(Class<T> clazz, Function<T, JsonElement> serializer, Function<JsonElement, T> deserializer) {
        this(clazz, serializer, deserializer, v -> true);
    }

    public static SettingType<Integer> positiveInt() {
        return forInt(0, Integer.MAX_VALUE);
    }

    public static SettingType<Integer> forInt(int min, int max) {
        return new SettingType<>(Integer.class, JsonPrimitive::new, JsonElement::getAsInt, i -> i >= min && i <= max);
    }

    public static SettingType<Float> positiveFloat() {
        return forFloat(0, Float.MAX_VALUE);
    }

    public static SettingType<Float> forFloat(float min, float max) {
        return new SettingType<>(Float.class, JsonPrimitive::new, JsonElement::getAsFloat, i -> i >= min && i <= max);
    }

    public static SettingType<Double> positiveDouble() {
        return forDouble(0, Double.MAX_VALUE);
    }

    public static SettingType<Double> fraction() {
        return forDouble(0, 1);
    }

    public static SettingType<Double> forDouble(double min, double max) {
        return new SettingType<>(Double.class, JsonPrimitive::new, JsonElement::getAsDouble, i -> i >= min && i <= max);
    }

    public static <T extends Enum<T>> SettingType<T> forEnum(Class<T> enumClass) {
        return new SettingType<>(enumClass, e -> new JsonPrimitive(e.name()), value -> Enum.valueOf(enumClass, value.getAsString()));
    }

    public JsonElement serialize(Object value) {
        return serializer.apply((T) value);
    }

    public Object deserialize(JsonElement value) {
        return deserializer.apply(value);
    }

    public boolean isValid(T value) {
        return validator.test(value);
    }

    public Class<T> getClazz() {
        return clazz;
    }
}
