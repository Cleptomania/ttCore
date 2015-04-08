package tterrag.core.common.util;

import javax.annotation.concurrent.Immutable;

import lombok.Value;
import lombok.experimental.Wither;

/**
 * An object to represent a bounds limit on a property.
 * 
 * @param <T>
 *            The type of the bound.
 */
@Immutable
@Value
public class Bound<T extends Number & Comparable<T>>
{
    public static final Bound<Double> MAX_BOUND = Bound.of(Double.MIN_VALUE, Double.MAX_VALUE);

    @Wither
    public T min, max;

    /**
     * Static factory method that returns a {@code Bound<T>} object of the type of the params passed.
     */
    public static <T extends Number & Comparable<T>> Bound<T> of(T min, T max)
    {
        return new Bound<T>(min, max);
    }

    public T clamp(T val)
    {
        return val.compareTo(min) < 0 ? min : val.compareTo(max) > 0 ? max : val;
    }
}