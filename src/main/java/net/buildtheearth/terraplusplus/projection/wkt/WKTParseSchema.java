package net.buildtheearth.terraplusplus.projection.wkt;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.daporkchop.lib.common.function.io.IOFunction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ObjDoubleConsumer;
import java.util.function.Supplier;

import static net.daporkchop.lib.common.util.PValidation.*;
import static net.daporkchop.lib.common.util.PorkUtil.*;

/**
 * @author DaPorkchop_
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public abstract class WKTParseSchema<T> {
    public static <T, B> UsingBuilder.Builder<T, B> builder(@NonNull Supplier<B> builderFactory, @NonNull Function<B, T> builderBuild) {
        return new UsingBuilder.Builder<>(builderFactory, builderBuild);
    }

    public T parse(@NonNull WKTReader reader) throws IOException {
        return this.parse(reader, reader.nextKeyword());
    }

    public abstract T parse(@NonNull WKTReader reader, @NonNull String keyword) throws IOException;

    /**
     * @author DaPorkchop_
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class UsingBuilder<T, B> extends WKTParseSchema<T> {
        private final Supplier<B> builderFactory;
        private final Function<B, T> builderBuild;

        private final Set<String> permittedKeywords;
        private final List<Property<? super B>> properties;

        @Override
        public T parse(@NonNull WKTReader reader, @NonNull String keyword) throws IOException {
            checkState(this.permittedKeywords.contains(keyword), "unexpected keyword '%s' (should be one of %s)", keyword, this.permittedKeywords);

            B builder = this.builderFactory.get();

            for (Iterator<Property<? super B>> itr = this.properties.iterator(); itr.hasNext(); ) {
                WKTReader.Token nextToken = reader.peek();
                String nextKeyword = nextToken == WKTReader.Token.BEGIN_OBJECT ? reader.nextKeyword() : null;

                do {
                    Property<? super B> property = itr.next();
                    if (property.expectedToken != null && property.expectedToken != nextToken) {
                        checkState(property.optional, "while parsing '%s': next non-optional property expects %s, but found %s!", keyword, property.expectedToken, nextToken);
                        continue; //this is an optional property, skip it
                    } else if (!property.tryParse(reader, builder, nextToken, nextKeyword)) {
                        checkState(property.optional, "while parsing '%s': next non-optional property couldn't accept %s (%s)", keyword, nextToken, nextKeyword);
                        continue; //this is an optional property, skip it
                    } else { //we successfully parsed the property, advance to the next one
                        break;
                    }
                } while (itr.hasNext());
            }

            reader.nextObjectEnd();
            return this.builderBuild.apply(builder);
        }

        /**
         * @author DaPorkchop_
         */
        @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
        public static final class Builder<T, B> {
            private final Supplier<B> builderFactory;
            private final Function<B, T> builderBuild;

            private final ImmutableSet.Builder<String> permittedKeywords = ImmutableSet.builder();
            private final List<Property<? super B>> properties = new ArrayList<>();

            public Builder<T, B> permitKeyword(@NonNull String... keywords) {
                this.permittedKeywords.add(keywords);
                return this;
            }

            private Builder<T, B> addProperty(@NonNull Property<? super B> property) {
                this.properties.add(property);
                return this;
            }

            public <TMP> Builder<T, B> addSimpleProperty(@NonNull IOFunction<? super WKTReader, TMP> read, @NonNull BiConsumer<? super B, ? super TMP> set, boolean optional) {
                return this.addProperty(new Property<B>(null, optional) {
                    @Override
                    public boolean tryParse(@NonNull WKTReader reader, @NonNull B builder, @NonNull WKTReader.Token token, String keyword) throws IOException {
                        set.accept(builder, read.applyThrowing(reader));
                        return true;
                    }
                });
            }

            public Builder<T, B> addStringProperty(@NonNull BiConsumer<? super B, ? super String> builderCallback, boolean optional) {
                return this.addSimpleProperty(WKTReader::nextQuotedLatinString, builderCallback, optional);
            }

            public Builder<T, B> requiredStringProperty(@NonNull BiConsumer<? super B, ? super String> builderCallback) {
                return this.addStringProperty(builderCallback, false);
            }

            public Builder<T, B> optionalStringProperty(@NonNull BiConsumer<? super B, ? super String> builderCallback) {
                return this.addStringProperty(builderCallback, true);
            }

            public Builder<T, B> addUnsignedNumericProperty(@NonNull BiConsumer<? super B, ? super Number> builderCallback, boolean optional) {
                return this.addSimpleProperty(WKTReader::nextUnsignedNumericLiteral, builderCallback, optional);
            }

            public Builder<T, B> requiredUnsignedNumericProperty(@NonNull BiConsumer<? super B, ? super Number> builderCallback) {
                return this.addUnsignedNumericProperty(builderCallback, false);
            }

            public Builder<T, B> optionalUnsignedNumericProperty(@NonNull BiConsumer<? super B, ? super Number> builderCallback) {
                return this.addUnsignedNumericProperty(builderCallback, true);
            }

            public Builder<T, B> addUnsignedNumericAsDoubleProperty(@NonNull ObjDoubleConsumer<? super B> builderCallback, boolean optional) {
                return this.addSimpleProperty(WKTReader::nextUnsignedNumericLiteral, (builder, value) -> builderCallback.accept(builder, value.doubleValue()), optional);
            }

            public Builder<T, B> requiredUnsignedNumericAsDoubleProperty(@NonNull ObjDoubleConsumer<? super B> builderCallback) {
                return this.addUnsignedNumericAsDoubleProperty(builderCallback, false);
            }

            public Builder<T, B> optionalUnsignedNumericAsDoubleProperty(@NonNull ObjDoubleConsumer<? super B> builderCallback) {
                return this.addUnsignedNumericAsDoubleProperty(builderCallback, true);
            }

            public <O> Builder<T, B> addObjectProperty(@NonNull WKTParseSchema<? extends O> schema, @NonNull BiConsumer<? super B, ? super O> set, boolean optional) {
                UsingBuilder<? extends O, ?> realSchema = uncheckedCast(schema);
                return this.addProperty(new Property<B>(WKTReader.Token.BEGIN_OBJECT, optional) {
                    @Override
                    public boolean tryParse(@NonNull WKTReader reader, @NonNull B builder, @NonNull WKTReader.Token token, String keyword) throws IOException {
                        checkArg(token == WKTReader.Token.BEGIN_OBJECT && keyword != null, "token=%s, keyword=%s", token, keyword);

                        if (this.optional() && !realSchema.permittedKeywords.contains(keyword)) { //skip if optional and keyword mismatch
                            return false;
                        }

                        set.accept(builder, realSchema.parse(reader, keyword));
                        return true;
                    }
                });
            }

            public <O> Builder<T, B> requiredObjectProperty(@NonNull WKTParseSchema<? extends O> schema, @NonNull BiConsumer<? super B, ? super O> set) {
                return this.addObjectProperty(schema, set, false);
            }

            public <O> Builder<T, B> optionalObjectProperty(@NonNull WKTParseSchema<? extends O> schema, @NonNull BiConsumer<? super B, ? super O> set) {
                return this.addObjectProperty(schema, set, true);
            }

            public Builder<T, B> inheritFrom(@NonNull WKTParseSchema<? super T> baseSchema) {
                UsingBuilder<? super T, ? super B> realSchema = uncheckedCast(baseSchema);
                realSchema.properties.forEach(this::addProperty);
                return this;
            }

            public UsingBuilder<T, B> build() {
                Set<String> permittedKeywords = this.permittedKeywords.build();
                checkState(!permittedKeywords.isEmpty(), "at least one permitted keyword must be given!");
                return new UsingBuilder<>(this.builderFactory, this.builderBuild, permittedKeywords, ImmutableList.copyOf(this.properties));
            }
        }

        /**
         * @author DaPorkchop_
         */
        @FunctionalInterface
        public interface IPropertyParser<B> {
            boolean tryParse(@NonNull WKTReader reader, @NonNull B builder, @NonNull WKTReader.Token token, String keyword) throws IOException;
        }

        /**
         * @author DaPorkchop_
         */
        @RequiredArgsConstructor
        @Getter
        private static abstract class Property<B> implements IPropertyParser<B> {
            private final WKTReader.Token expectedToken;
            private final boolean optional;
        }
    }
}
