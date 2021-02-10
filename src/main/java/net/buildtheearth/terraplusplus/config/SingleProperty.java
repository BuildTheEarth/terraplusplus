package net.buildtheearth.terraplusplus.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a type deserialized by {@link TypedDeserializer} is represented as a single value rather than an object.
 *
 * @author DaPorkchop_
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface SingleProperty {
}
