package net.buildtheearth.terraplusplus.projection.wkt;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.google.common.collect.ImmutableList;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.experimental.SuperBuilder;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTID;
import net.buildtheearth.terraplusplus.projection.wkt.misc.WKTUsage;
import net.buildtheearth.terraplusplus.projection.wkt.misc.extent.WKTGeographicBoundingBox;
import net.buildtheearth.terraplusplus.projection.wkt.misc.extent.WKTVerticalExtent;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static net.daporkchop.lib.common.util.PValidation.*;

/**
 * @author DaPorkchop_
 */
@EqualsAndHashCode
@SuperBuilder(toBuilder = true)
public abstract class AbstractWKTObject implements WKTObject {
    @Override
    public final String toString() {
        return this.toString(WKTStyle.ONE_LINE);
    }

    @Override
    public final String toPrettyString() {
        return this.toString(WKTStyle.PRETTY);
    }

    @Override
    @SneakyThrows(IOException.class)
    public final String toString(@NonNull WKTStyle style) {
        StringBuilder builder = new StringBuilder();
        try (WKTWriter writer = new WKTWriter.ToAppendable(builder, style)) {
            this.write(writer);
        }
        return builder.toString();
    }

    @Override
    public abstract void write(@NonNull WKTWriter writer) throws IOException;

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithID extends AbstractWKTObject implements WKTObject.WithID {
        protected static final WKTParseSchema<AbstractWKTObject.WithID> BASE_PARSE_SCHEMA = WKTParseSchema.<AbstractWKTObject.WithID, WithIDBuilder<AbstractWKTObject.WithID, ?>>builder(() -> null, WithIDBuilder::build)
                .permitKeyword("")
                .optionalObjectProperty(WKTID.PARSE_SCHEMA, WithIDBuilder::id)
                .build();

        @Builder.Default
        private final WKTID id = null;
    }

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithName extends AbstractWKTObject implements WKTObject.WithName {
        @NonNull
        private final String name;
    }

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithNameAndID extends AbstractWKTObject.WithID implements WKTObject.WithName {
        @NonNull
        private final String name;
    }

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithScopeExtentIdentifierRemark extends AbstractWKTObject.WithID implements WKTObject.WithScopeExtentIdentifierRemark {
        @NonNull
        @Builder.Default
        private final List<WKTUsage> usages = Collections.emptyList();

        public abstract static class WithScopeExtentIdentifierRemarkBuilder<C extends WithScopeExtentIdentifierRemark, B extends WithScopeExtentIdentifierRemarkBuilder<C, B>> extends WithIDBuilder<C, B> {
            private WKTUsage currentUsage() {
                if (this.usages$set) {
                    checkState(this.usages$value.size() == 1, "more than one usage is currently set!");
                    return this.usages$value.get(0);
                } else {
                    return WKTUsage.builder().scope("UNSET").build();
                }
            }

            public B scope(@NonNull String scope) {
                return this.usages(ImmutableList.of(this.currentUsage().toBuilder().scope(scope).build()));
            }

            public B area(@NonNull String area) {
                return this.usages(ImmutableList.of(this.currentUsage().toBuilder().area(area).build()));
            }

            public B bbox(@NonNull WKTGeographicBoundingBox bbox) {
                return this.usages(ImmutableList.of(this.currentUsage().toBuilder().bbox(bbox).build()));
            }

            @JsonProperty("vertical_extent")
            public B verticalExtent(@NonNull WKTVerticalExtent verticalExtent) {
                return this.usages(ImmutableList.of(this.currentUsage().toBuilder().verticalExtent(verticalExtent).build()));
            }

            //TODO: temporal extent
            /*public B bbox(@NonNull WKTGeographicBoundingBox bbox) {
                return this.usages(ImmutableList.of(this.currentUsage().toBuilder().bbox(bbox).build()));
            }*/
        }
    }

    /**
     * @author DaPorkchop_
     */
    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder(toBuilder = true)
    @Getter
    public static abstract class WithNameAndScopeExtentIdentifierRemark extends AbstractWKTObject.WithScopeExtentIdentifierRemark implements WKTObject.WithName {
        @NonNull
        private final String name;
    }
}
