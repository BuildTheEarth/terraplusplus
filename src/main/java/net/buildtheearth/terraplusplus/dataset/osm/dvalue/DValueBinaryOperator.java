package net.buildtheearth.terraplusplus.dataset.osm.dvalue;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Map;

/**
 * @author DaPorkchop_
 */
@Getter(onMethod_ = { @JsonGetter })
@RequiredArgsConstructor
public abstract class DValueBinaryOperator implements DValue {
    @NonNull
    protected final DValue first;
    @NonNull
    protected final DValue second;

    @JsonDeserialize
    public static final class Add extends DValueBinaryOperator {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Add(
                @JsonProperty(value = "first", required = true) @JsonAlias({"a"}) @NonNull DValue first,
                @JsonProperty(value = "second", required = true) @JsonAlias({"b"}) @NonNull DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return this.first.apply(tags) + this.second.apply(tags);
        }
    }

    @JsonDeserialize
    public static final class Subtract extends DValueBinaryOperator {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Subtract(
                @JsonProperty(value = "first", required = true) @JsonAlias({"a"}) @NonNull DValue first,
                @JsonProperty(value = "second", required = true) @JsonAlias({"b"}) @NonNull DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return this.first.apply(tags) - this.second.apply(tags);
        }
    }

    @JsonDeserialize
    public static final class Multiply extends DValueBinaryOperator {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Multiply(
                @JsonProperty(value = "first", required = true) @JsonAlias({"a"}) @NonNull DValue first,
                @JsonProperty(value = "second", required = true) @JsonAlias({"b"}) @NonNull DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return this.first.apply(tags) * this.second.apply(tags);
        }
    }

    @JsonDeserialize
    public static final class Divide extends DValueBinaryOperator {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Divide(
                @JsonProperty(value = "first", required = true) @JsonAlias({"a"}) @NonNull DValue first,
                @JsonProperty(value = "second", required = true) @JsonAlias({"b"}) @NonNull DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return this.first.apply(tags) / this.second.apply(tags);
        }
    }

    @JsonDeserialize
    public static final class FloorDiv extends DValueBinaryOperator {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public FloorDiv(
                @JsonProperty(value = "first", required = true) @JsonAlias({"a"}) @NonNull DValue first,
                @JsonProperty(value = "second", required = true) @JsonAlias({"b"}) @NonNull DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return Math.floor(this.first.apply(tags) / this.second.apply(tags));
        }
    }

    @JsonDeserialize
    public static final class Min extends DValueBinaryOperator {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Min(
                @JsonProperty(value = "first", required = true) @JsonAlias({"a"}) @NonNull DValue first,
                @JsonProperty(value = "second", required = true) @JsonAlias({"b"}) @NonNull DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return Math.min(this.first.apply(tags), this.second.apply(tags));
        }
    }

    @JsonDeserialize
    public static final class Max extends DValueBinaryOperator {
        @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
        public Max(
                @JsonProperty(value = "first", required = true) @JsonAlias({"a"}) @NonNull DValue first,
                @JsonProperty(value = "second", required = true) @JsonAlias({"b"}) @NonNull DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return Math.max(this.first.apply(tags), this.second.apply(tags));
        }
    }
}
