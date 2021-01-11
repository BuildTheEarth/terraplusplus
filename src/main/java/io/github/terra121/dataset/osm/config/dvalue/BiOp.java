package io.github.terra121.dataset.osm.config.dvalue;

import com.google.gson.annotations.JsonAdapter;
import com.google.gson.stream.JsonReader;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.Map;

/**
 * @author DaPorkchop_
 */
@RequiredArgsConstructor
abstract class BiOp implements DValue {
    @NonNull
    protected final DValue first;
    @NonNull
    protected final DValue second;

    static abstract class Parser extends DValueParser {
        @Override
        public DValue read(JsonReader in) throws IOException {
            in.beginObject();
            DValue first = super.read(in);
            DValue second = super.read(in);
            in.endObject();

            return this.construct(first, second);
        }

        protected abstract DValue construct(@NonNull DValue first, @NonNull DValue second);
    }

    @JsonAdapter(Add.Parser.class)
    static final class Add extends BiOp {
        public Add(DValue first, DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return this.first.apply(tags) + this.second.apply(tags);
        }

        static class Parser extends BiOp.Parser {
            @Override
            protected DValue construct(@NonNull DValue first, @NonNull DValue second) {
                return new Add(first, second);
            }
        }
    }

    @JsonAdapter(Subtract.Parser.class)
    static final class Subtract extends BiOp {
        public Subtract(DValue first, DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return this.first.apply(tags) - this.second.apply(tags);
        }

        static class Parser extends BiOp.Parser {
            @Override
            protected DValue construct(@NonNull DValue first, @NonNull DValue second) {
                return new Subtract(first, second);
            }
        }
    }

    @JsonAdapter(Multiply.Parser.class)
    static final class Multiply extends BiOp {
        public Multiply(DValue first, DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return this.first.apply(tags) * this.second.apply(tags);
        }

        static class Parser extends BiOp.Parser {
            @Override
            protected DValue construct(@NonNull DValue first, @NonNull DValue second) {
                return new Multiply(first, second);
            }
        }
    }

    @JsonAdapter(Divide.Parser.class)
    static final class Divide extends BiOp {
        public Divide(DValue first, DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return this.first.apply(tags) / this.second.apply(tags);
        }

        static class Parser extends BiOp.Parser {
            @Override
            protected DValue construct(@NonNull DValue first, @NonNull DValue second) {
                return new Divide(first, second);
            }
        }
    }

    @JsonAdapter(Min.Parser.class)
    static final class Min extends BiOp {
        public Min(DValue first, DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return Math.min(this.first.apply(tags), this.second.apply(tags));
        }

        static class Parser extends BiOp.Parser {
            @Override
            protected DValue construct(@NonNull DValue first, @NonNull DValue second) {
                return new Min(first, second);
            }
        }
    }

    @JsonAdapter(Max.Parser.class)
    static final class Max extends BiOp {
        public Max(DValue first, DValue second) {
            super(first, second);
        }

        @Override
        public double apply(@NonNull Map<String, String> tags) {
            return Math.max(this.first.apply(tags), this.second.apply(tags));
        }

        static class Parser extends BiOp.Parser {
            @Override
            protected DValue construct(@NonNull DValue first, @NonNull DValue second) {
                return new Max(first, second);
            }
        }
    }
}
