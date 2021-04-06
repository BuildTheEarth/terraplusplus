package net.buildtheearth.terraplusplus.config;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a type deserialized by {@link TypedDeserializer} should be constructed directly, rather than being parsed as a value.
 *
 * @author DaPorkchop_
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConstructDirectly {
}
